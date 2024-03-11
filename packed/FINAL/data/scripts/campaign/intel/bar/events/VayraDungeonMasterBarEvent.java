package data.scripts.campaign.intel.bar.events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.OptionPanelAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.VayraModPlugin.IS_AVANITIA;
import static data.scripts.VayraModPlugin.IS_CJUICY;
import static data.scripts.VayraModPlugin.VAYRA_DEBUG;
import static data.scripts.VayraModPlugin.aOrAn;
import static data.scripts.campaign.intel.bar.events.VayraDungeonMasterData.*;
import java.awt.Color;
import java.util.*;

import org.apache.log4j.Logger;

// TODO
// add dialogue to neutral/friendly monsters
// interaction images
// (future) framework support for content additions/setting swaps
public class VayraDungeonMasterBarEvent extends BaseBarEventWithPerson {

    public static final String KEY_CHARACTER_SHEET = "$vayra_dungeonMasterBarEvent";
    public static final String KEY_RETIRED = "$vayra_dungeonMasterBarEventRetired";
    public static final String KEY_OP_BONUS_LEVEL_20 = "$vayra_dungeonMasterRetiredLevelTwenty";
    public static Logger log = Global.getLogger(VayraDungeonMasterBarEvent.class);

    public static final int CHARACTER_COST = 15000;
    public static final int LEVEL_UP_PAYMENT = 5000;
    public static final int OIL_COST = 10;
    public static final int BRIBE_COST = 10; // per HD squared
    public static final float SIP_CHANCE = 0.1f;
    public static final float BASE_LOST_CHANCE = 0.333f;
    public static final int LANTERN_ROOMS = 5; // plus 1d6

    // these are all of the things it is possible to do in a tabletop RPG
    public static enum Options {
        // meta-interactions
        INIT, // first thing we do
        ROLL_NEW, // make a new character even if you already have one
        SHEET, // read your character sheet and inventory and stuff
        LEAVE, // last thing we do
        // town interactions
        BUY_OIL, // spend some of that filthy lucre
        CAROUSE, // spend all of that filthy lucre
        LEVEL_UP, // ding!
        RETIRE, // consign your character to the graveyard of heroes, in town
        RETIRE_CONFIRM, // it is done
        QUIT, // quit on good terms, in town
        // location interactions
        TOWN, // where we start
        GO_DEEPER, // once more into the breach
        GO_HOME, // time to leave
        RAGEQUIT, // flip the table and walk out, anywhere
        // combat interactions
        TALK, // not everything has to end in bloodshed
        ATTACK, // make them pay
        OIL, // perhaps fire will save you
        SCROLL, // perhaps magic will save you
        POTION, // perhaps magic will save you
        FLEE, // run away!
    }

    // these are the local variables we only care about during a session
    VayraRPGCharacterSheetData data;
    int currHp;
    Options lastOption;
    String gameName;
    String townName;
    String dungeonName;

    // these too, for encounters
    VayraRPGRoomData room;
    VayraRPGMonsterData monster;
    VayraRPGTrapData trap;
    boolean monsterHostile;
    boolean wonInitiative;
    boolean checkedMorale;
    int negotiation; // -1: negotiation failed, 0: negotiation not started, 1: trying to negotiate, 2: demanding bribe, 3: paying bribe, accepting bribe, friendly now
    int monsterHp;
    int monsterHpMax;

    // these too, and we might as well initialize them now
    PersonAPI player = Global.getSector().getPlayerPerson();
    int depth = 0; // in rooms
    int light = 0; // in rooms
    Set<VayraRPGRoomData> visitedSpecial = new HashSet<>();

    // some useful shorthand
    Color h = Misc.getHighlightColor();
    Color p = Misc.getPositiveHighlightColor();
    Color n = Misc.getNegativeHighlightColor();
    Color g = Misc.getGrayColor();
    Color t = Misc.getTextColor();
    Color s = Misc.getStoryOptionColor();

    // and here's the whole rest of the thing
    @Override
    public void optionSelected(String optionText, Object option) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        OptionPanelAPI optionsPanel = dialog.getOptionPanel();

        if (option instanceof Options) {

            // if we're just checking our sheet, don't clear anything or whatever
            if (option == Options.SHEET) {

                // display character sheet
                textPanel.addPara("You take a quick look at your character sheet...", h);
                showCharacterSheet();

                // otherwise, main option list
            } else {

                // clear shown options before we show new ones
                optionsPanel.clearOptions();

                // handler for all other options the player can choose
                switch ((Options) option) {

                    case INIT: // sitting down at the table, checking if we have a character
                        textPanel.addPara("The " + getManOrWoman() + " looks up in surprise as you approach " + getHimOrHer() + ".");
                        textPanel.addPara("\"Oh, hello!,\" " + getHeOrShe() + " says brightly. \"Would you like to play " + gameName + "?\"");
                        textPanel.addPara("\"The rules are super simple, and I'll handle all of them for you anyway! "
                                + "I'll be the 'dungeon master', which means I'll run the game for you.\"");
                        textPanel.addPara("\"There's a fee for the TriPad software you need to generate a character, "
                                + "but that's the only cost and you can keep re-using that character until they die.\"");
                        textPanel.addPara("\"Do you have a character yet?,\" " + getHeOrShe() + " asks.");

                        boolean hasCreditsForNewCharacter = false;
                        int costForNewCharacter = CHARACTER_COST;
                        try {
                            hasCreditsForNewCharacter = (int) Global.getSector().getPlayerFleet().getCargo().getCredits().get() > costForNewCharacter || VAYRA_DEBUG;
                        } catch (NullPointerException npx) {
                            log.error("Vayra got lazy and I couldn't figure out how many credits the player has");
                        }

                        if (data != null && !data.dead && !data.retired) {
                            optionsPanel.addOption("Say yes, and sit down to play", Options.TOWN, s, "Once you're playing, further text will be in-character");
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                        } else if (hasCreditsForNewCharacter) {
                            optionsPanel.addOption("Say no, and sit down to roll one", Options.ROLL_NEW);
                        } else if (!hasCreditsForNewCharacter) {
                            textPanel.addPara("Unfortunately, you don't have enough credits to roll a new character. Damn this commercialization of the hobby!", n);
                        }
                        optionsPanel.addOption("Leave", Options.LEAVE);
                        break;

                    case ROLL_NEW: // making a character
                        textPanel.addPara("You download the TriPad software and start generating a character...");
                        if (!VAYRA_DEBUG) {
                            AddRemoveCommodity.addCreditsLossText(CHARACTER_COST, dialog.getTextPanel());
                            try {
                                Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(CHARACTER_COST);
                            } catch (NullPointerException npx) {
                                log.error("Vayra got lazy and I couldn't figure out how to charge the player");
                            }
                        }
                        data = new VayraRPGCharacterSheetData();
                        data.name = getNewCharacterName();
                        data.className = getNewClassName();
                        textPanel.addPara("Looks like your name is " + data.name + ", and you're a level 1 " + data.className + ".", t, h, data.name, data.className);
                        data.STR = rollStat();
                        data.DEX = rollStat();
                        data.INT = rollStat();
                        data.CHA = rollStat();
                        textPanel.addPara("You have " + data.textStr() + " Strength, which applies to damage and HP.", t, h, data.textStr());
                        textPanel.addPara("You have " + data.textDex() + " Dexterity, which applies to attack rolls and fleeing.", t, h, data.textDex());
                        textPanel.addPara("You have " + data.textInt() + " Intelligence, which applies to finding traps, initiative, navigation, and spellcasting.", t, h, data.textInt());
                        textPanel.addPara("You have " + data.textCha() + " Charisma, which applies to reaction rolls and bribery.", t, h, data.textCha());
                        data.maxHp = 8 + data.STR;
                        currHp = data.maxHp;
                        textPanel.addPara("You start out with " + data.maxHp + " HP, and gain 1d8" + data.textStr() + " at levels 2, 3, and 4. "
                                + "You reroll HP every level and take the new total if it's higher, even above 4th.",
                                t, h, data.maxHp + "", "1d8" + data.textStr());
                        WeightedRandomPicker<Armor> startArmors = new WeightedRandomPicker<>();
                        startArmors.add(Armor.CLOTH, 1f);
                        startArmors.add(Armor.LEATHER, 1f);
                        startArmors.add(Armor.CHAIN, 0.1f);
                        data.armor = startArmors.pick();
                        WeightedRandomPicker<Weapon> startWeapons = new WeightedRandomPicker<>();
                        startWeapons.add(Weapon.DAGGER, 1f);
                        startWeapons.add(Weapon.CLUB, 1f);
                        startWeapons.add(Weapon.STAFF, 0.33f);
                        startWeapons.add(Weapon.MACE, 0.33f);
                        startWeapons.add(Weapon.SLING, 0.33f);
                        data.weapon = startWeapons.pick();
                        textPanel.addPara("You wear " + armorName(data.armor) + " and wield a " + weaponName(data.weapon) + ".", t,
                                h, armorName(data.armor), weaponName(data.weapon));
                        textPanel.addPara("You get 1 XP per gold piece you successfully recover. It takes 200 XP to reach level 2. Good luck!", t, h, "1 XP", "200 XP");
                        data.oil = 2; // might as well start with some oil, that seems nice
                        WeightedRandomPicker<String> canteens = new WeightedRandomPicker<>();
                        canteens.addAll(CANTEENS);
                        data.canteen = canteens.pick();
                        if (IS_CJUICY && Math.random() < 0.5f) {
                            data.canteen = "Vault 13 canteen";
                        }
                        Global.getSector().getPersistentData().put(KEY_CHARACTER_SHEET, data);

                        if (VayraRPGAliveCharacterIntel.getInstance() == null) {
                            VayraRPGAliveCharacterIntel aliveIntel = new VayraRPGAliveCharacterIntel();
                            Global.getSector().getIntelManager().addIntel(aliveIntel);
                        }

                        optionsPanel.addOption("Start playing", Options.TOWN, s, "Once you're playing, further text will be in-character");
                        optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                        optionsPanel.addOption("Leave", Options.LEAVE, s, "Your character will be saved for the next time you want to play");
                        break;

                    case RETIRE:
                        showCharacterSheet();
                        textPanel.addPara("Are you sure you want to retire this character?", h);
                        optionsPanel.addOption("Yes", Options.RETIRE_CONFIRM);
                        optionsPanel.addOption("No", Options.TOWN);
                        break;

                    case RETIRE_CONFIRM:
                        @SuppressWarnings("unchecked") List<VayraRPGCharacterSheetData> retired = (List<VayraRPGCharacterSheetData>) Global.getSector().getPersistentData().get(KEY_RETIRED);
                        if (retired == null) {
                            retired = new ArrayList<>();
                        }

                        retireRewards();

                        data.retired = true;
                        data.fate = "retired happily";
                        retired.add(data);
                        data = null;
                        Global.getSector().getPersistentData().remove(KEY_CHARACTER_SHEET);
                        Global.getSector().getPersistentData().put(KEY_RETIRED, retired);

                        if (VayraRPGRetiredCharacterIntel.getInstance() == null) {
                            VayraRPGRetiredCharacterIntel retiredIntel = new VayraRPGRetiredCharacterIntel();
                            Global.getSector().getIntelManager().addIntel(retiredIntel);
                        }

                        optionsPanel.addOption("Leave", Options.LEAVE);
                        break;

                    case TOWN: // set us up in town
                        depth = 0;
                        light = 1;
                        visitedSpecial.clear();
                        // this is gonna generate stupid bullshit i *know* it is
                        if (townName == null) {
                            townName = ProcgenUsedNames.pickName(NameGenData.TAG_PLANET, null, null).nameWithRomanSuffixIfAny;
                        }
                        if (dungeonName == null) {
                            dungeonName = ProcgenUsedNames.pickName(NameGenData.TAG_STAR, null, null).nameWithRomanSuffixIfAny;
                        }

                        textPanel.addPara("The hamlet of " + townName + " spreads out before you. In the distance, the dungeon of " + dungeonName + " looms menacingly.");
                        textPanel.addPara("Honestly, there isn't much to do here.");

                        // if we're low on hit points (or too high?), restore us to full
                        if (currHp != data.maxHp) {
                            currHp = data.maxHp;
                            textPanel.addPara("You take advantage of your time in town to rest and recuperate, restoring you to your maximum hit points.", p);
                        }

                        // if we're out of oil, have someone give us more
                        if (data.oil < 1) {
                            data.oil = 1;
                            textPanel.addPara("The local adventuring outfitter gives you a flask of oil \"on the house.\"", p, h, "a flask of oil");
                        }
                        if (data.gp >= OIL_COST) {
                            optionsPanel.addOption("Buy a flask of oil (" + OIL_COST + "gp)", Options.BUY_OIL);
                        }
                        if (data.gp > 0) {
                            optionsPanel.addOption("Go out for a night on the town", Options.CAROUSE, s, "This will convert all of your available gold pieces into experience points");
                            optionsPanel.addOption("Venture into the dungeon of " + dungeonName, Options.GO_DEEPER, n, "You should really spend your money first, or risk losing it");
                            optionsPanel.addOption("Stop playing for now", Options.QUIT, n, "Your character will be saved for the next time you want to play - but you should really spend your money first");
                        } else {
                            optionsPanel.addOption("Venture into the dungeon of " + dungeonName, Options.GO_DEEPER);
                            optionsPanel.addOption("Stop playing for now", Options.QUIT, s, "Your character will be saved for the next time you want to play");
                            optionsPanel.addOption("Retire your character", Options.RETIRE, s, "If you're at level 5 or higher, retiring will grant you some rewards");
                        }
                        optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                        break;

                    case BUY_OIL:

                        textPanel.addPara("You purchase a flask of oil from the local adventuring outfitter.");
                        textPanel.setFontSmallInsignia();
                        String str = Misc.getWithDGS(OIL_COST);
                        textPanel.addPara("Lost " + str + " gold pieces", n, h, str);
                        textPanel.setFontInsignia();
                        data.oil++;
                        data.gp -= OIL_COST;
                        if (VAYRA_DEBUG) {
                            textPanel.setFontSmallInsignia();
                            textPanel.addPara("But you found 1000 more just lying on the ground!", p, h, "1000");
                            data.gp += 1000;
                            textPanel.setFontInsignia();
                        }
                        textPanel.addPara("You now have " + data.oil + " flasks of oil and " + data.gp + " gold pieces.", t, h, data.oil + "", data.gp + "");

                        if (data.gp >= OIL_COST) {
                            optionsPanel.addOption("Buy another flask of oil (" + OIL_COST + "gp)", Options.BUY_OIL);
                        }
                        optionsPanel.addOption("Walk back out into the streets of " + townName, Options.TOWN);
                        break;

                    case CAROUSE:

                        textPanel.addPara("You head to the local tavern and spend all your money in an orgiastic celebration of your continued life.");
                        data.xp += data.gp;
                        textPanel.setFontSmallInsignia();
                        String carouseValue = Misc.getWithDGS(data.gp);
                        textPanel.addPara("Lost " + carouseValue + " gold pieces", n, h, carouseValue);
                        data.gp = 0;
                        textPanel.addPara("Gained " + carouseValue + " experience points", p, h, carouseValue);
                        textPanel.setFontInsignia();

                        // tell us how much xp we have now, and calculate if we've levelled up
                        boolean levelUp;
                        if (data.level < 20) {
                            int toLevel = XP.get(data.level + 1);
                            levelUp = data.xp >= toLevel;
                            textPanel.addPara("You now have " + data.xp + "/" + toLevel + " experience points.", t, h, data.xp + "/" + toLevel);
                        } else {
                            levelUp = false;
                            textPanel.addPara("You now have " + data.xp + " experience points, and are at the maximum level. Perhaps it's time to think about retirement?", t, p, data.xp + "", "maximum level");
                        }

                        // if we level up, kick us to the level up screen, otherwise back to town
                        if (levelUp) {
                            textPanel.addPara("You've leveled up!", p);
                            payout();
                            optionsPanel.addOption("Consider what you've learned", Options.LEVEL_UP);
                        } else {
                            optionsPanel.addOption("Shake off the hangover and stumble out into the streets of " + townName, Options.TOWN);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                        }
                        break;

                    case LEVEL_UP:

                        data.level++;
                        textPanel.addPara("You've reached level " + data.level + "!", h, p, data.level + "");
                        if (data.level <= 4) {
                            String oldTextAttack = (data.getToHit() - 1 >= 0 ? "+" : "") + (data.getToHit() - 1);
                            String textAttack = (data.getToHit() >= 0 ? "+" : "") + data.getToHit();
                            textPanel.addPara("Your modifier to hit has increased from " + oldTextAttack + " to " + textAttack + ".", h, p, oldTextAttack, textAttack);
                        }
                        int levelForHp = data.level >= 4 ? 4 : data.level;
                        int maxPossibleHp = levelForHp * (8 + data.STR);
                        if (data.maxHp < maxPossibleHp) {
                            int roll = 0;
                            for (int i = 0; i < levelForHp; i++) {
                                int addRoll = d(8) + data.STR;
                                if (addRoll < 1) {
                                    addRoll = 1;
                                }
                                roll += addRoll;
                            }
                            if (roll > data.maxHp) {
                                textPanel.addPara("You roll " + roll + " hit points. That's higher than your current total of "
                                        + data.maxHp + ", so you take it as your new maximum hit points.", h, p, roll + "", data.maxHp + "");
                                data.maxHp = roll;
                                currHp = data.maxHp;
                            } else {
                                textPanel.addPara("You roll " + roll + " hit points. Your current total is " + data.maxHp
                                        + ", so you just ignore it. Oh well.", h, p, roll + "", data.maxHp + "");
                            }
                        } else {
                            textPanel.addPara("You already have the maximum possible hit points for your level and Strength score, so you don't bother rolling.", h);
                        }

                        // we need to check for level up again, just in case the player's been stockpiling gold
                        boolean levelUpAgain;
                        if (data.level < 20) {
                            int toLevel = XP.get(data.level + 1);
                            levelUpAgain = data.xp >= toLevel;
                            textPanel.addPara("You now have " + data.xp + "/" + toLevel + " experience points.", t, h, data.xp + "/" + toLevel);
                        } else {
                            levelUpAgain = false;
                            textPanel.addPara("You now have " + data.xp + " experience points, and are at the maximum level. Perhaps it's time to think about retirement?", t, p, data.xp + "", "maximum level");
                        }

                        // if we level up (again), kick us to the level up screen, otherwise back to town
                        if (levelUpAgain) {
                            textPanel.addPara("You've leveled up... again!", p);
                            payout();
                            optionsPanel.addOption("Keep considering", Options.LEVEL_UP);
                        } else {
                            optionsPanel.addOption("Shake off the hangover and stumble out into the streets of " + townName, Options.TOWN);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                        }
                        break;

                    case QUIT:
                        textPanel.addPara("\"Oh... Okay!,\" " + person.getName().getFirst() + " says, abruptly breaking character. \"I hope you had fun!\"");
                        textPanel.addPara("\"Your character sheet is saved on your TriPad so you can use it again whenever you want, "
                                + "even with another dungeon master - it's compatible with pretty much every game like this.\"");
                        optionsPanel.addOption("Leave", Options.LEAVE);
                        break;

                    case RAGEQUIT:

                        WeightedRandomPicker<String> screams = new WeightedRandomPicker<>();
                        screams.add("aaaaaa");
                        screams.add("augh");
                        screams.add("oof ouch my bones");
                        screams.add("what the fuck");
                        screams.add("what the fuck is wrong with you");
                        screams.add("what the fuck are you doing");
                        screams.add("ah fuck, i can't believe you've done this");
                        screams.add("piss christ");
                        screams.add("fuck my eyes");
                        screams.add("aw fudge");
                        screams.add("frickin' heck");
                        screams.add("bruh");
                        screams.add("this is vayra's fault somehow");
                        screams.add("lobster bisque");
                        screams.add("by ludd's mossy tonsils");
                        screams.add("blood and martyrs");
                        screams.add("reeeeeeeeee");
                        screams.add("skreeeeeeeeee");
                        screams.add("eek");
                        screams.add("<wilhelm scream>");
                        screams.add("u wot m8");
                        screams.add("bloody hell mate");
                        screams.add("GARGANKSFHISGBCXBS");
                        screams.add("i have very fragile bones");
                        screams.add("i have a skin condition");
                        screams.add("curse you, " + Global.getSector().getPlayerPerson().getNameString());
                        screams.add("aaaiiieee", screams.getItems().size());
                        String scream = screams.pick().toUpperCase();
                        if ("<wilhelm scream>".equals(scream)) {
                            Global.getSoundPlayer().playUISound("vayra_wilhelm_scream", 1f, 0.333f);
                        }

                        textPanel.addPara("\"" + scream + "!,\" " + person.getName().getFirst() + " shrieks as you flip the table, "
                                + "sending sheets of paper and polyhedral dice scattering across the floor of the bar. "
                                + "\"For the love of Ludd, it's just a game!\"");
                        if (depth > 20) {
                            depth = 20;
                        }
                        if (data != null && !data.dead && d20() < depth) {
                            sufferConsequencesForYourIndiscretion();
                        }
                        if (data != null && !data.dead && data.gp > 0) {
                            textPanel.addPara("As you turn to leave, your TriPad chimes to alert you that it has automatically "
                                    + "deducted all unrecovered gold pieces from your character sheet.");
                            textPanel.setFontSmallInsignia();
                            String loseGpForRagequit = Misc.getWithDGS(data.gp);
                            textPanel.addPara("Lost " + loseGpForRagequit + " gold pieces", n, h, loseGpForRagequit);
                            textPanel.setFontInsignia();
                            data.gp = 0;
                        }
                        optionsPanel.addOption("Leave", Options.LEAVE);
                        break;

                    case GO_DEEPER:
                        depth++;
                        if (depth > data.maxDepth) {
                            data.maxDepth = depth;
                        }
                        tickLantern();
                        maybeSip();
                        describeRoom((Options) option);
                        rollEncounter();

                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case GO_HOME:
                        if (depth <= 7) {
                            depth = 1;
                        } else {
                            depth -= d6();
                        }
                        tickLantern();
                        maybeSip();
                        describeRoom((Options) option);
                        rollEncounter();

                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case TALK:
                        if (negotiation == 0) {
                            negotiation++;
                        } else if (negotiation == 2) {
                            textPanel.addPara("You hand over some of your filthy lucre.", h);
                            int bribeCost = (monster.HD ^ 2) * BRIBE_COST;
                            textPanel.setFontSmallInsignia();
                            String bribeString = Misc.getWithDGS(bribeCost);
                            textPanel.addPara("Lost " + bribeString + " gold pieces", n, h, bribeString);
                            textPanel.setFontInsignia();
                            monsterHostile = false;
                            negotiation++;
                        }
                        takeMonsterTurn();
                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case FLEE:
                        int fledRoll = d20();
                        boolean fled = fledRoll + data.DEX >= 10 + monster.HD;
                        textPanel.addPara("You rolled a " + fledRoll + data.textDex() + " (1d20+DEX), total " + (fledRoll + data.DEX) + ".",
                                g, h, fledRoll + data.textDex(), (fledRoll + data.DEX) + "");

                        if (fled && monsterHostile) {
                            textPanel.addPara("You sprint headlong down a random passageway!", p);
                            monster = null;
                            if (Math.random() < 0.5f) {
                                optionsPanel.addOption("Run for your life!", Options.GO_DEEPER);
                            } else {
                                optionsPanel.addOption("Run for your life!", Options.GO_HOME);
                            }
                        } else if (!monsterHostile) {
                            textPanel.addPara("The " + monster.name + " lets you go in peace.", p);
                            monster = null;
                            roomOptions();
                        } else {
                            textPanel.addPara("The " + monster.name + " blocks your exit!", n);
                            takeMonsterTurn();
                            roomOptions();
                        }

                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case ATTACK:
                        if (light < 1 && Math.random() < 0.5f) {
                            textPanel.addPara("You stumble around in the dark and fail to make an attack.", n);
                        } else {
                            monsterHostile = true;
                            negotiation = -1;
                            int attackRoll = d20();
                            String textAttack = (data.getToHit() >= 0 ? "+" : "") + data.getToHit();
                            textPanel.addPara("You rolled a " + attackRoll + textAttack + " to hit, total " + (attackRoll + data.getToHit()) + ".",
                                    g, h, attackRoll + textAttack, (attackRoll + data.getToHit()) + "");
                            attackRoll += data.getToHit();
                            if (attackRoll >= monster.AC) {
                                int dmg = d(DMG.get(data.weapon)) + data.getDamageModifier();
                                if (dmg < 1) {
                                    dmg = 1;
                                }
                                textPanel.addPara("You hit the " + monster.name + " with your " + weaponName(data.weapon) + " for " + dmg + " damage!", p, h, dmg + "");
                                monsterHp -= dmg;
                            } else {
                                textPanel.addPara("You miss the " + monster.name + " with your " + weaponName(data.weapon) + "!", n);
                            }
                        }

                        takeMonsterTurn();
                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case OIL:
                        // if it's dark, we don't have any oil, so don't bother checking
                        monsterHostile = true;
                        negotiation = -1;
                        data.oil--;

                        int attackRoll = d20();
                        int levelForAttack = data.level > 4 ? 4 : data.level;
                        String textAttack = ((levelForAttack + data.DEX >= 0) ? "+" : "") + (levelForAttack + data.DEX);
                        textPanel.addPara("You rolled a " + attackRoll + textAttack + " to hit, total " + (attackRoll + levelForAttack + data.DEX) + ".",
                                g, h, attackRoll + textAttack, (attackRoll + levelForAttack + data.DEX) + "");
                        attackRoll += levelForAttack + data.DEX;
                        if (attackRoll >= 5) {
                            int dmg = d6() + d6();
                            textPanel.addPara("Your flask shatters on the " + monster.name + " and ignites, burning it for " + dmg + " damage!", p, h, dmg + "");
                            monsterHp -= dmg;
                        } else {
                            textPanel.addPara("Your makeshift firebomb flies wide, missing the " + monster.name + " completely!", n);
                        }

                        takeMonsterTurn();
                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case SCROLL:
                        // if it's dark, we can't read scrolls, so don't bother checking
                        monsterHostile = true;
                        negotiation = -1;
                        data.scrolls--;

                        WeightedRandomPicker<String> scrolls = new WeightedRandomPicker<>();
                        scrolls.add("kill"); // damage = hp, save for half
                        scrolls.add("fireball"); // 4d6 damage, save for half
                        scrolls.add("lightning"); // 4d6 damage, save for half
                        scrolls.add("cloudkill"); // 4d6 damage, save for half
                        scrolls.add("magic missile"); // 12 damage
                        scrolls.add("vampirism"); // 2d6 damage, save for half, heals for damage dealt
                        scrolls.add("fear"); // save or flee
                        scrolls.add("banish"); // save or vanish
                        scrolls.add("charm"); // save or no longer hostile
                        scrolls.add("firebolt"); // 4d6 damage, as an attack roll
                        scrolls.add("sword"); // weapon turns into a sword; if it was already a sword, a double sword; if already a double sword, etc
                        scrolls.add("acquirement"); // instant loot
                        scrolls.add("enchant armor"); // armor +1
                        scrolls.add("enchant weapon"); // weapon +1
                        scrolls.add("polymorph"); // save or replace monster with random new monster
                        scrolls.add("teleport"); // bloop! you're much further or father back in the dungeon
                        scrolls.add("escape"); // bloop! you're in the next room
                        scrolls.add("identity shift"); // new name and class string, reroll everything
                        scrolls.add("curse armor"); // armor -1
                        scrolls.add("curse weapon"); // weapon -1
                        String scroll = scrolls.pick();

                        Color scrollColor = p;
                        String effectString = "";
                        int saveDC = 10 + data.INT + (data.level > 4 ? 4 : data.level);
                        int dmg = 0;
                        boolean skip = false;
                        boolean acquirement = false;
                        boolean fear = false;
                        boolean shift = false;

                        switch (scroll) {
                            case "kill":
                                dmg = monsterHp;
                                if (d20() + monster.HD >= saveDC) {
                                    dmg = (int) dmg / 2;
                                    effectString = "The " + monster.name + " shrieks and staggers, taking " + dmg + " damage, but does not fall.";
                                }
                                break;
                            case "fireball":
                                dmg = d6() + d6() + d6() + d6();
                                effectString = "The " + monster.name + " catches the full force of the firey explosion and takes " + dmg + " damage!";
                                if (d20() + monster.HD >= saveDC) {
                                    dmg = (int) dmg / 2;
                                    effectString = "The " + monster.name + " dodges the worst of the blast, and takes " + dmg + " damage.";
                                }
                                break;
                            case "lightning":
                                dmg = d6() + d6() + d6() + d6();
                                effectString = "The " + monster.name + " catches the full force of the bolt and takes " + dmg + " damage!";
                                if (d20() + monster.HD >= saveDC) {
                                    dmg = (int) dmg / 2;
                                    effectString = "The " + monster.name + " dodges the worst of the blast, and takes " + dmg + " damage.";
                                }
                                break;
                            case "cloudkill":
                                dmg = d6() + d6() + d6() + d6();
                                effectString = "The " + monster.name + " catches the full force of the choking cloud and takes " + dmg + " damage!";
                                if (d20() + monster.HD >= saveDC) {
                                    dmg = (int) dmg / 2;
                                    effectString = "The " + monster.name + " stumbles out of the worst of the gas, and takes " + dmg + " damage.";
                                }
                                break;
                            case "magic missile":
                                dmg = 12;
                                effectString = "A quartet of glowing projectiles strike unerringly at " + monster.name + ", dealing " + dmg + " damage.";
                                break;
                            case "vampirism":
                                dmg = d6() + d6();
                                if (d20() + monster.HD >= saveDC) {
                                    dmg = (int) dmg / 2;
                                }
                                if (dmg > monsterHp) {
                                    dmg = monsterHp;
                                }
                                effectString = "Chains of fel power link you and the " + monster.name + " as you siphon away " + dmg + " of its hit points!";
                                currHp += dmg;
                                break;
                            case "fear":
                                if (d20() + monster.HD < saveDC) {
                                    fear = true;
                                } else {
                                    effectString = "The " + monster.name + " manages to resist the effect!";
                                }
                                break;
                            case "banish":
                                if (d20() + monster.HD < saveDC) {
                                    effectString = "The " + monster.name + " vanishes with a popping noise!";
                                    monster = null;
                                } else {
                                    effectString = "The " + monster.name + " blinks. You blink. The " + monster.name + " blinks. Nothing happens.";
                                }
                                break;
                            case "charm":
                                if (d20() + monster.HD < saveDC) {
                                    monsterHostile = false;
                                } else {
                                    effectString = "The " + monster.name + " manages to resist the effect!";
                                }
                                break;
                            case "firebolt":
                                int fireboltAttack = d20();
                                fireboltAttack += (data.level > 4 ? 4 : data.level) + data.DEX;
                                if (fireboltAttack >= monster.AC) {
                                    dmg = d6() + d6() + d6() + d6();
                                    effectString = "The bolt impacts the " + monster.name + " directly, burning it for " + dmg + " damage!";
                                } else {
                                    effectString = "The bolt of fire flies wide, missing the " + monster.name + " completely!";
                                }
                                break;
                            case "sword":
                                switch (data.weapon) {
                                    case SWORD:
                                        effectString = "Your sword splits and divides, becoming a bifurcated blade!";
                                        data.weapon = Weapon.BIFURCATED_BLADE;
                                        break;
                                    case BIFURCATED_BLADE:
                                        effectString = "Your bifurcated blade splits and divides, becoming a triple talwar!";
                                        data.weapon = Weapon.TRIPLE_TALWAR;
                                        break;
                                    case TRIPLE_TALWAR:
                                        effectString = "Your triple talwar splits and divides, becoming a quadrangular cutter!";
                                        data.weapon = Weapon.QUADRANGULAR_CUTTER;
                                        break;
                                    case QUADRANGULAR_CUTTER:
                                        effectString = "Your quadrangular cutter splits and divides, becoming a five-fingered falchion!";
                                        data.weapon = Weapon.FIVE_FINGERED_FALCHION;
                                        break;
                                    case FIVE_FINGERED_FALCHION:
                                        effectString = "Your five-fingered falchion splits and divides, becoming a sextuple spatha!";
                                        data.weapon = Weapon.SEXTUPLE_SPATHA;
                                        break;
                                    case SEXTUPLE_SPATHA:
                                        effectString = "Your sextuple spatha splits and divides, becoming a seven-bladed scimitar!";
                                        data.weapon = Weapon.SEVEN_BLADED_SCIMITAR;
                                        break;
                                    case SEVEN_BLADED_SCIMITAR:
                                        effectString = "Your seven-bladed scimitar splits and divides, becoming an octuple o-dachi!";
                                        data.weapon = Weapon.OCTUPLE_ODACHI;
                                        break;
                                    case OCTUPLE_ODACHI:
                                        effectString = "Your octuple o-dachi splits and divides, becoming a ninefold knife!";
                                        data.weapon = Weapon.NINEFOLD_KNIFE;
                                        break;
                                    case NINEFOLD_KNIFE:
                                        scrollColor = h;
                                        effectString = "Your ninefold knife is already the best type of sword, so it doesn't do anything.";
                                        break;
                                    default:
                                        effectString = "Your " + weaponName(data.weapon) + " sharpens and hardens, twisting itself into a sword!";
                                        data.weapon = Weapon.SWORD;
                                        break;
                                }
                                break;
                            case "acquirement":
                                acquirement = true;
                                break;
                            case "enchant armor":
                                data.magicArmor++;
                                effectString = "Your " + armorName(data.armor) + " shimmers with a soft golden light as the scroll improves it to "
                                        + data.getMagicArmorString() + ".";
                                break;
                            case "enchant weapon":
                                data.magicWeapon++;
                                effectString = "Your " + weaponName(data.weapon) + " shimmers with a soft golden light as the scroll improves it to "
                                        + data.getMagicWeaponString() + ".";
                                break;
                            case "polymorph":
                                scrollColor = h;
                                String oldMonsterName = monster.name;
                                if (d20() + monster.HD < saveDC) {
                                    monster = generateMonster();
                                    effectString = "The shape of the " + oldMonsterName + " twists hideously as it transforms into "
                                            + aOrAn(monster.name) + " " + monster.name + "!";
                                } else {
                                    effectString = "The " + monster.name + " blinks. You blink. The " + monster.name + " blinks. Nothing happens.";
                                }
                                break;
                            case "teleport":
                                scrollColor = h;
                                effectString = "You disappear with a resounding CRACK!";
                                monster = null;
                                // this can take us all the way back to the entrance, or up to twice as far into the dungeon.
                                depth += (int) (depth * Math.random() - depth * Math.random());
                                if (depth < 1) {
                                    depth = 1;
                                }
                                optionsPanel.addOption("Where do I end up?", Options.GO_DEEPER);
                                skip = true;
                                break;
                            case "escape":
                                scrollColor = h;
                                effectString = "The " + monster.name + " disappears into the darkness as you are abruptly yanked backwards through a doorway!";
                                monster = null;
                                optionsPanel.addOption("Where do I end up?", Options.GO_HOME);
                                skip = true;
                                break;
                            case "identity shift":
                                scrollColor = h;
                                data.name = getNewCharacterName();
                                data.className = getNewClassName();
                                shift = true;
                                int newHpFromShift = 0;
                                int levelForHpFromShift = data.level > 4 ? 4 : data.level;
                                for (int i = 0; i < levelForHpFromShift; i++) {
                                    newHpFromShift += d(8);
                                }
                                data.STR = rollStat();
                                data.DEX = rollStat();
                                data.INT = rollStat();
                                data.CHA = rollStat();
                                data.maxHp = newHpFromShift;
                                currHp = data.maxHp;

                                effectString = "You feel your body twist and morph. You are now " + data.name + ", a level " + data.level + " "
                                        + data.className + "... Or perhaps you always were?";
                                break;
                            case "curse armor":
                                scrollColor = n;
                                data.magicArmor--;
                                effectString = "Your " + armorName(data.armor) + " glows a sickly red as the scroll degrades it to "
                                        + data.getMagicArmorString() + ".";
                                break;
                            case "curse weapon":
                                scrollColor = n;
                                data.magicWeapon--;
                                effectString = "Your " + weaponName(data.weapon) + " glows a sickly red as the scroll degrades it to "
                                        + data.getMagicWeaponString() + ".";
                                break;
                            default:
                                break;
                        }
                        monsterHp -= dmg;
                        textPanel.addPara("It was a scroll of " + scroll.toUpperCase() + "!" + " " + effectString,
                                scrollColor == h ? n : h, scrollColor, scroll.toUpperCase(), dmg + "");
                        if (acquirement) {
                            findTreasure();
                        }
                        if (fear) {
                            monsterFlee();
                        }
                        if (shift) {
                            showCharacterSheet();
                        }

                        takeMonsterTurn();
                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else if (skip) {
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case POTION:
                        // it being dark doesn't matter for drinking potions, so don't bother checking
                        data.potions--;
                        WeightedRandomPicker<String> potions = new WeightedRandomPicker<>();
                        potions.addAll(POTIONS);
                        if (IS_AVANITIA || IS_CJUICY) {
                            potions.add("cum");
                            potions.add("gamer girl bathwater");
                        }
                        String potion = potions.pick();
                        int healing = data.level;
                        for (int i = 0; i < data.level; i++) {
                            healing += d6();
                        }
                        healing = healing <= data.maxHp - currHp ? healing : data.maxHp - currHp;
                        currHp += healing;
                        textPanel.addPara("It was a potion of " + potion.toUpperCase() + "! You heal " + healing + " HP!", h, p, potion.toUpperCase(), healing + "");

                        takeMonsterTurn();
                        if (data.dead) {
                            textPanel.addPara("You have died.", n);
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "You have nothing to lose now, so why not?");
                            optionsPanel.addOption("Leave", Options.LEAVE);
                        } else {
                            roomOptions();
                            optionsPanel.addOption("Examine your character sheet", Options.SHEET);
                            optionsPanel.addOption("Flip the table and walk out", Options.RAGEQUIT, n, "This will result in you losing all your gp, and might kill your character");
                        }
                        break;

                    case LEAVE:
                        // They've chosen to leave, so end our interaction. This will send them back to the bar.
                        // If noContinue is false, then there will be an additional "Continue" option shown before they are returned to the bar.
                        noContinue = false;

                        // also, might as well reset our depth and light now
                        depth = 0;
                        light = 1;

                        // also, call the DM a nerd (this is important)
                        WeightedRandomPicker<String> insults = new WeightedRandomPicker<>();
                        insults.add("nerd");
                        insults.add("dweeb");
                        insults.add("dork");
                        insults.add("loser");
                        insults.add("geek");
                        insults.add("wierdo");
                        insults.add("freak");
                        String insult = insults.pick();
                        textPanel.addPara("\"Hah! What a " + insult + ",\" you chuckle to yourself as you walk back to the bar for another drink.");
                        textPanel.addPara("You're pretty sure " + getHeOrShe() + " heard you, but whatever.");
                        done = true;

                        // Removes this event from the bar so it isn't offered again
                        if (!VAYRA_DEBUG) {
                            BarEventManager.getInstance().notifyWasInteractedWith(this);
                        }
                        break;
                }
                lastOption = (Options) option;
            }
        }
    }

    // called at the bar, before the player chooses this option
    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.addPromptAndOption(dialog, memoryMap);
        TextPanelAPI textPanel = dialog.getTextPanel();
        OptionPanelAPI optionsPanel = dialog.getOptionPanel();

        // create a random person and do some other behind-the-scenes bar event setup shit
        regen(dialog.getInteractionTarget().getMarket());

        // huh why does this crash
        if (random == null) {
            random = new Random();
        }

        // Display the text that will appear when the player first enters the bar and looks around
        textPanel.addPara("A " + getManOrWoman() + " sits in a comfortable-looking booth at the back, with sheets of paper and polyhedral dice set out before "
                + getHimOrHer() + " as if " + getHeOrShe() + " were about to play some sort of board game.");

        // Display the option that lets the player choose to investigate our bar event
        optionsPanel.addOption("Walk over to the game table and ask if " + getHeOrShe() + " needs another player", this);
    }

    // called after the player chooses this option from the bar
    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);

        // If player starts our event, then backs out of it, `done` will be set to true.
        // If they then start the event again without leaving the bar, we should reset `done` to false.
        done = false;

        // The boolean is for whether to show only minimal person information. True == minimal
        dialog.getVisualPanel().showPersonInfo(person, true);

        // load our character sheet, if we have one
        data = (VayraRPGCharacterSheetData) Global.getSector().getPersistentData().get(KEY_CHARACTER_SHEET);

        // decide what this particular DM is running
        if (gameName == null) {
            gameName = getNewGameName();
        }

        // Launch into our event by triggering the "INIT" option, which will call `optionSelected()`
        this.optionSelected(null, Options.INIT);
    }

    private void retireRewards() {
        TextPanelAPI textPanel = dialog.getTextPanel();

        textPanel.addPara("You move " + data.name + " to the retired category on your TriPad.", p, data.name, "retired");
        List<DropData> dropList = new ArrayList<>();
        DropData d;
        switch (data.level) {
            case 20:
                // that's it, we won, it's over
                textPanel.addPara(person.getName().getFirst() + " lets out a long, slow whistle. \"Damn, fair enough,\" " + getHeOrShe() + " says. "
                        + "\"You've basically won the game at this point.\"");
                boolean hasOPBonus = Global.getSector().getPlayerStats().getShipOrdnancePointBonus().getMultBonuses().containsKey(KEY_OP_BONUS_LEVEL_20);
                if (!hasOPBonus) {
                    textPanel.addPara("\"Hey, before you go,\" " + getHeOrShe() + " adds, pressing a datachip into your hand. "
                            + "\"You've been such a good friend to me. My mother was an engineer, she used to lead a team...\" " + getHeOrShe()
                            + " trails off, then smiles sadly. \"This was her life's work, but I've never been able to make any sense of it. "
                            + "It'll do you more good than it does me.\"");
                    textPanel.setFontSmallInsignia();
                    textPanel.addPara("Learned arcane secrets of engineering (+5%% ship OP)", p, h, "arcane secrets", "+5%");
                    textPanel.setFontInsignia();
                    Global.getSector().getPlayerStats().getShipOrdnancePointBonus().modifyMult(KEY_OP_BONUS_LEVEL_20, 1.05f, "Arcane secrets of engineering");
                }
            case 19:
            case 18:
            case 17:
            case 16:
            case 15:
            case 14:
            case 13:
            case 12:
            case 11:
            case 10:
                d = new DropData();
                d.chances = data.level / 4;
                d.group = "ship_bp";
                dropList.add(d);
            case 9:
            case 8:
            case 7:
            case 6:
            case 5:
                // if we retired a post-4th level character we've basically won so we get special rewards
                textPanel.addPara("\"You're a starship captain, right? I used to fancy myself one, "
                        + "but some friends of mine managed to convince me otherwise before I got myself killed,\" " + person.getName().getFirst()
                        + " laughs, sliding a keycard to you across the table. \"Here, all my equipment's been sitting in a warehouse "
                        + "gathering dust these past few cycles. Maybe you can make use of it.\"");
                d = new DropData();
                d.chances = data.level;
                d.group = "weapons2";
                dropList.add(d);
                d = new DropData();
                d.chances = data.level / 4;
                d.group = "weapon_bp";
                dropList.add(d);
                d = new DropData();
                d.chances = data.level / 4;
                d.group = "fighter_bp";
                dropList.add(d);
                CargoAPI salvage = SalvageEntity.generateSalvage(random, 1f, 1f, 1f, 1f, null, dropList);
                for (CargoStackAPI stack : salvage.getStacksCopy()) {
                    textPanel.setFontSmallInsignia();
                    textPanel.addPara("Gained " + (int) stack.getSize() + " " + stack.getDisplayName(), p, h, stack.getDisplayName());
                    textPanel.setFontInsignia();
                }
                CargoAPI cargo = Global.getSector().getPlayerFleet().getCargo();
                cargo.addAll(salvage);
            case 4:
            case 3:
            case 2:
                // we also get paid, if we're above 1st level
                textPanel.addPara(person.getName().getFirst() + " kicks a plastic crate full of rulebooks out from under the table. "
                        + "\"Here, these weren't doing me any good down there. I'd like you to have them,\" " + getHeOrShe() + " says. "
                        + "You surreptitiously check the total value of the titles on your TriPad and your eyes widen.");
                AddRemoveCommodity.addCreditsGainText(LEVEL_UP_PAYMENT * data.level, dialog.getTextPanel());
                try {
                    Global.getSector().getPlayerFleet().getCargo().getCredits().add(LEVEL_UP_PAYMENT * data.level);
                } catch (NullPointerException npx) {
                    log.error("Vayra got lazy and I couldn't figure out how to credit the player");
                }
            default:
                // we also get xp
                Global.getSector().getPlayerStats().addXP(data.xp, textPanel, true);
                break;
        }
        textPanel.addPara("\"Thanks for playing with me!,\" " + person.getName().getFirst() + " grins. \"I hope you had fun!\"");
    }

    private void describeRoom(Options option) {
        TextPanelAPI textPanel = dialog.getTextPanel();
        String forceNext = room == null ? null : room.forceNext;
        room = null;

        // if no forced room, generate one randomly based on depth
        // also note we don't go into the forced room if we're retreating
        if (Options.GO_HOME.equals(option) || forceNext == null) {
            WeightedRandomPicker<VayraRPGRoomData> rooms = new WeightedRandomPicker<>();
            for (VayraRPGRoomData r : ROOMS) {

                // skip special rooms that we've already been to since entering the dungeon
                if (visitedSpecial.contains(r)) {
                    continue;
                }

                // normal weight stuff
                if (r.depth != 0) {
                    float weight = 0f;
                    if (depth == r.depth) {
                        weight = 3f; // thrice as likely to encounter a given weighted room at its exact depth
                    } else if (depth > r.depth) {
                        weight = (float) (r.depth / depth) * 3f; // progressively less likely to encounter rooms beyond their depth
                    }
                    rooms.add(r, weight);
                } else {
                    rooms.add(r, 1f); // quite likely to encounter a lot of normal rooms
                }
            }
            room = rooms.pick();
        } else {
            // if we have a forced room, just use that
            for (VayraRPGRoomData r : ROOMS) {
                if (forceNext.equals(r.name)) {
                    room = r;
                }
            }
        }

        // safety corridor
        if (room == null) {
            room = ROOMS.get(0);
            log.warn("what the fuck where are we? i guess it's a corridor now because i couldn't decide.");
        }
        log.info("now entering " + room.name + " at depth " + depth);

        // track us having entered this room, if it's special
        if (room.forceNext != null) {
            visitedSpecial.add(room);
            log.info("adding " + room.name + " to list of visited special rooms");
        }

        if (light > 0) {
            textPanel.addPara("You enter " + aOrAn(room.name) + " " + room.name + ".");
            if (room.desc != null) {
                textPanel.appendToLastParagraph(" " + room.desc);
            }
        } else {
            textPanel.addPara("You enter a dark room.");
        }
    }

    private VayraRPGMonsterData generateMonster() {
        WeightedRandomPicker<VayraRPGMonsterData> monsters = new WeightedRandomPicker<>();

        if (room.forceMonster != null) {
            log.info("this room is supposed to have a " + room.forceMonster + " in it, so i'm gonna look for one");
        } else {
            log.info("rolling random encounter for " + room.name + " at depth " + depth);
        }

        for (VayraRPGMonsterData m : MONSTERS) {
            int mDepth = m.HD * 10;
            String monsterLogString = "flipping through the monster manual... ah, " + m.name + ", page " + mDepth + ". ";
            float weight;
            if (mDepth >= depth) {
                if (mDepth == 10) {
                    // special case for easy bois
                    weight = 1f;
                    if (VAYRA_DEBUG) {
                        monsterLogString += m.name + " is harder than this room but still very easy so its weight is " + weight + ". ";
                    }
                } else {
                    // if monster is harder than level, scale this way
                    weight = (float) depth / mDepth;
                    weight = weight >= 0.3f ? weight : 0f; // never spawn something way too out of depth
                    if (VAYRA_DEBUG) {
                        monsterLogString += m.name + " is too hard for this place so its weight is " + weight + ". ";
                    }
                }
            } else {
                // if level is harder than monster, scale this way
                weight = (float) mDepth / depth;
                if (depth <= 130) {
                    weight = depth - mDepth >= 35 ? 0f : weight; // keep monsters within ~3 levels of what they should be
                } else {
                    weight = mDepth >= 100 ? weight : 0f; // never run out of 10+ HD monsters at higher depths
                }
                if (VAYRA_DEBUG) {
                    monsterLogString += m.name + " is too easy for this place so its weight is " + weight + ". ";
                }
            }
            weight *= m.weight;
            if (VAYRA_DEBUG) {
                monsterLogString += m.name + " has a personal weight of " + m.weight + " so i'm multiplying by that to get " + weight + ". ";
            }

            // if we have a forceMonster, ignore any monsters that aren't that one and set that one's weight to 1
            if (room.forceMonster != null) {
                if (!room.forceMonster.equals(m.name)) {
                    weight = 0f;
                } else {
                    weight = 1f;
                    log.info("here it is! just the " + m.name + " i was looking for. setting weight to " + weight + " and everything else to 0");
                }
            }
            monsters.add(m, weight);
            if (VAYRA_DEBUG) {
                log.info(monsterLogString + "added " + m.name + " to picker with weight " + weight + ".");
            }
        }

        if (VAYRA_DEBUG) {
            log.info("monsters contains " + monsters.getItems().size() + " items");
        }

        VayraRPGMonsterData fucko = monsters.pick();
        if (fucko == null) {
            log.warn("i couldn't find any monsters for the " + room.name + " so i'm putting a NPE there to menace the player with its sharp teeth instead");
            fucko = new VayraRPGMonsterData("null pointer exception", 1, 8, 6, "sharp teeth", 4, 7, 0f);
        } else {
            log.info("picked " + fucko.name + " to menace the player with its " + fucko.weapon);
        }

        // duplicate it so we don't have to worry if we change its name
        fucko = fucko.duplicate();

        // this is simpler as a hack than a bunch of other if checks later
        if (light < 1) {
            WeightedRandomPicker<String> shadows = new WeightedRandomPicker<>();
            shadows.add("shadowy figure", 3f);
            shadows.add("shadowed creature", 3f);
            shadows.add("dark shape", 3f);
            shadows.add("indistinguishable patch of darkness");
            shadows.add("grue");
            fucko.name = shadows.pick();
        }

        return fucko;
    }

    private void rollEncounter() {
        TextPanelAPI textPanel = dialog.getTextPanel();
        monsterHostile = false;
        wonInitiative = false;
        checkedMorale = false;
        negotiation = 0;
        monster = null;
        trap = null;
        monsterHpMax = 0;
        monsterHp = 0;

        // roll for monster
        if (Math.random() < room.monsterChance) {

            // get a monster (this automatically gets a duplicate, actually, so we don't have to worry about editing it)
            monster = generateMonster();

            WeightedRandomPicker<String> lurkins = new WeightedRandomPicker<>();
            lurkins.addAll(LURKS);
            String lurks = lurkins.pick();

            textPanel.addPara(Misc.ucFirst(aOrAn(monster.name)) + " " + monster.name + " " + lurks + " here.", h);

            // monster encounter
            int monsterHpRoll = 0;
            for (int i = 0; i < monster.HD; i++) {
                monsterHpRoll += d6();
            }
            monsterHpMax = monsterHpRoll;
            monsterHp = monsterHpMax;

            // reaction roll
            int reactionRoll = d6() + d6() + data.CHA;
            textPanel.addPara(person.getName().getFirst() + " rolls for initial reaction (2d6+CHA) behind " + getHisOrHer() + " DM screen.", g);
            if (room.forceHostile == -1 && monster.aggression < 12) {
                // friendly
            } else if (reactionRoll <= monster.aggression || monster.aggression >= 12 || room.forceHostile == 1) {
                // hostile
                monsterHostile = true;
            } else {
                // neutral, which is functionally the same as friendly
            }

            // roll initiative
            int initiative = d20();
            textPanel.addPara("You rolled a " + initiative + data.textInt() + " (1d20+INT) for initiative, total " + (initiative + data.INT) + ".",
                    g, h, initiative + data.textInt(), (initiative + data.INT) + "");
            initiative += data.INT;
            if (initiative >= 10) {
                // won initiative
                wonInitiative = true;
                textPanel.addPara("You react before the " + monster.name + "!", p);
            } else {
                // lost initiative
                wonInitiative = false;
                takeMonsterTurn();
            }

            // if no monster, roll for trap
        } else if (Math.random() < room.trapChance) {
            WeightedRandomPicker<VayraRPGTrapData> traps = new WeightedRandomPicker<>();

            for (VayraRPGTrapData trp : TRAPS) {
                if (depth >= trp.depth) {
                    traps.add(trp);
                }
            }
            trap = traps.pick();

            // trapfinding
            boolean foundTrap;
            int trapFinding = d20();
            if (light > 0) {
                foundTrap = trapFinding + data.INT >= 10;
            } else {
                foundTrap = trapFinding + data.INT >= 20;
            }
            textPanel.addPara(person.getName().getFirst() + " asks you innocently for an Intelligence check.", g);
            textPanel.addPara("You rolled a " + trapFinding + data.textInt() + " (1d20+INT), total " + (trapFinding + data.INT) + ".",
                    g, h, trapFinding + data.textInt(), (trapFinding + data.INT) + "");

            boolean triggeredTrap;
            // found the trap, have enough light
            if (foundTrap && light > 0) {
                textPanel.addPara("You spot " + aOrAn(trap.name) + " " + trap.name + " and carefully edge your way around it.", p);
                triggeredTrap = false;

                // found the trap in the dark!
            } else if (foundTrap) {
                textPanel.addPara("You notice " + aOrAn(trap.name) + " " + trap.name + " in the darkness just in time to avoid it.", p);
                triggeredTrap = false;

                // walked into the trap, had enough light
            } else if (light > 0) {
                textPanel.addPara("You stumble into " + aOrAn(trap.name) + " " + trap.name + ".", h);
                triggeredTrap = true;

                // walked into the trap in the dark
            } else {
                textPanel.addPara("You stumble into " + aOrAn(trap.name) + " " + trap.name + " in the darkness.", h);
                triggeredTrap = true;
            }

            if (triggeredTrap) {
                boolean failedSave;
                String avoid = "avoid";
                int trapSave = d20();
                switch (trap.save) {
                    case "str":
                        textPanel.addPara("You rolled a " + trapSave + data.textStr() + " (1d20+STR), total " + (trapSave + data.STR) + ".",
                                g, h, trapSave + data.textStr(), " " + (trapSave + data.STR));
                        failedSave = trapSave + data.STR < trap.DC;
                        avoid = "resist";
                        break;
                    case "dex":
                        textPanel.addPara("You rolled a " + trapSave + data.textDex() + " (1d20+DEX), total " + (trapSave + data.DEX) + ".",
                                g, h, trapSave + data.textDex(), " " + (trapSave + data.DEX));
                        failedSave = trapSave + data.DEX < trap.DC;
                        avoid = "avoid";
                        break;
                    case "int":
                        textPanel.addPara("You rolled a " + trapSave + data.textInt() + " (1d20+INT), total " + (trapSave + data.INT) + ".",
                                g, h, trapSave + data.textInt(), " " + (trapSave + data.INT));
                        failedSave = trapSave + data.INT < trap.DC;
                        avoid = "think your way through";
                        break;
                    case "cha":
                        textPanel.addPara("You rolled a " + trapSave + data.textCha() + " (1d20+CHA), total " + (trapSave + data.CHA) + ".",
                                g, h, trapSave + data.textCha(), " " + (trapSave + data.CHA));
                        failedSave = trapSave + data.CHA < trap.DC;
                        avoid = "resist";
                        break;
                    default:
                        failedSave = true;
                        break;
                }
                if (failedSave) {
                    int dmg = d(trap.dmgDie);
                    currHp -= dmg;
                    textPanel.addPara("You fail to " + avoid + " the " + trap.name + " and take " + dmg + " damage, dropping you to " + currHp + " HP!",
                            h, n, dmg + "", currHp + " HP");
                    if (currHp <= 0) {
                        killCharacter();
                    }
                } else {
                    textPanel.addPara("You manage to " + avoid + " the " + trap.name + ".", p);
                }

            }
        }

        // also roll separately for loot, if we didn't die immediately and there's no monster here (monsters have their own treasure)
        if (!data.dead && Math.random() < room.lootChance && monster == null) {
            findTreasure();
        }
    }

    private void monsterFlee() {
        TextPanelAPI textPanel = dialog.getTextPanel();

        textPanel.addPara("The " + monster.name + " flees in terror!", p);
        monster = null;
        findTreasure();
    }

    private void takeMonsterTurn() {
        TextPanelAPI textPanel = dialog.getTextPanel();

        // null lol
        if (monster == null) {
            return; // monster ever existed
        }

        // dead lol
        if (monsterHp <= 0) {
            textPanel.addPara("The " + monster.name + " falls to the ground, dead.", p);
            findTreasure();
            monster = null;
            return;
        }

        // friendly lol
        if (!monsterHostile) {
            if (d6() + d6() < monster.aggression) {
                // monsters are not very good at guessing
                int guessRooms = (int) (((float) depth * 0.75f) + (Math.random() * (float) depth / 2f));
                guessRooms = guessRooms < 2 ? 2 : guessRooms;
                textPanel.addPara("\"Real far from home ain'tcha, pig?\" the " + monster.name + " says. \"Ayup, ah'd reckon we'sabout "
                        + guessRooms + " rooms deep, we is.\"", h);
                textPanel.addPara("You snicker quietly at " + person.getName().getFirst() + "'s terrible accent. " + Misc.ucFirst(getHeOrShe()) + " looks embarrassed.", g);
            } else {
                textPanel.addPara("The " + monster.name + " seems to be ignoring you.", h);
            }
            return;
        }

        // check if it will accept a bribe
        if (negotiation == 1) {
            int reactionRollForBribe = d6() + d6();
            textPanel.addPara("You rolled a " + reactionRollForBribe + data.textCha() + " (2d6+CHA), total " + (reactionRollForBribe + data.CHA) + ".",
                    g, h, reactionRollForBribe + data.textCha(), (reactionRollForBribe + data.CHA) + "");
            reactionRollForBribe += +data.CHA;
            if (reactionRollForBribe <= monster.aggression || monster.aggression >= 12) {
                // no negotiation is possible
                negotiation = -1;
            } else {
                // demand money
                negotiation = 2;
            }
        }

        // if it's demanding a bribe
        if (negotiation == 2) {
            int bribeCost = (monster.HD ^ 2) * BRIBE_COST;
            textPanel.addPara("\"Listen, I'm a reasonable " + monster.name + ",\" the " + monster.name + " says. \"I think we can come to an arrangement here. "
                    + "I'll let you go for " + Misc.getWithDGS(bribeCost) + " gold pieces. How does that strike you?\"", h);

            // otherwise it checks morale
        } else if (!checkedMorale && monsterHp <= (monsterHpMax / 2)) {
            checkedMorale = true;
            int moraleCheck = d6() + d6();
            textPanel.addPara(person.getName().getFirst() + " frowns and rolls a morale check for the " + monster.name + "... " + Misc.ucFirst(getHeOrShe()) + " rolled a " + moraleCheck + " (2d6)!", g, h, moraleCheck + "");
            if (moraleCheck > monster.morale) {
                monsterFlee();
            }
            takeMonsterTurn();
            // otherwise it just hits us
        } else {
            textPanel.addPara("The " + monster.name + " attacks you with its " + monster.weapon, h);
            int monsterAttackRoll = d20() + monster.HD;
            if (monsterAttackRoll >= data.getAc()) {
                int monsterDamageRoll = d(monster.dmgDie);
                currHp -= monsterDamageRoll;
                textPanel.appendToLastParagraph(" and hits for " + monsterDamageRoll + " damage, dropping you to " + currHp + " HP!");
                textPanel.highlightInLastPara(n, monsterDamageRoll + "", currHp + " HP");
                if (currHp <= 0) {
                    killCharacter();
                }
            } else {
                textPanel.appendToLastParagraph(" and misses!");
            }
        }
    }

    private void findTreasure() {

        TextPanelAPI textPanel = dialog.getTextPanel();

        // loot picker
        WeightedRandomPicker<String> loot = new WeightedRandomPicker<>();

        // set up some variables
        String lootString = "";

        int gp = 0;
        int oil = 0;
        boolean enchantedArmor = false;
        boolean improvedArmor = false;
        boolean enchantedWeapon = false;
        boolean improvedWeapon = false;
        boolean plural = false;
        String oldGear = null;

        // first round of picks
        loot.add("gp", 5f); // gold is always a likely option
        if (data.getAc() < 17) {
            loot.add("armor", 1f);
        }
        if ((float) (DMG.get(data.weapon) / 2f) + 0.5f + data.magicWeapon < 5.5f) {
            loot.add("weapon", 1f);
        }
        loot.add("items", 1f);
        loot.add("enchant", 0.2f);

        // pick the category, then clear the picker
        String lootType = loot.pick();
        loot.clear();

        // second round of picks, more logic
        switch (lootType) {
            case "gp":
                int goldDice = 2 + ((depth / 5) * 2);
                for (int i = 0; i < goldDice; i++) {
                    gp += d6();
                }
                lootString += gp + " gold pieces";
                data.gp += gp;
                plural = true;
                break;
            case "armor":
                if (!data.hasShield) {
                    loot.add("shield");
                }
                if (AC.get(data.armor) + data.magicArmor < 10) {
                    loot.add("cloth");
                }
                if (AC.get(data.armor) + data.magicArmor < 12) {
                    loot.add("leather");
                }
                if (AC.get(data.armor) + data.magicArmor < 14) {
                    loot.add("chain");
                }
                if (AC.get(data.armor) + data.magicArmor < 16) {
                    loot.add("plate");
                }
                String armorType = loot.pick();
                switch (armorType) {
                    case "shield":
                        data.hasShield = true;
                        lootString += "a shield";
                        break;
                    default:
                        lootString += "a set of " + armorType + " armor";
                        oldGear = data.getMagicArmorString() + armorName(data.armor);
                        data.magicArmor = 0;
                        data.armor = Armor.valueOf(armorType.toUpperCase());
                        improvedArmor = true;
                        break;
                }
                break;
            case "weapon":
                if ((float) (DMG.get(data.weapon) / 2f) + 0.5f + data.magicWeapon < 2.5f) {
                    loot.add("dagger");
                    loot.add("club");
                }
                if ((float) (DMG.get(data.weapon) / 2f) + 0.5f + data.magicWeapon < 3.5f) {
                    loot.add("staff");
                    loot.add("mace");
                    loot.add("sling");
                }
                if ((float) (DMG.get(data.weapon) / 2f) + 0.5f + data.magicWeapon < 4.5f) {
                    loot.add("spear");
                    loot.add("axe");
                    loot.add("bow");
                }
                if ((float) (DMG.get(data.weapon) / 2f) + 0.5f + data.magicWeapon < 5.5f) {
                    loot.add("sword");
                }
                String weaponType = loot.pick();
                switch (weaponType) {
                    default:
                        lootString += aOrAn(weaponType) + " " + weaponType;
                        oldGear = data.getMagicWeaponString() + weaponName(data.weapon);
                        data.magicWeapon = 0;
                        data.weapon = Weapon.valueOf(weaponType.toUpperCase());
                        improvedWeapon = true;
                        break;
                }
                break;
            case "items":
                loot.add("oil", 2f);
                loot.add("scroll");
                loot.add("potion");
                String item = loot.pick();
                switch (item) {
                    case "scroll":
                        data.scrolls++;
                        lootString += "a magic scroll";
                        break;
                    case "potion":
                        data.potions++;
                        lootString += "an unidentifiable potion";
                        break;
                    default:
                        oil += d6();
                        if (oil > 1) {
                            lootString += oil + " flasks of oil";
                            plural = true;
                        } else {
                            lootString += "a flask of oil";
                        }
                        data.oil += oil;
                        break;
                }
                break;
            case "enchant":
                loot.add("enchantArmor");
                loot.add("enchantWeapon");
                String enchantment = loot.pick();
                switch (enchantment) {
                    case "enchantArmor":
                        data.magicArmor++;
                        lootString += "an enchanted armor runestone";
                        enchantedArmor = true;
                        break;
                    case "enchantWeapon":
                        data.magicWeapon++;
                        lootString += "a vial of enchanted weapon oil";
                        enchantedWeapon = true;
                        break;
                    default:
                        break;
                }
            default:
                break;
        }

        WeightedRandomPicker<String> lootLocations = new WeightedRandomPicker<>();
        lootLocations.addAll(LOOT_LOCATIONS);
        String lootLocation = lootLocations.pick();
        textPanel.addPara("You spy " + lootString + " " + lootLocation + ".", h, p, lootString);

        if (enchantedArmor) {
            textPanel.appendToLastParagraph(" Your " + armorName(data.armor) + " shimmers with a soft golden light as the runestone improves it to " + data.getMagicArmorString() + ".");
        } else if (improvedArmor) {
            textPanel.appendToLastParagraph(" It's better than the " + oldGear + " you were wearing, so you don it immediately.");
        } else if (enchantedWeapon) {
            textPanel.appendToLastParagraph(" Your " + weaponName(data.weapon) + " shimmers with a soft golden light as the weapon oil improves it to " + data.getMagicWeaponString() + ".");
        } else if (improvedWeapon) {
            textPanel.appendToLastParagraph(" It's better than the " + oldGear + " you were carrying, so you pick it up immediately.");
        } else if (plural) {
            textPanel.appendToLastParagraph(" You pick them up.");
        } else {
            textPanel.appendToLastParagraph(" You pick it up.");
        }
    }

    private void roomOptions() {

        TextPanelAPI textPanel = dialog.getTextPanel();
        OptionPanelAPI optionsPanel = dialog.getOptionPanel();

        if (data.dead) {
            return;
        }

        // options
        if (monster != null) {
            int bribeCost = (monster.HD ^ 2) * BRIBE_COST;
            if (negotiation == 0) {
                optionsPanel.addOption("Attempt to negotiate with the " + monster.name, Options.TALK);
            } else if (negotiation == 2 && data.gp >= bribeCost) {
                optionsPanel.addOption("Pay the " + monster.name + " " + Misc.getWithDGS(bribeCost) + "gp", Options.TALK);
            }
            optionsPanel.addOption("Attack the " + monster.name, Options.ATTACK, "Attacking will make the monster hostile, if it wasn't already");
            if (data.oil > 0) {
                optionsPanel.addOption("Throw a flask of oil at the " + monster.name, Options.OIL, "Oil attacks deal 2d6 damage and are always made against AC 5, but receive no bonus from magical weapons. This will make the monster hostile, if it wasn't already");
            }
            if (data.scrolls > 0 && light > 0) {
                optionsPanel.addOption("Read a magic scroll", Options.SCROLL, "Scrolls have unpredictable effects, but are usually devastating. This will make the monster hostile, if it wasn't already");
            }
            optionsPanel.addOption("Flee from the " + monster.name, Options.FLEE);
            if (data.potions > 0 && currHp < data.maxHp) {
                optionsPanel.addOption("Drink a potion", Options.POTION, "Potions will heal you for most of your hit points.");
            }
        } else {
            if (depth <= 1) {
                textPanel.addPara("The light of day filters into the dungeon through an opening in one wall, beckoning you back to the outside world.");
                optionsPanel.addOption("Venture deeper into the dungeon", Options.GO_DEEPER);
                optionsPanel.addOption("Leave the dungeon", Options.TOWN);
            } else if (depth <= 6) {
                textPanel.addPara("You can feel a cool breeze. You can find your way back to the entrance easily from here.");
                optionsPanel.addOption("Venture deeper into the dungeon", Options.GO_DEEPER);
                optionsPanel.addOption("Make your way back to the entrance", Options.GO_HOME);
            } else if (light < 1 && Math.random() < BASE_LOST_CHANCE - (data.INT * 0.05f)) {
                optionsPanel.addOption("Wander onwards in the darkness", Options.GO_HOME);
                optionsPanel.addOption("Wander back towards the entrance... you hope", Options.GO_DEEPER);
            } else if (light < 1) {
                optionsPanel.addOption("Wander onwards in the darkness", Options.GO_DEEPER);
                optionsPanel.addOption("Wander back towards the entrance... you hope", Options.GO_HOME);
            } else {
                optionsPanel.addOption("Venture deeper into the dungeon", Options.GO_DEEPER);
                optionsPanel.addOption("Try to find your way back to the entrance", Options.GO_HOME);
            }
            if (data.potions > 0 && currHp < data.maxHp) {
                optionsPanel.addOption("Drink a potion", Options.POTION, "Potions will heal you for most of your hit points.");
            }
        }
    }

    private void tickLantern() {
        TextPanelAPI textPanel = dialog.getTextPanel();
        if (light > 0) {
            light--; // the lantern burns lower...
            if (light < 1) {
                if (data.oil > 0) {
                    data.oil--;
                    light += LANTERN_ROOMS + d6();
                    textPanel.addPara("You fill your lantern with oil.", h);
                    if (data.oil > 1) {
                        textPanel.appendToLastParagraph("You've got " + data.oil + " flasks of oil left.");
                    } else if (data.oil == 1) {
                        textPanel.addPara("You have one flask of oil left.", n);
                    } else {
                        textPanel.addPara("You're out of oil. Hope you can make it back before your lantern burns out.", n);
                    }
                } else {
                    textPanel.addPara("Your lantern gutters and burns out.", n);
                }
            }
        } else if (light < 1 && data.oil > 0) {
            data.oil--;
            light += LANTERN_ROOMS + d6();
            textPanel.addPara("You fill your lantern with oil.", h);
            if (data.oil > 1) {
                textPanel.appendToLastParagraph("You've got " + data.oil + " flasks of oil left.");
            } else if (data.oil == 1) {
                textPanel.addPara("You have one flask of oil left.", n);
            } else {
                textPanel.addPara("You're out of oil. Hope you can make it back before your lantern burns out.", n);
            }
        }

        if (light < 1 && depth > 1) {
            textPanel.addPara("It is dark. You are likely to be eaten by a grue.", n);
        }
    }

    private void maybeSip() {
        TextPanelAPI textPanel = dialog.getTextPanel();
        if (Math.random() < SIP_CHANCE) {
            if (currHp < data.maxHp) {
                int sip = data.level <= data.maxHp - currHp ? data.level : data.maxHp - currHp;
                textPanel.addPara("You take a sip from your trusty " + data.canteen + " and are healed for " + sip + " HP.", h, p, sip + "");
                currHp += sip;
            } else {
                textPanel.addPara("You take a sip from your trusty " + data.canteen + ".");
            }
        }
    }

    private void showCharacterSheet() {
        TextPanelAPI textPanel = dialog.getTextPanel();

        if (data == null) {
            textPanel.addPara("You don't seem to have a character sheet anymore.", g);
        } else {
            textPanel.addPara(data.name, g);
            textPanel.addPara("Level " + data.level + " " + data.className, g, h, data.level + "");
            if (data.level < 20) {
                int toLevel = XP.get(data.level + 1);
                textPanel.addPara("XP: " + data.xp + "/" + toLevel, g, h, data.xp + "/" + toLevel);
            } else {
                textPanel.addPara("XP: " + data.xp + " (at maximum level)", g, p, data.xp + " (at maximum level)");
            }
            textPanel.addPara("HP: " + currHp + "/" + data.maxHp + "    AC: " + data.getAc() + " (" + data.getMagicArmorString() + armorName(data.armor)
                    + data.getShieldString() + ")", g, h, currHp + "/" + data.maxHp, data.getAc() + "", data.getMagicArmorString());

            if (data.dead) {
                textPanel.addPara("You descended " + data.maxDepth + " rooms into the dungeon, and were " + data.fate + ".", g);
                textPanel.addPara("You are dead.", g, n, "dead");
            }

            textPanel.addPara("STR: " + data.textStr() + "    DEX: " + data.textDex() + "    INT: " + data.textInt() + "    CHA: " + data.textCha(),
                    g, h, data.textStr(), data.textDex(), data.textInt(), data.textCha());
            String textAttack = (data.getToHit() >= 0 ? "+" : "") + data.getToHit();
            textPanel.addPara("Attack: " + textAttack + " to hit, " + data.getMagicWeaponString() + weaponName(data.weapon) + ", " + data.getDamageString(),
                    g, h, textAttack, data.getMagicWeaponString(), data.getDamageString());
            textPanel.addPara(data.getInventoryString(), g, h, data.gp + "", data.oil + "", data.scrolls + "", data.potions + "");
        }
    }

    private void payout() {
        TextPanelAPI textPanel = dialog.getTextPanel();

        // we also get paid
        textPanel.addPara(person.getName().getFirst() + " hands you a custom painted miniature of your character to commemorate your achievements."
                + " You laugh it off at first, then you realize how much these things are worth...", g);
        AddRemoveCommodity.addCreditsGainText(LEVEL_UP_PAYMENT, dialog.getTextPanel());
        try {
            Global.getSector().getPlayerFleet().getCargo().getCredits().add(LEVEL_UP_PAYMENT);
        } catch (NullPointerException npx) {
            log.error("Vayra got lazy and I couldn't figure out how to credit the player");
        }
    }

    private void sufferConsequencesForYourIndiscretion() {
        TextPanelAPI textPanel = dialog.getTextPanel();

        WeightedRandomPicker<Consequence> cons = new WeightedRandomPicker<>();
        cons.add(Consequence.DEATH);
        cons.add(Consequence.PERMANENT_INJURY);
        cons.add(Consequence.CURSE_ARMOR);
        cons.add(Consequence.CURSE_WEAPON);
        if (data.hasShield) {
            cons.add(Consequence.LOSE_SHIELD);
        }
        if (data.armor != Armor.CLOTH) {
            cons.add(Consequence.LOSE_ARMOR);
        }
        if (DMG.get(data.weapon) > 4) {
            cons.add(Consequence.LOSE_WEAPON);
        }
        if (data.oil > 5 || data.scrolls + data.potions > 0) {
            cons.add(Consequence.LOSE_ITEMS);
        }
        Consequence con = cons.pick();

        WeightedRandomPicker<String> voices = new WeightedRandomPicker<>();
        voices.add("shrill");
        voices.add("smug");
        voices.add("quavering");
        String voice = voices.pick();

        textPanel.addPara(Misc.ucFirst(getHeOrShe()) + " clears " + getHisOrHer() + " throat and, glaring at you from where "
                + getHeOrShe() + " cowers behind the upturned table, informs you in a " + voice + " voice that ");

        switch (con) {
            case DEATH:
                textPanel.appendToLastParagraph("your character has died while trying to escape from the dungeon.");
                textPanel.highlightInLastPara(n, "your character has died");
                killCharacter();
                break;
            case PERMANENT_INJURY:
                data.maxHp -= d6();
                if (data.maxHp < 1) {
                    data.maxHp = 1;
                }
                currHp = data.maxHp;
                textPanel.appendToLastParagraph("your character has suffered an injury while trying to escape from the dungeon "
                        + "which reduces your maximum HP to " + data.maxHp + ".");
                textPanel.highlightInLastPara(n, "reduces your maximum HP to " + data.maxHp);
                break;
            case CURSE_ARMOR:
                if (data.magicArmor > -1) {
                    data.magicArmor = -1;
                    textPanel.appendToLastParagraph("your character's armor has been cursed to " + data.magicArmor + " while trying to escape from the dungeon.");
                    textPanel.highlightInLastPara(n, "cursed to " + data.magicArmor);
                } else {
                    data.magicArmor--;
                    textPanel.appendToLastParagraph("the curse on your character's armor has worsened to " + data.magicArmor + " while trying to escape from the dungeon.");
                    textPanel.highlightInLastPara(n, "worsened to " + data.magicArmor);
                }
                break;
            case CURSE_WEAPON:
                if (data.magicWeapon > -1) {
                    data.magicWeapon = -1;
                    textPanel.appendToLastParagraph("your character's weapon has been cursed to " + data.magicWeapon + " while trying to escape from the dungeon.");
                    textPanel.highlightInLastPara(n, "cursed to " + data.magicWeapon);
                } else {
                    data.magicWeapon--;
                    textPanel.appendToLastParagraph("the curse on your character's weapon has worsened to " + data.magicWeapon + " while trying to escape from the dungeon.");
                    textPanel.highlightInLastPara(n, "worsened to " + data.magicWeapon);
                }
                break;
            case LOSE_SHIELD:
                data.hasShield = false;
                textPanel.appendToLastParagraph("your character's shield has been lost while trying to escape from the dungeon.");
                textPanel.highlightInLastPara(n, "shield has been lost");
                break;
            case LOSE_ARMOR:
                data.armor = Armor.CLOTH;
                data.magicArmor = 0;
                textPanel.appendToLastParagraph("your character's armor has been lost while trying to escape from the dungeon.");
                textPanel.highlightInLastPara(n, "armor has been lost");
                break;
            case LOSE_WEAPON:
                data.weapon = Weapon.CLUB;
                data.magicWeapon = 0;
                textPanel.appendToLastParagraph("your character's weapon has been lost while trying to escape from the dungeon.");
                textPanel.highlightInLastPara(n, "weapon has been lost");
                break;
            case LOSE_ITEMS:
                data.oil = 0;
                data.scrolls = 0;
                data.potions = 0;
                textPanel.appendToLastParagraph("all of your character's oil, scrolls, and potions have been lost while trying to escape from the dungeon.");
                textPanel.highlightInLastPara(n, "oil, scrolls, and potions have been lost");
                break;
            default:
                break;
        }
    }

    private void killCharacter() {
        @SuppressWarnings("unchecked")
        List<VayraRPGCharacterSheetData> retired = (List<VayraRPGCharacterSheetData>) Global.getSector().getPersistentData().get(KEY_RETIRED);
        if (retired == null) {
            retired = new ArrayList<>();
        }
        data.dead = true;
        if (monster != null) {
            data.fate = "struck down by " + aOrAn(monster.name) + " " + monster.name;
        } else if (trap != null) {
            data.fate = "struck down by " + aOrAn(trap.name) + " " + trap.name;
        } else {
            data.fate = "killed while trying to escape the dungeon";
        }
        retired.add(data);

        if (VayraRPGDeadCharacterIntel.getInstance() == null) {
            VayraRPGDeadCharacterIntel deadIntel = new VayraRPGDeadCharacterIntel();
            Global.getSector().getIntelManager().addIntel(deadIntel);
        }

        Global.getSector().getPersistentData().remove(KEY_CHARACTER_SHEET);
        Global.getSector().getPersistentData().put(KEY_RETIRED, retired);
    }

    public String getNewClassName() {

        WeightedRandomPicker<String> adjs = new WeightedRandomPicker<>();
        adjs.addAll(VayraDungeonMasterData.CLASS_ADJECTIVES);
        String adj = adjs.pick();

        WeightedRandomPicker<String> nouns = new WeightedRandomPicker<>();
        nouns.addAll(VayraDungeonMasterData.CLASS_NOUNS);
        String noun = nouns.pick();

        if (IS_AVANITIA && Math.random() < 0.1f) {
            nouns.clear();
            nouns.add("Bugfinder");
            nouns.add("Beta Tester");
            nouns.add("Vayra Botherer");
            noun = nouns.pick();
        }

        return adj + " " + noun;
    }

    public String getNewGameName() {

        WeightedRandomPicker<String> games = new WeightedRandomPicker<>();
        games.addAll(VayraDungeonMasterData.GAMES);
        String gameOne = games.pickAndRemove();

        String startsWith = gameOne.substring(0, 1);

        List<String> temp = new ArrayList<>();
        temp.addAll(games.getItems());
        for (String game : temp) {
            if (!game.startsWith(startsWith)) {
                games.remove(game);
            }
        }

        String gameTwo = games.pick();

        return gameOne + " & " + gameTwo;
    }

    public String getNewCharacterName() {

        WeightedRandomPicker<FactionAPI> factions = new WeightedRandomPicker<>();
        factions.addAll(Global.getSector().getAllFactions());
        FactionAPI faction = factions.pick();

        try {
            return faction.createRandomPerson(player.getGender()).getName().getFullName();
        } catch (NullPointerException npe) {
            log.error("Vayra got lazy");
            if (IS_AVANITIA) {
                return "Avanitia, probably";
            } else {
                return "Uranus I Guess";
            }
        }
    }

    public int rollStat() {
        int roll = d6() + d6() + d6();
        int mod;
        switch (roll) {
            case 18:
                mod = 4;
                break;
            case 17:
            case 16:
                mod = 3;
                break;
            case 15:
            case 14:
                mod = 2;
                break;
            case 13:
            case 12:
                mod = 1;
                break;
            case 11:
            case 10:
                mod = 0;
                break;
            case 9:
            case 8:
                mod = -1;
                break;
            case 7:
            case 6:
                mod = -2;
                break;
            case 5:
            case 4:
                mod = -3;
                break;
            case 3:
                mod = -4;
                break;
            default:
                mod = 0;
                break;
        }
        return mod;
    }

    public static String weaponName(Weapon weapon) {
        return weapon.name().toLowerCase().replace("_", " ");
    }

    public static String armorName(Armor armor) {
        return armor.name().toLowerCase() + " armor";
    }

    public int d6() {
        return 1 + random.nextInt(6);
    }

    public int d20() {
        return 1 + random.nextInt(20);
    }

    public int d(int n) {
        return 1 + random.nextInt(n);
    }

    public int rollDamage(VayraRPGCharacterSheetData sheet) {
        return d(DMG.get(sheet.weapon) + sheet.getDamageModifier());
    }
}
