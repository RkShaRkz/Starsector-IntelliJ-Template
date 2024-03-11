package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.DeliveryFailureConsequences;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.TriTachLoanIncentiveScript;
import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.VayraMergedModPlugin.BOUNTY_DURATION;
import static data.scripts.VayraMergedModPlugin.MOD_ID;
import static data.scripts.VayraMergedModPlugin.PLAYER_BOUNTIES;
import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VayraPlayerBountyIntel extends BaseIntelPlugin {

    public static Logger log = Global.getLogger(VayraPlayerBountyIntel.class);
    public static final String KEY = "$vayra_playerBountyManager";
    public static final String PLAYER_BOUNTY_FACTION_LIST_PATH = "data/config/vayraBounties/";
    public static final String PLAYER_BOUNTY_FACTION_CSV = "player_bounty_factions.csv";

    public VayraPlayerBountyListener listener;
    protected List<String> participants = new ArrayList<>();
    protected List<PlayerBountyData> sorted = new ArrayList<>(); // just used to display them in order
    protected Map<String, PlayerBountyData> bountiesToPost = new HashMap<>(); // factionId, bounty
    protected Map<String, PlayerBountyData> bountiesPosted = new HashMap<>(); // factionId, bounty
    protected IntervalUtil hunterDays = new IntervalUtil(30, 60);
    protected IntervalUtil updateDays = new IntervalUtil(7, 7);
    protected float timerMultBecauseOfCrimes = 0f;
    protected float previousCrimeMult = 0f;
    
    public boolean hasBounty() {
        return !bountiesPosted.isEmpty();
    }

    public static class PlayerBountyData implements Comparable {

        protected final FactionAPI postedByFaction;
        protected final float duration;

        protected int value;
        protected float elapsedDays;

        protected VayraPlayerBountyIntel intel;

        public PlayerBountyData(
                String factionId,
                int value
        ) {
            this.postedByFaction = Global.getSector().getFaction(factionId);
            this.duration = BOUNTY_DURATION;
            this.value = value;
            this.elapsedDays = 0f;
            this.intel = getInstance();
        }

        public void advance(float amount) {
            float days = Global.getSector().getClock().convertToDays(amount);
            elapsedDays += days;
        }

        public float getRemaining() {
            float remaining = duration - elapsedDays;
            if (remaining < 0f) {
                remaining = 0f;
            }
            return remaining;
        }

        public void addBounty(PlayerBountyData newBounty) {
            this.elapsedDays -= (BOUNTY_DURATION * 0.5f);
            if (this.elapsedDays < 0f) {
                this.elapsedDays = 0f;
            }
            this.value += (int) newBounty.value;
        }

        public void addValue(int credits) {
            this.value += credits;
        }

        public String getFactionId() {
            return this.postedByFaction.getId();
        }

        @Override
        public int compareTo(Object o) {
            PlayerBountyData other = (PlayerBountyData) o;
            return (int) Math.signum(this.value - other.value);
        }
    }

    public void loadParticipatingFactionList() {
        try {
            JSONArray participatingFactionsCsv = Global.getSettings().getMergedSpreadsheetDataForMod("faction", PLAYER_BOUNTY_FACTION_LIST_PATH + PLAYER_BOUNTY_FACTION_CSV, MOD_ID);
            for (int i = 0; i < participatingFactionsCsv.length(); i++) {
                JSONObject row = participatingFactionsCsv.getJSONObject(i);
                String factionName = row.getString("faction");
                boolean whitelist = row.getBoolean("whitelist");
                if (whitelist && !participants.contains(factionName)) {
                    participants.add(factionName);
                    log.info("added " + factionName + " to player-targeted bounty participants list");
                } else if (!whitelist && participants.contains(factionName)) {
                    participants.remove(factionName);
                    log.info("removed " + factionName + " from player-targeted bounty participants list");
                }
            }
        } catch (IOException | JSONException ex) {
            log.error("Player Bounty Participant list CSV loading failed!!! ;.....;", ex);
        }
    }

    // so now bountyValue doesn't actually have any effect on fleet strength, lol. it's just a number for fun
    // and for visually tracking how much people hate you (for your level) i guess
    // it still influences all the other stuff - hunter frequency and range, basically
    public int getBountyValue() {
        float base = Global.getSettings().getFloat("basePersonBounty");
        float perLevel = Global.getSettings().getFloat("personBountyPerLevel");
        int level = (int) Global.getSector().getPlayerStats().getLevel();
        // bounty levels are 1-10, player levels are 1-50, but I DON'T CARE this is more fun big numbers are fun
        int value = (int) (base + (perLevel * level));
        return value;
    }

    public static PersonBountyEventData getSharedData() {
        return SharedData.getData().getPersonBountyEventData();
    }

    public boolean isParticipating(String factionId) {
        boolean inSharedData = getSharedData().isParticipating(factionId);
        boolean inParticipantList = participants.contains(factionId);
        FactionAPI check = Global.getSector().getFaction(factionId);
        boolean hasMarkets = false;
        if (check != null) {
            hasMarkets = Misc.getFactionMarkets(Global.getSector().getFaction(factionId)).size() > 0;
        }
        return inSharedData && inParticipantList && hasMarkets;
    }

    @Override
    protected void advanceImpl(float amount) {

        Object memoryCheck = Global.getSector().getMemory().get(KEY);
        
        if (memoryCheck == null || !(memoryCheck instanceof VayraPlayerBountyIntel)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
            log.info("adding the player-targeted bounty key, pointing to this instance");
        }

        if (!PLAYER_BOUNTIES) {
            Global.getSector().getIntelManager().removeIntel(this);
            bountiesToPost.clear();
            bountiesPosted.clear();
            return;
        }

        if (!Global.getSector().getListenerManager().hasListener(listener)) {
            log.info("adding the player-targeted bounty posting listener");
            listener = new VayraPlayerBountyListener(true, this);
            Global.getSector().getListenerManager().addListener(listener);
        }

        if (participants.isEmpty()) {
            log.info("loading player-targeted bounty participants whitelists");
            loadParticipatingFactionList();
        }

        super.advanceImpl(amount);

        if (VAYRA_DEBUG) {
            amount *= 20f;
        }

        for (PlayerBountyData bounty : bountiesPosted.values()) {
            bounty.advance(amount);
        }
        float days = Misc.getDays(amount);
        updateDays.advance(days);
        if (updateDays.intervalElapsed()) {
            log.info("updating player-targeted bounties");
            updateBounties();
            if (Math.abs(timerMultBecauseOfCrimes - previousCrimeMult) >= 0.2f) {
                sendUpdateIfPlayerHasIntel(new Object(), false);
            } else if (timerMultBecauseOfCrimes != previousCrimeMult) {
                sendUpdateIfPlayerHasIntel(new Object(), true);
            }
            previousCrimeMult = timerMultBecauseOfCrimes;
        }

        hunterDays.advance(days * timerMultBecauseOfCrimes);
        if (hunterDays.intervalElapsed()) {
            log.info("sending a bounty hunter after the player");
            VayraPlayerBountyHunter hunter = new VayraPlayerBountyHunter(this);
            Global.getSector().addScript(hunter);
        }
    }

    public void addBounty(PlayerBountyData newBounty) {
        String factionId = newBounty.getFactionId();
        PlayerBountyData existingBounty = bountiesToPost.get(factionId);
        if (!isParticipating(factionId)) {
            log.info(factionId + " doesn't know how to post a bounty on the player, because they're idiots, but they would have if they could");
            return;
        }
        if (existingBounty != null) {
            existingBounty.addBounty(newBounty);
            log.info(factionId + " has updated their future bounty on the player's head, adding " + (newBounty.value * 0.5f) + " credits.");
        } else {
            bountiesToPost.put(factionId, newBounty);
            log.info(factionId + " has declared their intention to post a bounty of " + newBounty.value + " credits on the player's head.");
        }
    }

    private void updateBounties() {

        for (PlayerBountyData newBounty : bountiesToPost.values()) {
            String factionId = newBounty.getFactionId();
            PlayerBountyData existingBounty = bountiesPosted.get(factionId);
            if (existingBounty != null && isParticipating(factionId)) {
                existingBounty.addBounty(newBounty);
                log.info(factionId + " has updated their existing bounty on the player's head, adding " + (newBounty.value * 0.5f) + " credits.");
            } else if (isParticipating(factionId)) {
                bountiesPosted.put(factionId, newBounty);
                log.info(factionId + " has posted a bounty of " + newBounty.value + " credits on the player's head.");
            }
        }
        bountiesToPost.clear();

        List<PlayerBountyData> remove = new ArrayList<>();
        for (PlayerBountyData existing : bountiesPosted.values()) {
            int minBounty = (int) (getBountyValue() * 0.5f);
            int maxBounty = (int) (getBountyValue() * 1.5f);
            if (existing.postedByFaction.getRelToPlayer().isAtBest(RepLevel.VENGEFUL)) {
                maxBounty += (int) (getBountyValue() * 0.5f); // if they really hate you they'll pay more for your death
            }
            if (existing.elapsedDays >= existing.duration
                    || existing.postedByFaction.getRelToPlayer().isAtWorst(RepLevel.NEUTRAL)
                    || existing.value < minBounty) {
                remove.add(existing);
            }
            if (existing.value > (getBountyValue() * 1.5f)) {
                existing.value = maxBounty;
                log.info(existing.getFactionId() + " has reduced their bounty on the player's head to " + existing.value
                        + " credits as the paltry threat they pose makes it unreasonable to pay more.");
            }
        }
        for (PlayerBountyData toRemove : remove) {
            bountiesPosted.remove(toRemove.getFactionId());
            log.info(toRemove.getFactionId() + " has lifted their bounty on the player's head.");
        }

        int total = 0;
        for (PlayerBountyData existing : bountiesPosted.values()) {
            total += existing.value;
        }
        int fairValue = getBountyValue();
        if (total > 0 && fairValue > 0) {
            timerMultBecauseOfCrimes = total / fairValue;
            if (timerMultBecauseOfCrimes > 2f) {
                timerMultBecauseOfCrimes = 2f;
            } else if (timerMultBecauseOfCrimes > 0f && timerMultBecauseOfCrimes < 0.5f) {
                timerMultBecauseOfCrimes = 0.5f;
            }
        } else {
            timerMultBecauseOfCrimes = 0;
        }

        if (bountiesPosted.size() > 0 && !Global.getSector().getIntelManager().hasIntel(this)) {
            Global.getSector().getIntelManager().addIntel(this);
        }
    }

    @SuppressWarnings("unchecked")
    protected void addBulletPoints(TooltipMakerAPI info, ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;
        float initPad = pad;

        if (mode == ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        sorted.clear();
        for (PlayerBountyData bounty : bountiesPosted.values()) {
            sorted.add(bounty);
        }
        Collections.sort(sorted);

        for (PlayerBountyData bounty : sorted) {
            String credits = Misc.getDGSCredits(bounty.value);
            String factionName = Misc.ucFirst(bounty.postedByFaction.getDisplayName());

            Color fh = bounty.postedByFaction.getBaseUIColor();
            info.addPara("Posted By: " + factionName, initPad, tc, fh, factionName);
            info.addPara(credits + " credits, " + (int) bounty.getRemaining() + " days remaining", pad, tc, h, credits, "" + (int) bounty.getRemaining());
        }

        for (EveryFrameScript script : Global.getSector().getScripts()) {
            if (script instanceof TriTachLoanIncentiveScript) {
                String credits = Misc.getDGSCredits(getBountyValue());
                FactionAPI tt = Global.getSector().getFaction(Factions.TRITACHYON);
                String factionName = Misc.ucFirst(tt.getDisplayName());

                Color fh = tt.getBaseUIColor();
                info.addPara("Posted By: " + factionName, initPad, tc, fh, factionName);
                info.addPara(credits + " credits.", pad, tc, h, credits);
            } else if (script instanceof DeliveryFailureConsequences) {
                String credits = Misc.getDGSCredits(getBountyValue());
                FactionAPI ind = Global.getSector().getFaction(Factions.INDEPENDENT);
                String factionName = Misc.ucFirst(ind.getDisplayName());

                Color fh = ind.getBaseUIColor();
                info.addPara("Posted By: " + factionName, initPad, tc, fh, factionName);
                info.addPara(credits + " credits.", pad, tc, h, credits);
            }
        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();

        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);
    }

    @Override
    public String getSortString() {
        return "Assassination Target";
    }

    public String getName() {
        return "Wanted Status";
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return Global.getSector().getPlayerFaction();
    }

    public PersonAPI getPerson() {
        return Global.getSector().getPlayerPerson();
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        PersonAPI player = getPerson();
        String crest = getFactionForUIColors().getCrest();
        if (crest == null) {
            crest = player.getFaction().getCrest();
        }
        info.addImages(width, 128, opad, opad, player.getPortraitSprite(), crest);

        if (bountiesPosted.size() > 0) {
            info.addPara("Your network of informants has alerted you of the following bounties for your capture and/or death:", opad);
        } else {
            info.addPara("Your name is clear and there is, to the best of your knowledge, no price currently on your head.", opad);
        }

        addBulletPoints(info, ListInfoMode.IN_DESC);

        String timerMultString;
        if (timerMultBecauseOfCrimes <= 0.555) {
            timerMultString = "rarely, if at all.";
        } else if (timerMultBecauseOfCrimes <= 0.666) {
            timerMultString = "maybe once or twice per cycle.";
        } else if (timerMultBecauseOfCrimes <= 1) {
            timerMultString = "every few months.";
        } else if (timerMultBecauseOfCrimes <= 1.5) {
            timerMultString = "every month or two.";
        } else if (timerMultBecauseOfCrimes <= 1.95) {
            timerMultString = "once a month or more.";
        } else {
            timerMultString = "whenever and wherever you show your extremely valuable head.";
        }

        if (bountiesPosted.size() > 0) {
            info.addPara("Based on the bounties offered in relation to the danger you pose as a target, you can expect to be accosted by bounty hunters "
                    + timerMultString, opad, h, timerMultString);

            String distMultString;
            if (timerMultBecauseOfCrimes <= 0.75) {
                distMultString = "a short distance";
            } else if (timerMultBecauseOfCrimes <= 1.25) {
                distMultString = "a significant distance";
            } else if (timerMultBecauseOfCrimes <= 1.75) {
                distMultString = "a very long distance";
            } else {
                distMultString = "as far as possible";
            }

            info.addPara("Bounties will eventually decay with time and bounty hunters can be avoided by staying " + distMultString + " away from the sector core, or only spending a very short time within any system if you must venture closer.", opad, h, distMultString, "a very short time");
        }
    }

    @Override
    public String getIcon() {
        return getPerson().getPortraitSprite();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BOUNTY);
        tags.add("Wanted Status");
        return tags;
    }

    public static VayraPlayerBountyIntel getInstance() {
        Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
        return (VayraPlayerBountyIntel) test;
    }
}
