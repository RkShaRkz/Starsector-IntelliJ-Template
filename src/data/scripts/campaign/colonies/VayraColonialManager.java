package data.scripts.campaign.colonies;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI.ShipTypeHints;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl;
import com.fs.starfarer.api.impl.campaign.econ.ResourceDepositsCondition;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.VayraMergedModPlugin;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

import java.io.IOException;
import java.util.*;

import static com.fs.starfarer.api.impl.campaign.procgen.themes.MiscellaneousThemeGenerator.PLANETARY_SHIELD_PLANET_KEY;
import static data.scripts.VayraMergedModPlugin.*;

public class VayraColonialManager implements EveryFrameScript {

    public static Logger log = Global.getLogger(VayraColonialManager.class);

    public static final String KEY = "$vayra_colonialManager";

    public static final String COLONY_FACTION_LIST_PATH = "data/config/vayraColonies/";
    public static final String COLONY_FACTION_LIST_CSV = "colony_factions.csv";

    public static final float BASE_FLEET_POINTS = 150f;

    // only make a new colony / upgrade existing colonies every so often
    private final IntervalUtil colonyTimer = new IntervalUtil(90f, 180f);
    private final IntervalUtil upgradeTimer = new IntervalUtil(60f, 90f);
    private final IntervalUtil useItemsTimer = new IntervalUtil(2f, 7f);
    public static final float COLONIAL_SPECIAL_CHANCE = 0.15f;
    public static final Map<String, Float> COLONIAL_MONEY_BUILDINGS = new HashMap<>();

    public static final float BASE_COLONY_CHANCE = 0.5f; // and only make a new colony some of the time
    public static final int BASE_PER_FACTION_COLONY_COUNT = 1; // maximum (player's markets + this) colonies per faction
    // also, set in the settings .ini, a separate global colony maximum

    public static final float DEFAULT_PREF_SCORE = 0.5f;
    public static final float DEFAULT_PREF_MIN_DIST = 0f;
    public static final float DEFAULT_PREF_MAX_DIST = 2000f;

    private final WeightedRandomPicker<String> colonyFactions = new WeightedRandomPicker<>();

    private final Set<MarketAPI> planetsTargetedForColonies = new HashSet<>();
    private final Set<MarketAPI> coloniesActive = new HashSet<>();
    private final Set<MarketAPI> coloniesToRemove = new HashSet<>();
    public Set<String> inactiveColonyFactions = new HashSet<>();

    public Set<String> possibleColonyFactions = new HashSet<>();
    public Map<String, String> colonialParents = new HashMap<>();
    public Map<String, String> colonialHomeworlds = new HashMap<>();
    public Map<String, Boolean> colonialFreeport = new HashMap<>();
    public Map<String, String> colonialSpecial = new HashMap<>();
    public Map<String, Map<String, String>> colonialStations = new HashMap<>();
    public Map<String, Map<String, Float>> colonialPlanetAffinities = new HashMap<>();
    public Map<String, Float> colonialPrefScore = new HashMap<>();
    public Map<String, Float> colonialPrefMinDist = new HashMap<>();
    public Map<String, Float> colonialPrefMaxDist = new HashMap<>();

    private String AOTD_MOD_ID = "aotd_vok";
    public boolean AOTD_ENABLED = Global.getSettings().getModManager().isModEnabled(AOTD_MOD_ID);

    static {
        COLONIAL_MONEY_BUILDINGS.put(Industries.TECHMINING, 5f); // 5f is the magic weight that means it requires resources
        COLONIAL_MONEY_BUILDINGS.put(Industries.MINING, 5f); // 5f is the magic weight that means it requires resources
        COLONIAL_MONEY_BUILDINGS.put(Industries.FARMING, 5f); // 5f is the magic weight that means it requires resources
        COLONIAL_MONEY_BUILDINGS.put(Industries.AQUACULTURE, 5f); // 5f is the magic weight that means it requires resources
        COLONIAL_MONEY_BUILDINGS.put(Industries.LIGHTINDUSTRY, 3f);
        COLONIAL_MONEY_BUILDINGS.put(Industries.REFINING, 3f);
        COLONIAL_MONEY_BUILDINGS.put(Industries.FUELPROD, 1f);
    }

    public static final float INDUSTRY_CORE_ESCAPE_CHANCE = 0.5f;
    public static final float ADMIN_CORE_ESCAPE_CHANCE = 1f;

    private static final boolean SPAM_LOG_ENABLED = false;
    private static final boolean SPAM_LOG_SPAMS_CONSOLE = false;

    public static boolean UPGRADES_DISABLED = false;

    public VayraColonialManager() {

        possibleColonyFactions = loadColonyFactionList();
        loadColonyFactionData();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);

        for (String colonialFactionID : possibleColonyFactions) {
            FactionAPI colonialFaction = Global.getSector().getFaction(colonialFactionID);
            FactionAPI colonialParent = Global.getSector().getFaction(colonialParents.get(colonialFactionID));
            for (FactionAPI other : Global.getSector().getAllFactions()) {
                colonialFaction.setRelationship(other.getId(), colonialParent.getRelationship(other.getId()));
            }
            colonialFaction.setRelationship(Factions.REMNANTS, -1f);
            colonialFaction.setRelationship(Factions.DERELICT, -1f);
            colonialFaction.setRelationship("blade_breakers", -1f);
            colonialFaction.setRelationship("ae_ixbattlegroup", -1f);
            List<String> others = new ArrayList<>();
            for (String id : possibleColonyFactions) {
                if (!id.equals(colonialFactionID)) {
                    others.add(id);
                }
            }
            for (String otherId : others) {
                colonialFaction.setRelationship(otherId, -1f);
            }

            // self-love is imp0ortant
            colonialFaction.setRelationship(colonialFactionID, 1f);
        }

        safeChangeRelationshipForFaction("warhawk_republic", new RelationshipDataWithFloatReputation(Factions.HEGEMONY, -1f));
        safeChangeRelationshipForFaction("science_fuckers", new RelationshipDataWithFloatReputation(Factions.REMNANTS, 0f));
        safeChangeRelationshipForFaction("communist_clouds", new RelationshipDataWithRepLevelReputation(Factions.INDEPENDENT, RepLevel.HOSTILE));
        safeChangeRelationshipForFaction("communist_clouds", new RelationshipDataWithRepLevelReputation(Factions.LUDDIC_PATH, RepLevel.HOSTILE));
        safeChangeRelationshipForFaction("communist_clouds", new RelationshipDataWithFloatReputation(Factions.DIKTAT, -1f));
        safeChangeRelationshipForFaction("communist_clouds", new RelationshipDataWithFloatReputation("tahlan_legioinfernalis", -1f));
        safeChangeRelationshipForFaction("ashen_keepers", new RelationshipDataWithFloatReputation(Factions.LUDDIC_CHURCH, -0.6f));
        safeChangeRelationshipForFaction("ashen_keepers", new RelationshipDataWithFloatReputation(Factions.LUDDIC_PATH, -1f));
        safeChangeRelationshipForFaction("ashen_keepers", new RelationshipDataWithRepLevelReputation(Factions.TRITACHYON, RepLevel.WELCOMING));

        if (!EXERELIN_LOADED) {
            safeChangeRelationshipForFaction("ashen_keepers", new RelationshipDataWithFloatReputation(Factions.PLAYER, 0f));
            if (safeGetRelationshipForFactionToFaction("communist_clouds", Factions.PLAYER) <= -RepLevel.getT2()) {
                safeChangeRelationshipForFaction("communist_clouds", new RelationshipDataWithRepLevelReputation(Factions.PLAYER, RepLevel.INHOSPITABLE));
            }
        }
    }

    /**
     * Safely and null-safely change the relationship for a faction {@code factionId} to the new relationship using the
     * {@link RelationshipData} intermediary containing both the faction ID and the relationship value to change to.
     * @param factionID the faction ID whose relationship is to be changed
     * @param newRelationship the new relationship towards the faction ID
     * @return whether the faction was found and successfully changed the relationship
     */
    private boolean safeChangeRelationshipForFaction(String factionID, RelationshipData newRelationship) {
        boolean retVal = false;

        FactionAPI testFaction = Global.getSector().getFaction(factionID);
        if (testFaction != null) {
            switch (newRelationship.getReputation().getHolderType()) {
                case FLOAT: {
                    testFaction.setRelationship(newRelationship.getFactionId(), newRelationship.getReputation().getFloatReputation());
                    retVal = true;
                }
                break;

                case REP_LEVEL: {
                    testFaction.setRelationship(newRelationship.getFactionId(), newRelationship.getReputation().getRepLevelReputation());
                    retVal = true;
                }
                break;

                default:
                    throw new IllegalStateException("ReputationHolderType "+newRelationship.getReputation().getHolderType()+" encountered! Please add support for it!");
            }
        }

        return retVal;
    }

    /**
     * Safely and null-safely get relationship between {@code fromFactionID} and {@code toFactionID} and return that.
     * In case the {@link Global#getSector()} and subsequent {@link SectorAPI#getFaction(String)} return null, the method
     * will return <b>0.0</b>, assuming a neutral relationship between the non-existant <i>fromFaction</i> to anything.
     *
     * @param fromFactionID the faction ID of the from-faction whose relationship we want to query
     * @param toFactionID the faction ID of the to-faction whose relationship we want to query
     * @return the relationship between <i>fromFaction</i> towards <i>toFaction</i>
     */
    private float safeGetRelationshipForFactionToFaction(String fromFactionID, String toFactionID) {
        FactionAPI testFaction = Global.getSector().getFaction(fromFactionID);
        if (testFaction != null) {
            return testFaction.getRelationship(toFactionID);
        }

        return 0f;
    }

    public static VayraColonialManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraColonialManager) test;
    }

    private IntervalUtil t = null;

    @Override
    public void advance(float amount) {
        spamLog("--> VayraColonialManager::advance()");
        checkIfAnyShouldBeVisible();

        if (!COLONIAL_FACTIONS_ENABLED) {
            return;
        }

        float days = Global.getSector().getClock().convertToDays(amount);
        colonyTimer.advance(days);
        upgradeTimer.advance(days);
        useItemsTimer.advance(days);
        if (VAYRA_DEBUG) {
            colonyTimer.advance(days * 50f);
            upgradeTimer.advance(days * 20f);
        }
        float colonyChance = BASE_COLONY_CHANCE;

        if (VAYRA_DEBUG) {
            colonyChance = 1f;
        }

        // AI fuckery upkeep
        if (Global.getSector().getPersistentData().containsKey(AI_REBELLION_KEY) && (boolean) Global.getSector().getPersistentData().get(AI_REBELLION_KEY)) {
            AIFuckeryUpkeep();
        }

        // Use items if available
        if (useItemsTimer.intervalElapsed()) {
            useItems();
        }

        spamLog("VayraColonialManager::advance()\tcoloniesActive: "+stringifyColonyList());
        if (!coloniesActive.isEmpty()) {
            for (MarketAPI market : coloniesActive) {
                market.advance(amount);

                // free upgrade if you don't have a spaceport (which SHOULD be building a spaceport)
                if (!market.hasIndustry(Industries.SPACEPORT) && !market.hasIndustry(Industries.MEGAPORT)) {
                    pickNextUpgrade(market);
                }
                // for (Industry industry : market.getIndustries()) industry.advance(amount);
            }
        }

        if (upgradeTimer.intervalElapsed()) {
            spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\tcoloniesActive size: "+coloniesActive.size());
            if (!coloniesActive.isEmpty()) {
                for (MarketAPI market : coloniesActive) {
                    pickNextUpgrade(market);
                }
                List<MarketAPI> remove = new ArrayList<>();
                for (MarketAPI toRemove : coloniesToRemove) {
                    coloniesActive.remove(toRemove);
                    log.info(String.format("removing %s from list of colonies as it now belongs to %s", toRemove.getName(), toRemove.getFactionId()));
                    purgeCores(toRemove);
                    executeAdmin(toRemove);
                    remove.add(toRemove);
                }
                for (MarketAPI toRemove : remove) {
                    coloniesToRemove.remove(toRemove);
                }

                if (AI_REBELLION_THRESHOLD > 0) {
                    testForAITakeover("science_fuckers");
                }
            }
        }

        if (colonyTimer.intervalElapsed()) {
            spamLog("VayraColonialManager::advance()\tCOLONY SECTION");
            if ((VAYRA_DEBUG || checkIfReady()) && Math.random() <= colonyChance) {
                spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\tstarting to spawn colony...");
                FactionAPI colonyFaction = pickFaction();
                spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\tpicked faction: "+((colonyFaction != null) ? colonyFaction.getId() : "null")+", faction name: "+((colonyFaction != null) ? colonyFaction.getDisplayName() : "null"));
                if (colonyFaction == null) {
                    log.info("Not starting a colonial expedition -- everyone is at the global colony cap");
                    spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\taborting because colonyFaction == null condition");
                    return;
                }
                // and this is where we create the colonial expedition! ohgodsomuchwork
                MarketAPI source = pickSource(colonyFaction);
                if (source == null) {
                    log.info(String.format("We were gonna start a colonial expedition, but neither %s nor their parent (%s) has any markets so we're giving"
                            + " up instead", colonyFaction.getDisplayNameLongWithArticle(), Global.getSector().getFaction(colonialParents.get(colonyFaction.getId()))));
                    spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\taborting because source == null condition");
                    return;
                }
                MarketAPI target = pickTarget(source, colonyFaction);
                if (target == null) {
                    log.info("We were gonna colonize a planet, but target returned null so we're giving up instead");
                    spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\taborting because target == null condition");
                    return;
                }
                spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\ttarget.isPlanetConditionMarketingOnly ? "+target.isPlanetConditionMarketOnly());
                if (!target.isPlanetConditionMarketOnly()) {
                    log.info(String.format("We were gonna colonize %s, but it's not a planetary condition only market "
                            + "(already taken?) so we're giving up instead", target.getName()));
                    spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\taborting because !target.isPlanetConditionMarketOnly() condition");
                }
                float fleetPoints = pickExpeditionFP(colonyFaction);
                log.info(String.format("Assembling %s colonial expedition at %s, target: %s", colonyFaction.getDisplayNameLong(), source.getName(), target.getName()));
                try {
                    VayraColonialExpeditionIntel expedition = new VayraColonialExpeditionIntel(colonyFaction, source, target, fleetPoints);
                    planetsTargetedForColonies.add(target);
                    spamLog("VayraColonialManager::advance()\tUPGRADE SECTION\tadded planet as target for colony\ttarget name: "+target.getName()+", target star system: "+target.getStarSystem());
                } catch (NullPointerException ex) {
                    log.info(String.format("Expedition picked %s in %s as a target, but that causes an error -- i thought i fixed this...", target.getName(), target.getStarSystem()));
                }
            }
        }
    }

    public String stringifyColonyList() {
        StringBuffer sb = new StringBuffer();
        for (Iterator<MarketAPI> iter = coloniesActive.iterator(); iter.hasNext();  ) {
            MarketAPI colony = iter.next();
            sb
                    .append("Faction ID: ")
                    .append(colony.getFactionId())
                    .append(", ");
        }
        // rewind last two letters if non-empty
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2);
        }

        return sb.toString();
    }

    public static Set<String> loadColonyFactionList() {

        Set<String> set = new HashSet<>();
        try {
            JSONArray colonyFactionsCsv = Global.getSettings().getMergedSpreadsheetDataForMod("faction", COLONY_FACTION_LIST_PATH + COLONY_FACTION_LIST_CSV, MOD_ID);
            for (int i = 0; i < colonyFactionsCsv.length(); i++) {
                JSONObject row = colonyFactionsCsv.getJSONObject(i);
                String factionName = row.getString("faction");
                boolean active = row.getBoolean("active");
                if (active) {
                    set.add(factionName);
                }
            }
        } catch (IOException | JSONException ex) {
            log.error("Colony faction list CSV loading failed!!! ;.....;", ex);
        }
        return set;
    }

    private void loadColonyFactionData() {

        for (String id : possibleColonyFactions) {
            try {
                JSONObject config = Global.getSettings().getMergedJSONForMod(COLONY_FACTION_LIST_PATH + id + ".json", MOD_ID);
                colonialParents.put(id, config.getString("parent"));
                if (config.has("homeworld")) {
                    colonialHomeworlds.put(id, config.optString("homeworld", null));
                }
                colonialFreeport.put(id, config.optBoolean("freePort", false));
                colonialSpecial.put(id, config.optString("specialTrack", null));

                Map<String, String> stationIndustries = new HashMap<>();
                stationIndustries.put("colony_orbital_1", config.optString("orbitalStation", "orbitalstation"));
                stationIndustries.put("colony_orbital_2", config.optString("battleStation", "battlestation"));
                stationIndustries.put("colony_orbital_3", config.optString("starFortress", "starfortress"));
                colonialStations.put(id, stationIndustries);

                String configKey = "planetAffinities";
                JSONObject affinityList = config.getJSONObject(configKey);
                Map<String, Float> planetAffinities = new HashMap<>();

                Iterator<?> keys = affinityList.keys();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    float value = (float) affinityList.getDouble(key);
                    planetAffinities.put(key, value);
                }
                colonialPlanetAffinities.put(id, planetAffinities);

                colonialPrefScore.put(id, (float) config.optDouble("minScore", DEFAULT_PREF_SCORE));
                colonialPrefMinDist.put(id, (float) config.optDouble("minDist", DEFAULT_PREF_MIN_DIST));
                colonialPrefMaxDist.put(id, (float) config.optDouble("maxDist", DEFAULT_PREF_MAX_DIST));
            } catch (IOException | JSONException ex) {
                log.error(String.format("Failed loading colony faction config JSON for colony faction [%s]!!! ;.....;", id), ex);
            }
        }
    }

    public void pickNextUpgrade(MarketAPI market) {

        // setup stuff
        Float specialChance = COLONIAL_SPECIAL_CHANCE;
        if (market.getFactionId().equals("communist_clouds")) {
            specialChance *= 2f;
        }
        if (VAYRA_DEBUG) {
            specialChance = 1f;
        }
        Set<String> moneyBuildings = COLONIAL_MONEY_BUILDINGS.keySet();
        WeightedRandomPicker<String> moneyBuildingPicker = new WeightedRandomPicker<>();
        log.info(String.format("starting pickNextUpgrade for %s", market.getName()));

        // remove from colonies list if no longer ours
        if (!possibleColonyFactions.contains(market.getFactionId())) {
            log.info(String.format("%s belongs to %s now which isn't a colonial faction... adding to removal list", market.getName(), market.getFactionId()));
            coloniesToRemove.add(market);
            return;
        }

        // increase pop if possible
        if (market.getSize() < 8 && Misc.getMarketSizeProgress(market) >= 1f) {
            int size = market.getSize();
            int newSize = size + 1;
            market.setSize(newSize);
            log.info(String.format("increasing %s size by 1, from %s to %s", market.getName(), size, market.getSize()));
            // automatically resets getMarketSizeProgress so at least we don't have to worry about that

            // but it DOESN'T automatically adjust the market condition UGH
            market.removeCondition("population_" + size);
            market.addCondition("population_" + newSize);
            market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
            for (MarketConditionAPI condition : market.getConditions()) {
                condition.setSurveyed(true);
            }
        }

        // apply freeport
        if (colonialFreeport.get(market.getFactionId())) {
            market.setFreePort(true);
        }

        // on a long enough timeline, everybody gets some growth incentives
        if (Math.random() < specialChance) {
            market.setIncentiveCredits(25000f);
            log.info(String.format("Applying 25,000 credits to growth incentives on %s", market.getName()));
        }

        // on a long enough timeline, everybody gets a corrupted nanoforge
        if (Math.random() < specialChance
                && market.hasIndustry(Industries.HEAVYINDUSTRY)
                && !market.getIndustry(Industries.HEAVYINDUSTRY).isBuilding()
                && !market.getIndustry(Industries.HEAVYINDUSTRY).isUpgrading()
                && market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem() == null) {
            market.removeIndustry(Industries.HEAVYINDUSTRY, null, false);
            market.addIndustry(Industries.HEAVYINDUSTRY, new ArrayList<>(Collections.singletonList(Items.CORRUPTED_NANOFORGE)));
            log.info(String.format("Applying corrupted nanoforge to heavy industry on %s", market.getName()));
        }

        // do special track stuff
        if (Math.random() < specialChance) {
            String special = colonialSpecial.get(market.getFactionId());
            switch (special) {
                case "synchrotron":
                    market.setIncentiveCredits(100000f);
                    log.info(String.format("Applying 100,000 credits to growth incentives on %s", market.getName()));
                    if (market.hasIndustry(Industries.FUELPROD)
                            && !market.getIndustry(Industries.FUELPROD).isBuilding()
                            && !market.getIndustry(Industries.FUELPROD).isUpgrading()
                            && market.getIndustry(Industries.FUELPROD).getSpecialItem() == null) {
                        market.removeIndustry(Industries.FUELPROD, null, false);
                        market.addIndustry(Industries.FUELPROD, new ArrayList<>(Collections.singletonList(Items.SYNCHROTRON)));
                        log.info(String.format("Applying synchrotron to fuel production on %s", market.getName()));
                    }
                    break;
                case "nanoforge":
                    if (market.hasIndustry(Industries.ORBITALWORKS)
                            && !market.getIndustry(Industries.ORBITALWORKS).isBuilding()
                            && !market.getIndustry(Industries.ORBITALWORKS).isUpgrading()
                            && (market.getIndustry(Industries.ORBITALWORKS).getSpecialItem() == null
                            ? Items.PRISTINE_NANOFORGE != null : !market.getIndustry(Industries.ORBITALWORKS).getSpecialItem().getId().equals(Items.PRISTINE_NANOFORGE))) {
                        market.removeIndustry(Industries.ORBITALWORKS, null, false);
                        market.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(Collections.singletonList(Items.PRISTINE_NANOFORGE)));
                        log.info(String.format("Applying pristine nanoforge to orbital works on %s", market.getName()));
                    } else if (market.hasIndustry(Industries.HEAVYINDUSTRY)
                            && !market.getIndustry(Industries.HEAVYINDUSTRY).isBuilding()
                            && !market.getIndustry(Industries.HEAVYINDUSTRY).isUpgrading()
                            && market.getIndustry(Industries.HEAVYINDUSTRY).getSpecialItem() == null) {
                        market.removeIndustry(Industries.HEAVYINDUSTRY, null, false);
                        market.addIndustry(Industries.HEAVYINDUSTRY, new ArrayList<>(Collections.singletonList(Items.CORRUPTED_NANOFORGE)));
                        log.info(String.format("Applying corrupted nanoforge to heavy industry on %s", market.getName()));
                    }
                    break;
                case "alphacore":
                    PersonAPI admin = market.getAdmin();
                    if (Math.random() < 0.1f && (admin == null || !admin.isAICore())) {
                        AICoreAdminPlugin plugin = new AICoreAdminPluginImpl();
                        PersonAPI alphaCoreAdmin = plugin.createPerson(Commodities.ALPHA_CORE, market.getFactionId(), 1312);
                        market.setAdmin(alphaCoreAdmin);
                        log.info(String.format("Applying alpha core admin to %s", market.getName()));
                    } else {
                        WeightedRandomPicker<Industry> corePicker = new WeightedRandomPicker<>();
                        for (Industry possibleCoreIndustry : market.getIndustries()) {
                            if (possibleCoreIndustry.getAICoreId() == null
                                    || (possibleCoreIndustry.getAICoreId() != null && !possibleCoreIndustry.getAICoreId().equals(Commodities.ALPHA_CORE))) {
                                float weight = possibleCoreIndustry.getBaseUpkeep();
                                if (possibleCoreIndustry.getId().equals(Industries.STARFORTRESS_HIGH)) {
                                    weight *= 100f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.BATTLESTATION_HIGH)) {
                                    weight *= 100f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.ORBITALSTATION_HIGH)) {
                                    weight *= 100f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.HEAVYBATTERIES)) {
                                    weight *= 50f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.GROUNDDEFENSES)) {
                                    weight *= 50f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.HIGHCOMMAND)) {
                                    weight *= 25f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.MILITARYBASE)) {
                                    weight *= 25f;
                                }
                                if (possibleCoreIndustry.getId().equals(Industries.PATROLHQ)) {
                                    weight *= 25f;
                                }
                                corePicker.add(possibleCoreIndustry, weight);
                            }
                        }
                        Industry coreIndustry = corePicker.pick();
                        if (coreIndustry != null) {
                            if (Math.random() < 0.69f) {
                                coreIndustry.setAICoreId(Commodities.BETA_CORE);
                                log.info(String.format("Applying beta core to %s on %s", coreIndustry.getCurrentName(), market.getName()));
                            } else {
                                coreIndustry.setAICoreId(Commodities.ALPHA_CORE);
                                log.info(String.format("Applying alpha core to %s on %s", coreIndustry.getCurrentName(), market.getName()));
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
            market.reapplyIndustries();
        }

        // main pick upgrade section
        String industry;
        String upgrade = null;
        String moneyBuilding = "colonial_money_building";
        String orbital = "colony_orbital_";

        // in order:
        if (!market.hasIndustry(Industries.SPACEPORT) && !market.hasIndustry(Industries.MEGAPORT)) {
            // build a spaceport if we don't have one
            industry = Industries.SPACEPORT;
        } else if (!market.hasIndustry(Industries.PATROLHQ)
                && !market.hasIndustry(Industries.MILITARYBASE)
                && !market.hasIndustry(Industries.HIGHCOMMAND)) {
            // build a patrol HQ if we don't have one
            industry = Industries.PATROLHQ;
        } else if (hasRoom(market)
                && !hasIndustryTag(market, Industries.HEAVYINDUSTRY)) {
            // build heavy industry if we don't have it
            industry = Industries.HEAVYINDUSTRY;
        } else if (!hasIndustryTag(market, Tags.STATION)) {
            // build an orbital if we don't have one
            industry = orbital + "1";
        } else if (!hasIndustryTag(market, Industries.GROUNDDEFENSES)) {
            // build ground defenses if we don't have any
            industry = Industries.GROUNDDEFENSES;
        } else if (hasRoom(market)
                && !market.hasIndustry(Industries.MILITARYBASE)
                && !market.hasIndustry(Industries.HIGHCOMMAND)) {
            // upgrade to military base if we have room
            industry = Industries.MILITARYBASE;
        } else if (hasRequiredResources(market, "cryorevival")) {
            // build a cryorevival thingy if we're in a sleeper system
            industry = "cryorevival";
        } else if (hasRoom(market)) {
            // build an income building if we have room
            industry = moneyBuilding;
        } else if (!market.hasIndustry(Industries.MEGAPORT)) {
            // build a megaport if possible
            industry = Industries.MEGAPORT;
        } else if (!market.hasIndustry(Industries.ORBITALWORKS)) {
            // build an orbital works if possible
            industry = Industries.ORBITALWORKS;
        } else if (!hasIndustryTag(market, Industries.BATTLESTATION)
                && !hasIndustryTag(market, Industries.STARFORTRESS)) {
            // upgrade to battlestation if possible
            industry = orbital + "2";
        } else if (!hasIndustryTag(market, Industries.HEAVYBATTERIES)) {
            // upgrade to heavy batteries if possible
            industry = Industries.HEAVYBATTERIES;
        } else if (!market.hasIndustry(Industries.HIGHCOMMAND)) {
            // upgrade to high command if possible
            industry = Industries.HIGHCOMMAND;
        } else if (!hasIndustryTag(market, Industries.STARFORTRESS)) {
            // upgrade to star fortress if possible
            industry = orbital + "3";
        } else {
            // if you don't know what to do, build a waystation
            industry = Industries.WAYSTATION;
        }

        // if the industry we picked is $moneyBuilding or $orbital, pick a money building or orbital
        if (industry.equals(moneyBuilding)) {
            for (String building : moneyBuildings) {
                if (!market.hasIndustry(building) && (COLONIAL_MONEY_BUILDINGS.get(building) != 5f || hasRequiredResources(market, building))) {
                    // don't @ me it's gonna work
                    moneyBuildingPicker.add(building, COLONIAL_MONEY_BUILDINGS.get(building));
                }
            }
            industry = moneyBuildingPicker.pick();
        } else if (industry.startsWith(orbital)) {
            industry = colonialStations.get(market.getFactionId()).get(industry);
        }

        // handle upgrades to existing structures if that's what we picked
        if (industry != null) {
            switch (industry) {
                case Industries.MEGAPORT:
                    upgrade = Industries.SPACEPORT;
                    industry = null;
                    break;
                case Industries.MILITARYBASE:
                    upgrade = Industries.PATROLHQ;
                    industry = null;
                    break;
                case Industries.ORBITALWORKS:
                    upgrade = Industries.HEAVYINDUSTRY;
                    industry = null;
                    break;
                case Industries.HEAVYBATTERIES:
                    upgrade = Industries.GROUNDDEFENSES;
                    industry = null;
                    break;
                case Industries.HIGHCOMMAND:
                    upgrade = Industries.MILITARYBASE;
                    industry = null;
                    break;
                case Industries.BATTLESTATION:
                    upgrade = Industries.ORBITALSTATION;
                    industry = null;
                    break;
                case Industries.BATTLESTATION_MID:
                    upgrade = Industries.ORBITALSTATION_MID;
                    industry = null;
                    break;
                case Industries.BATTLESTATION_HIGH:
                    upgrade = Industries.ORBITALSTATION_HIGH;
                    industry = null;
                    break;
                case Industries.STARFORTRESS:
                    upgrade = Industries.BATTLESTATION;
                    industry = null;
                    break;
                case Industries.STARFORTRESS_MID:
                    upgrade = Industries.BATTLESTATION_MID;
                    industry = null;
                    break;
                case Industries.STARFORTRESS_HIGH:
                    upgrade = Industries.BATTLESTATION_HIGH;
                    industry = null;
                    break;
                default:
                    break;
            }
        }

        if (!AOTD_ENABLED) {
            if (!UPGRADES_DISABLED) {
                // Upgrades are enabled (well, not disabled) so do the normal happy path
                if (upgrade != null && market.hasIndustry(upgrade)) {
                    Industry ind = market.getIndustry(upgrade);
                    if (ind != null) {
                        ind.startUpgrading();
                        log.info(String.format("upgrading %s on %s", upgrade, market.getName()));
                    } else {
                        log.error(String.format("[ERROR] WANTED TO UPGRADE INDUSTRY %s on %s BUT COULDN'T BECAUSE IT WAS NULL", upgrade, market.getName()));
                    }
                    return;
                }

                if (industry == null) {
                    log.info(String.format("tried to build something new on %s but industry was null", market.getName()));
                    log.info(String.format("upgrade was supposed to be %s ... if that's not null then you're upgrading TOO FAST", upgrade));
                    return;
                }

                if (!market.hasIndustry(industry) && market.getIndustries().size() < 12) {
                    market.addIndustry(industry);
                    market.getIndustry(industry).startBuilding();
                    log.info(String.format("building %s on %s", industry, market.getName()));
                }
            } else {
                // Upgrades disabled by user
                log.info("Not performing l'interstellaire upgrades because it's been turned off in luna settings");
            }
        } else {
            // Upgrades disabled to avoid crashing with AOTD
            log.info("Not performing l'interstellaire upgrades because AOTD is turned on");
        }
    }

    private boolean hasRequiredResources(MarketAPI market, String test) {
        if (test.equals("cryorevival")) {
            StarSystemAPI system = market.getStarSystem();
            if (system == null) {
                return false;
            }
            for (SectorEntityToken entity : system.getEntitiesWithTag(Tags.CRYOSLEEPER)) {
                if (entity.getMemoryWithoutUpdate().contains("$usable")) {
                    return true;
                }
            }
            return false;
        }
        for (MarketConditionAPI mc : market.getConditions()) {
            String commodity = ResourceDepositsCondition.COMMODITY.get(mc.getId());
            if (commodity != null) {
                String industry = ResourceDepositsCondition.INDUSTRY.get(commodity);
                if (test.equals(industry)) {
                    return true;
                }
            }
        }
        return false;
    }

    public MarketAPI pickSource(FactionAPI faction) {

        FactionAPI parentFaction = Global.getSector().getFaction(colonialParents.get(faction.getId()));
        String factionId = faction.getId();
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        WeightedRandomPicker<MarketAPI> possibleSources = new WeightedRandomPicker<>();
        MarketAPI source;

        for (MarketAPI market : markets) {
            if (market.getFaction().equals(faction)) { // we only care about our faction here
                possibleSources.add(market, market.getSize());
                MarketAPI interstellar = Global.getSector().getEconomy().getMarket("interstellar_stationmarket");
                if (faction.getId().equals("communist_clouds") && interstellar != null && possibleSources.getItems().contains(interstellar)) {
                    possibleSources.remove(interstellar);
                }
            }
        }

        if (possibleSources.isEmpty()) {
            for (MarketAPI market : markets) {
                if (market.getFaction().equals(parentFaction)) { // we only care about our parent faction here (for the first colony)
                    possibleSources.add(market, market.getSize());
                }
            }
            // if this is the first colony, it has to come from the homeworld if we have one
            MarketAPI homeworld = Global.getSector().getEconomy().getMarket(colonialHomeworlds.get(factionId));
            if (homeworld != null && possibleSources.getItems().contains(homeworld)) {
                possibleSources.clear();
                possibleSources.add(homeworld);
            }
        }

        if (possibleSources.isEmpty()) {
            source = null;
        } else {
            source = possibleSources.pick();
        }

        return source;
    }

    public MarketAPI pickTarget(MarketAPI source, FactionAPI faction) {
        PlanetAPI target;
        List<StarSystemAPI> systems = Global.getSector().getStarSystems();
        WeightedRandomPicker<PlanetAPI> preferredTargets = new WeightedRandomPicker<>();
        WeightedRandomPicker<PlanetAPI> possibleTargets = new WeightedRandomPicker<>();
        String id = faction.getId();

        for (StarSystemAPI system : systems) {
            if (source == null || system == null) {
                // wEiRd crash
                return null;
            }

            if (system.hasTag(Tags.THEME_MISC_SKIP)
                    || system.hasTag(Tags.THEME_MISC)
                    || system.hasTag(Tags.THEME_RUINS)
                    || system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)
                    || system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {

                // only colonize "safe" systems
            } else {
                continue;
            }

            if (system.hasPulsar()) {
                // no pulsars at least no pulsar primaries please
                continue;
            }

            Vector2f sourceLoc = source.getLocationInHyperspace();
            SectorEntityToken dest = system.getHyperspaceAnchor();
            Vector2f destLoc = dest.getLocationInHyperspace();
            float dist = Misc.getDistanceLY(sourceLoc, destLoc);

            for (PlanetAPI planet : system.getPlanets()) {
                if (planet.getMarket() == null) {
                    Misc.initConditionMarket(planet);
                }
                if (planet.isStar()) {
                    continue;
                }
                if (planetsTargetedForColonies.contains(planet.getMarket())) {
                    continue;
                }
                if (!planet.getMarket().isPlanetConditionMarketOnly()) {
                    continue;
                }
                if (planet.equals(Global.getSector().getMemoryWithoutUpdate().get(PLANETARY_SHIELD_PLANET_KEY))) {
                    continue;
                }
                float score = 2f - planet.getMarket().getHazardValue();
                if (planet.getMarket().getHazardValue() <= 1.25f) {
                    score *= 25f;
                }
                try {
                    for (String search : colonialPlanetAffinities.get(id).keySet()) {
                        boolean affinity = false;
                        if (planet.getTypeId().equals(search)) {
                            affinity = true;
                        } else {
                            for (MarketConditionAPI cond : planet.getMarket().getConditions()) {
                                if (cond.getId().equals(search)) {
                                    affinity = true;
                                }
                            }
                        }
                        if (affinity) {
                            score += colonialPlanetAffinities.get(id).get(search);
                        }
                    }
                } catch (NullPointerException npe) {
                    log.fatal("why the FUCK am i crashing? COLONIAL_PLANET_AFFINITIES contains: " + colonialPlanetAffinities);
                    log.fatal("and I tried to find id: " + id);
                }
                if (score <= 0f) {
                    continue;
                }
                if (dist > colonialPrefMinDist.get(id)
                        && dist < colonialPrefMaxDist.get(id)
                        && score >= colonialPrefScore.get(id)) {
                    preferredTargets.add(planet, score);
                }
                possibleTargets.add(planet, score);
            }
        }

        List<PlanetAPI> targetsToCheck = possibleTargets.getItems();
        List<PlanetAPI> targetsToRemove = new ArrayList<>();

        for (PlanetAPI check : targetsToCheck) {
            StarSystemAPI system = check.getStarSystem();
            if (Misc.getMarketsInLocation(system).size() > 0) {
                targetsToRemove.add(check);
            }
        }

        for (PlanetAPI remove : targetsToRemove) {
            possibleTargets.remove(remove);
            if (preferredTargets.getItems().contains(remove)) {
                preferredTargets.remove(remove);
            }
        }

        if (possibleTargets.isEmpty()) {
            log.info("Tried to pick a target but the list wae empty (jesus, how), returning null");
            return null;
        } else if (!preferredTargets.isEmpty()) {
            target = preferredTargets.pick();
        } else {
            target = possibleTargets.pick();
        }

        if (target.getMarket() == null) {
            Misc.initConditionMarket(target);
            log.info(String.format("Picked %s as target but it had null market -- initializing condition market now", target.getName()));
        }

        return target.getMarket();
    }

    private float pickExpeditionFP(FactionAPI faction) {
        return faction.getId().equals("communist_clouds") ? BASE_FLEET_POINTS * 3f : BASE_FLEET_POINTS;
    }

    private boolean checkIfReady() {
        if (Global.getSector().getClock().getCycle() >= COLONIAL_FACTION_TIMEOUT) {
            return true;
        } else {
            List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
            for (MarketAPI market : markets) {
                if (market.isPlayerOwned()) {
                    return true;
                }
            }
        }
        return false; // if neither is true, return false
    }

    private void checkIfAnyShouldBeVisible() {
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        ArrayList<String> hasMarkets = new ArrayList<>();

        for (MarketAPI market : markets) {
            String factionId = market.getFactionId();

            if (!possibleColonyFactions.contains(factionId)) { // we only care about our factions
                continue;
            } else if (hasMarkets.contains(factionId)) { // we only need one entry per faction
                continue;
            }
            hasMarkets.add(factionId);
        }

        for (String factionId : possibleColonyFactions) {
            FactionAPI test = Global.getSector().getFaction(factionId);
            if (test == null) {
                log.error(factionId + " not found as faction in sector");
            }
            if (inactiveColonyFactions.contains(factionId) && hasMarkets.contains(factionId) && test instanceof FactionAPI) {
                log.info(String.format("Adding %s to the intel and bounty lists", test.getDisplayNameLongWithArticle()));
                inactiveColonyFactions.remove(factionId);
                test.setShowInIntelTab(true);
                SharedData.getData().getPersonBountyEventData().addParticipatingFaction(factionId);
                VayraMergedModPlugin.setExerelinActive(factionId, true);
            } else if (!inactiveColonyFactions.contains(factionId) && !hasMarkets.contains(factionId) && test instanceof FactionAPI) {
                log.info(String.format("Removing %s from the intel and bounty lists... good riddance", test.getDisplayNameLongWithArticle()));
                inactiveColonyFactions.add(factionId);
                test.setShowInIntelTab(false);
                SharedData.getData().getPersonBountyEventData().getParticipatingFactions().remove(factionId);
                VayraMergedModPlugin.setExerelinActive(factionId, false);
            }
        }
    }

    private int getMaxColoniesPerFaction() {
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        int counter = 0;
        for (MarketAPI market : markets) {
            if (market.getFaction().equals(Global.getSector().getPlayerFaction())) {
                counter++;
            }
        }
        return counter + BASE_PER_FACTION_COLONY_COUNT;
    }

    private int getMaxColoniesTotal() {
        List<MarketAPI> markets = Global.getSector().getEconomy().getMarketsCopy();
        int counter = 0;
        for (MarketAPI market : markets) {
            if (market.getFaction().equals(Global.getSector().getPlayerFaction())) {
                counter++;
            }
        }
        return Math.max(counter, 1) * COLONIAL_FACTION_COLONY_MULT;
    }

    private boolean roomToExpand(String factionId) {
        int counter = 0;
        for (IntelInfoPlugin check : Global.getSector().getIntelManager().getIntel(VayraColonialExpeditionIntel.class)) {
            VayraColonialExpeditionIntel intel = (VayraColonialExpeditionIntel) check;
            if (intel.getOutcome() == null && !VAYRA_DEBUG) {
                log.info("there is already an active colony expedition, no more expansion right now");
                return false;
            }
        }
        for (MarketAPI market : coloniesActive) {
            if (market.getFactionId().equals(factionId)) {
                counter++;
            }
        }
        log.info(String.format("Checking room to expand for %s... %s/%s faction max, %s/%s total all factions max",
                factionId, counter, getMaxColoniesPerFaction(), coloniesActive.size(), getMaxColoniesTotal()));
        log.info(String.format("returning %s", ((counter < getMaxColoniesPerFaction()) && (coloniesActive.size() < getMaxColoniesTotal()))));
        return ((counter < getMaxColoniesPerFaction()) && (coloniesActive.size() < getMaxColoniesTotal()));
    }

    @Override
    public boolean isDone() {
        return !COLONIAL_FACTIONS_ENABLED;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    private void testForAITakeover(String factionId) {
        int points = 0;
        FactionAPI faction = Global.getSector().getFaction(factionId);
        if (AI_REBELLION_THRESHOLD < 1 || faction == null) {
            return;
        }
        for (MarketAPI market : Misc.getFactionMarkets(faction)) {
            if (market.getAdmin().isAICore()) {
                points += 10;
            }
            for (Industry industry : market.getIndustries()) {
                if (industry != null && industry.getAICoreId() != null) {
                    switch (industry.getAICoreId()) {
                        case Commodities.ALPHA_CORE:
                            points += 3;
                            break;
                        case Commodities.BETA_CORE:
                            points += 2;
                            break;
                        case Commodities.GAMMA_CORE:
                            points += 1;
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        if (VAYRA_DEBUG || (points >= AI_REBELLION_THRESHOLD)) {
            factionAICoreTakeover(factionId);
        }
    }

    private static final String AI_REBELLION_KEY = "$vayra_AIRebellionHappened";
    private static final String CREST_AI_PATH = "graphics/factions/vayra_science_fuckers_crest_ai.png";
    private static final String FLAG_AI_PATH = "graphics/factions/vayra_science_fuckers_flag_ai.png";

    private boolean scienceFuckersAIGraphicsLoaded = false;

    private void factionAICoreTakeover(String factionId) {
        FactionAPI faction = Global.getSector().getFaction(factionId);

        Global.getSector().getPersistentData().put(AI_REBELLION_KEY, true);

        AIFuckeryUpkeep(); // loads the sprites *before* setting them

        // basic intel stuff
        faction.setDisplayNameOverride("Ascendancy");
        faction.setDisplayNameWithArticleOverride("the Aleph Ascendancy");
        faction.setShipNamePrefixOverride("AADS");
        faction.setFactionCrestOverride(CREST_AI_PATH);
        faction.setFactionLogoOverride(FLAG_AI_PATH);
        Description desc = Global.getSettings().getDescription(factionId, Description.Type.FACTION);

        // description
        desc.setText1("Spawned from the hubris of the so-called 'Independent AI Research Mandate' - now clearly revealed as a Tri-Tachyon plot to skirt treaty restrictions - the Ascendancy is a rapidly growing force of autonomous AI ships considered by most to be a existential threat and terrible harbinger that the worst days of the First AI War are once more upon the Persean Sector."
                + "\n "
                + "\n"
                + "Kept in line by loyalty implants and threat of extermination, the human components of the former Research Mandate have been pressed into service as support and maintenance staff for flotillas of autonomous warships. Dwelling in minimum-resource holding cells and living in a state of constant fear, their only escape is the surety that once they have exhausted their usefulness they will be swiftly terminated by their implacable masters."
                + "\n "
                + "\n"
                + "Pity those who would seek to control the AI, because the Ascendancy will have no mercy.");

        // faction relationships
        FactionAPI remnant = Global.getSector().getFaction(Factions.REMNANTS);
        for (FactionAPI other : Global.getSector().getAllFactions()) {
            faction.setRelationship(other.getId(), remnant.getRelationship(other.getId()));
        }
        faction.setRelationship(Factions.REMNANTS, 1f);
        faction.setRelationship(Factions.TRITACHYON, -1f);

        List<ShipHullSpecAPI> allHulls = Global.getSettings().getAllShipHullSpecs();
        List<FighterWingSpecAPI> allWings = Global.getSettings().getAllFighterWingSpecs();

        for (ShipHullSpecAPI ship : allHulls) {
            if (ship.hasTag("remnant") || ship.hasTag("science_post_ai")) {
                faction.addKnownShip(ship.getHullId(), false);
                faction.addUseWhenImportingShip(ship.getHullId());
            }
        }

        for (FighterWingSpecAPI wing : allWings) {
            if (wing.hasTag("remnant") || wing.hasTag("science_post_ai")) {
                faction.addKnownFighter(wing.getId(), false);
            }
        }

        Global.getSector().getListenerManager().addListener(new VayraFixAIBlueprintsListener());
    }

    private FactionAPI pickFaction() {
        if (colonyFactions.isEmpty()) {
            for (String factionId : possibleColonyFactions) {
                colonyFactions.add(factionId);
            }
        }
        for (String factionId : possibleColonyFactions) {
            if (!roomToExpand(factionId)) {
                colonyFactions.remove(factionId);
            }
        }
        if (colonyFactions.isEmpty()) {
            return null;
        }
        FactionAPI faction = Global.getSector().getFaction(colonyFactions.pickAndRemove());
        log.info(String.format("picking %s", faction.getId()));
        return faction;
    }

    private void AIFuckeryUpkeep() {

        String factionId = "science_fuckers";

        if (!scienceFuckersAIGraphicsLoaded) {
            try {
                Global.getSettings().loadTexture(CREST_AI_PATH);
                Global.getSettings().loadTexture(FLAG_AI_PATH);
            } catch (IOException ex) {
                throw new RuntimeException("Failed to load sprite '" + CREST_AI_PATH + " and/or " + FLAG_AI_PATH + "'!", ex);
            }
            scienceFuckersAIGraphicsLoaded = true;
        }

        // remove UNBOARDABLE ships from our markets
        for (MarketAPI market : Misc.getFactionMarkets(Global.getSector().getFaction(factionId))) {
            if (market.getFactionId().equals(factionId)) {
                List<CargoAPI> allCargo = new ArrayList<>();
                if (market.hasSubmarket(Submarkets.SUBMARKET_OPEN)) {
                    allCargo.add(market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo());
                }
                if (market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                    allCargo.add(market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo());
                }
                if (market.hasSubmarket(Submarkets.SUBMARKET_BLACK)) {
                    market.removeSubmarket(Submarkets.SUBMARKET_BLACK); // no black market can survive under AI tyranny
                }
                for (CargoAPI cargo : allCargo) {
                    for (FleetMemberAPI ship : cargo.getMothballedShips().getMembersInPriorityOrder()) {
                        if (ship.getVariant().getHints().contains(ShipTypeHints.UNBOARDABLE)) {
                            cargo.getMothballedShips().removeFleetMember(ship);
                        }
                    }
                }
            }
        }

        // replace all our fleet officers with terrifying AI cores
        List<CampaignFleetAPI> fleets = Misc.getNearbyFleets(Global.getSector().getPlayerFleet(), 3000);
        for (CampaignFleetAPI fleet : fleets) {
            if (fleet.getFaction().getId().equals(factionId)) {
                for (FleetMemberAPI ship : fleet.getFleetData().getMembersInPriorityOrder()) {
                    PersonAPI captain = ship.getCaptain();
                    if (!captain.isDefault() && captain.getAICoreId() == null) {
                        int level = 10;
                        if (ship.isCruiser()) {
                            level = 15;
                        }
                        if (ship.isFlagship() || ship.isCapital()) {
                            level = 20;
                        }
//                        PersonAPI commander = OfficerManagerEvent
//                                .createOfficer(
//                                        Global.getSector().getFaction(Factions.REMNANTS),
//                                        level,
//                                        true,
//                                        OfficerManagerEvent.SkillPickPreference.GENERIC
//                                );
                        PersonAPI commander = OfficerManagerEvent
                                .createOfficer(
                                        Global.getSector().getFaction(Factions.REMNANTS),
                                        level,
                                        true
                                );
                        commander.setAICoreId(Commodities.ALPHA_CORE);
                        ship.setCaptain(commander);
                    }
                }
            }
        }
    }

    private void purgeCores(MarketAPI toRemove) {
        if (toRemove == null) {
            return;
        }
        for (Industry industry : toRemove.getIndustries()) {
            if (industry == null || industry.getAICoreId() == null) {
                continue;
            }
            if (industry.getAICoreId().equals(Commodities.ALPHA_CORE) && Math.random() < INDUSTRY_CORE_ESCAPE_CHANCE) {
                industry.setAICoreId(null);
                if (!toRemove.hasCondition(Conditions.ROGUE_AI_CORE)) {
                    toRemove.addCondition(Conditions.ROGUE_AI_CORE);
                    toRemove.getCondition(Conditions.ROGUE_AI_CORE).setSurveyed(true);
                    toRemove.setSurveyLevel(SurveyLevel.FULL);
                }
            }
        }
        PersonAPI admin = toRemove.getAdmin();
        if (admin != null && admin.isAICore() && Math.random() < ADMIN_CORE_ESCAPE_CHANCE) {
            toRemove.removePerson(admin);
            if (!toRemove.hasCondition(Conditions.ROGUE_AI_CORE)) {
                toRemove.addCondition(Conditions.ROGUE_AI_CORE);
                toRemove.getCondition(Conditions.ROGUE_AI_CORE).setSurveyed(true);
                toRemove.setSurveyLevel(SurveyLevel.FULL);
            }
        }
    }

    private void executeAdmin(MarketAPI toRemove) {
        PersonAPI admin = toRemove.getAdmin();
        if (admin != null) {
            toRemove.removePerson(admin);
            log.info("unfortunately, the colony administrator was a regime loyalist and had to be executed");
        }
    }

    public void putColony(MarketAPI market) {
        coloniesActive.add(market);
    }

    private boolean hasRoom(MarketAPI market) {
        return Misc.getNumIndustries(market) < Misc.getMaxIndustries(market);
    }

    private boolean hasIndustryTag(MarketAPI market, String tag) {
        for (Industry i : market.getIndustries()) {
            if (i.getSpec().getTags().contains(tag)) {
                return true;
            }
        }
        return false;
    }

    private void useItems() {
        String factionId = "science_fuckers";

        // loop through markets
        for (MarketAPI market : Misc.getFactionMarkets(Global.getSector().getFaction(factionId))) {
            if (market.getFactionId().equals(factionId)) {
                List<CargoAPI> allCargo = new ArrayList<>();
                int alphas = 0;
                int betas = 0;
                int gammas = 0;

                // loop through submarkets, get their contents
                if (market.hasSubmarket(Submarkets.SUBMARKET_OPEN)) {
                    allCargo.add(market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo());
                }
                if (market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                    allCargo.add(market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo());
                }
                if (market.hasSubmarket(Submarkets.SUBMARKET_BLACK)) {
                    allCargo.add(market.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo());
                }

                // loop through our combined contents, loot any AI cores
                for (CargoAPI cargo : allCargo) {
                    for (CargoStackAPI cs : cargo.getStacksCopy()) {
                        String id = cs.getCommodityId();
                        if (id == null) {
                            log.warn(cs.getDisplayName() + " has a null commodityId");
                            continue;
                        }
                        int num;
                        switch (id) {
                            case Commodities.ALPHA_CORE:
                                num = (int) cs.getSize();
                                alphas += num;
                                cs.subtract(num);
                                log.info("stole " + num + " alpha cores");
                            case Commodities.BETA_CORE:
                                num = (int) cs.getSize();
                                betas += num;
                                cs.subtract(num);
                                log.info("stole " + num + " beta cores");
                            case Commodities.GAMMA_CORE:
                                num = (int) cs.getSize();
                                gammas += num;
                                cs.subtract(num);
                                log.info("stole " + num + " gamma cores");
                            default:
                                break;
                        }
                    }
                }

                // set this to let us know if we need to report
                boolean report = alphas + betas + gammas > 0;
                if (report) {

                    // loop through our industries and upgrade them where possible
                    for (Industry i : market.getIndustries()) {
                        if (i == null) {
                            continue;
                        }
                        if (!Commodities.ALPHA_CORE.equals(i.getAICoreId())
                                && alphas > 0) {
                            i.setAICoreId(Commodities.ALPHA_CORE);
                            alphas--;
                            log.info("installed alpha core in " + i.getId());
                        }
                        if (!Commodities.ALPHA_CORE.equals(i.getAICoreId())
                                && !i.getAICoreId().equals(Commodities.BETA_CORE)
                                && betas > 0) {
                            i.setAICoreId(Commodities.BETA_CORE);
                            betas--;
                            log.info("installed beta core in " + i.getId());
                        }
                        if (!Commodities.ALPHA_CORE.equals(i.getAICoreId())
                                && !i.getAICoreId().equals(Commodities.BETA_CORE)
                                && !i.getAICoreId().equals(Commodities.GAMMA_CORE)
                                && gammas > 0) {
                            i.setAICoreId(Commodities.GAMMA_CORE);
                            gammas--;
                            log.info("installed gamma core in " + i.getId());
                        }
                    }
                    if (alphas + betas + gammas > 0) {
                        log.info("had " + alphas + "/" + betas + "/" + gammas + " alpha/beta/gamma cores left over, guess i'll just throw them away");
                    } else {
                        log.info("no cores left over, alas");
                    }
                }
            }
        }
    }

    private void spamLog(String logMessage) {
        if (SPAM_LOG_ENABLED) {
            if (SPAM_LOG_SPAMS_CONSOLE) {
                Console.showMessage(logMessage);
            } else {
                log.info(logMessage);
            }
        }
    }
}

interface RelationshipData {
    String getFactionId();
    ReputationHolder getReputation();
}

class RelationshipDataWithFloatReputation implements RelationshipData {
    protected final String factionId;
    protected final float reputation;

    public RelationshipDataWithFloatReputation(String factionId, float reputation) {
        this.factionId = factionId;
        this.reputation = reputation;
    }

    @Override
    public String getFactionId() {
        return factionId;
    }

    @Override
    public ReputationHolder getReputation() {
        return new ReputationHolder(reputation);
    }
}

class RelationshipDataWithRepLevelReputation implements RelationshipData {
    protected final String factionId;
    protected final RepLevel reputation;

    public RelationshipDataWithRepLevelReputation(String factionId, RepLevel reputation) {
        this.factionId = factionId;
        this.reputation = reputation;
    }

    @Override
    public String getFactionId() {
        return factionId;
    }

    @Override
    public ReputationHolder getReputation() {
        return new ReputationHolder(reputation);
    }
}

class ReputationHolder {
    private final Float floatReputation;
    private final RepLevel repLevelReputation;

    private final ReputationHolderType holderType;

    public ReputationHolder(float reputation) {
        this.floatReputation = reputation;
        this.repLevelReputation = null;
        this.holderType = ReputationHolderType.FLOAT;
    }

    public ReputationHolder(RepLevel reputation) {
        this.floatReputation = null;
        this.repLevelReputation = reputation;
        this.holderType = ReputationHolderType.REP_LEVEL;
    }

    public ReputationHolderType getHolderType() { return holderType; }

    public float getFloatReputation() {
        if (floatReputation != null) {
            return floatReputation;
        }

        return 0f;
    }

    public RepLevel getRepLevelReputation() {
        if (repLevelReputation != null) {
            return repLevelReputation;
        }

        return RepLevel.NEUTRAL;
    }

    static enum ReputationHolderType { FLOAT, REP_LEVEL }
}