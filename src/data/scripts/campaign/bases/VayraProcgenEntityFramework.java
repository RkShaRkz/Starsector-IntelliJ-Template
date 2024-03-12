package data.scripts.campaign.bases;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.ColonyPlayerHostileActListener;
import com.fs.starfarer.api.campaign.listeners.DiscoverEntityListener;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.listeners.ListenerManagerAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.*;

import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator.addRemnantStationInteractionConfig;
import static com.fs.starfarer.api.util.Misc.setSalvageSpecial;
import static data.scripts.VayraMergedModPlugin.*;

public class VayraProcgenEntityFramework implements EveryFrameScript, DiscoverEntityListener, FleetEventListener, ColonyPlayerHostileActListener {

    public static Logger log = Global.getLogger(VayraProcgenEntityFramework.class);

    public static class ProcgenEntityData {

        public final String id;

        public final String factionId;

        protected final boolean isWreck;
        protected final String entityId;
        protected final String additionalDefensesLevel;
        protected final ShipCondition wreckCondition;
        protected final boolean wreckRecoverable;

        public boolean showInIntelTabAfterFound;
        public final String entityName;
        public final String entityDescriptionId;
        public final String entityIllustrationId;
        protected final float entityOrbitDistance;

        protected final int marketSize;
        protected final boolean freePort;
        protected final boolean hiddenMarket;
        protected final ArrayList<String> marketStuff;
        protected final ArrayList<String> submarkets;
        public boolean fuckOffWhenKilledAndDieForever;

        protected final String spawnInSystem;
        protected final float minSpawnDistanceFromCore;
        protected final float maxSpawnDistanceFromCore;
        protected final boolean hostile;

        protected SectorEntityToken entity = null;
        public List<CampaignFleetAPI> fleets = new ArrayList<>();

        public ProcgenEntityData(
                String id,
                String factionId,
                boolean isWreck,
                String entityId,
                String additionalDefensesLevel,
                String wreckCondition,
                boolean wreckRecoverable,
                boolean showInIntelTabAfterFound,
                String entityName,
                String entityDescriptionId,
                String entityIllustrationId,
                float entityOrbitDistance,
                int marketSize,
                boolean freePort,
                boolean hiddenMarket,
                ArrayList<String> marketStuff,
                ArrayList<String> submarkets,
                boolean fuckOffWhenKilledAndDieForever,
                String spawnInSystem,
                float minSpawnDistanceFromCore,
                float maxSpawnDistanceFromCore,
                boolean hostile
        ) {
            this.id = id;
            this.factionId = factionId;
            this.isWreck = isWreck;
            this.entityId = entityId;
            this.additionalDefensesLevel = additionalDefensesLevel;
            this.wreckCondition = ShipCondition.valueOf(wreckCondition);
            this.wreckRecoverable = wreckRecoverable;
            this.showInIntelTabAfterFound = showInIntelTabAfterFound;
            this.entityName = entityName;
            this.entityDescriptionId = entityDescriptionId;
            this.entityIllustrationId = entityIllustrationId;
            this.entityOrbitDistance = entityOrbitDistance;
            this.marketSize = marketSize;
            this.freePort = freePort;
            this.hiddenMarket = hiddenMarket;
            this.marketStuff = marketStuff;
            this.submarkets = submarkets;
            this.fuckOffWhenKilledAndDieForever = fuckOffWhenKilledAndDieForever;
            this.spawnInSystem = spawnInSystem;
            this.minSpawnDistanceFromCore = minSpawnDistanceFromCore;
            this.maxSpawnDistanceFromCore = maxSpawnDistanceFromCore;
            this.hostile = hostile;
        }
    }

    private boolean finishedSetup = false;

    public static String ENTITY_LIST_PATH = "data/config/vayraProcgenEntities/";
    private static final String TAG = "vayra_procgen_station";
    private static final Map<String, ProcgenEntityData> ENTITY_DATA = new HashMap<>();

    protected List<CampaignFleetAPI> stationsOnSuicideWatch = new ArrayList<>();
    protected List<MarketAPI> marketsOnSuicideWatch = new ArrayList<>();

    private SectorEntityToken addDerelictShip(LocationAPI location, String variantId, ShipCondition condition, boolean recoverable) {
        DerelictShipData params = new DerelictShipData(new ShipRecoverySpecial.PerShipData(variantId, condition), false);
        SectorEntityToken ship = addSalvageEntity(location, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        if (recoverable) {
            ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        return ship;
    }

    @Override
    public void advance(float amount) {

        // set up everything, if we haven't already
        if (!finishedSetup) {
            log.info("setting up...");
            loadData();
            placeEntities();

            // once we're set up, don't do it again
            finishedSetup = true;
        }

        // add ourselves as a listener to the sector listener manager
        ListenerManagerAPI manager = Global.getSector().getListenerManager();
        if (manager != null && !manager.hasListener(this)) {
            manager.addListener(this);
        }

        // make sure we're watching for any stations or markets we may need to kill
        for (ProcgenEntityData data : ENTITY_DATA.values()) {
            if (data.fuckOffWhenKilledAndDieForever && data.marketSize > 0) {
                MarketAPI market = data.entity == null ? null : data.entity.getMarket();
                CampaignFleetAPI fleet = market == null ? null : Misc.getStationFleet(market);
                if (fleet != null && !stationsOnSuicideWatch.contains(fleet)) {
                    stationsOnSuicideWatch.add(fleet);
                }
            }
            if (data.marketSize > 0 && data.hiddenMarket) {
                MarketAPI market = data.entity == null ? null : data.entity.getMarket();
                if (market != null && !marketsOnSuicideWatch.contains(market)) {
                    marketsOnSuicideWatch.add(market);
                }
            }
        }
    }

    private static void loadData() {

        log.info("loading data...");
        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("unique_id", ENTITY_LIST_PATH + "entities.csv", MOD_ID);
            List<String> entities = new ArrayList<>();

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);
                String factionId = row.getString("unique_id"); // get unique ID
                boolean active = row.getBoolean("active");
                if (active) {
                    entities.add(factionId);
                }
            }

            for (String id : entities) {
                try {
                    log.info("loading JSON for " + id);
                    JSONObject config = Global.getSettings().getMergedJSONForMod(ENTITY_LIST_PATH + id + ".json", MOD_ID);

                    if (VAYRA_DEBUG) {
                        log.info("loading industries/etc for " + id);
                    }
                    String industryListString = config.optString("marketStuff", null);
                    ArrayList<String> industryList = new ArrayList<>();
                    if (industryListString != null) {
                        industryList = new ArrayList<>(Arrays.asList(industryListString.split("\\s*(,\\s*)+")));
                        if (VAYRA_DEBUG) {
                            log.info("industries/etc list: " + industryListString);
                        }
                    }
                    if (VAYRA_DEBUG) {
                        log.info("loading submarkets for " + id);
                    }
                    String submarketListString = config.optString("submarkets", null);
                    ArrayList<String> submarketList = new ArrayList<>();
                    if (submarketListString != null) {
                        submarketList = new ArrayList<>(Arrays.asList(submarketListString.split("\\s*(,\\s*)+")));
                        if (VAYRA_DEBUG) {
                            log.info("submarket list: " + submarketListString);
                        }
                    }

                    ProcgenEntityData data = new ProcgenEntityData(
                            id,
                            config.getString("factionId"),
                            config.optBoolean("isWreck", false),
                            config.getString("entityId"),
                            config.optString("additionalDefensesLevel", null),
                            config.optString("wreckCondition", "AVERAGE"),
                            config.optBoolean("wreckRecoverable", true),
                            config.optBoolean("showInIntelTabAfterFound", false),
                            config.optString("entityName", "Unidentified Sensor Contact"),
                            config.optString("entityDescriptionId", null),
                            config.optString("entityIllustrationId", null),
                            (float) config.optDouble("entityOrbitDistance", 150d + (Math.random() * 200d)),
                            config.optInt("marketSize", 0),
                            config.optBoolean("freePort", true),
                            config.optBoolean("hiddenMarket", true),
                            industryList,
                            submarketList,
                            config.optBoolean("fuckOffWhenKilledAndDieForever", false),
                            config.optString("spawnInSystem", null),
                            (float) config.optDouble("minSpawnDistanceFromCore", 10000),
                            (float) config.optDouble("maxSpawnDistanceFromCore", 15000),
                            config.optBoolean("hostile", true)
                    );
                    ENTITY_DATA.put(data.id, data);
                } catch (IOException | JSONException | NullPointerException ex) {
                    log.error(String.format("Failed loading procgen entity JSON for [%s]!!! ;.....;", id), ex);
                }
            }
        } catch (IOException | JSONException ex) {
            log.error("entities.csv loading failed!!! ;.....;");
        }
    }

    // this method makes the market fade and despawn if it's set to fuckOffWhenKilledAndDieForever and the station is destroyed
    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, CampaignEventListener.FleetDespawnReason reason, Object param) {
        SectorEntityToken entity = null;
        for (ProcgenEntityData data : ENTITY_DATA.values()) {
            if (data.fuckOffWhenKilledAndDieForever && data.marketSize > 0) {
                MarketAPI market = data.entity == null ? null : data.entity.getMarket();
                CampaignFleetAPI check = market == null ? null : Misc.getStationFleet(market);
                if (check != null && check.equals(fleet)) {
                    entity = data.entity;
                }
            }
        }
        if (entity != null && stationsOnSuicideWatch != null && stationsOnSuicideWatch.contains(fleet)) {
            Misc.fadeAndExpire(entity);
            if (Misc.getFactionMarkets(entity.getFaction()).size() <= 0 && entity.getFaction().isShowInIntelTab()) {
                entity.getFaction().setShowInIntelTab(false);
            }
        }
    }

    // this method removes the NO_DECIV_KEY memory key from the market if it was a hidden market and got sat-bombed
    // and also manually decivilizes it if it woulda been decivilized
    @Override
    public void reportSaturationBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        if (market != null && marketsOnSuicideWatch != null && marketsOnSuicideWatch.contains(market)) {
            market.getMemoryWithoutUpdate().unset(DecivTracker.NO_DECIV_KEY);
            market.getPrimaryEntity().setDiscoverable(false);
            if (market.getSize() <= 4 && market.getStabilityValue() < 1f) {
                DecivTracker.decivilize(market, false);
            }
            if (Misc.getFactionMarkets(market.getFaction()).size() <= 0 && market.getFaction().isShowInIntelTab()) {
                market.getFaction().setShowInIntelTab(false);
            }
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        // nothing to do here
    }

    @Override
    public void reportRaidForValuablesFinishedBeforeCargoShown(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, CargoAPI cargo) {
        // nothing to do here
    }

    @Override
    public void reportRaidToDisruptFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData, Industry industry) {
        // nothing to do here
    }

    @Override
    public void reportTacticalBombardmentFinished(InteractionDialogAPI dialog, MarketAPI market, MarketCMD.TempData actionData) {
        // nothing to do here
    }

    private void placeEntities() {

        log.info("placing entities...");
        for (ProcgenEntityData data : ENTITY_DATA.values()) {

            // handle rep
            if (data.hostile) {
                FactionAPI faction = Global.getSector().getFaction(data.factionId);
                List<FactionAPI> factions = Global.getSector().getAllFactions();
                for (FactionAPI other : factions) {
                    faction.setRelationship(other.getId(), RepLevel.VENGEFUL);
                }
                faction.setRelationship("neutral", 0f);
            }

            // pick a location
            float dist = data.entityOrbitDistance;
            OrbitAPI orbit = null;
            @SuppressWarnings("UnusedAssignment")
            LocationAPI hyperspace = null;
            hyperspace = "hyperspace".equals(data.spawnInSystem) ? Global.getSector().getHyperspace() : null;
            StarSystemAPI system = data.spawnInSystem == null ? null : Global.getSector().getStarSystem(data.spawnInSystem);
            if (system == null) {
                system = pickSystem(data);
            }
            if (system == null) {
                log.error("couldn't place " + data.id + " procgen entity, no system available");
                continue;
            }
            Set<SectorEntityToken> exclude = new HashSet<>();

            LocationAPI location;
            if (hyperspace != null) {
                location = hyperspace;
            } else {
                location = system;
                orbit = BaseThemeGenerator.pickAnyLocation(new Random(), system, dist, exclude).orbit;
            }

            SectorEntityToken entity;
            // if it's a wreck, our job is ez
            if (data.isWreck) {
                entity = addDerelictShip(location, data.entityId, data.wreckCondition, data.wreckRecoverable);
                log.info("placed " + data.id + " in " + location.getId() + " as wreck");
            } else { // otherwise first we try to spawn it as a token
                try {

                    entity = location.addCustomEntity(data.id, // unique ID
                            data.entityName, // in-game display name
                            data.entityId, // entity type id from custom_entities.json
                            data.factionId // faction ID of entity
                    );

                    log.info("placed " + data.id + " in " + location.getId() + " as entity");

                } catch (RuntimeException ex) { // if that doesn't work, we try it as a ship
                    CampaignFleetAPI fleet = FleetFactoryV3.createEmptyFleet(data.factionId, FleetTypes.BATTLESTATION, null);
                    FleetMemberAPI member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.entityId);
                    fleet.getFleetData().addFleetMember(member);
                    entity = fleet;

                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
                    fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE, true);
                    fleet.setStationMode(true);
                    addRemnantStationInteractionConfig(fleet);
                    location.addEntity(fleet);

                    fleet.clearAbilities();
                    fleet.addAbility(Abilities.TRANSPONDER);
                    fleet.getAbility(Abilities.TRANSPONDER).activate();
                    fleet.getDetectedRangeMod().modifyFlat("gen", 1000f);
                    fleet.setAI(null);

                    fleet.setNoFactionInName(true);
                    fleet.setName(data.entityName);

                    int level = 5;
                    if (data.marketSize >= 7 || "CITADEL".equals(data.additionalDefensesLevel)) {
                        level += 15;
                    } else if (data.marketSize >= 5 || "BASTION".equals(data.additionalDefensesLevel)) {
                        level += 10;
                    } else if (data.marketSize >= 3 || "OUTPOST".equals(data.additionalDefensesLevel)) {
                        level += 5;
                    }
                    PersonAPI commander = OfficerManagerEvent.createOfficer(Global.getSector().getFaction(data.factionId), level, true);
                    FleetFactoryV3.addCommanderSkills(commander, fleet, new Random());
                    fleet.setCommander(commander);
                    fleet.getFlagship().setCaptain(commander);

                    member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());

                    log.info("placed " + data.id + " in " + location.getId() + " as fleet");
                }
            }
            data.entity = entity;

            if (data.marketSize > 0) {
                addMarketplace(data.factionId,
                        entity,
                        null,
                        data.entityName,
                        data.marketSize,
                        data.marketStuff,
                        data.submarkets,
                        false,
                        data.hiddenMarket,
                        data.freePort
                );
            }

            if (data.entityDescriptionId != null) {
                entity.setCustomDescriptionId(data.entityDescriptionId);
            }
            if (data.entityIllustrationId != null) {
                entity.setInteractionImage("illustrations", data.entityIllustrationId);
            }

            if (hyperspace != null) {
                pickLocationInHyperspace(entity, hyperspace, data);
            } else if (orbit != null) {
                entity.setOrbit(orbit);
                orbit.setEntity(entity);
            } else {
                log.error("couldn't find orbit for " + data.id + ", placing it with manual orbit or maybe no orbit i guess? god knows what that'll do lmao");
                if (location instanceof StarSystemAPI) {
                    system = (StarSystemAPI) location;
                    float maxDist = 0f;
                    for (SectorEntityToken obj : system.getAllEntities()) {
                        float rad2x = obj.getRadius() * 2;
                        if (rad2x > maxDist) {
                            maxDist = rad2x;
                        }
                    }
                    entity.setCircularOrbit(system.getCenter(), (float) (Math.random() * 360f), maxDist, 360f);
                } else {
                    pickLocationInHyperspace(entity, Global.getSector().getHyperspace(), data);
                }
            }

            if (data.additionalDefensesLevel != null) {
                spawnDefenseFleets(data);
            }
        }
    }

    private void pickLocationInHyperspace(SectorEntityToken entity, LocationAPI hyperspace, ProcgenEntityData data) {

        float min = data.minSpawnDistanceFromCore;
        float max = data.maxSpawnDistanceFromCore;
        float diff = Math.abs(max - min);
        float x = (float) (Math.random() * diff);
        float y = (float) (Math.random() * diff);
        WeightedRandomPicker<String> quadrants = new WeightedRandomPicker<>();
        quadrants.add("top left");
        quadrants.add("top right");
        quadrants.add("bottom left");
        quadrants.add("bottom right");
        String sector = quadrants.pick();
        switch (sector) {
            case "top left":
                x = (x * -1f) - min;
                y = y + min;
                break;
            case "top right":
                x = x + min;
                y = y + min;
                break;
            case "bottom left":
                x = (x * -1f) - min;
                y = (y * -1f) - min;
                break;
            case "bottom right":
                x = x + min;
                y = (y * -1f) - min;
                break;
        }
        float range = 2000f;
        boolean clear = false;
        while (!clear) {
            List<JumpPointAPI> close = new ArrayList<>();
            Vector2f loc = new Vector2f(x, y);
            for (Object o : hyperspace.getEntities(JumpPointAPI.class)) {
                JumpPointAPI point = (JumpPointAPI) o;
                float dist = Misc.getDistance(loc, point.getLocationInHyperspace());
                if (dist < range) {
                    close.add(point);
                }
            }
            if (close.isEmpty()) {
                clear = true;
            } else {
                switch (sector) {
                    case "top left":
                        x -= range * Math.random();
                        y += range * Math.random();
                        break;
                    case "top right":
                        x += range * Math.random();
                        y += range * Math.random();
                        break;
                    case "bottom left":
                        x -= range * Math.random();
                        y -= range * Math.random();
                        break;
                    case "bottom right":
                        x += range * Math.random();
                        y -= range * Math.random();
                        break;
                }
            }
        }
        Vector2f loc = new Vector2f(x, y);
        entity.setCircularOrbitWithSpin(hyperspace.createToken(loc), 0, 2000, 265, 25, 100);
    }

    protected void spawnDefenseFleets(ProcgenEntityData data) {

        StarSystemAPI system = data.entity.getStarSystem();
        if (system == null) {
            return;
        }

        Map<String, Integer> patrols = new HashMap<>();

        switch (data.additionalDefensesLevel) {
            case "OUTPOST":
                patrols.put(FleetTypes.PATROL_SMALL, 4);
                break;
            case "BASTION":
                patrols.put(FleetTypes.PATROL_SMALL, 6);
                patrols.put(FleetTypes.PATROL_LARGE, 1);
                break;
            case "CITADEL":
                patrols.put(FleetTypes.PATROL_SMALL, 6);
                patrols.put(FleetTypes.PATROL_MEDIUM, 3);
                patrols.put(FleetTypes.PATROL_LARGE, 2);
                break;
            default:
                break;
        }

        for (String patrol : patrols.keySet()) {
            while (patrols.get(patrol) > 0) {
                int toSpawn = patrols.get(patrol);
                toSpawn--;
                patrols.put(patrol, toSpawn);

                int combatPoints = 0;

                switch (patrol) {
                    case FleetTypes.PATROL_SMALL:
                        combatPoints = 50;
                        break;
                    case FleetTypes.PATROL_MEDIUM:
                        combatPoints = 150;
                        break;
                    case FleetTypes.PATROL_LARGE:
                        combatPoints = 300;
                        break;
                    default:
                        break;
                }

                Random random = new Random();
                FleetParamsV3 params = new FleetParamsV3(
                        system.getLocation(),
                        data.factionId,
                        1f,
                        patrol,
                        combatPoints, // combatPts
                        0f, // freighterPts 
                        0f, // tankerPts
                        0f, // transportPts
                        0f, // linerPts
                        0f, // utilityPts
                        0f // qualityMod
                );
                params.withOfficers = true;
                params.random = random;

                CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
                if (fleet == null) {
                    return;
                }
                system.addEntity(fleet);
                fleet.setLocation(data.entity.getLocation().x, data.entity.getLocation().y);

                if (patrol.equals(FleetTypes.PATROL_LARGE)) {
                    fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, data.entity, 99999f);
                } else {
                    fleet.addAssignment(FleetAssignment.PATROL_SYSTEM, data.entity, 99999f);
                }
            }
        }
    }

    @Override
    public void reportEntityDiscovered(SectorEntityToken entity) {
        for (ProcgenEntityData data : ENTITY_DATA.values()) {
            if (entity.equals(data.entity) && data.showInIntelTabAfterFound) {
                FactionAPI faction = Global.getSector().getFaction(data.factionId);
                if (!faction.isShowInIntelTab()) {
                    faction.setShowInIntelTab(true);
                }
            }
        }
    }

    // here's where we set weights for systems to pick or whatever
    public StarSystemAPI pickSystem(ProcgenEntityData data) {
        WeightedRandomPicker<StarSystemAPI> systems = new WeightedRandomPicker<>();
        WeightedRandomPicker<StarSystemAPI> backup = new WeightedRandomPicker<>();
        log.info(data.id + " lists null or invalid system, picking one myself");
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float weight = 0f;
            for (String tag : system.getTags()) {
                switch (tag) {
                    case Tags.THEME_MISC:
                        weight += 3f;
                        break;
                    case Tags.THEME_MISC_SKIP:
                        weight += 3f;
                        break;
                    default:
                        break;
                }
            }
            if (weight > 0f && !system.hasTag(TAG)) {
                float dist = Misc.getDistance(new Vector2f(0, 0), system.getLocation());
                if (dist >= data.minSpawnDistanceFromCore && dist <= data.maxSpawnDistanceFromCore) {
                    systems.add(system, weight);
                }
                backup.add(system, weight);
            }
        }
        StarSystemAPI system;
        if (!systems.isEmpty()) {
            system = systems.pick();
        } else {
            system = backup.pick();
            log.info("couldn't find an appropriate system for " + data.id + ", picking an inappropriate one");
        }
        if (system == null) {
            return null;
        }
        system.addTag(TAG);
        return system;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }
}
