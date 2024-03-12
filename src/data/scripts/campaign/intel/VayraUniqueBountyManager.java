package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.util.IntervalUtil;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import static data.scripts.VayraMergedModPlugin.*;

public class VayraUniqueBountyManager extends BaseEventManager {

    public static class UniqueBountyData {

        protected final String bountyId;
        protected final int level; // character level
        protected final String rank; // must match a rank ID string from Ranks
        protected final String firstName; // string
        protected final String lastName; // string
        protected final String captainPersonality; // string, must match a personality id from Personalities
        protected final String fleetName; // string
        protected final String flagshipName; // string
        protected final Gender gender; // MALE / FEMALE
        protected final String bountyFactionId; // TARGET faction ID
        protected final String portrait; // portrait ID, must have been loaded by the game?
        protected final String greetingText;

        protected final boolean suppressIntel; // ignore rest of this section if true
        protected final String factionId; // ISSUER faction ID
        protected final Integer creditReward; // credit reward
        protected final float repReward; // rep reward with posting faction (-1 to 1, but put -100 to 100 as int in the file)
        protected final String intelText; // any formatting?

        protected final String flagshipVariantId; // variant ID
        protected final List<String> fleetVariantIds; // variant IDs
        protected final int minimumFleetFP; // additional faction ships will be added to fleet until its FP > this value
        protected final float playerFPScalingFactor; // additional faction ships will be added to fleet until its FP > player fleet's FP * this value
        protected final int maximumFleetFP; // no random faction ships will be added once the fleet is above this value. default Integer.MAX_VALUE
        // maximum FP overrides minimum and scaling, but not the flagship or handpicked variants

        protected final float chanceToAutoRecover; // does not affect normal recovery chance! default 1, i.e. 100%

        protected final List<String> specialItemRewards; // item IDs, mix and match any item types i'll work it out

        // if bounty not found in spentBounties, never spawn
        protected final List<String> neverSpawnUnlessBountiesCompleted;

        // if either set to true, never spawn if evaluates true
        protected final boolean neverSpawnWhenFactionHostile; // issuing faction
        protected final boolean neverSpawnWhenFactionNonHostile; // target faction

        // if any evaluate true, never spawn even if other conditions are met
        protected final int neverSpawnBeforeCycle; // cycle (year)
        protected final int neverSpawnBeforeLevel; // player character level
        protected final int neverSpawnBelowFleetPoints; // current fleet points

        protected VayraUniqueBountyIntel intel; // dynamic field to store the intel and check if we're dead

        public void setIntel(VayraUniqueBountyIntel intel) {
            if (intel == null) {
                return;
            }
            this.intel = intel;
        }

        public boolean isDead() {
            boolean isDead = (this.intel == null || this.intel.isDone() || this.intel.isEnded() || this.intel.isEnding());
            return isDead;
        }

        public boolean conditionsMet() {

            if (Global.getSector() == null) {
                log.error("Sector was null... what?");
                return false;
            }

            if (Global.getSector().getFaction(this.factionId) == null || Global.getSector().getFaction(this.bountyFactionId) == null) {
                log.warn(this.bountyId + " has invalid factionId or bountyFactionId");
                return false;
            }

            if (!Global.getSettings().doesVariantExist(this.flagshipVariantId)) {
                log.warn(this.bountyId + " has invalid flagship variant ID");
                return false;
            }

            if (this.neverSpawnUnlessBountiesCompleted != null && !this.neverSpawnUnlessBountiesCompleted.isEmpty()) {
                MemoryAPI memory = Global.getSector().getMemory();
                Object test = memory.get(BOUNTY_KEY);
                List spentBounties = test instanceof List ? (List) test : null;
                for (String prereq : this.neverSpawnUnlessBountiesCompleted) {
                    if (spentBounties == null
                            || !spentBounties.contains(prereq)
                            || VayraUniqueBountyManager.getInstance().hasCurrentBounty(prereq)) {
                        return false;
                    }
                }
            }

            if (!VAYRA_DEBUG && (this.neverSpawnWhenFactionHostile && Global.getSector().getFaction(this.factionId).isHostileTo(Factions.PLAYER))) {
                return false;
            }

            if (!VAYRA_DEBUG && (this.neverSpawnWhenFactionNonHostile && !Global.getSector().getFaction(this.bountyFactionId).isHostileTo(Factions.PLAYER))) {
                return false;
            }

            if (!VAYRA_DEBUG && (Global.getSector().getClock().getCycle() < this.neverSpawnBeforeCycle)) {
                return false;
            }

            if (!VAYRA_DEBUG && (Global.getSector().getPlayerStats().getLevel() < this.neverSpawnBeforeLevel)) {
                return false;
            }

            if (VAYRA_DEBUG) {
                return true;
            }

            return Global.getSector().getPlayerFleet().getFleetPoints() >= this.neverSpawnBelowFleetPoints;
        }

        public UniqueBountyData(
                String bountyId,
                int level,
                String rank,
                String firstName,
                String lastName,
                String fleetName,
                String flagshipName,
                String gender,
                String faction,
                String portrait,
                String greetingText,
                boolean suppressIntel,
                String postedByFaction,
                Integer creditReward,
                float repReward,
                String intelText,
                String flagshipVariantId,
                List<String> fleetVariantIds,
                int minimumFleetFP,
                float playerFPScalingFactor,
                int maximumFleetFP,
                float chanceToAutoRecover,
                List<String> specialItemRewards,
                List<String> neverSpawnUnlessBountiesCompleted,
                boolean neverSpawnWhenFactionHostile,
                boolean neverSpawnWhenFactionNonHostile,
                int neverSpawnBeforeCycle,
                int neverSpawnBeforeLevel,
                int neverSpawnBeforeFleetPoints,
                String captainPersonality
        ) {
            this.bountyId = bountyId;
            this.level = level;
            this.rank = rank;
            this.firstName = firstName;
            this.lastName = lastName;
            this.fleetName = fleetName;
            this.flagshipName = flagshipName;
            this.gender = Gender.valueOf(gender);
            this.bountyFactionId = faction;
            this.portrait = portrait;
            this.greetingText = greetingText;
            this.suppressIntel = suppressIntel;
            this.factionId = postedByFaction;
            this.creditReward = creditReward;
            this.repReward = repReward;
            this.intelText = intelText;
            this.flagshipVariantId = flagshipVariantId;
            this.fleetVariantIds = fleetVariantIds;
            this.minimumFleetFP = minimumFleetFP;
            this.playerFPScalingFactor = playerFPScalingFactor;
            this.maximumFleetFP = maximumFleetFP;
            this.chanceToAutoRecover = chanceToAutoRecover;
            this.specialItemRewards = specialItemRewards;
            this.neverSpawnUnlessBountiesCompleted = neverSpawnUnlessBountiesCompleted;
            this.neverSpawnWhenFactionHostile = neverSpawnWhenFactionHostile;
            this.neverSpawnWhenFactionNonHostile = neverSpawnWhenFactionNonHostile;
            this.neverSpawnBeforeCycle = neverSpawnBeforeCycle;
            this.neverSpawnBeforeLevel = neverSpawnBeforeLevel;
            this.neverSpawnBelowFleetPoints = neverSpawnBeforeFleetPoints;
            this.captainPersonality = captainPersonality;
        }
    }

    public static final String KEY = "$vayra_uniqueBountyManager";
    public static final String BOUNTY_KEY = "$vayra_uniqueBountiesSpent";
    public static Logger log = Global.getLogger(VayraUniqueBountyManager.class);
    public static String BOUNTY_LIST_PATH = "data/config/vayraBounties/unique_bounty_data.csv";

    public boolean loaded = false;
    private final Map<String, UniqueBountyData> bounties = new HashMap<>();
    private Map<String, VayraUniqueBountyIntel> currentBounties = new HashMap<>();
    private final IntervalUtil spawnCheckTimer = new IntervalUtil(45f, 90f);
    public static final float UNIQUE_BOUNTY_CHANCE = 0.69f;

    public VayraUniqueBountyManager() {
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    public static VayraUniqueBountyManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraUniqueBountyManager) test;
    }

    public boolean hasBounty(String id) {
        return bounties.containsKey(id);
    }

    public List<String> getBountiesList() {
        List<String> result = new ArrayList<>();
        result.addAll(bounties.keySet());
        return result;
    }

    @SuppressWarnings("unchecked")
    public List<String> getSpentBountiesList() {
        MemoryAPI memory = Global.getSector().getMemory();
        List<String> spentBounties = (List<String>) memory.get(BOUNTY_KEY);
        List<String> result = new ArrayList<>();
        if (spentBounties == null) {
            spentBounties = new ArrayList<>();
            memory.set(BOUNTY_KEY, spentBounties);
            result.add("unique spentBounties was null, adding empty list");
        } else {
            result.addAll(spentBounties);
        }
        return result;
    }

    public UniqueBountyData getBounty(String id) {
        return bounties.get(id);
    }

    public boolean hasCurrentBounty(String id) {
        return currentBounties.containsKey(id);
    }

    public void removeCurrentBounty(String id) {
        log.info("removing " + id + " from unique currentBounties");
        VayraUniqueBountyIntel intel = currentBounties.get(id);
        currentBounties.remove(id);
        log.info("unique currentBounties contains: " + currentBounties);
    }

    public List<String> getCurrentBountiesList() {
        List<String> result = new ArrayList<>();
        result.addAll(currentBounties.keySet());
        return result;
    }

    // loader for CSV file
    public void loadBounties() {

        try {
            JSONArray spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("bounty_id", BOUNTY_LIST_PATH, MOD_ID);

            for (int i = 0; i < spreadsheet.length(); i++) {
                JSONObject row = spreadsheet.getJSONObject(i);

                // get bounty ID
                String bountyId;
                if (row.has("bounty_id") && row.getString("bounty_id") != null && !row.getString("bounty_id").isEmpty()) {
                    bountyId = row.getString("bounty_id");
                    log.info("loading unique bounty " + bountyId);
                } else {
                    log.info("hit empty line, unique bounty loading ended");
                    continue;
                }

                // build list of variants in fleet (gotta do this separately because its fucky to make a string into a list)
                String fleetListString = row.optString("fleetVariantIds");
                List<String> fleetList = null;
                if (fleetListString != null) {
                    fleetList = new ArrayList<>(Arrays.asList(fleetListString.split("\\s*(,\\s*)+")));
                    if (fleetList.isEmpty() || fleetList.get(0).isEmpty()) {
                        fleetList = null;
                    }
                }

                // build list of item IDs
                String itemListString = row.optString("specialItemRewards");
                List<String> itemList = null;
                if (itemListString != null) {
                    itemList = new ArrayList<>(Arrays.asList(itemListString.split("\\s*(,\\s*)+")));
                    if (itemList.isEmpty() || itemList.get(0).isEmpty()) {
                        itemList = null;
                    }
                }

                // build list of prerequisite bounty IDs
                String prerequisiteBountiesString = row.optString("neverSpawnUnlessBountiesCompleted");
                List<String> prerequisiteBountiesList = null;
                if (prerequisiteBountiesString != null) {
                    prerequisiteBountiesList = new ArrayList<>(Arrays.asList(prerequisiteBountiesString.split("\\s*(,\\s*)+")));
                    if (prerequisiteBountiesList.isEmpty() || prerequisiteBountiesList.get(0).isEmpty()) {
                        prerequisiteBountiesList = null;
                    }
                }

                // create bountyData object
                try {
                    UniqueBountyData data = new UniqueBountyData(
                            bountyId,
                            row.getInt("level"),
                            row.getString("rank"),
                            row.getString("firstName"),
                            row.getString("lastName"),
                            row.getString("fleetName"),
                            row.getString("flagshipName"),
                            row.getString("gender"),
                            row.getString("faction"),
                            row.getString("portrait"),
                            row.getString("greetingText"),
                            row.getBoolean("suppressIntel"),
                            row.getString("postedByFaction"),
                            row.getInt("creditReward"),
                            (float) row.getInt("repReward") / 100f,
                            row.getString("intelText"),
                            row.getString("flagshipVariantId"),
                            fleetList,
                            row.getInt("minimumFleetFP"),
                            (float) row.getDouble("playerFPScalingFactor"),
                            row.optInt("maximumFleetFP", Integer.MAX_VALUE),
                            (float) row.optDouble("chanceToAutoRecover", 1.0),
                            itemList,
                            prerequisiteBountiesList,
                            row.getBoolean("neverSpawnWhenFactionHostile"),
                            row.getBoolean("neverSpawnWhenFactionNonHostile"),
                            row.getInt("neverSpawnBeforeCycle"),
                            row.getInt("neverSpawnBeforeLevel"),
                            row.getInt("neverSpawnBeforeFleetPoints"),
                            row.optString("captainPersonality", Personalities.AGGRESSIVE)
                    );
                    bounties.put(
                            bountyId,
                            data
                    );
                    log.info("loaded unique bounty id " + bountyId);
                } catch (JSONException ex) {
                    log.error("row with id " + bountyId + " is malformed, skipping");
                }
            }
        } catch (IOException | JSONException ex) {
            log.error("unique_bounty_data.csv loading crashed");
        }
    }

    @Override
    protected int getMinConcurrent() {
        return 0;
    }

    @Override
    protected int getMaxConcurrent() {
        return VAYRA_DEBUG ? 42069 : UNIQUE_BOUNTIES_MAX;
    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    public void reload() {
        loadBounties();
        loaded = true;
        log.info("loaded unique bounty data");
        log.info("unique bounty data list contains: " + bounties.keySet());
    }

    @Override
    public void advance(float amount) {

        // just return if option disabled
        if (!UNIQUE_BOUNTIES) {
            return;
        }

        // if we haven't loaded yet, load the bounties
        if (!loaded) {
            reload();
        }

        if (currentBounties == null) {
            currentBounties = new HashMap<>();
            log.warn("currentBounties was null, creating new map");
        }

        // advance the spawn timer
        float days = Global.getSector().getClock().convertToDays(amount);
        if (VAYRA_DEBUG) {
            days *= 30f;
        }
        spawnCheckTimer.advance(days);

        // and check if we should spawn any bounties
        float bountyChance = VAYRA_DEBUG ? 1f : UNIQUE_BOUNTY_CHANCE;
        if (spawnCheckTimer.intervalElapsed() && this.getActive().size() < getMaxConcurrent() && bounties.size() > 0) {

            log.info("unique bounty interval elapsed, " + this.getActive().size() + " unique bounties active of " + getMaxConcurrent());
            purgeBountiesIfNeeded();
            log.info("unique bounty data list contains: " + bounties.keySet());

            float roll = (float) Math.random();
            if (roll <= bountyChance) {

                VayraUniqueBountyIntel jerk = (VayraUniqueBountyIntel) createEvent();

                if (jerk != null) {
                    Global.getSector().addScript(jerk);
                } else {
                    log.info("failed to spawn unique bounty");
                    log.info("unique bounty data list contains: " + bounties.keySet());
                }
            } else {
                log.info("failed unique bounty roll (" + roll + " over " + UNIQUE_BOUNTY_CHANCE + ", needed to be under), not spawning one");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void purgeBountiesIfNeeded() {
        MemoryAPI memory = Global.getSector().getMemory();
        List<String> spentBounties = (List<String>) memory.get(BOUNTY_KEY);
        if (spentBounties == null) {
            spentBounties = new ArrayList<>();
            memory.set(BOUNTY_KEY, spentBounties);
            log.info("unique spentBounties was null, adding empty list");
        } else {
            log.info("unique spentBounties contains: " + spentBounties);
            for (String spent : spentBounties) {
                if (bounties.containsKey(spent)) {
                    bounties.remove(spent);
                    log.info("removed " + spent + " from unique bounties");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void spendBounty(String id) {
        MemoryAPI memory = Global.getSector().getMemoryWithoutUpdate();
        List<String> spentBounties = (List<String>) memory.get(BOUNTY_KEY);
        if (spentBounties == null) {
            spentBounties = new ArrayList<>();
            memory.set(BOUNTY_KEY, spentBounties);
            log.info("unique spentBounties was null, adding empty list");
        } else {
            log.info("unique spentBounties contains: " + spentBounties);
        }
        log.info("spending unique bounty " + id);
        if (!spentBounties.contains(id)) {
            spentBounties.add(id);
            log.info("unique spentBounties contains: " + spentBounties);
        } else {
            log.warn("unique spentBounties already contains " + id);
        }
    }

    @Override
    public float getIntervalRateMult() {
        return super.getIntervalRateMult();
    }

    public EveryFrameScript forceSpawn(String forceSpawn) {

        log.info("attempting to force-spawn unique bounty: " + forceSpawn);
        VayraUniqueBountyIntel intel = new VayraUniqueBountyIntel(forceSpawn);
        if (intel.isDone()) {
            intel = null;
        }

        if (intel != null) {
            addActive(intel);
            Global.getSector().addScript(intel);
            currentBounties.put(intel.bountyId, intel);
            log.info("unique currentBounties contains: " + currentBounties);
        }

        return intel;
    }

    @Override
    public EveryFrameScript createEvent() {

        VayraUniqueBountyIntel intel = new VayraUniqueBountyIntel();
        if (intel.isDone()) {
            intel = null;
        }

        if (intel != null) {
            addActive(intel);
            currentBounties.put(intel.bountyId, intel);
            log.info("unique currentBounties contains: " + currentBounties);
        }

        return intel;
    }
}
