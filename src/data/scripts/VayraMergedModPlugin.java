package data.scripts;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.ImportantPeopleAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.ai.VayraLRMAI;
import data.scripts.ai.VayraSplinterAI;
import data.scripts.campaign.*;
import data.scripts.campaign.bases.VayraProcgenEntityFramework;
import data.scripts.campaign.bases.VayraRaiderBaseManager;
import data.scripts.campaign.bases.VayraRaiderBaseReaper;
import data.scripts.campaign.colonies.VayraColonialManager;
import data.scripts.campaign.events.VayraDistressCallManager;
import data.scripts.campaign.fleets.VayraPopularFrontManager;
import data.scripts.campaign.fleets.VayraTreasureFleetManager;
import data.scripts.campaign.intel.VayraPersonBountyManager;
import data.scripts.campaign.intel.VayraPlayerBountyIntel;
import data.scripts.campaign.intel.VayraUniqueBountyManager;
import data.scripts.campaign.intel.bar.events.VayraDungeonMasterBarEventCreator;
import data.scripts.world.KadurGen;
import data.scripts.world.VayraAddPlanets;
import exerelin.campaign.DiplomacyManager;
import exerelin.campaign.SectorManager;
import exerelin.campaign.fleets.InvasionFleetManager;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static data.scripts.hullmods.VayraGhostShip.GHOST_GALLEON_BOUNTY_ID;
import static java.lang.Math.random;

public class VayraMergedModPlugin extends BaseModPlugin {

    public static Logger logger = Global.getLogger(VayraMergedModPlugin.class);

    public static final String MOD_ID = "vayramerged";

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

    // Kadur globals here
    public static final String KADUR_ID = "kadur_remnant";

    public static final String KADUR_SRM = "vayra_partisanmis";
    public static final String KADUR_LRM = "vayra_jerichomis";
    public static final String KADUR_SLOWLRM = "vayra_slowlrm_copy";
    public static final String KADUR_SPLINTER = "vayra_splintergun_shot_copy";
    public static final String KADUR_ANTIFTR = "vayra_antifighter_mis";

    public enum PirateMode {
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

        try {
            Global.getSettings().getScriptClassLoader().loadClass("data.scripts.util.MagicTargeting");
        } catch (ClassNotFoundException magic) {
            String message = System.lineSeparator()
                    + System.lineSeparator() + "MagicLib is required to run Kadur Remnant."
                    + System.lineSeparator() + System.lineSeparator()
                    + "You can download MagicLib at http://fractalsoftworks.com/forum/index.php?topic=13718.0"
                    + System.lineSeparator();
            throw new ClassNotFoundException(message);
        }

        EXERELIN_LOADED = Global.getSettings().getModManager().isModEnabled("nexerelin");
        loadVayraSettings();

        if (EXERELIN_LOADED) {
            for (String factionId : VayraColonialManager.loadColonyFactionList()) {
                setExerelinActive(factionId, COLONIAL_FACTIONS_ENABLED);
            }
        }
        //Mayasuran Navy
        if (!Global.getSettings().getModManager().isModEnabled("Mayasuran Navy")) {
            try {
                if (Global.getSettings().getFighterWingSpec("vayra_phalanx_m_wing") != null) {
                    Global.getSettings().getFighterWingSpec("vayra_phalanx_m_wing").getTags().remove("fighter4");
                    Global.getSettings().getFighterWingSpec("vayra_phalanx_m_wing").getTags().remove("fighter");
                    Global.getSettings().getFighterWingSpec("vayra_phalanx_m_wing").addTag(Tags.NO_DROP);
                    Global.getSettings().getFighterWingSpec("vayra_phalanx_m_wing").addTag(Items.TAG_NO_DEALER);
                    Global.getSettings().getFighterWingSpec("vayra_phalanx_m_wing").addTag(Tags.NO_SELL);
                }
            } catch (Exception ex) {
            }
            try {
                if (Global.getSettings().getHullSpec("vayra_phalanx_m") != null) {
                    Global.getSettings().getHullSpec("vayra_phalanx_m").getHints().add(ShipHullSpecAPI.ShipTypeHints.HIDE_IN_CODEX);
                }
            } catch (Exception ex) {
            }
            try {
                if (Global.getSettings().getFighterWingSpec("vayra_hoplite_m_wing") != null) {
                    Global.getSettings().getFighterWingSpec("vayra_hoplite_m_wing").getTags().remove("bomber3");
                    Global.getSettings().getFighterWingSpec("vayra_hoplite_m_wing").getTags().remove("bomber");
                    Global.getSettings().getFighterWingSpec("vayra_hoplite_m_wing").addTag(Tags.NO_DROP);
                    Global.getSettings().getFighterWingSpec("vayra_hoplite_m_wing").addTag(Items.TAG_NO_DEALER);
                    Global.getSettings().getFighterWingSpec("vayra_hoplite_m_wing").addTag(Tags.NO_SELL);
                }
            } catch (Exception ex) {
            }
            try {
                if (Global.getSettings().getHullSpec("vayra_hoplite_m") != null) {
                    Global.getSettings().getHullSpec("vayra_hoplite_m").getHints().add(ShipHullSpecAPI.ShipTypeHints.HIDE_IN_CODEX);
                }
            } catch (Exception ex) {
            }
        }

        try {
            if (Global.getSettings().getVariant("vayra_caliph_revenant") != null) {
                Global.getSettings().getVariant("vayra_caliph_revenant").addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                Global.getSettings().getVariant("vayra_caliph_revenant").addTag(Tags.SHIP_UNIQUE_SIGNATURE);
            }
        } catch (Exception ex) {
        }
        try {
            if (Global.getSettings().getVariant("vayra_caliph_revenant_Hull") != null) {
                Global.getSettings().getVariant("vayra_caliph_revenant_Hull").addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                Global.getSettings().getVariant("vayra_caliph_revenant_Hull").addTag(Tags.SHIP_UNIQUE_SIGNATURE);
            }
        } catch (Exception ex) {
        }
        try {
            if (Global.getSettings().getVariant("vayra_caliph_revenant_station") != null) {
                Global.getSettings().getVariant("vayra_caliph_revenant_station").addTag(Tags.SHIP_CAN_NOT_SCUTTLE);
                Global.getSettings().getVariant("vayra_caliph_revenant_station").addTag(Tags.SHIP_UNIQUE_SIGNATURE);
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public PluginPick<MissileAIPlugin> pickMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        switch (missile.getProjectileSpecId()) {
            case KADUR_LRM:
                return new PluginPick<MissileAIPlugin>(new VayraLRMAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            case KADUR_SPLINTER:
                return new PluginPick<MissileAIPlugin>(new VayraSplinterAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SET);
            default:
                return null;
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
            logger.info("reloading unique bounties");
            uniques.reload();
        } else {
            logger.warn("VayraUniqueBountyManager.getInstance() returned null");
        }

        VayraPersonBountyManager bounties = VayraPersonBountyManager.getInstance();
        if (bounties != null) {
            logger.info("reloading regular bounties");
            bounties.reload();
        } else {
            logger.warn("VayraPersonBountyManager.getInstance() returned null");
        }

        VayraPlayerBountyIntel player = VayraPlayerBountyIntel.getInstance();
        if (player != null) {
            logger.info("reloading player-targeted bounties");
            player.loadParticipatingFactionList();
        } else {
            logger.warn("VayraPlayerBountyIntel.getInstance() returned null");
        }

        VayraDistressCallManager distress = VayraDistressCallManager.getInstance();
        if (distress != null) {
            logger.info("reloading distress calls");
            distress.loadEvents();
        } else {
            logger.warn("VayraDistressCallManager.getInstance() returned null");
        }
        if (Global.getSector().getMemoryWithoutUpdate().get("$vayra_missions_generated") == null) {
            Global.getSector().getMemoryWithoutUpdate().set("$vayra_missions_generated", true);
            StarSystemAPI system = Global.getSector().getStarSystem("Mirage");
            if (system != null && system.getEntityById("vayra_mirage_star") != null) {
                for (Object object : Global.getSector().getStarSystem("Mirage").getEntities(CampaignTerrainAPI.class)) {
                    //K1 && K2 Mission Rewards
                    if (object instanceof SectorEntityToken) {
                        SectorEntityToken asteroidsociety = (SectorEntityToken) object;
                        if (asteroidsociety.getCircularOrbitRadius() == 12500 && asteroidsociety.getCircularOrbitPeriod() == 778) {
                            DerelictShipEntityPlugin.DerelictShipData paramsk1 = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("falcon_xiv_Hull"), ShipRecoverySpecial.ShipCondition.WRECKED, "HSS Left Hook", Factions.HEGEMONY, 0f), false);
                            SectorEntityToken K1 = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsk1);
                            K1.setDiscoverable(true);
                            K1.setDiscoveryXP(250f);
                            K1.setCircularOrbit(asteroidsociety, 270, 200, 15); //L4 Asteroid.
                            K1.setSalvageXP(250f);
                            K1.getMemoryWithoutUpdate().set("$vayra_k1_eventRef", true);
                            ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                            data.addShip(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("falcon_xiv_Hull"), ShipRecoverySpecial.ShipCondition.WRECKED, "HSS Left Hook", Factions.INDEPENDENT, 0f));
                            Misc.setSalvageSpecial(K1, data);
                            DerelictShipEntityPlugin.DerelictShipData paramsk2 = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("vayra_eagle_k_Hull"), ShipRecoverySpecial.ShipCondition.BATTERED, "KHS Babylonian Raven", "kadur_remnant", 0f), false);
                            SectorEntityToken K2 = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsk2);
                            K2.setCircularOrbit(asteroidsociety, 90, 300, 15);
                            K2.setDiscoverable(true);
                            K2.setDiscoveryXP(250f);
                            K2.setSalvageXP(250f);
                            K2.getMemoryWithoutUpdate().set("$vayra_k2_eventRef", true);
                            ShipRecoverySpecial.ShipRecoverySpecialData data2 = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                            data2.addShip(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("vayra_eagle_k_Hull"), ShipRecoverySpecial.ShipCondition.BATTERED, "KHS Babylonian Raven", "kadur_remnant", 0f));
                            Misc.setSalvageSpecial(K2, data2);
                            break;
                        }
                    }
                }
                //K3
                if (system.getEntityById("mirage_ii_jump") != null) {
                    DerelictShipEntityPlugin.DerelictShipData paramsk3 = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("vayra_seraph_standard"), ShipRecoverySpecial.ShipCondition.WRECKED, "KHS Holy Sword", "kadur_remnant", 0f), false);
                    SectorEntityToken K3 = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsk3);
                    K3.setDiscoverable(true);
                    K3.setDiscoveryXP(250f);
                    K3.setCircularOrbit(system.getEntityById("mirage_ii_jump"), 270, 200, 15); //Mirage Jump Point of course...
                    K3.setSalvageXP(250f);
                    K3.getMemoryWithoutUpdate().set("$vayra_k3_eventRef", true);
                    ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                    data.addShip(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("vayra_seraph_standard"), ShipRecoverySpecial.ShipCondition.WRECKED, "KHS Holy Sword", "kadur_remnant", 0f));
                    Misc.setSalvageSpecial(K3, data);
                }
                //K4
                if (system.getEntityById("mirage_relay") != null) {
                    DerelictShipEntityPlugin.DerelictShipData paramsk4 = new DerelictShipEntityPlugin.DerelictShipData(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("vayra_targe_crystal"), ShipRecoverySpecial.ShipCondition.WRECKED, "KHS Ceto", "kadur_remnant", 0f), false);
                    SectorEntityToken K4 = BaseThemeGenerator.addSalvageEntity(system, Entities.WRECK, Factions.NEUTRAL, paramsk4);
                    K4.setDiscoverable(true);
                    K4.setDiscoveryXP(250f);
                    K4.setCircularOrbit(system.getEntityById("mirage_relay"), 270, 200, 15); //Mirage Relay of course...
                    K4.setSalvageXP(250f);
                    K4.getMemoryWithoutUpdate().set("$vayra_k4_eventRef", true);
                    ShipRecoverySpecial.ShipRecoverySpecialData data = new ShipRecoverySpecial.ShipRecoverySpecialData(null);
                    data.addShip(new ShipRecoverySpecial.PerShipData(Global.getSettings().getVariant("vayra_targe_crystal"), ShipRecoverySpecial.ShipCondition.WRECKED, "KHS Ceto", "kadur_remnant", 0f));
                    Misc.setSalvageSpecial(K4, data);
                }
                //K5 and K6
                if (Global.getSettings().getMissionScore("vayra_k006") > 0) {
                    Global.getSector().getEntityById("vayra_kadur_revenant").getMarket().getMemoryWithoutUpdate().set("$nex_recentlyCapturedByPlayer", true, 365f);
                }
                if (Global.getSettings().getMissionScore("vayra_k005") > 0) {
                    Global.getSector().getEntityById("vayra_refugestation").getMarket().getMemoryWithoutUpdate().set("$nex_recentlyCapturedByPlayer", true, 365f);
                }
                //K7 (if player start with Caliph, disable the HVB)
                if (newGame) {
                    if ("vayra_caliph".equals(Global.getSector().getPlayerFleet().getFlagship().getHullSpec().getBaseHullId())) {
                        //HVB Handling...
                        if (Global.getSettings().getModManager().isModEnabled("vayrasector")) {
                            List<String> spentBounties = (List<String>) Global.getSector().getMemory().get("$vayra_uniqueBountiesSpent");
                            if (spentBounties == null) {
                                spentBounties = new ArrayList<>();
                                Global.getSector().getMemory().set("$vayra_uniqueBountiesSpent", spentBounties);
                                logger.info("unique spentBounties was null, adding empty list");
                            } else {
                                logger.info("unique spentBounties contains: " + spentBounties);
                            }
                            logger.info("spending unique bounty " + "vayra_caliph_bounty");
                            if (!spentBounties.contains("vayra_caliph_bounty")) {
                                spentBounties.add("vayra_caliph_bounty");
                                logger.info("unique spentBounties contains: " + spentBounties);
                            } else {
                                logger.warn("unique spentBounties already contains " + "vayra_caliph_bounty");
                            }
                        }
                        //MagicBounties Handling...
                        Global.getSector().getMemoryWithoutUpdate().set("$HVB_vayra_caliph_bounty_succeeded", true);
                    }
                }
            }
        }
    }

    private static void loadVayraSettings() {
        // Start safely opening the JSON

        JSONObject setting = null;
        try {
            setting = Global.getSettings().loadJSON(SETTINGS_FILE);
        } catch (IOException | JSONException e) {
            logger.error(stringifyException(e), e);
            // Lets not halt the mod-loading VM.
            //throw new RuntimeException(e);
        }

        if (setting == null) {
            logger.warn("Could not load settings; loading default settings...");
            loadDefaultVayraSettings();
        } else {
            // If settings were loaded, lets try loading each individual parameter, but presume they're not there.
            // In that case, lets make them optional and fallback to their default value to avoid throwing JSONException

            VAYRA_DEBUG = setting.optBoolean("vayraDebug", false);
            PIRATE_BOUNTY_MODE = PirateMode.valueOf(setting.optString("usePirateBountyManager", "ALWAYS"));
            EXTRA_BOUNTY_LEVEL_MULT = (float) setting.optDouble("extraBountyLevelMult", 1.5);
            BOUNTY_DURATION = (float) setting.optDouble("bountyDuration", 90.0f);
            RARE_BOUNTY_FLAGSHIP_CHANCE = (float) setting.optDouble("rareBountyFlagshipChance", 0.075f);
            CRUMB_CHANCE = (float) setting.optDouble("bountyIntelCrumbChance", 0.5f);
            BOUNTY_SOFT_MAX_DIST = setting.optInt("bountySoftMaxDist", 10);
            UNIQUE_BOUNTIES = setting.optBoolean("spawnUniqueBounties", true);
            UNIQUE_BOUNTIES_MAX = setting.optInt("maxActiveUniqueBounties", 5);
            PLAYER_BOUNTIES = setting.optBoolean("bountiesOnPlayer", true);
            PLAYER_BOUNTY_FP_SCALING = (float) setting.optDouble("playerBountyBaseFPScaling", 1.25f);
            PLAYER_BOUNTY_SPAWN_RANGE = (float) setting.optDouble("playerBountySpawnRange", 2000.0f);
            PLAYER_BOUNTY_MAX_RANGE = (float) setting.optDouble("playerBountySpawnRangeFromCore", 20000.0f);
            PLAYER_BOUNTY_SYSTEM_DAYS = (float) setting.optDouble("playerBountyDaysInSystem", 7f);
            PLAYER_BOUNTY_RANGE_DAYS = (float) setting.optDouble("playerBountyDaysOutOfRange", 45f);
            RAIDER_BASE_REAPER_ENABLED = setting.optBoolean("stopSpawningRaiderBasesWhenFactionDelet", true);
            COLONIAL_FACTIONS_ENABLED = setting.optBoolean("spawnColonialCompetitors", true);
            COLONIAL_FACTION_TIMEOUT = setting.optInt("colonialCompetitorsStartCycle", 300);
            COLONIAL_FACTION_COLONY_MULT = setting.optInt("colonialCompetitorsColonyCountMult", 4);
            AI_REBELLION_THRESHOLD = setting.optInt("coreCriticalMass", 30);
            POPULAR_FRONT_ENABLED = setting.optBoolean("spawnPopularFront", true);
            POPULAR_FRONT_TIMEOUT = setting.optInt("popularFrontStartCycle", 350);
            PROCGEN_ENTITIES = setting.optBoolean("spawnEntities", true);
            LEAGUE_SUBFACTIONS = setting.optBoolean("leagueSubfactions", true);
            PLAY_TTRPG = setting.optBoolean("playTabletopRoleplayingGame", true);
            ADD_BARREN_PLANETS = setting.optBoolean("addBarrenPlanets", true);
        }
    }

    private static void loadDefaultVayraSettings() {
        VAYRA_DEBUG = false;
        PIRATE_BOUNTY_MODE = PirateMode.ALWAYS;
        EXTRA_BOUNTY_LEVEL_MULT = 1.5f;
        BOUNTY_DURATION = 90f;
        RARE_BOUNTY_FLAGSHIP_CHANCE = 0.075f;
        CRUMB_CHANCE = 0.5f;
        BOUNTY_SOFT_MAX_DIST = 10;
        UNIQUE_BOUNTIES = true;
        UNIQUE_BOUNTIES_MAX = 5;
        PLAYER_BOUNTIES = true;
        PLAYER_BOUNTY_FP_SCALING = 1.25f;
        PLAYER_BOUNTY_SPAWN_RANGE = 2000f;
        PLAYER_BOUNTY_MAX_RANGE = 20000f;
        PLAYER_BOUNTY_SYSTEM_DAYS = 7f;
        PLAYER_BOUNTY_RANGE_DAYS = 45f;
        RAIDER_BASE_REAPER_ENABLED = true;
        COLONIAL_FACTIONS_ENABLED = true;
        COLONIAL_FACTION_TIMEOUT = 212;
        COLONIAL_FACTION_COLONY_MULT = 4;
        AI_REBELLION_THRESHOLD = 30;
        POPULAR_FRONT_ENABLED = true;
        POPULAR_FRONT_TIMEOUT = 210;
        PROCGEN_ENTITIES = true;
        LEAGUE_SUBFACTIONS = true;
        PLAY_TTRPG = true;
        ADD_BARREN_PLANETS = true;
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

        // Handle exerelin specially
        if (EXERELIN_LOADED) {
            if (!SectorManager.getCorvusMode()) {
                // return here because we don't want to generate our handcrafted systems if we're in an exerelin random sector
                return;
            }
        }
        genKadur();
    }

    @Override
    public void onNewGameAfterTimePass() {

        logger.info("new game started, adding scripts");

        Global.getSector().addScript(new VayraSunPusher());
        // add these scripts regardless of setting, since they all just return immediately if not activated
        // and this way they will activate if you activate the setting midgame
        Global.getSector().addScript(new KadurBlueprintStocker());

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

            logger.info(String.format("fleet [%s] is at [%s, %s]... player fleet is at [%s, %s]", fleet.getNameWithFaction(), x, y, playerFleet.getLocation().x, playerFleet.getLocation().y));

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
            logger.info(String.format("Moving [%s] to [%s, %s] get da FUK out my gOD DAMN FAcE", fleet.getNameWithFaction(), x, y));
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
                InvasionFleetManager.EXCEPTION_LIST.remove(conf.factionId);
                DiplomacyManager.disallowedFactions.remove(conf.factionId);

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

                InvasionFleetManager.EXCEPTION_LIST.add(conf.factionId);
                if (!DiplomacyManager.disallowedFactions.contains(conf.factionId)) {
                    DiplomacyManager.disallowedFactions.add(conf.factionId);
                }

            }
        }
    }

    private static void genKadur() {
        new KadurGen().generate(Global.getSector());
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

        logger.info("created " + factionID + " market " + name);

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

        logger.info(String.format("Applying admin %s %s to market %s", market.getFaction().getRank(admin.getRankId()), admin.getNameString(), market.getName()));

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
            logger.warn(e);
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

    public static float Rotate(float currAngle, float addAngle) {
        return (currAngle + addAngle) % 360;
    }

    public static void addAccretionDisk(PlanetAPI star, String name) {
        StarSystemAPI system = star.getStarSystem();
        float orbitRadius = star.getRadius() * 8f;
        float bandWidth = 256f;
        int numBands = 12;

        for (float i = 0; i < numBands; i++) {
            float radius = orbitRadius - i * bandWidth * 0.25f - i * bandWidth * 0.1f;
            float orbitDays = radius / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_dust0");
            rings.add("rings_ice0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(star, "misc", ring, 256f, 0, Color.white, bandWidth,
                    radius + bandWidth / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(star.getRadius());
            visual.setSpiralFactor(spiralFactor);
        }
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius, orbitRadius / 2f, star, name == null ? "Accretion Disk" : name));
        ring.addTag(Tags.ACCRETION_DISK);
        ring.setCircularOrbit(star, 0, 0, -100);
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

    public static String stringifyException(Exception ex) {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = ex.getStackTrace();
        int stacktraceDepth = ex.getStackTrace().length - 1;
        sb.append("Exception ").append(ex).append(" happened!\n");
        sb.append("STACKTRACE: \n");

        for (int i = stacktraceDepth; i > 0; i--) {
            sb.append(stackTrace[i]).append("\n");
        }

        return sb.toString();
    }
}
