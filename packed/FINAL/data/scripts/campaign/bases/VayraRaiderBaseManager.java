package data.scripts.campaign.bases;

import java.util.Random;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import static com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager.CHECK_DAYS;
import static com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager.CHECK_PROB;
import data.scripts.campaign.bases.VayraRaiderBaseIntel.RaiderBaseTier;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.VayraMergedModPlugin.MOD_ID;
import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;
import static data.scripts.VayraMergedModPlugin.JSONArrayToStringArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VayraRaiderBaseManager extends BaseEventManager {

    public static final String KEY = "$Vayra_RaiderBaseManager";
    public static Logger log = Global.getLogger(VayraRaiderBaseManager.class);

    // minimum Fleet Points for non-event raids, in order of size -- max = this * 2
    public static final Float FP_SCOUT = 15f;
    public static final Float FP_BOUNTY_HUNTER = FP_SCOUT * 2f;
    public static final Float FP_PRIVATEER = FP_BOUNTY_HUNTER * 2f;
    public static final Float FP_PATROL = FP_PRIVATEER * 2f;

    // spawn weights for non-event raids, in order of size 
    public static final Float SPAWN_WEIGHT_SCOUT = 4f;
    public static final Float SPAWN_WEIGHT_BOUNTY_HUNTER = 4f;
    public static final Float SPAWN_WEIGHT_PRIVATEER = 3f;
    public static final Float SPAWN_WEIGHT_PATROL = 1f;

    // average Fleet Points for event raids, in order of size 
    // will be modified +/- 0-25%, then +/- 0-25% again ... wtf alex lol
    public static final Float RAID_TIER1_FP = 100f;
    public static final Float RAID_TIER2_FP = 200f;
    public static final Float RAID_TIER3_FP = 300f;
    public static final Float RAID_TIER4_FP = 600f;
    public static final Float RAID_TIER5_FP = 900f;
    // these are the default values for pirates! wow!

    // these bases use the same min/max as pirates (set in settings.json)
    // BUT they track their count independently
    // i.e. these don't count towards number of pirate bases
    // and pirate bases don't count towards number of these
    public static class RaiderData {

        public final String raiderFactionId;
        public boolean onlySpawnWhenVisibleInIntelTab;
        public boolean enabled;
        protected final boolean isFirebase;
        protected final boolean freePort;
        protected final boolean spawnNonEventFleets;
        protected final String raiderActivityConditionId = "vayra_raider_activity";
        protected final String raiderActivityString;
        protected final String raiderActivityIntelIcon;
        protected final String raiderBaseIntelIcon;
        protected final String raiderBaseBarEventScript;
        protected final String raidingActionText;
        protected final String raidingStanddownText;
        protected final Map<String, Float> raidTargetWeights;
        protected final int raiderBaseSize;
        protected final Map<String, Float> raiderBaseTypes;
        protected final List<String> raiderBaseConditionsAndIndustries;
        protected final List<String> raiderBaseSubmarkets;
        protected final List<String> raiderBaseTypeNames;

        public RaiderData(
                String raiderFactionId,
                boolean onlySpawnWhenVisibleInIntelTab,
                boolean enabled,
                boolean isFirebase,
                boolean freePort,
                boolean spawnNonEventFleets,
                String raiderActivityString,
                String raiderActivityIntelIcon,
                String raiderBaseIntelIcon,
                String raiderBaseBarEventScript,
                String raidingActionText,
                String raidingStanddownText,
                Map<String, Float> raidTargetsWeight,
                int raiderBaseSize,
                Map<String, Float> raiderBaseTypes,
                List<String> raiderBaseConditionsAndIndustries,
                List<String> raiderBaseSubmarkets,
                List<String> raiderBaseTypeNames
        ) {
            this.raiderFactionId = raiderFactionId;
            this.onlySpawnWhenVisibleInIntelTab = onlySpawnWhenVisibleInIntelTab;
            this.enabled = enabled;
            this.isFirebase = isFirebase;
            this.freePort = freePort;
            this.spawnNonEventFleets = spawnNonEventFleets;
            this.raiderActivityString = raiderActivityString;
            this.raiderActivityIntelIcon = raiderActivityIntelIcon;
            this.raiderBaseIntelIcon = raiderBaseIntelIcon;
            this.raiderBaseBarEventScript = raiderBaseBarEventScript;
            this.raidingActionText = raidingActionText;
            this.raidingStanddownText = raidingStanddownText;
            this.raidTargetWeights = raidTargetsWeight;
            this.raiderBaseSize = raiderBaseSize;
            this.raiderBaseTypes = raiderBaseTypes;
            this.raiderBaseConditionsAndIndustries = raiderBaseConditionsAndIndustries;
            this.raiderBaseSubmarkets = raiderBaseSubmarkets;
            this.raiderBaseTypeNames = raiderBaseTypeNames;
        }
    }

    public static String RAIDER_LIST_PATH = "data/config/vayraRaiders/";
    public static Map<String, RaiderData> RAIDERS = new HashMap<>();
    public static Map<String, VayraRaiderBaseIntel> ACTIVE = new HashMap<>();

    @Override
    public void advance(float amount) {
        if (RAIDERS.isEmpty()) {
            loadData();
        }
        if (VAYRA_DEBUG) {
            amount *= 10f;
        }
        super.advance(amount);
    }

    public static void loadData() {
        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("faction", RAIDER_LIST_PATH + "raider_factions.csv", MOD_ID);
            List<String> raiders = new ArrayList<>();

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);
                String factionId = row.getString("faction"); // get faction ID
                boolean active = row.getBoolean("active");
                if (active) {
                    raiders.add(factionId);
                }
            }

            for (String id : raiders) {
                try {
                    log.info("loading JSON for " + id);
                    JSONObject config = Global.getSettings().getMergedJSONForMod(RAIDER_LIST_PATH + id + ".json", MOD_ID);

                    String raidTargetsWeightKey = "raidTargetWeights";
                    Map<String, Float> raidTargetsWeight = new HashMap<>();
                    if (config.has(raidTargetsWeightKey)) {
                        JSONObject list = config.getJSONObject(raidTargetsWeightKey);
                        Iterator<?> raidTargetsWeightKeys = list.keys();
                        while (raidTargetsWeightKeys.hasNext()) {
                            String key = (String) raidTargetsWeightKeys.next();
                            float value = (float) list.getDouble(key);
                            raidTargetsWeight.put(key, value);
                        }
                    }

                    String raiderBaseTypesKey = "raiderBaseTypes";
                    Map<String, Float> raiderBaseTypes = new HashMap<>();
                    if (config.has(raiderBaseTypesKey)) {
                        JSONObject raiderBaseTypesList = config.getJSONObject(raiderBaseTypesKey);
                        Iterator<?> raiderBaseTypesKeys = raiderBaseTypesList.keys();
                        while (raiderBaseTypesKeys.hasNext()) {
                            String key = (String) raiderBaseTypesKeys.next();
                            float value = (float) raiderBaseTypesList.getDouble(key);
                            raiderBaseTypes.put(key, value);
                        }
                    }

                    List<String> raiderBaseConditionsAndIndustries = new ArrayList<>(Arrays.asList(
                            "Frontier", "No Atmosphere", "Population", "Spaceport", "Military Base", "Orbital Works"));
                    if (config.has("raiderBaseConditionsAndIndustries")) {
                        raiderBaseConditionsAndIndustries = Arrays.asList(JSONArrayToStringArray(config.getJSONArray("raiderBaseConditionsAndIndustries")));
                        log.info("raiderBaseConditionsAndIndustries for " + id + ": " + raiderBaseConditionsAndIndustries);
                    }

                    List<String> raiderBaseSubmarkets = new ArrayList<>(Arrays.asList(
                            Submarkets.SUBMARKET_OPEN, Submarkets.SUBMARKET_BLACK, Submarkets.GENERIC_MILITARY));
                    if (config.has("raiderBaseSubmarkets")) {
                        raiderBaseSubmarkets = Arrays.asList(JSONArrayToStringArray(config.getJSONArray("raiderBaseSubmarkets")));
                        log.info("raiderBaseSubmarkets for " + id + ": " + raiderBaseSubmarkets);
                    }

                    List<String> raiderBaseTypeNames = new ArrayList<>(Arrays.asList(
                            "Fort", "Base", "Outpost"));
                    if (config.has("raiderBaseTypeNames")) {
                        raiderBaseTypeNames = Arrays.asList(JSONArrayToStringArray(config.getJSONArray("raiderBaseTypeNames")));
                    }

                    RaiderData data = new RaiderData(
                            id,
                            config.optBoolean("onlySpawnWhenVisibleInIntelTab", false),
                            config.optBoolean("startEnabled", true),
                            config.optBoolean("isFirebase", false),
                            config.optBoolean("freePort", true),
                            config.optBoolean("spawnNonEventFleets", false),
                            config.optString("raiderActivityString", Misc.ucFirst(Global.getSector().getFaction(id).getPersonNamePrefix() + "Raider Activity")),
                            config.optString("raiderActivityIntelIcon", "graphics/icons/markets/pirates.png"),
                            config.optString("raiderBaseIntelIcon", "graphics/icons/markets/vayra_generic_base.png"),
                            config.optString("raiderBaseBarEventScript", "data.scripts.campaign.bases.VayraRaiderBaseBarEvent"),
                            config.optString("raidingActionText", "raiding"),
                            config.optString("raidingStanddownText", "standing down"),
                            raidTargetsWeight,
                            config.optInt("raiderBaseSize", 3),
                            raiderBaseTypes,
                            raiderBaseConditionsAndIndustries,
                            raiderBaseSubmarkets,
                            raiderBaseTypeNames
                    );
                    RAIDERS.put(id, data);
                    boolean spawnNonEventFleets = data.spawnNonEventFleets;
                    if (spawnNonEventFleets) {
                        log.info("RaiderNonEventFleets? Never heard of 'em.");
                    }
                } catch (IOException | JSONException ex) {
                    log.error(String.format("Failed loading raider faction JSON for[%s]!!! ;.....;", id), ex);
                }
            }
        } catch (IOException | JSONException ex) {
            log.error("raider_factions.csv loading failed!!! ;.....;");
        }
    }

    public static VayraRaiderBaseManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraRaiderBaseManager) test;
    }

    protected long start = 0;
    protected float extraDays = 0;

    protected int numDestroyed = 0;

    public VayraRaiderBaseManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
        start = Global.getSector().getClock().getTimestamp();
    }

    @Override
    protected int getMinConcurrent() {
        return Global.getSettings().getInt("minPirateBases");
    }

    @Override
    protected int getMaxConcurrent() {
        return Global.getSettings().getInt("maxPirateBases");
    }

    @Override
    protected float getBaseInterval() {
        return CHECK_DAYS;
    }

    protected Random randomForRaiderBaseEvent = new Random();

    private String pickFaction() {
        WeightedRandomPicker<String> factions = new WeightedRandomPicker<>();
        for (String id : RAIDERS.keySet()) {
            RaiderData data = RAIDERS.get(id);
            FactionAPI faction = Global.getSector().getFaction(id);
            if (data.enabled && (!data.onlySpawnWhenVisibleInIntelTab || faction.isShowInIntelTab())) {
                factions.add(id);
            }
        }
        return factions.pick();
    }

    @Override
    protected EveryFrameScript createEvent() {

        float roll = randomForRaiderBaseEvent.nextFloat();
        if (VAYRA_DEBUG) {
            roll = 1f;
        }
        if (this.isDone() || roll < CHECK_PROB) {
            if (VAYRA_DEBUG) {
                log.info("giving up, failed roll to spawn raider base: " + roll);
            }
            return null;
        }

        RaiderBaseTier tier = pickRaiderBaseTier();

        String factionId = pickFaction();
        if (factionId == null) {
            if (VAYRA_DEBUG) {
                log.info("giving up, picked null faction: " + factionId);
            }
            return null;
        }

        RaiderData data = RAIDERS.get(factionId);

        StarSystemAPI system = pickSystemForRaiderBase(data);
        if (system == null) {
            if (VAYRA_DEBUG) {
                log.info("giving up, picked null system");
            }
            return null;
        }

        VayraRaiderBaseIntel intel = new VayraRaiderBaseIntel(system, factionId, tier);
        if (intel.isDone()) {
            if (VAYRA_DEBUG) {
                log.info("giving up, intel said it was done even though i just made it");
            }
            return null;
        }

        if (VAYRA_DEBUG) {
            log.info("created raider base intel: " + intel.getName());
        }
        return intel;
    }

    public float getDaysSinceStart() {
        float days = Global.getSector().getClock().getElapsedDaysSince(start) + extraDays;
        if (Misc.isFastStartExplorer()) {
            days += 180f - 30f;
        } else if (Misc.isFastStart()) {
            days += 180f + 60f;
        }
        return days;
    }

    public float getExtraDays() {
        return extraDays;
    }

    public void setExtraDays(float extraDays) {
        this.extraDays = extraDays;
    }

    protected RaiderBaseTier pickRaiderBaseTier() {
        float days = getDaysSinceStart();

        days += numDestroyed * 200;

        WeightedRandomPicker<RaiderBaseTier> picker = new WeightedRandomPicker<>();

        if (days < 360) {
            picker.add(RaiderBaseTier.TIER_1_1MODULE, 10f);
            picker.add(RaiderBaseTier.TIER_2_1MODULE, 10f);
        } else if (days < 720f) {
            picker.add(RaiderBaseTier.TIER_2_1MODULE, 10f);
            picker.add(RaiderBaseTier.TIER_3_2MODULE, 10f);
        } else if (days < 1080f) {
            picker.add(RaiderBaseTier.TIER_3_2MODULE, 10f);
            picker.add(RaiderBaseTier.TIER_4_3MODULE, 10f);
        } else {
            picker.add(RaiderBaseTier.TIER_3_2MODULE, 10f);
            picker.add(RaiderBaseTier.TIER_4_3MODULE, 10f);
            picker.add(RaiderBaseTier.TIER_5_3MODULE, 10f);
        }

        return picker.pick();
    }

    protected StarSystemAPI pickSystemForRaiderBase(RaiderData data) {
        if (data.isFirebase) {
            return pickFirebaseTarget(data);
        }

        WeightedRandomPicker<StarSystemAPI> far = new WeightedRandomPicker<>(randomForRaiderBaseEvent);
        WeightedRandomPicker<StarSystemAPI> picker = new WeightedRandomPicker<>(randomForRaiderBaseEvent);

        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float days = Global.getSector().getClock().getElapsedDaysSince(system.getLastPlayerVisitTimestamp());
            if (days < 45f) {
                continue;
            }

            float weight = 0f;
            if (system.hasTag(Tags.THEME_MISC_SKIP)) {
                weight = 1f;
            } else if (system.hasTag(Tags.THEME_MISC)) {
                weight = 3f;
            } else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
                weight = 3f;
            } else if (system.hasTag(Tags.THEME_RUINS)) {
                weight = 5f;
            } else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                weight = 1f;
            }
            if (weight <= 0f) {
                continue;
            }

            float usefulStuff = system.getCustomEntitiesWithTag(Tags.OBJECTIVE).size()
                    + system.getCustomEntitiesWithTag(Tags.STABLE_LOCATION).size();
            if (usefulStuff <= 0) {
                continue;
            }

            if (Misc.hasPulsar(system)) {
                continue;
            }
            if (Misc.getMarketsInLocation(system).size() > 0) {
                continue;
            }

            float dist = system.getLocation().length();

            float distMult = 1f;

            if (dist > 36000f) {
                far.add(system, weight * usefulStuff * distMult);
            } else {
                picker.add(system, weight * usefulStuff * distMult);
            }
        }

        if (picker.isEmpty()) {
            picker.addAll(far);
        }

        return picker.pick();
    }

    protected StarSystemAPI pickFirebaseTarget(RaiderData data) {

        if (data == null) {
            return null;
        }

        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        FactionAPI faction = Global.getSector().getFaction(data.raiderFactionId);

        for (MarketAPI targetMarket : Global.getSector().getEconomy().getMarketsCopy()) {
            String targetFactionId = targetMarket.getFactionId();
            float repWeight = -faction.getRelationship(targetFactionId);
            float bonusWeight;

            if (data.raidTargetWeights == null || data.raidTargetWeights.isEmpty() || data.raidTargetWeights.get(targetFactionId) == null) {
                log.warn("data.raidTargetWeights was null or couldn't get weight for " + targetFactionId);
                bonusWeight = 1f;
            } else {
                bonusWeight = data.raidTargetWeights.get(targetFactionId);
            }

            if (repWeight <= 0 || bonusWeight <= 0) {
                continue;
            }

            if (data.raidTargetWeights.containsKey(targetFactionId)) {
                picker.add(targetMarket, targetMarket.getSize() * repWeight * bonusWeight);
            } else {
                picker.add(targetMarket, targetMarket.getSize() * repWeight);
            }
        }

        if (picker.isEmpty()) {
            return null;
        }

        StarSystemAPI newTarget = picker.pick().getStarSystem();

        return newTarget;
    }

    public int getNumDestroyed() {
        return numDestroyed;
    }

    public void setNumDestroyed(int numDestroyed) {
        this.numDestroyed = numDestroyed;
    }

    public void incrDestroyed() {
        numDestroyed++;
    }

}
