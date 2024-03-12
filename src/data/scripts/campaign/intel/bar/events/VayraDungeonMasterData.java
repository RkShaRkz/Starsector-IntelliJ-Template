package data.scripts.campaign.intel.bar.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class VayraDungeonMasterData {

    // these are all of the things it is possible to wear in a tabletop RPG
    public enum Armor {
        CLOTH,
        LEATHER,
        CHAIN,
        PLATE
    }

    // these are all of the things it is possible to wield in a tabletop RPG
    public enum Weapon {
        DAGGER,
        CLUB,
        STAFF,
        MACE,
        SLING,
        SPEAR,
        AXE,
        BOW,
        SWORD,
        BIFURCATED_BLADE,
        TRIPLE_TALWAR,
        QUADRANGULAR_CUTTER,
        FIVE_FINGERED_FALCHION,
        SEXTUPLE_SPATHA,
        SEVEN_BLADED_SCIMITAR,
        OCTUPLE_ODACHI,
        NINEFOLD_KNIFE
    }

    // these are the stats for the above, and also how much xp it takes to level
    public static final HashMap<Armor, Integer> AC = new HashMap<>();
    public static final HashMap<Weapon, Integer> DMG = new HashMap<>();
    public static final HashMap<Integer, Integer> XP = new HashMap<>();

    static {
        AC.put(Armor.CLOTH, 10);
        AC.put(Armor.LEATHER, 12);
        AC.put(Armor.CHAIN, 14);
        AC.put(Armor.PLATE, 16);
        DMG.put(Weapon.DAGGER, 4);
        DMG.put(Weapon.CLUB, 4);
        DMG.put(Weapon.STAFF, 6);
        DMG.put(Weapon.MACE, 6);
        DMG.put(Weapon.SLING, 6);
        DMG.put(Weapon.SPEAR, 8);
        DMG.put(Weapon.AXE, 8);
        DMG.put(Weapon.BOW, 8);
        DMG.put(Weapon.SWORD, 10);
        DMG.put(Weapon.BIFURCATED_BLADE, 20);
        DMG.put(Weapon.TRIPLE_TALWAR, 30);
        DMG.put(Weapon.QUADRANGULAR_CUTTER, 40);
        DMG.put(Weapon.FIVE_FINGERED_FALCHION, 50);
        DMG.put(Weapon.SEXTUPLE_SPATHA, 60);
        DMG.put(Weapon.SEVEN_BLADED_SCIMITAR, 70);
        DMG.put(Weapon.OCTUPLE_ODACHI, 80);
        DMG.put(Weapon.NINEFOLD_KNIFE, 90);
        XP.put(1, 0);
        XP.put(2, 200);
        XP.put(3, 600);
        XP.put(4, 1200);
        XP.put(5, 2000);
        XP.put(6, 3000);
        XP.put(7, 4000);
        XP.put(8, 5000);
        XP.put(9, 6000);
        XP.put(10, 7000);
        XP.put(11, 8000);
        XP.put(12, 9000);
        XP.put(13, 10000);
        XP.put(14, 11000);
        XP.put(15, 12000);
        XP.put(16, 13000);
        XP.put(17, 14000);
        XP.put(18, 15000);
        XP.put(19, 16000);
        XP.put(20, 17000);
        XP.put(21, 69420);
    }

    // this is our cute little holder class for our character sheet
    public static class VayraRPGCharacterSheetData implements Comparable {

        public String name;         // WHO AMMM IIIIIIII
        public String className;    // WHAT AMMM IIIIIIII
        public int level = 1;       // maximum level is 20
        public int xp = 0;          // 0/200/600/1200/2000/+1000 to level up
        // you get xp by recovering gp back to town, by the way

        public int maxDepth = 0;    // bragging rights

        public int maxHp = 6;           // Maximum. I guess this is 6 + STR, +1d6+STR reroll and take higher each level

        // stats can be positive or negative, and are a flat modifier on d20 rolls
        public int STR = 0;             // applies to HP and damage
        public int DEX = 0;             // applies to attack rolls and fleeing
        public int INT = 0;             // applies to finding traps, initiative, navigating in the dark, and scroll save DCs
        public int CHA = 0;             // applies to reaction rolls and bribery rolls

        public Weapon weapon = Weapon.DAGGER;     // how bad can you hurt 'em
        public int magicWeapon = 0;                 // boon or bane
        public Armor armor = Armor.CLOTH;         // protect ya neck
        public int magicArmor = 0;                  // boon or bane
        public boolean hasShield = false;           // and cover ya ass
        public int gp = 0;                          // filthy lucre
        public int oil = 0;                         // while there's still light (also, you can throw it)
        public int scrolls = 0;                     // usually destructive
        public int potions = 0;                     // usually restorative
        public String canteen;

        public boolean dead = false;
        public boolean retired = false;
        public String fate = null;

        public int getAc() {
            int val = AC.get(this.armor) + this.magicArmor;
            if (this.hasShield) {
                val += 1;
            }
            return val;
        }

        public int getToHit() {
            int val = this.level;
            if (val > 4) {
                val = 4;
            }
            val += this.magicWeapon;
            val += this.DEX;
            return val;
        }

        public int getDamageModifier() {
            return this.magicWeapon + this.STR;
        }

        public String getDamageString() {
            String dmg = "1d" + DMG.get(this.weapon);
            if (getDamageModifier() >= 0) {
                dmg += "+";
            }
            dmg += getDamageModifier();
            return dmg;
        }

        public String textStr() {
            return (this.STR >= 0 ? "+" : "") + this.STR;
        }

        public String textDex() {
            return (this.DEX >= 0 ? "+" : "") + this.DEX;
        }

        public String textInt() {
            return (this.INT >= 0 ? "+" : "") + this.INT;
        }

        public String textCha() {
            return (this.CHA >= 0 ? "+" : "") + this.CHA;
        }

        public String getMagicArmorString() {
            String ma = "";
            if (this.magicArmor != 0) {
                if (this.magicArmor > 0) {
                    ma += "+" + this.magicArmor + " enchanted ";
                } else {
                    ma += this.magicArmor + " cursed ";
                }
            }
            return ma;
        }

        public String getMagicWeaponString() {
            String mw = "";
            if (this.magicWeapon != 0) {
                if (this.magicWeapon > 0) {
                    mw += "+" + this.magicWeapon + " enchanted ";
                } else {
                    mw += this.magicWeapon + " cursed ";
                }
            }
            return mw;
        }

        public String getShieldString() {
            String s = "";
            if (this.hasShield) {
                s += " & shield";
            }
            return s;
        }

        public String getInventoryString() {
            String inv = "You are carrying ";
            int items = 0;

            // gold pieces
            if (this.gp > 1) {
                items++;
                inv += this.gp + " gold pieces";
            } else if (this.gp == 1) {
                items++;
                inv += this.gp + " gold piece";
            }

            // oil flasks
            if (this.oil > 1) {

                if (items > 0) {
                    inv += ", ";
                }
                items++;
                inv += this.oil + " flasks of oil";
            } else if (this.oil == 1) {

                if (items > 0) {
                    inv += ", ";
                }
                items++;
                inv += this.oil + " flask of oil";
            }

            // magic scrolls
            if (this.scrolls > 1) {

                if (items > 0) {
                    inv += ", ";
                }
                items++;
                inv += this.scrolls + " magic scrolls";
            } else if (this.scrolls == 1) {

                if (items > 0) {
                    inv += ", ";
                }
                items++;
                inv += this.scrolls + " magic scroll";
            }

            // magic potions
            if (this.potions > 1) {

                if (items > 0) {
                    inv += ", ";
                }
                items++;
                inv += this.potions + " potions";
            } else if (this.potions == 1) {

                if (items > 0) {
                    inv += ", ";
                }
                items++;
                inv += this.potions + " potion";
            }

            if (items > 1) {
                inv += ", and your trusty " + this.canteen;
            } else if (items == 1) {
                inv += " and your trusty " + this.canteen;
            } else {
                inv += "your trusty " + this.canteen;
            }

            return inv + ".";
        }

        @Override
        public int compareTo(Object o) {
            VayraRPGCharacterSheetData other = (VayraRPGCharacterSheetData) o;
            return (int) Math.signum(this.xp - other.xp);
        }
    }

    // this is our little holder class for rooms
    public static class VayraRPGRoomData {

        public String name;
        public int depth;
        public int monsterDifficulty;
        public float monsterChance;
        public float lootChance;
        public float trapChance;
        public String desc;

        public String forceMonster; // OPTIONAL, does NOT override monsterChance - just overrides which monster will appear IF one does
        public int forceHostile; // OPTIONAL, -1 = force friendly, 1 = force hostile
        public String forceNext; // OPTIONAL, forces the named room to spawn next. if extant, only spawn once per entering the dungeon

        VayraRPGRoomData(String name, Integer depth, Float monsterChance, Float lootChance, Float trapChance, String desc, String forceMonster, Integer forceHostile, String forceNext) {
            this.name = name;
            this.depth = depth == null ? 0 : depth;
            this.monsterChance = monsterChance == null ? 0.3f : monsterChance;
            this.lootChance = lootChance == null ? 0.15f : lootChance;
            this.trapChance = trapChance == null ? 0.15f : trapChance;
            this.desc = desc;
            this.forceMonster = forceMonster;
            this.forceHostile = forceHostile == null ? 0 : forceHostile;
            this.forceNext = forceNext;
        }
    }

    // these are the rooms
    // name, depth, difficulty, monsterChance, lootChance, trapChance, desc, OPTIONAL forceMonster, OPTIONAL forceHostile, OPTIONAL forceNext
    public static final List<VayraRPGRoomData> ROOMS = new ArrayList<>(Arrays.asList(
            new VayraRPGRoomData("corridor", null, null, 0f, 0.3f, null, null, null, null),
            new VayraRPGRoomData("hallway", null, null, 0f, 0.3f, null, null, null, null),
            new VayraRPGRoomData("passage", null, null, 0f, 0.3f, null, null, null, null),
            new VayraRPGRoomData("chamber", null, null, null, null, null, null, null, null),
            new VayraRPGRoomData("cavern", null, null, null, null, null, null, null, null),
            new VayraRPGRoomData("room", null, null, null, null, null, null, null, null),
            new VayraRPGRoomData("grotto", null, null, null, null, null, null, null, null),
            new VayraRPGRoomData("dripping grotto", null, null, null, null, "The sound of water dripping from limestone stalactites echoes around the cavern.", null, null, null),
            new VayraRPGRoomData("prison cell", null, 0.66f, 0f, 0f,
                    "Black iron manacles set into the wall leave no possible doubt as to the purpose of this chamber.", null, -1, null),
            new VayraRPGRoomData("goblin kitchen", 10, 0.7f, 0.5f, 0f, "Pots and cauldrons bubble merrily away, their bitter fragrances enough to cause an instant headache.", "goblin mom", null, "storage chamber"),
            new VayraRPGRoomData("goblin shop", 10, 1f, 0.5f, 0f, "Goblin wares are spread across the ground on filthy blankets.", "friendly goblin", -1, "storage chamber"),
            new VayraRPGRoomData("storage chamber", 20, null, 0.75f, 0.3f, "This high-ceilinged cavern is filled with wooden crates and barrels.", null, null, null),
            new VayraRPGRoomData("geothermal terrace", 20, 0.1f, 0f, 0.1f, "This huge, warm cavern is filled with the sound of bubbling water and the smell of sulfur. The space is dominated by a faintly steaming lake, fed by water trickling over terraces of glistening pink and white.", null, null, null),
            new VayraRPGRoomData("old diner", 25, 1f, 0f, 0f, "It smells like greasy fries, burnt beans, and the start of a buddy cop movie.", "old detective", null, "wooden cellar"),
            new VayraRPGRoomData("mineshaft", 25, 0.25f, 0.75f, 0.5f, "Forgotten cart tracks line the floor, almost entirely rusted through.", null, null, null),
            new VayraRPGRoomData("goblin pit", 20, null, null, 1f, "The ground slopes steeply down into a reeking pit, full nearly to the brim with goblin-produced detritus.", "goblin", null, "goblin bathhouse"),
            new VayraRPGRoomData("goblin bathhouse", 999, 1f, 0f, 0f, "Shallow pools of water steam quietly in this warm, muggy chamber.", "goblin", null, "goblin storeroom"),
            new VayraRPGRoomData("goblin storeroom", 999, null, 1f, 1f, "Goblin wares line rows of rickety shelves, nearly all of them worthless.", "goblin", null, "goblin yard"),
            new VayraRPGRoomData("goblin yard", 999, 1f, 1f, 0f, "This wide-open cave is filled with signs of inhabitation, tracks worn smooth in the stone floor.", "goblin", null, "goblin hall"),
            new VayraRPGRoomData("goblin hall", 999, 1f, 1f, 0f, "The cavern opens up into a great hall. A half-built wooden throne sits atop a crude dais at the distant end.", "gobin king", 1, null),
            new VayraRPGRoomData("wooden cellar", 30, null, 0.5f, 0.75f, "A dull gloom permeated by the smell of moist wooden rot.", null, null, null),
            new VayraRPGRoomData("subterranean forest", 30, null, null, null, "Somehow, there's a forest down here. At least the air is fresh.", null, null, null),
            new VayraRPGRoomData("thorned cavern", 30, 0.7f, 0.5f, 0.5f, "The thin layer of cave lichen over the walls gives way here to wrist-thick vines, each studded with thorns as long as your forearm.", "elf", null, "bramble passage"),
            new VayraRPGRoomData("bramble passage", 999, 0.5f, 0f, 1f, "Thorn-bearing vines direct you as surely as would any wall.", "elven warrior", null, "gilded halls"),
            new VayraRPGRoomData("gilded halls", 999, 1f, 1f, null, "The space opens up into a courtyard of sorts, the cavern roof hidden by thickly interwoven branches. The leafless trees glitter sullenly in your lantern light.", "elven warrior", null, "winter court"),
            new VayraRPGRoomData("winter court", 999, 1f, 1f, null, "A long silk carpet stretches from where you stand towards a throne woven from rigid vines, as much a part of the chamber as it is placed in it.", "elf queen", null, null),
            new VayraRPGRoomData("tomb entrance", 40, 0f, 0f, 1f, "Reflective crystals cast your lantern light as strange hues upon walls of rough-hewn sandstone.", null, null, "tomb crypt"),
            new VayraRPGRoomData("modder corner", 50, 1f, 0f, 1f, "In the distance, someone is yelling about 'weapon balance'.", "techpriest", 1, null),
            new VayraRPGRoomData("dwarven gate", 35, 0.9f, null, null, "The cave wall ahead is split by a massive gate worked from black iron.", "dwarf", null, "dwarven homes"),
            new VayraRPGRoomData("dwarven city", 999, 0.5f, 0.5f, null, "Access tunnels riddle the walls of this well-traveled cavern.", "dwarf", null, "dwarven barracks"),
            new VayraRPGRoomData("dwarven barracks", 999, 0.7f, 0.7f, null, "Small cots line the edges of the chamber, seemingly no softer than the stone they rest upon.", "dwarven warrior", null, "dwarven forge"),
            new VayraRPGRoomData("dwarven forge", 999, 1f, 1f, null, "Two rivers, one of lava and the other cold, glassy water, run through the forge in carefully carved channels. The anvil stands proudly in the center of the room, better-kept than any you have seen in the realms of Man.", "dwarven warrior", null, "hall of the mountain king"),
            new VayraRPGRoomData("hall of the mountain king", 999, 1f, 1f, null, "The hall is richly appointed, lustrous tapestries and fine furs hanging from artfully engraved pillars. A massive throne stands at the far end.", "dwarven warlord", null, null),
            new VayraRPGRoomData("volcanic chamber", 30, null, null, null, "The cavern floor of blasted blackstone is cut thtrough with winding channels of lava.", null, null, "underground forge"),
            new VayraRPGRoomData("kebab shop", 45, 0.5f, 1f, 0f, "Smells of frying meat and warm bread fill the air.", "friendly goblin", -1, "wooden cellar"),
            new VayraRPGRoomData("crystal cave", 55, 0.1f, 1f, 0.5f, "Glistening crystals spear from beneath the ground and through the ceiling, positively dazzling in the light of your lantern. Flickering shadows are twisted by the refractive surfaces, giving the appearance of movement out of the corner of your eye.", "crystalloid vanguard", 1, "crystal corridor"),
            new VayraRPGRoomData("mossy temple entrance", 60, 1f, 0f, 0.25f, "Flickering lamp light plays across masked stone effigies and ancient dilapidated ceilings.", null, null, "mossy temple pit"),
            new VayraRPGRoomData("abyssal catwalk", 50, null, 0f, null, "Your boots clang unexpectedly on metal gratework as the ground drops away into a dizzying abyss below. The walkway creaks distressingly with every step.", null, null, null),
            new VayraRPGRoomData("catwalk over water", 50, null, 0f, null, "You feet go from ground to wooden plank and metal grate. Below the ground yields to glassy water, inky black and inpenetrable, the walkway creaking with every step.", null, null, null),
            new VayraRPGRoomData("fleshweaver's cavern", 65, 1f, 0.2f, 0.2f, "The room is dark, with the walls burnt black in caustic sigils. The sick smell of blood couples with a slickening slurping sound that seems to come from every direction at once. A lidless eye emerges from a pile of twisted flesh to gurgle pitifully at you.", "flesh golem", 1, "colossus chamber"),
            new VayraRPGRoomData("sewer entrance", 15, 0.5f, 0f, null, "An unassuming rusted manhole in the center of a claustrophobic and dank cavern makes a corroded shrieking sound when opened, revealing a gloomy corridor of damp bricks coated in a congealing slime.", null, null, "sewer crossroads"),
            new VayraRPGRoomData("stony mountain path", 70, 0f, 0f, 0f, "A precarious cobbled path is walled on one side by a rough stone face, and on the other by a deep void.", null, null, "stony mountain plateau"),
            new VayraRPGRoomData("bone zone", 100, 0f, 1f, 0f, "Bones of different shapes and sizes litter the room, some broken and others pristine.", null, null, null),
            new VayraRPGRoomData("ancient crypt entrance", 100, 0f, 0f, 0.5f, "The stone here is even older than the rest, deeply carved engravings depicting scenes of ancient pastoral life.", null, null, "ancient armory"),
            new VayraRPGRoomData("ancient armory", 999, 0f, 1f, 0.5f, "Weapon racks line the walls, their contents rusted away to nothing. The engravings grow progressively more unsettling as you proceed.", null, null, "ancient false tomb"),
            new VayraRPGRoomData("ancient false tomb", 999, 0f, 1f, 1f, "Engravings on every available surface depict scenes of blood and terror, people being struck down by unholy forces. The furnishings appear rich at first, but upon closer inspection are merely gilded wood.", null, null, "ancient burial chamber"),
            new VayraRPGRoomData("ancient burial chamber", 999, 0f, 1f, 0f, "The chamber is small and spartan, little more than four thick walls surrounding a single ornate sarcophagus of heavy stone.", "ancient undead swordmaster", -1, null),
            new VayraRPGRoomData("inversion", 75, 0.2f, 0.5f, 0.5f, "The colour flickers, changes, and now everything is wrong. The floor is the wrong colour and on the ceiling, and the roof is perpendicular to what it should be. You're not sure if you see the shapes or hear them, or if the shapes are living or mere fascimilies - until you realise you're looking at your own hand in front of your face. Or are you?", null, null, null),
            new VayraRPGRoomData("simulation room", 200, 1f, 0f, 0f, "Gleaming racks of arcane devices beep and whirr happily along the walls.", "the architect", -1, null),
            new VayraRPGRoomData("tomb crypt", 999, 1f, 1f, 0f, "A pale alabaster slab is surrounded by sandstone columns etched with strange symbols.", "crypt totem guardian", 1, null),
            new VayraRPGRoomData("stony mountain plateau", 999, 0f, 0f, 0f, "A vast stony shelf covered in rocky outcroppings and a few stunted mushrooms. The wind howls through distant caves.", null, null, "cave entrance"),
            new VayraRPGRoomData("cave entrance", 999, 0f, 0f, 0f, "The mouth of a cave looms open, thick smoke occasionally issuing forth from its depths.", null, null, "volcanic cavern"),
            new VayraRPGRoomData("volcanic cavern", 999, 0f, 0f, 0f, "Sulfurous smoke wafts up from fiery glowing chasms snaking between columns of black stone.", null, null, "dragon's lair"),
            new VayraRPGRoomData("dragon's lair", 999, 0.9f, 1f, 0f, "An intense heat fills the cavern. Occasional bursts of fire illuminate the soot-covered walls.", "red dragon", null, null),
            new VayraRPGRoomData("meme bilge", 999, 1f, 0.25f, 0.5f, "An accursed place, thought lost to history. It is not safe here.", "edgelord", 1, null),
            new VayraRPGRoomData("mossy temple pit", 999, 0f, 0f, 1f, "The corridor halts at a cold and seemingly bottomless pit flanked by two ledges supported by crumbling stone bricks.", null, null, "mossy temple altar"),
            new VayraRPGRoomData("mossy temple altar", 999, 1f, 1f, 0.25f, "A bolt of daylight illuminates a dusty codex. A periodic mechanical whirring echoes around the stony chamber.", "mechanical golem", 1, null),
            new VayraRPGRoomData("underground forge", 999, 1f, 1f, 0f, "Tools line the walls, and the heat from the forge is unbearable.", "blacksmith", -1, null),
            new VayraRPGRoomData("colossus chamber", 999, 1f, 0.5f, 1f, "The floor cracks apart under your feet like brittle bone over the stringy flesh underneath. The walls split apart to reveal a bulbous gelatinous eye, a wide lipless mouth, and long tongues that drip acid.", "flesh colossus", 1, null),
            new VayraRPGRoomData("sewer crossroads", 999, 0f, 0f, 1f, "The shallow stream of filth lapping your shoes falls into a large room of treacherous acqueducts and rusted catwalks.", null, null, "sewer pumping station"),
            new VayraRPGRoomData("sewer pumping station", 999, 0f, 0.8f, 1f, "The tunnel opens up into a gloomy void. A whir of choking machinery filters downward from above.", null, null, "sewer delta"),
            new VayraRPGRoomData("sewer delta", 999, 0f, 0.25f, 1f, "A number of smaller side tunnels join with your passage until the murky water is waist-deep. The air begins to feel warm and stifling.", null, null, "sewer heart"),
            new VayraRPGRoomData("sewer heart", 999, 0f, 1f, 1f, "Your tunnel emerges into the side of a vast cavern with similar tunnels protruding around its lip, following a gradual circumference. A forest of corroded steel cables are anchored to the wall seemingly at random, their focal point at the center of the chamber obscured by their sinewy proliferation.", null, null, null),
            new VayraRPGRoomData("crystal corridor", 999, 0.75f, 0.5f, 0f, "A long hallway of crystal. The air is cold and stale.", "crystalloid vanguard", 1, "crystal garden"),
            new VayraRPGRoomData("crystal garden", 999, 0.75f, 0.5f, 0f, "A once verdant courtyard garden, which must have been beautiful before it was transformed to cold, hard crystal. Some might be inclined to say that this too, is beautiful in its own way.", "crystalloid vanguard", 1, "crystal spire"),
            new VayraRPGRoomData("crystal spire", 999, 1f, 0.75f, 0f, "The translucent crystal walls are thin enough here that you can clearly see the throne room beyond.", "crystalloid royal guard", 1, "heart of crystal"),
            new VayraRPGRoomData("heart of crystal", 999, 1f, 1f, 0f, "What must have once been a luxurious throne room has been transformed into a crystal creche. Budding spires and stalactites pulse with an unearthly glow.", "mother crystal", 1, null)
    ));

    // this is our little holder class for monsters
    public static class VayraRPGMonsterData {

        public String name;
        public int HD;
        public int AC;
        public int morale;
        public String weapon;
        public int dmgDie;
        public int aggression; // 12 is always hostile, 0 is always friendly
        public float weight;

        VayraRPGMonsterData(String name, Integer HD, Integer AC, Integer morale, String weapon, Integer dmgDie, Integer aggression, Float weight) {
            this.name = name;
            this.HD = HD;
            this.AC = AC;
            this.morale = morale;
            this.weapon = weapon;
            this.dmgDie = dmgDie;
            this.aggression = aggression;
            this.weight = weight;
        }

        VayraRPGMonsterData(VayraRPGMonsterData copy) {
            this.name = copy.name;
            this.HD = copy.HD;
            this.AC = copy.AC;
            this.morale = copy.morale;
            this.weapon = copy.weapon;
            this.dmgDie = copy.dmgDie;
            this.aggression = copy.aggression;
            this.weight = copy.weight;
        }

        public VayraRPGMonsterData duplicate() {
            return new VayraRPGMonsterData(this);
        }
    }

    // these are the monsters
    // name, HD, AC, morale, weapon, dmgDie, aggression, commonality
    public static final List<VayraRPGMonsterData> MONSTERS = new ArrayList<>(Arrays.asList(
            new VayraRPGMonsterData("goblin", 1, 8, 6, "sharp stick", 4, 7, 1f),
            new VayraRPGMonsterData("friendly goblin", 1, 8, 6, "sharp stick", 4, 0, 0.5f),
            new VayraRPGMonsterData("skeleton", 1, 10, 12, "bony fingers", 4, 12, 1f),
            new VayraRPGMonsterData("dire rooster", 1, 12, 5, "sharp comb", 4, 10, 1f),
            new VayraRPGMonsterData("filth licker", 1, 12, 7, "tongue", 4, 9, 1f),
            new VayraRPGMonsterData("disguised bandit", 1, 10, 6, "crude bludgeon", 4, 7, 1f),
            new VayraRPGMonsterData("giant rat", 1, 10, 5, "gnawing teeth", 4, 9, 1f),
            new VayraRPGMonsterData("ratman", 1, 12, 3, "ratty poke-stick", 2, 7, 1f),
            new VayraRPGMonsterData("techpriest", 1, 10, 1, "keyboard", 4, 7, 0f),
            new VayraRPGMonsterData("imp", 2, 12, 5, "poison sting", 6, 9, 1f),
            new VayraRPGMonsterData("bat man", 2, 12, 5, "poison dart", 6, 7, 1f),
            new VayraRPGMonsterData("wolf spider", 2, 12, 5, "chelicerae", 6, 9, 1f),
            new VayraRPGMonsterData("giant cave pigeon", 2, 8, 5, "beak", 8, 10, 1f),
            new VayraRPGMonsterData("zombie", 2, 8, 12, "rotting teeth", 4, 12, 1f),
            new VayraRPGMonsterData("blacksmith", 2, 12, 7, "hammer", 4, 0, 0.5f),
            new VayraRPGMonsterData("cultist", 2, 10, 9, "bloody fingernails", 2, 9, 1f),
            new VayraRPGMonsterData("cultist", 2, 10, 9, "poison dagger", 10, 9, 1f),
            new VayraRPGMonsterData("goblin mom", 2, 10, 7, "shoe", 2, 4, 0.5f),
            new VayraRPGMonsterData("masked anarchist", 2, 10, 10, "iron bomb", 8, 5, 0.5f),
            new VayraRPGMonsterData("old detective", 2, 10, 6, "worn revolver", 8, 0, 0f),
            new VayraRPGMonsterData("rabid wolf", 2, 12, 6, "infected bite", 6, 10, 1f),
            new VayraRPGMonsterData("apprentice wizard", 2, 10, 6, "magic missile", 4, 5, 1f),
            new VayraRPGMonsterData("skeleton archer", 2, 10, 12, "ancient bow", 6, 12, 1f),
            new VayraRPGMonsterData("skeleton warrior", 2, 12, 12, "rusty mace", 6, 12, 1f),
            new VayraRPGMonsterData("elf", 2, 12, 5, "fine-bladed rapier", 8, 7, 1f),
            new VayraRPGMonsterData("lesser demon", 2, 13, 10, "forked spear", 6, 9, 0.5f),
            new VayraRPGMonsterData("dwarf", 2, 15, 7, "hammer", 4, 7, 1f),
            new VayraRPGMonsterData("cave dweller", 2, 10, 6, "bone spear", 6, 9, 1f),
            new VayraRPGMonsterData("troglodyte", 2, 12, 6, "bone spear", 6, 9, 1f),
            new VayraRPGMonsterData("myconite", 2, 8, 6, "fungal spores", 6, 5, 1f),
            new VayraRPGMonsterData("elven warrior", 3, 14, 7, "glowing scimitar", 10, 7, 0.5f),
            new VayraRPGMonsterData("demonologist", 3, 10, 9, "eldritch blast", 8, 9, 0.5f),
            new VayraRPGMonsterData("ghoul", 2, 10, 12, "slavering jaws", 6, 12, 1f),
            new VayraRPGMonsterData("poltergeist", 3, 16, 7, "flying debris", 4, 7, 1f),
            new VayraRPGMonsterData("spiderwolf", 3, 14, 8, "horrible fangs", 8, 8, 1f),
            new VayraRPGMonsterData("cave gnoll", 3, 13, 6, "bladed spear", 8, 8, 1f),
            new VayraRPGMonsterData("mushroom dryad", 3, 13, 4, "questing mycelium", 8, 0, 1f),
            new VayraRPGMonsterData("rock leech", 3, 15, 9, "leechglass slashers", 6, 9, 1f),
            new VayraRPGMonsterData("skeletal mage", 3, 10, 12, "fire blast", 8, 12, 1f),
            new VayraRPGMonsterData("burning skeleton", 3, 10, 12, "flaming scimitar", 10, 12, 1f),
            new VayraRPGMonsterData("slug knight", 4, 16, 9, "snail flail", 8, 7, 1f),
            new VayraRPGMonsterData("elf queen", 4, 15, 9, "ice blast", 10, 7, 0f),
            new VayraRPGMonsterData("bog mummy", 4, 8, 12, "rotting claws", 6, 12, 1f),
            new VayraRPGMonsterData("crab man", 4, 17, 7, "pincers", 6, 7, 1f),
            new VayraRPGMonsterData("crystalloid vanguard", 4, 14, 8, "crystalline fists", 6, 7, 0.5f),
            new VayraRPGMonsterData("cave bear", 4, 12, 6, "bear hands", 8, 7, 1f),
            new VayraRPGMonsterData("bugbear", 4, 12, 6, "morningstar", 8, 7, 1f),
            new VayraRPGMonsterData("gingerbread man", 4, 8, 12, "gingerbread limb", 4, 7, 0.5f),
            new VayraRPGMonsterData("necromancer", 4, 10, 9, "cruel scythe", 8, 7, 1f),
            new VayraRPGMonsterData("necromancer", 4, 10, 9, "death spell", 12, 7, 1f),
            new VayraRPGMonsterData("edgelord", 4, 10, 1, "racism", 6, 12, 0f),
            new VayraRPGMonsterData("skeletal knight", 4, 16, 12, "cursed blade", 10, 12, 1f),
            new VayraRPGMonsterData("dwarven warrior", 4, 15, 8, "warhammer", 6, 7, 0.5f),
            new VayraRPGMonsterData("crypt totem guardian", 5, 14, 12, "crystal blaster", 6, 12, 0f),
            new VayraRPGMonsterData("flesh golem", 5, 8, 12, "malformed limbs", 6, 12, 1f),
            new VayraRPGMonsterData("lemure", 5, 8, 9, "fleshy appendage", 8, 9, 1f),
            new VayraRPGMonsterData("witch", 5, 10, 7, "curse", 6, 7, 1f),
            new VayraRPGMonsterData("gargoyle", 5, 17, 9, "stone talons", 10, 6, 1f),
            new VayraRPGMonsterData("minotaur", 5, 14, 10, "bearded axe", 12, 7, 1f),
            new VayraRPGMonsterData("ogre", 5, 10, 8, "giant club", 12, 8, 1f),
            new VayraRPGMonsterData("rock lamprey", 5, 17, 9, "leechglass crashers", 8, 9, 1f),
            new VayraRPGMonsterData("gryphon", 5, 12, 7, "talons", 8, 7, 1f),
            new VayraRPGMonsterData("slime cube", 5, 6, 12, "engulfing blow", 8, 7, 1f),
            new VayraRPGMonsterData("goblin king", 5, 14, 7, "rusted cutlass", 10, 7, 0f),
            new VayraRPGMonsterData("dwarven warlord", 6, 15, 10, "blessed warhammer", 10, 7, 0f),
            new VayraRPGMonsterData("basilisk", 6, 14, 8, "venomous fangs", 10, 7, 1f),
            new VayraRPGMonsterData("crystalloid royal guard", 6, 16, 10, "crystalline spear", 10, 12, 0f),
            new VayraRPGMonsterData("clay golem", 6, 10, 12, "clay fists", 8, 7, 1f),
            new VayraRPGMonsterData("illusionist", 6, 17, 6, "maddening visions", 4, 5, 1f),
            new VayraRPGMonsterData("dark inquisitor", 6, 15, 10, "unholy flail", 10, 10, 1f),
            new VayraRPGMonsterData("skeletal ogre", 6, 8, 12, "giant club", 12, 12, 1f),
            new VayraRPGMonsterData("veiled duelist", 6, 17, 9, "enchanted sabre", 12, 7, 1f),
            new VayraRPGMonsterData("storm knight", 6, 17, 9, "thunder sword", 12, 7, 1f),
            new VayraRPGMonsterData("hag", 7, 14, 10, "evil eye", 10, 10, 1f),
            new VayraRPGMonsterData("troll", 7, 12, 9, "immense claws", 12, 9, 1f),
            new VayraRPGMonsterData("horned devil", 7, 15, 10, "spiked chain", 10, 10, 1f),
            new VayraRPGMonsterData("barbed devil", 7, 15, 10, "impaling thorns", 10, 10, 1f),
            new VayraRPGMonsterData("bearded devil", 7, 15, 10, "cruel glaive", 12, 10, 1f),
            new VayraRPGMonsterData("mother crystal", 8, 17, 12, "crystal barrage", 16, 12, 0f),
            new VayraRPGMonsterData("stone golem", 8, 14, 12, "stone fist", 12, 7, 1f),
            new VayraRPGMonsterData("mushroom treant", 8, 12, 12, "fungal fists", 12, 7, 1f),
            new VayraRPGMonsterData("massive enemy crab", 8, 17, 7, "pincers", 12, 12, 1f),
            new VayraRPGMonsterData("mechanical golem", 9, 16, 12, "pummeling pistons", 14, 7, 1f),
            new VayraRPGMonsterData("stone giant", 9, 14, 10, "giant maul", 16, 7, 1f),
            new VayraRPGMonsterData("flesh colossus", 10, 6, 12, "corpse smasher", 18, 12, 1f),
            new VayraRPGMonsterData("winged devil", 10, 15, 10, "barbed talons", 14, 10, 1f),
            new VayraRPGMonsterData("pit fiend", 11, 15, 10, "flaming cleaver", 18, 10, 1f),
            new VayraRPGMonsterData("hellknight", 11, 17, 10, "unholy greatsword", 16, 10, 1f),
            new VayraRPGMonsterData("red dragon", 12, 16, 10, "fire breath", 16, 9, 1f),
            new VayraRPGMonsterData("balor", 12, 16, 10, "flaming whip", 16, 10, 1f),
            new VayraRPGMonsterData("demon prince", 14, 17, 10, "godslaying axe", 18, 10, 1f),
            new VayraRPGMonsterData("lich", 16, 10, 12, "finger of death", 20, 12, 1f),
            new VayraRPGMonsterData("lord of hell", 17, 17, 10, "scepter of divinity", 20, 10, 0.5f),
            new VayraRPGMonsterData("ancient undead swordmaster", 18, 17, 12, "ninefold knife", 90, 0, 0f),
            new VayraRPGMonsterData("the architect", 20, 17, 5, "finger", 30, 0, 0f),
            new VayraRPGMonsterData("dracolich", 20, 17, 12, "spectral fire", 30, 12, 1f)
    ));

    // this is our little holder class for traps
    public static class VayraRPGTrapData {

        public String name;
        public int depth;
        public String save;
        public int DC;
        public int dmgDie;

        VayraRPGTrapData(String name, Integer depth, String save, Integer DC, Integer dmgDie) {
            this.name = name;
            this.depth = depth == null ? 0 : depth;
            this.save = save == null ? "dex" : save;
            this.DC = DC == null ? 10 : DC;
            this.dmgDie = dmgDie == null ? 6 : dmgDie;
        }
    }

    // these are the traps
    // name, minimum depth, save, DC, dmgDie
    public static final List<VayraRPGTrapData> TRAPS = new ArrayList<>(Arrays.asList(
            new VayraRPGTrapData("rock trap", 0, "dex", 8, 4),
            new VayraRPGTrapData("needle trap", 0, "dex", 10, 2),
            new VayraRPGTrapData("miasma", 10, "str", 10, 4),
            new VayraRPGTrapData("pit trap", 10, "dex", 8, 6),
            new VayraRPGTrapData("spike trap", 20, "dex", 10, 6),
            new VayraRPGTrapData("illusory floor", 20, "int", 10, 6),
            new VayraRPGTrapData("gas cloud", 30, "str", 10, 8),
            new VayraRPGTrapData("spear trap", 40, "dex", 12, 8),
            new VayraRPGTrapData("fungal spores", 50, "str", 10, 10),
            new VayraRPGTrapData("collapsing wall", 50, "dex", 10, 10),
            new VayraRPGTrapData("bladed pendulum", 50, "dex", 10, 10),
            new VayraRPGTrapData("poison needle trap", 50, "dex", 10, 8),
            new VayraRPGTrapData("doorway painted on a wall", 50, "int", 10, 4),
            new VayraRPGTrapData("gacha mechanic", 100, "int", 20, 2)
    ));

    public static final List<String> CLASS_ADJECTIVES = new ArrayList<>(Arrays.asList(
            "Abstinent",
            "Aloof",
            "Amateur",
            "Angelic",
            "Annoying",
            "Average",
            "Bedraggled",
            "Beefy",
            "Blue",
            "Bothersome",
            "Bourgeois",
            "Broken",
            "Brutish",
            "Cancer",
            "Celibate",
            "Chaste",
            "Complaining",
            "Condenscending",
            "Conspiratorial",
            "Cowardly",
            "Crispy",
            "Cultured",
            "Cut-rate",
            "Daft",
            "Dastardly",
            "Dead",
            "Decaying",
            "Decrepit",
            "Dirty",
            "Diseased",
            "Disheveled",
            "Drunk",
            "English",
            "Excellent",
            "Exploding",
            "Fake",
            "Fast",
            "Faux",
            "Faux-intellectual",
            "Freelance",
            "Functional",
            "Green",
            "Honking",
            "Horny",
            "Idol-Obsessed",
            "Illegitimate",
            "Invasive",
            "Itchy",
            "Left-handed",
            "Legitimate",
            "Lewd",
            "Melancholy",
            "Mousey",
            "Multiclass",
            "Mutant",
            "Nocturnal",
            "Normal",
            "Obnoxious",
            "Off-brand",
            "Orange",
            "Pained",
            "Poor",
            "Primitive",
            "Proletarian",
            "Puritan",
            "Purple",
            "Querulous",
            "Raging",
            "Real",
            "Red",
            "Roasted",
            "Royal",
            "Sad",
            "Salacious",
            "Scandalous",
            "Secret",
            "Seventh",
            "Shifty",
            "Shitty",
            "Sick",
            "Slovenly",
            "Smelly",
            "Soggy",
            "Space",
            "Squamous",
            "Stinky",
            "Subterranean",
            "Tightly Wound",
            "Tired",
            "Titillating",
            "Tragic-Backstoried",
            "Tsundere",
            "Ugly",
            "Undead",
            "Unethical",
            "Violent",
            "Weird",
            "Wrong",
            "Zealous"
    ));

    public static final List<String> CLASS_NOUNS = new ArrayList<>(Arrays.asList(
            "Adept",
            "Adventurer",
            "Alchemist",
            "Apothecary",
            "Archeaologist",
            "Archer",
            "Armorer",
            "Arsonist",
            "Artist",
            "Asphyxiator",
            "Assassin",
            "Asshole",
            "Astrologer",
            "Baker",
            "Bandit",
            "Barbarian",
            "Bard",
            "Benchwarmer",
            "Berserker",
            "Bibliographer",
            "Blackmailer",
            "Blacksmith",
            "Body",
            "Boxer",
            "Boy Racer",
            "Butcher",
            "Cabbie",
            "Captain",
            "Catgirl",
            "Chef",
            "Chirurgeon",
            "Cleric",
            "Clown",
            "Comedian",
            "Communist",
            "Con Artist",
            "Confectioner",
            "Constable",
            "Cop",
            "Corpse",
            "Courier",
            "Courtesan",
            "Cowpoke",
            "Criminal",
            "Critic",
            "Cultist",
            "Cutthroat",
            "Cyberpunk",
            "Deciever",
            "Deliquent",
            "Detective",
            "Dilettante",
            "Discord Moderator",
            "Doctor",
            "Dragonkin",
            "Driver",
            "Drone",
            "Druid",
            "Drunk",
            "Duelist",
            "Economist",
            "Edgelord",
            "Elvis Impersonator",
            "Engineer",
            "Escort",
            "Expert",
            "Explorer",
            "False Knight",
            "Farmer",
            "Fetishist",
            "Fiend",
            "Fighter",
            "Fish",
            "Fishmonger",
            "Fletcher",
            "Forklift Driver",
            "Furry",
            "Gamer",
            "Goon",
            "Graverobber",
            "Halberdier",
            "Helmsman",
            "Hermit",
            "Hero",
            "Historian",
            "Human Body",
            "Human",
            "Hunter",
            "Idol",
            "Imam",
            "Influencer",
            "Insurgent",
            "Jester",
            "Journalist",
            "Knight",
            "Laborer",
            "Labourer",
            "Librarian",
            "Lieutenant",
            "Lobbyist",
            "Looter",
            "Mage",
            "Magic-User",
            "Magician",
            "Mariner",
            "Martial Artist",
            "Mathematician",
            "Memelord",
            "Mendicant",
            "Mercenary",
            "Min-Maxer",
            "Minister",
            "Miscreant",
            "Modder",
            "Model",
            "Monarchist",
            "Monk",
            "Mook",
            "Navigatrix",
            "Necromancer",
            "Noble",
            "Novice",
            "Page",
            "Paladin",
            "Pather",
            "Peasant",
            "Peltast",
            "Person",
            "Philosopher",
            "Photographer",
            "Pickpocket",
            "Pilot",
            "Pirate",
            "Pissant",
            "Poet",
            "Poledancer",
            "Pope",
            "Potato",
            "Preacher",
            "Priest",
            "Propagandist",
            "Psion",
            "Psychic",
            "Pugilist",
            "Pundit",
            "Puppeteer",
            "Ragamuffin",
            "Ranger",
            "Rapscallion",
            "Revisionist",
            "Revolutionary",
            "Robber",
            "Robot",
            "Rogue",
            "Saboteur",
            "Sailor",
            "Sapper",
            "Scientist",
            "Scion",
            "Self-Insert",
            "Serial Killer",
            "Shaman",
            "Shark",
            "Shill",
            "Shitposter",
            "Skald",
            "Skirmisher",
            "Smuggler",
            "Sneak",
            "Soldier",
            "Specialist",
            "Speculator",
            "Spy",
            "Squire",
            "Starfarer",
            "Stiff",
            "Stunt Double",
            "Submariner",
            "Swordmaster",
            "Syndicalist",
            "Teamster",
            "Thief",
            "Thug",
            "Troubadour",
            "Tyrant",
            "Union Organizer",
            "Vampire",
            "Veteran",
            "Vigilante",
            "Wainwright",
            "Wanderer",
            "Warlock",
            "Warlord",
            "Warrior",
            "Watcher",
            "Watchman",
            "Weeaboo",
            "Whaler",
            "Witch",
            "Wizard",
            "Zombie"
    ));

    public static final List<String> GAMES = new ArrayList<>(Arrays.asList(
            "Aberrations",
            "Action",
            "Activities",
            "Adventurers",
            "Aether",
            "Aftermath",
            "Agents",
            "Airships",
            "Aliens",
            "Allies",
            "Angels",
            "Apocalypse",
            "Arcana",
            "Archers",
            "Arduin",
            "Armageddon",
            "Armies",
            "Armories",
            "Ashes",
            "Atlanteans",
            "Attacks",
            "Avengers",
            "Badlands",
            "Bald",
            "Bandits",
            "Barbarians",
            "Barons",
            "Barrows",
            "Bastards",
            "Bastions",
            "Battlements",
            "Battles",
            "Bears",
            "Beasts",
            "Beer",
            "Beyond",
            "Bifrost",
            "Blackguards",
            "Blades",
            "Blood",
            "Blues",
            "Bold",
            "Bombardiers",
            "Boom",
            "Boots",
            "Boudoirs",
            "Brigands",
            "Buccaneers",
            "Bunkers",
            "Bushido",
            "Cadillacs",
            "Canteens",
            "Capes",
            "Carnage",
            "Carnivals",
            "Castles",
            "Catgirls",
            "Cavaliers",
            "Challengers",
            "Champions",
            "Changelings",
            "Chemists",
            "Chivalry",
            "Chronicles",
            "Clockwork",
            "Clouds",
            "Cobolds",
            "Commandos",
            "Commissars",
            "Conquerors",
            "Conquests",
            "Conspiracies",
            "Crusades",
            "Crystals",
            "Cults",
            "Danger",
            "Daredevils",
            "Darkness",
            "Deadlands",
            "Death",
            "Delvers",
            "Delves",
            "Demons",
            "Depths",
            "Despair",
            "Devils",
            "Dice",
            "Dictators",
            "Dinosaurs",
            "Disorder",
            "Dragons",
            "Dread",
            "Dreams",
            "Droids",
            "Duchesses",
            "Dukes",
            "Dunes",
            "Dungeons",
            "Dwarves",
            "Eclipses",
            "Ecoterrorists",
            "Elementals",
            "Elves",
            "Empires",
            "Enemies",
            "Energies",
            "Esoterica",
            "Essence",
            "Ethereals",
            "Evils",
            "Exalted",
            "Excaliburs",
            "Excursions",
            "Expanses",
            "Expeditions",
            "Fables",
            "Factories",
            "Faeries",
            "Fantasy",
            "Fatalities",
            "Fates",
            "Fears",
            "Feelings",
            "Fiascos",
            "Fiends",
            "Fighting",
            "Fireflies",
            "Fires",
            "Frankensteins",
            "Futures",
            "Gangsters",
            "Gaols",
            "Gargoyles",
            "Gates",
            "Geists",
            "Ghosts",
            "Ghouls",
            "Glory",
            "Goblins",
            "Gods",
            "Gold",
            "Grimness",
            "Grues",
            "Gulches",
            "Gullies",
            "Guts",
            "Hawks",
            "Hearts",
            "Heroes",
            "Hills",
            "Hollows",
            "Holograms",
            "Horrors",
            "Hunters",
            "Immortals",
            "Infernos",
            "Innocents",
            "Intrigue",
            "Iron",
            "Killers",
            "Kings",
            "Knaves",
            "Knights",
            "Knives",
            "Labyrinths",
            "Ladies",
            "Lancers",
            "Lands",
            "Lasers",
            "Legends",
            "Legions",
            "Lords",
            "Machines",
            "Maelstroms",
            "Mages",
            "Magic",
            "Maleficars",
            "Mariners",
            "Mazes",
            "Mercenaries",
            "Metal",
            "Midgard",
            "Millennia",
            "Minotaurs",
            "Monarchies",
            "Money",
            "Monsters",
            "Moons",
            "Mountains",
            "Mummies",
            "Mutants",
            "Myths",
            "Paladins",
            "Paragons",
            "Pirates",
            "Polearms",
            "Problems",
            "Rocs",
            "Roses",
            "Ruins",
            "Rust",
            "Scavengers",
            "Scrap",
            "Soldiers",
            "Stabbing",
            "Stilettos",
            "Sundering",
            "Templars",
            "Terrors",
            "Thunder",
            "Towers",
            "Traps",
            "Treants",
            "Turrets"
    ));

    // these are the things it is possible to drink out of
    public static final List<String> CANTEENS = new ArrayList<>(Arrays.asList(
            "paint can",
            "mason jar",
            "hip flask",
            "ankle flask",
            "soup can",
            "canteen",
            "drum canteen",
            "waterskin",
            "water bottle",
            "whiskey bottle",
            "gin and tonic",
            "can of branded energy drink",
            "vodka bottle",
            "thermos",
            "sippy cup",
            "commemorative mug",
            "teacup",
            "donations tin",
            "jerrycan",
            "plant pot",
            "dog bowl",
            "wine bottle",
            "old fashioned",
            "martini",
            "milk jug"
    ));

    // these are the things it is possible to drink as a potion
    public static final List<String> POTIONS = new ArrayList<>(Arrays.asList(
            "oatmeal",
            "chamomile tea",
            "healing",
            "pea soup",
            "clam chowder",
            "eggnog",
            "hot chocolate",
            "ice cold beer",
            "warm milk",
            "nonfat soy latte",
            "foot cream",
            "cough syrup",
            "raw egg",
            "ice water with lemon",
            "chocolate milk",
            "nutrients",
            "sports beverage",
            "ketchup",
            "motor oil",
            "mysterious liquid",
            "rotgut moonshine",
            "antifreeze",
            "tomatopaste",
            "fresh blood",
            "bleach",
            "borscht",
            "vinegar",
            "mashed potatoes",
            "milk"
    ));

    // these are the things it is possible to find loot in
    public static final List<String> LOOT_LOCATIONS = new ArrayList<>(Arrays.asList(
            "behind a pillar",
            "in a treasure chest",
            "beneath a false floor tile",
            "hidden in a corner",
            "buried under a pile of detritus",
            "buried in rubble",
            "on a dusty shelf",
            "wrapped in cobwebs",
            "in a barrel",
            "in an urn",
            "leaning against a broken pillar",
            "beneath a fragment of wall",
            "hanging on a wall",
            "placed carefully on an altar",
            "in a corner",
            "in a poorly hidden stash",
            "on the corpse of a fallen adventurer",
            "lying on the ground",
            "placed as bait for an extremely obvious trap",
            "inside a hollow stump",
            "on a pedestal"
    ));

    // these are the words that describe something waiting in a room for you to kill it
    public static final List<String> LURKS = new ArrayList<>(Arrays.asList(
            "lurks",
            "is encamped",
            "waits",
            "sits",
            "stands guard",
            "lairs",
            "monologues",
            "soliloquies",
            "broods",
            "hides",
            "looms",
            "gambols",
            "leers",
            "snoozes",
            "contemplates"
    ));

    // these are the things it is possible to suffer as a result of quitting the game without returning to town
    public enum Consequence {
        DEATH,
        PERMANENT_INJURY,
        LOSE_WEAPON,
        CURSE_WEAPON,
        LOSE_ARMOR,
        CURSE_ARMOR,
        LOSE_SHIELD,
        LOSE_ITEMS,
    }
}
