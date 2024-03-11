package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.colonies.VayraColonialManager;
import data.scripts.campaign.intel.VayraPersonBountyManager;
import data.scripts.campaign.bases.VayraRaiderBaseReaper;
import data.scripts.campaign.bases.VayraRaiderBaseManager;
import data.scripts.campaign.fleets.VayraTreasureFleetManager;
import data.scripts.campaign.intel.VayraUniqueBountyManager;
import data.scripts.campaign.ColonyHullmodFixer;
import data.scripts.campaign.VayraCampaignPlugin;
import data.scripts.campaign.VayraAbandonedStationAndLeagueSubfactionBonker;
import data.scripts.campaign.VayraLoreObjectsFramework;
import data.scripts.campaign.bases.VayraProcgenEntityFramework;
import data.scripts.campaign.events.VayraDistressCallManager;
import data.scripts.campaign.fleets.VayraPopularFrontManager;
import data.scripts.campaign.intel.VayraPlayerBountyIntel;
import data.scripts.campaign.intel.bar.events.VayraDungeonMasterBarEventCreator;
import static data.scripts.hullmods.VayraGhostShip.GHOST_GALLEON_BOUNTY_ID;
import data.scripts.world.VayraAddPlanets;
import exerelin.campaign.DiplomacyManager;
import exerelin.campaign.fleets.InvasionFleetManager;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import java.io.IOException;
import static java.lang.Math.random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraMergedModPlugin extends BaseModPlugin {

    public static Logger logger = Global.getLogger(VayraMergedModPlugin.class);

    public static final String MOD_ID = "vayrasector";

    private static final String SETTINGS_FILE = "VAYRA_SETTINGS.ini";
    public static boolean VAYRA_DEBUG;
    public static boolean RAIDER_BASE_REAPER_ENABLED;
    public static boolean COLONIAL_FACTIONS_ENABLED;
    public static int COLONIAL_FACTION_TIMEOUT;
    public static int COLONIAL_FACTION_COLONY_MULT;
    public static boolean POPULAR_FRONT_ENABLED;
    public static int POPULAR_FRONT_TIMEOUT;
    public static int AI_REBELLION_THRESHOLD;
    public static boolean UNIQUE_BOUNTIES;
    public static int UNIQUE_BOUNTIES_MAX;
    public static float EXTRA_BOUNTY_LEVEL_MULT;
    public static float BOUNTY_DURATION;
    public static float RARE_BOUNTY_FLAGSHIP_CHANCE;
    public static float CRUMB_CHANCE;
    public static float BOUNTY_SOFT_MAX_DIST;
    public static boolean PROCGEN_ENTITIES;
    public static boolean PLAYER_BOUNTIES;
    public static float PLAYER_BOUNTY_FP_SCALING;
    public static float PLAYER_BOUNTY_SPAWN_RANGE;
    public static float PLAYER_BOUNTY_MAX_RANGE;
    public static float PLAYER_BOUNTY_SYSTEM_DAYS;
    public static float PLAYER_BOUNTY_RANGE_DAYS;
    public static boolean LEAGUE_SUBFACTIONS;
    public static boolean PLAY_TTRPG;
    public static boolean ADD_BARREN_PLANETS;

    public static boolean IS_AVANITIA;
    public static boolean IS_CJUICY;

    public static enum PirateMode {
        ALWAYS,
        NEVER,
        SOMETIMES,
        BOTH
    }
    public static PirateMode PIRATE_BOUNTY_MODE = PirateMode.ALWAYS;

    public static boolean EXERELIN_LOADED;
    public static Set<String> EXERELIN_ACTIVE = new HashSet<>();

    @Override
    public void onApplicationLoad() throws Exception {

        try {
            Global.getSettings().getScriptClassLoader().loadClass("org.lazywizard.lazylib.ModUtils");
        } catch (ClassNotFoundException lazy) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "LazyLib is required to run Vayra's Sector."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download LazyLib at http://fractalsoftworks.com/forum/index.php?topic=5444"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }

        EXERELIN_LOADED = Global.getSettings().getModManager().isModEnabled("nexerelin");
        try {
            loadVayraSettings();
        } catch (IOException | JSONException e) {
            logger.log(Level.ERROR, "VAYRA_SETTINGS.ini loading failed! ;....; " + e.getMessage());
        }

        if (EXERELIN_LOADED) {
            for (String factionId : VayraColonialManager.loadColonyFactionList()) {
                setExerelinActive(factionId, COLONIAL_FACTIONS_ENABLED);
            }
        }
    }

    @Override
    public void onGameLoad(boolean newGame) {

        Global.getSector().registerPlugin(new VayraCampaignPlugin());

        BarEventManager barEventManager = BarEventManager.getInstance();
        if (!barEventManager.hasEventCreator(VayraDungeonMasterBarEventCreator.class)) {
            if (PLAY_TTRPG) {
                barEventManager.addEventCreator(new VayraDungeonMasterBarEventCreator());
            }
        }

        VayraUniqueBountyManager uniques = VayraUniqueBountyManager.getInstance();
        if (uniques != null) {
            log.info("reloading unique bounties");
            uniques.reload();
        } else {
            log.warn("VayraUniqueBountyManager.getInstance() returned null");
        }

        VayraPersonBountyManager bounties = VayraPersonBountyManager.getInstance();
        if (bounties != null) {
            log.info("reloading regular bounties");
            bounties.reload();
        } else {
            log.warn("VayraPersonBountyManager.getInstance() returned null");
        }

        VayraPlayerBountyIntel player = VayraPlayerBountyIntel.getInstance();
        if (player != null) {
            log.info("reloading player-targeted bounties");
            player.loadParticipatingFactionList();
        } else {
            log.warn("VayraPlayerBountyIntel.getInstance() returned null");
        }

        VayraDistressCallManager distress = VayraDistressCallManager.getInstance();
        if (distress != null) {
            log.info("reloading distress calls");
            distress.loadEvents();
        } else {
            log.warn("VayraDistressCallManager.getInstance() returned null");
        }
    }

    private static void loadVayraSettings() throws IOException, JSONException {

        JSONObject setting = Global.getSettings().loadJSON(SETTINGS_FILE);

        VAYRA_DEBUG = setting.getBoolean("vayraDebug");

        PIRATE_BOUNTY_MODE = PirateMode.valueOf(setting.getString("usePirateBountyManager"));
        EXTRA_BOUNTY_LEVEL_MULT = (float) setting.getDouble("extraBountyLevelMult");
        BOUNTY_DURATION = (float) setting.getDouble("bountyDuration");
        RARE_BOUNTY_FLAGSHIP_CHANCE = (float) setting.getDouble("rareBountyFlagshipChance");
        CRUMB_CHANCE = (float) setting.getDouble("bountyIntelCrumbChance");
        BOUNTY_SOFT_MAX_DIST = setting.getInt("bountySoftMaxDist");

        UNIQUE_BOUNTIES = setting.getBoolean("spawnUniqueBounties");
        UNIQUE_BOUNTIES_MAX = setting.getInt("maxActiveUniqueBounties");

        PLAYER_BOUNTIES = setting.getBoolean("bountiesOnPlayer");
        PLAYER_BOUNTY_FP_SCALING = (float) setting.getDouble("playerBountyBaseFPScaling");
        PLAYER_BOUNTY_SPAWN_RANGE = (float) setting.getDouble("playerBountySpawnRange");
        PLAYER_BOUNTY_MAX_RANGE = (float) setting.getDouble("playerBountySpawnRangeFromCore");
        PLAYER_BOUNTY_SYSTEM_DAYS = (float) setting.getDouble("playerBountyDaysInSystem");
        PLAYER_BOUNTY_RANGE_DAYS = (float) setting.getDouble("playerBountyDaysOutOfRange");

        RAIDER_BASE_REAPER_ENABLED = setting.getBoolean("stopSpawningRaiderBasesWhenFactionDelet");

        COLONIAL_FACTIONS_ENABLED = setting.getBoolean("spawnColonialCompetitors");
        COLONIAL_FACTION_TIMEOUT = setting.getInt("colonialCompetitorsStartCycle");
        COLONIAL_FACTION_COLONY_MULT = setting.getInt("colonialCompetitorsColonyCountMult");
        AI_REBELLION_THRESHOLD = setting.getInt("coreCriticalMass");

        POPULAR_FRONT_ENABLED = setting.getBoolean("spawnPopularFront");
        POPULAR_FRONT_TIMEOUT = setting.getInt("popularFrontStartCycle");

        PROCGEN_ENTITIES = setting.getBoolean("spawnEntities");

        LEAGUE_SUBFACTIONS = setting.getBoolean("leagueSubfactions");

        PLAY_TTRPG = setting.getBoolean("playTabletopRoleplayingGame");

        ADD_BARREN_PLANETS = setting.getBoolean("addBarrenPlanets");
    }

    @Override
    public void onNewGame() {
        try {
            IS_AVANITIA = Global.getSector().getPlayerPerson().getNameString().startsWith("Avan");
        } catch (NullPointerException n) {
            IS_AVANITIA = true; // if you don't exist, you're Avanitia now, them's the rules
        }
        try {
            IS_CJUICY = Global.getSector().getPlayerPerson().getNameString().contains("Argo");
        } catch (NullPointerException n) {
            IS_CJUICY = true; // if you don't exist, you're Cjuicy now, them's the rules
            // yes this means you can be Cjuicy and Avanitia at the same time
        }
    }

    @Override
    public void onNewGameAfterTimePass() {

        log.info("new game started, adding scripts");

        Global.getSector().addScript(new VayraSunPusher());

        if (ADD_BARREN_PLANETS) {
            new VayraAddPlanets().generate(Global.getSector());
        }

        if (PROCGEN_ENTITIES) {
            Global.getSector().addScript(new VayraProcgenEntityFramework());
            Global.getSector().addScript(new VayraLoreObjectsFramework());
        }

        // add these scripts regardless of setting, since they all just return immediately if not activated
        // and this way they will activate if you activate the setting midgame
        Global.getSector().addScript(new VayraRaiderBaseReaper());
        Global.getSector().addScript(new VayraColonialManager());
        Global.getSector().addScript(new VayraPopularFrontManager());
        Global.getSector().addScript(new VayraRaiderBaseManager());
        Global.getSector().addScript(new VayraPersonBountyManager());
        Global.getSector().addScript(new VayraUniqueBountyManager());
        Global.getSector().addScript(new VayraPlayerBountyIntel());
        Global.getSector().addScript(new VayraTreasureFleetManager());
        Global.getSector().addScript(new VayraDistressCallManager());
        Global.getSector().addScript(new ColonyHullmodFixer());
        Global.getSector().addScript(new VayraAbandonedStationAndLeagueSubfactionBonker());

        VayraPersonBountyManager.getInstance().advance(6.66f);

        float range = 3000f;
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();

        for (CampaignFleetAPI fleet : playerFleet.getContainingLocation().getFleets()) {
            if (!fleet.isHostileTo(playerFleet)) {
                continue;
            }

            float dist = Misc.getDistance(playerFleet.getLocation(), fleet.getLocation());
            if (dist > range) {
                continue;
            }

            float x = fleet.getLocation().x;
            float y = fleet.getLocation().y;

            log.info(String.format("fleet [%s] is at [%s, %s]... player fleet is at [%s, %s]", fleet.getNameWithFaction(), x, y, playerFleet.getLocation().x, playerFleet.getLocation().y));

            if (playerFleet.getLocation().x > x) {
                x -= 3000f;
            } else {
                x += 3000f;
            }

            if (playerFleet.getLocation().y > y) {
                y -= 3000f;
            } else {
                y += 3000f;
            }

            fleet.setLocation(x, y);
            log.info(String.format("Moving [%s] to [%s, %s] get da FUK out my gOD DAMN FAcE", fleet.getNameWithFaction(), x, y));
        }

        // hacky thing here to spend the Galleon P bounty
        if ("vayra_galleon_p".equals(playerFleet.getFlagship().getHullId())) {
            VayraUniqueBountyManager uniques = VayraUniqueBountyManager.getInstance();
            if (uniques != null && uniques.hasBounty(GHOST_GALLEON_BOUNTY_ID)) {
                uniques.spendBounty(GHOST_GALLEON_BOUNTY_ID);
            }
        }
    }

    public static void setExerelinActive(String factionId, boolean active) {

        // only do anything if exerelin is active, just in case
        if (EXERELIN_LOADED) {

		// PATCHED BY SHARK - the added parameter is the 'useDefault' in case the factionId isn't found
			NexFactionConfig conf = NexConfig.getFactionConfig(factionId, true);
            if (active) {

                // if we already did it just return to save time/not fuck up faction relationships etc
                if (EXERELIN_ACTIVE.contains(factionId)) {
                    return;
                } else {
                    EXERELIN_ACTIVE.add(factionId);
                }

                // just reload most settings
                conf.loadFactionConfig();

                // i don't think exerelin edits these automatically when you load settings?
                if (InvasionFleetManager.EXCEPTION_LIST.contains(conf.factionId)) {
                    InvasionFleetManager.EXCEPTION_LIST.remove(conf.factionId);
                }
                if (DiplomacyManager.disallowedFactions.contains(conf.factionId)) {
                    DiplomacyManager.disallowedFactions.remove(conf.factionId);
                }

            } else {

                // if we already did it just return to save time
                if (!EXERELIN_ACTIVE.contains(factionId)) {
                    return;
                } else {
                    EXERELIN_ACTIVE.remove(factionId);
                }

                // if inactive, turn everything off manually
                conf.disableDiplomacy = true;
                conf.invasionPointMult = 0f;
                conf.allowAgentActions = false;
                conf.allowPrisonerActions = false;
                conf.marketSpawnWeight = 0f;
                conf.playableFaction = false;
                conf.startingFaction = false;
                conf.noHomeworld = true;
                conf.showIntelEvenIfDead = false;

                if (!InvasionFleetManager.EXCEPTION_LIST.contains(conf.factionId)) {
                    InvasionFleetManager.EXCEPTION_LIST.add(conf.factionId);
                }
                if (!DiplomacyManager.disallowedFactions.contains(conf.factionId)) {
                    DiplomacyManager.disallowedFactions.add(conf.factionId);
                }

            }
        }
    }

    public static float randomRange(float min, float max) {
        return (float) (random() * (max - min) + min);
    }

    public static MarketAPI addMarketplace(String factionID, SectorEntityToken primaryEntity, ArrayList<SectorEntityToken> connectedEntities, String name,
            int size, ArrayList<String> marketConditions, ArrayList<String> submarkets, boolean WithJunkAndChatter, boolean pirateMode, boolean freePort) {

        EconomyAPI globalEconomy = Global.getSector().getEconomy();
        String entityId = primaryEntity.getId();
        String marketId = entityId + "market";

        MarketAPI newMarket = Global.getFactory().createMarket(marketId, name, size);
        newMarket.setFactionId(factionID);
        newMarket.setPrimaryEntity(primaryEntity);
        newMarket.getTariff().modifyFlat("generator", newMarket.getFaction().getTariffFraction());

        if (submarkets != null) {
            for (String market : submarkets) {
                newMarket.addSubmarket(market);
            }
        }

        newMarket.addCondition("population_" + size);
        if (marketConditions != null) {
            for (String condition : marketConditions) {
                try {
                    newMarket.addCondition(condition);
                } catch (RuntimeException e) {
                    newMarket.addIndustry(condition);
                }
            }
        }

        if (connectedEntities != null) {
            for (SectorEntityToken entity : connectedEntities) {
                newMarket.getConnectedEntities().add(entity);
            }
        }

        globalEconomy.addMarket(newMarket, WithJunkAndChatter);
        primaryEntity.setMarket(newMarket);
        primaryEntity.setFaction(factionID);

        createAdmin(newMarket);

        if (connectedEntities != null) {
            for (SectorEntityToken entity : connectedEntities) {
                entity.setMarket(newMarket);
                entity.setFaction(factionID);
            }
        }

        if (pirateMode) {
            newMarket.setEconGroup(newMarket.getId());
            newMarket.setHidden(true);
            primaryEntity.setSensorProfile(1f);
            primaryEntity.setDiscoverable(true);
            primaryEntity.getDetectedRangeMod().modifyFlat("gen", 5000f);
            newMarket.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);
        }

        newMarket.setFreePort(freePort);

        for (MarketConditionAPI mc : newMarket.getConditions()) {
            mc.setSurveyed(true);
        }
        newMarket.setSurveyLevel(MarketAPI.SurveyLevel.FULL);

        newMarket.reapplyIndustries();

        log.info("created " + factionID + " market " + name);

        return newMarket;
    }

    public static PersonAPI createAdmin(MarketAPI market) {
        FactionAPI faction = market.getFaction();
        PersonAPI admin = faction.createRandomPerson();
        int size = market.getSize();

        admin.setPostId(Ranks.POST_ADMINISTRATOR);

        switch (size) {
            case 3:
            case 4:
                admin.setRankId(Ranks.GROUND_CAPTAIN);
                break;
            case 5:
                admin.setRankId(Ranks.GROUND_MAJOR);
                break;
            case 6:
                admin.setRankId(Ranks.GROUND_COLONEL);
                break;
            case 7:
            case 8:
            case 9:
            case 10:
                admin.setRankId(Ranks.GROUND_GENERAL);
                break;
            default:
                admin.setRankId(Ranks.GROUND_LIEUTENANT);
                break;
        }

        List<String> skills = Global.getSettings().getSortedSkillIds();

        int industries = 0;
        int defenses = 0;
        boolean military = market.getMemoryWithoutUpdate().getBoolean(MemFlags.MARKET_MILITARY);

        for (Industry curr : market.getIndustries()) {
            if (curr.isIndustry()) {
                industries++;
            }
            if (curr.getSpec().hasTag(Industries.TAG_GROUNDDEFENSES)) {
                defenses++;
            }
        }

        admin.getStats().setSkipRefresh(true);

        int num = 0;
        if (industries >= 2 || (industries == 1 && defenses == 1)) {
            if (skills.contains(Skills.INDUSTRIAL_PLANNING)) {
                admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
            }
            num++;
        }

        if (num == 0 || size >= 7) {
            /*
            if (military) {
                if (skills.contains(Skills.FLEET_LOGISTICS)) {
                    admin.getStats().setSkillLevel(Skills.FLEET_LOGISTICS, 3);
                }
            } else if (defenses > 0) {
                if (skills.contains(Skills.PLANETARY_OPERATIONS)) {
                    admin.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 3);
                }
            } else {
            */
                // nothing else suitable, so just make sure there's at least one skill, if this wasn't already set
                if (skills.contains(Skills.INDUSTRIAL_PLANNING)) {
                    //admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 3);
                    admin.getStats().setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1);
                }
//            }
        }

        ImportantPeopleAPI ip = Global.getSector().getImportantPeople();
        admin.getStats().setSkipRefresh(false);
        admin.getStats().refreshCharacterStatsEffects();
        market.addPerson(admin);
        market.setAdmin(admin);
        market.getCommDirectory().addPerson(admin);
        ip.addPerson(admin);
        ip.getData(admin).getLocation().setMarket(market);
        ip.checkOutPerson(admin, "permanent_staff");

        log.info(String.format("Applying admin %s %s to market %s", market.getFaction().getRank(admin.getRankId()), admin.getNameString(), market.getName()));

        return admin;
    }

    public static String[] JSONArrayToStringArray(JSONArray jsonArray) {
        try {
            String[] ret = new String[jsonArray.length()];
            for (int i = 0; i < jsonArray.length(); i++) {
                ret[i] = jsonArray.getString(i);
            }
            return ret;
        } catch (JSONException e) {
            log.warn(e);
            return new String[]{};
        }
    }

    public static String aOrAn(String input) {

        ArrayList<String> vowels = new ArrayList<>(Arrays.asList(
                "a",
                "e",
                "i",
                "o",
                "u"));

        String firstLetter = input.substring(0, 1).toLowerCase();

        if (vowels.contains(firstLetter)) {
            return "an";
        } else {
            return "a";
        }
    }

    public static boolean isEntityInArc(Vector2f source, CombatEntityAPI entity, float range, float angle, float arc) {
        Vector2f point = MathUtils.getNearestPointOnLine(entity.getLocation(), source, MathUtils.getPointOnCircumference(source, range, angle));
        point = CollisionUtils.getNearestPointOnBounds(point, entity);
        return Misc.isInArc(angle, arc, source, point) && Misc.getDistance(point, source) <= range;
    }
	
	    public static List<ShipAPI> getFighters(ShipAPI carrier) {
        List<ShipAPI> result = new ArrayList<>();

        for (ShipAPI ship : Global.getCombatEngine().getShips()) {
            if (!ship.isFighter()) {
                continue;
            }
            if (ship.getWing() == null) {
                continue;
            }
            if (ship.getWing().getSourceShip() == carrier) {
                result.add(ship);
            }
        }

        return result;
    }
}
