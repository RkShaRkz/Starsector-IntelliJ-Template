package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.intel.BaseEventManager;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyManager;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.VayraMergedModPlugin.*;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

import static data.scripts.VayraMergedModPlugin.*;
import static data.scripts.campaign.intel.VayraPersonBountyIntel.getSharedData;

public class VayraPersonBountyManager extends BaseEventManager {

    public static final String KEY = "$kadur_personBountyManager";
    public static Logger log = Global.getLogger(VayraPersonBountyManager.class);

    public static final String BOUNTY_DATA_PATH = "data/config/vayraBounties/";
    public static Map<String, RareBountyFlagshipData> RARE_FLAGSHIPS = new HashMap<>();

    public static ArrayList<String> JERK_KILL_WORDS = new ArrayList<>();
    public static ArrayList<String> JERK_DESCS = new ArrayList<>();
    public static ArrayList<String> JERK_TYPES = new ArrayList<>();
    public static ArrayList<String> JERK_TITLES = new ArrayList<>();
    public static ArrayList<String> JERK_REASONS = new ArrayList<>();
    public static ArrayList<String> JERK_CRIMES = new ArrayList<>();
    public static ArrayList<String> JERK_VICTIMS = new ArrayList<>();
    public static ArrayList<String> JERK_FLEETS = new ArrayList<>();

    public transient boolean checkedForFuckedUpParticipants = false;

    public static JSONObject KadurBountyShit() throws IOException, JSONException {
        return Global.getSettings().getMergedJSONForMod(BOUNTY_DATA_PATH + "bounty_strings.json", MOD_ID);
    }

    public static ArrayList<String> loadFromBountyJSON(String key) {
        try {
            JSONArray array = KadurBountyShit().getJSONArray(key);
            ArrayList<String> asList = new ArrayList<>(array.length());
            for (int i = 0; i < array.length(); i++) {
                asList.add(array.getString(i));
            }
            return asList;
        } catch (IOException | JSONException e) {
            log.info("bounty_shit.json loading ended" + e.getMessage());
            return new ArrayList<>(Collections.singletonList("SOMETHING HAS GONE TERRIBLY WRONG"));
        }
    }

    public static VayraPersonBountyManager getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraPersonBountyManager) test;
    }

    public VayraPersonBountyManager() {
        super();
        Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
    }

    private synchronized void checkForFuckedUpParticipants() {
        checkedForFuckedUpParticipants = true;
        for (Iterator<String> iter = getSharedData().getParticipatingFactions().iterator(); iter.hasNext(); ) {
            String factionId = iter.next();
            if (Global.getSector().getFaction(factionId) == null) {
                Global.getSector().getCampaignUI().addMessage(factionId + " is an invalid bounty participant, please yell at its mod author", Color.red);
                Global.getSector().getCampaignUI().addMessage(factionId + " bounties will not function until they fix it", Color.red);
                Global.getSector().getCampaignUI().addMessage("i could have crashed the game right here, i SHOULD have crashed the game right here", Color.red);
                Global.getSector().getCampaignUI().addMessage("god knows you'd fuckin' deserve it", Color.red);
                iter.remove();
            }
        }
    }

    public static class RareBountyFlagshipData {

        public String variantId;
        public float spawnWeight;
        public Set<FactionAPI> allowedFactions;
        public String shipSource;
        public int fleetPoints;
        public int diff;

        public RareBountyFlagshipData(String variantId, float spawnWeight, Set<FactionAPI> allowedFactions, String shipSource) {
            this.variantId = variantId;
            this.spawnWeight = spawnWeight;
            this.allowedFactions = allowedFactions;
            this.shipSource = shipSource;
            try {
                this.fleetPoints = Global.getSettings().getVariant(variantId).getHullSpec().getFleetPoints();
            } catch (NullPointerException ex) {
                this.fleetPoints = 0;
                log.warn("couldn't get fleet points for " + variantId);
                log.warn(ex);
            }
        }

        int setDiff(int diff) {
            this.diff = diff;
            return diff;
        }
    }

    public void reload() {
        log.info("loading rare_flagships.csv");
        loadRareFlagships();
    }

    // loader for CSV file
    public static void loadRareFlagships() {

        JSONArray spreadsheet;
        try {
            spreadsheet = Global.getSettings().getMergedSpreadsheetDataForMod("bounty", BOUNTY_DATA_PATH + "rare_flagships.csv", MOD_ID);

            if (VAYRA_DEBUG) {
                log.info("looping through rare_flagships.csv");
            }
            for (int i = 0; i < spreadsheet.length(); i++) {
                if (VAYRA_DEBUG) {
                    log.info("getting row " + i);
                }
                JSONObject row = spreadsheet.getJSONObject(i);

                // get variant ID
                if (VAYRA_DEBUG) {
                    log.info("getting variant id");
                }
                String variantId = row.getString("variant");

                // build list of allowed factions (gotta do this separately because its fucky to make a string into a list)
                if (VAYRA_DEBUG) {
                    log.info("getting allowed factions");
                }
                String factionListString = row.getString("factions");
                List<String> factionList = new ArrayList<>(Arrays.asList(factionListString.split("\\s*(,\\s*)+")));
                Set<FactionAPI> allowedFactions = new HashSet<>();
                if (factionList.isEmpty() || factionList.get(0).isEmpty()) {
                    allowedFactions = null;
                } else {
                    for (String id : factionList) {
                        FactionAPI faction = Global.getSector().getFaction(id);
                        if (faction != null) {
                            allowedFactions.add(faction);
                        }
                    }
                }

                // create RareBountyFlagshipData object
                if (VAYRA_DEBUG) {
                    log.info("creating RareBountyFlagshipData");
                }
                RareBountyFlagshipData data = new RareBountyFlagshipData(
                        variantId,
                        (float) row.getDouble("weight"),
                        allowedFactions,
                        row.getString("source")
                );
                RARE_FLAGSHIPS.put(
                        variantId,
                        data
                );

            }
        } catch (IOException | JSONException ex) {
            log.warn(ex);
            log.info("rare_flagships.csv loading ended");
        }
    }

    public static RareBountyFlagshipData pickRareFlagship(FleetMemberAPI currFlag, FactionAPI flagFaction) {

        int baseFp = currFlag.getFleetPointCost();
        List<RareBountyFlagshipData> allowed = new ArrayList<>();
        WeightedRandomPicker<RareBountyFlagshipData> flags = new WeightedRandomPicker<>();

        int min = 9999;
        int max = 0;
        for (String candidate : RARE_FLAGSHIPS.keySet()) {
            RareBountyFlagshipData data = RARE_FLAGSHIPS.get(candidate);
            if (data.allowedFactions.contains(flagFaction) && data.fleetPoints >= baseFp) {
                allowed.add(data);
                int fleetPoints = data.fleetPoints;
                int diff = data.setDiff(fleetPoints - baseFp);
                int absDiff = Math.abs(diff);
                if (absDiff < min) {
                    min = absDiff;
                }
                if (absDiff > max) {
                    max = absDiff;
                }
            }
        }

        for (RareBountyFlagshipData candidate : allowed) {
            int absDiff = Math.abs(candidate.diff);
            float diffMult = 1f - ((absDiff - min) / (max - min == 0 ? 0.0000001f : max - min));
            float weight = candidate.spawnWeight * diffMult;
            flags.add(candidate, weight);
        }

        if (flags.isEmpty()) {
            return null;
        } else {
            // we add a null at 1.0 weight just to make the spawn weights work
            // and to reduce incidence of way-out-of-band flagship picks
            // because like, otherwise if a 0.01 weight 10000FP ship is the only one for the faction...
            // you're gonna see it every time
            flags.add(null, 1f);
            return flags.pick();
        }
    }

    @Override
    protected int getMinConcurrent() {
        return Global.getSettings().getInt("minPersonBounties");
    }

    @Override
    protected int getMaxConcurrent() {
        return VAYRA_DEBUG ? 100 : Global.getSettings().getInt("maxPersonBounties");
    }

    @Override
    public void advance(float amount) {

        if (JERK_KILL_WORDS.isEmpty()) {
            JERK_KILL_WORDS = loadFromBountyJSON("jerkKillWords");
        }
        if (JERK_DESCS.isEmpty()) {
            JERK_DESCS = loadFromBountyJSON("jerkDescs");
        }
        if (JERK_TYPES.isEmpty()) {
            JERK_TYPES = loadFromBountyJSON("jerkTypes");
        }
        if (JERK_TITLES.isEmpty()) {
            JERK_TITLES = loadFromBountyJSON("jerkTitles");
        }
        if (JERK_REASONS.isEmpty()) {
            JERK_REASONS = loadFromBountyJSON("jerkReasons");
        }
        if (JERK_CRIMES.isEmpty()) {
            JERK_CRIMES = loadFromBountyJSON("jerkCrimes");
        }
        if (JERK_VICTIMS.isEmpty()) {
            JERK_VICTIMS = loadFromBountyJSON("jerkVictims");
        }
        if (JERK_FLEETS.isEmpty()) {
            JERK_FLEETS = loadFromBountyJSON("jerkFleets");
        }
        if (RARE_FLAGSHIPS.isEmpty()) {
            reload();
        }

        if (!checkedForFuckedUpParticipants) {
            checkForFuckedUpParticipants();
        }

        if (PIRATE_BOUNTY_MODE.equals(PirateMode.NEVER) || (PIRATE_BOUNTY_MODE.equals(PirateMode.SOMETIMES)
                && Global.getSector().getFaction(Factions.PIRATES).isHostileTo(Factions.PLAYER))) {
            if (!Global.getSector().hasScript(PersonBountyManager.class)) {
                Global.getSector().addScript(new PersonBountyManager());
                log.info("Re-adding the vanilla bounty manager, alas");
            }
            if (getSharedData().isParticipating(Factions.PIRATES)) {
                getSharedData().getParticipatingFactions().remove(Factions.PIRATES);
                log.info("Fucking off for now, since player is a bootlicker and hates freedom");
            }
            return;
        }

        if (!getSharedData().isParticipating(Factions.PIRATES)) {
            SharedData.getData().getPersonBountyEventData().addParticipatingFaction(Factions.PIRATES);
        }

        super.advance(amount);

        if (PIRATE_BOUNTY_MODE.equals(PirateMode.BOTH)) {
            return;
        }

        if (amount == 6.66f) {
            for (EveryFrameScript s : PersonBountyManager.getInstance().getActive()) {
                ((PersonBountyIntel) s).endImmediately();
                Global.getSector().removeScript(s);
                log.info("Killing a vanilla bounty, pirates run the show now");
                EveryFrameScript jerk = createEvent();
                if (jerk != null) {
                    addActive(jerk);
                }
            }
        }

        if ((PIRATE_BOUNTY_MODE.equals(PirateMode.ALWAYS) || PIRATE_BOUNTY_MODE.equals(PirateMode.SOMETIMES)) && Global.getSector().hasScript(PersonBountyManager.class)) {
            Global.getSector().removeScript(PersonBountyManager.getInstance());
            log.info("Killing the vanilla bounty manager, RIP losers");
        }
    }

    @Override
    public float getIntervalRateMult() {
        return super.getIntervalRateMult();
    }

    @Override
    public EveryFrameScript createEvent() {

        if (getInstance().getActiveCount() >= getInstance().getMaxConcurrent()) {
            log.info(String.format("Bounty hunting? Fuck off, we're full. [%s/%s]", VayraPersonBountyManager.getInstance().getActiveCount(), VayraPersonBountyManager.getInstance().getMaxConcurrent()));
            return null;
        }

        VayraPersonBountyIntel intel = new VayraPersonBountyIntel();
        if (intel.isDone()) {
            intel = null;
        }

        return intel;
    }

}
