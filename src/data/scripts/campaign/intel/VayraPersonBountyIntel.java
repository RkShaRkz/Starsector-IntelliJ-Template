package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.FactoryAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.bases.PirateBaseManager;
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.*;

import static com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.BreadcrumbSpecialCreator.isLargeShipOrNonShip;
import static com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial.getLocatedString;
import static data.scripts.VayraMergedModPlugin.*;
import static data.scripts.campaign.intel.VayraPersonBountyManager.*;
import static java.lang.Math.random;

public final class VayraPersonBountyIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener {

    public static Logger log = Global.getLogger(VayraPersonBountyIntel.class);

    public enum BountyType {
        HOSTILE,
        DESERTER,
    }

    public static float MAX_TIME_BASED_ADDED_LEVEL = 3;
    public static final float CARE_ABOUT_RANGE = 4000f;
    private int allies = 0;
    private int enemies = 0;

    private float elapsedDays = 0f;
    private float duration = BOUNTY_DURATION;
    private float bountyCredits = 0;
    private float perLevel;

    private FactionAPI faction;
    private FactionAPI bountyFaction;
    private PersonAPI person;
    private String wordForKillingJerk;
    private String descOfJerk;
    private String typeOfJerk;
    private String titleOfJerk;
    private String reasonForJerk;
    private CampaignFleetAPI fleet;
    private FleetMemberAPI flagship;
    private RareBountyFlagshipData rareFlagship = null;

    private BountyType bountyType;

    private SectorEntityToken hideoutLocation = null;

    private int level = 0;

    public FactionAPI getBountyFaction() {
        return bountyFaction;
    }

    public FactionAPI getPostingFaction() {
        return faction;
    }

    public PersonAPI getPerson() {
        return person;
    }

    public CampaignFleetAPI getFleet() {
        return fleet;
    }

    public FleetMemberAPI getFlagship() {
        return flagship;
    }

    public float getElapsedDays() {
        return elapsedDays;
    }

    public void setElapsedDays(float elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    public static PersonBountyEventData getSharedData() {
        return SharedData.getData().getPersonBountyEventData();
    }

    public VayraPersonBountyIntel() {

        if (JERK_KILL_WORDS.isEmpty()) {
            log.error("Jerk kill words list was empty for some reason...");
            log.info("Fucking off for now to try again later.");
            endImmediately();
            return;
        }

        pickLevel();
        if (VAYRA_DEBUG) {
            log.info("picked level");
        }

        pickJerk();
        if (VAYRA_DEBUG) {
            log.info("picked jerk");
        }

        pickFaction();
        if (VAYRA_DEBUG) {
            log.info("picked faction");
        }

        Global.getSector().getIntelManager().removeAllThatShouldBeRemoved();

        if (VayraPersonBountyManager.getInstance().getActiveCount() >= VayraPersonBountyManager.getInstance().getMaxConcurrent()) {
            log.info(String.format("I should never have come here and now I'm gonna pay. (bounties are full) [%s/%s]",
                    VayraPersonBountyManager.getInstance().getActiveCount(), VayraPersonBountyManager.getInstance().getMaxConcurrent()));
            endImmediately();
            return;
        }

        if (isDone()) {
            log.error("decided I was done before picking bountyFaction");
            return;
        }

        pickBountyFaction();
        if (VAYRA_DEBUG) {
            log.info("picked bounty faction");
        }
        if (isDone()) {
            log.error("decided I was done after trying to pick bounty faction");
            return;
        }

        initBountyAmount();
        if (VAYRA_DEBUG) {
            log.info("picked bounty amount");
        }

        this.hideoutLocation = VayraBountySharedMethods.pickHideoutLocation(bountyFaction);
        if (hideoutLocation == null) {
            log.error("failed to pick hideoutLocation, aborting");
            endImmediately();
            return;
        } else if (VAYRA_DEBUG) {
            log.info("picked hideout location");
        }
        modifyBountyForNearbyFleets();

        if (isDone()) {
            log.error("decided I was done before picking bounty type");
            return;
        }

        pickBountyType();
        if (VAYRA_DEBUG) {
            log.info("picked bounty type");
        }
        if (bountyType == BountyType.DESERTER) {
            bountyCredits *= 1.5f;
        }

        initPerson();
        if (VAYRA_DEBUG) {
            log.info("picked person");
        }
        if (isDone()) {
            log.error("decided I was done before spawning fleet");
            return;
        }

        spawnFleet();
        if (VAYRA_DEBUG) {
            log.info("spawned fleet");
        }
        pickReason();
        if (VAYRA_DEBUG) {
            log.info("picked reason");
        }
        if (isDone()) {
            log.error("got to the end of initialization and decided I was done");
            return;
        }

        log.info(String.format("Starting person bounty posted by faction [%s] for person [%s] from faction [%s]", faction.getDisplayName(), person.getName().getFullName(), bountyFaction.getDisplayName()));

        Global.getSector().getIntelManager().queueIntel(this);
    }

    @Override
    public void reportMadeVisibleToPlayer() {
        if (!isEnding() && !isEnded()) {
            duration = Math.max(duration * 0.5f, Math.min(duration * 2f, BOUNTY_DURATION));
        }
    }

    private void pickLevel() {

        int base = getSharedData().getLevel();

        float timeFactor = (PirateBaseManager.getInstance().getDaysSinceStart() - 180f) / 365f;
        if (timeFactor < 0) {
            timeFactor = 0;
        }
        if (timeFactor > 1) {
            timeFactor = 1;
        }

        int add = Math.round(MAX_TIME_BASED_ADDED_LEVEL * timeFactor);
        base += add;

        if (base > 10) {
            base = 10;
        }

        boolean hasLow = false;
        boolean hasHigh = false;
        for (EveryFrameScript s : VayraPersonBountyManager.getInstance().getActive()) {
            VayraPersonBountyIntel bounty = (VayraPersonBountyIntel) s;

            int curr = bounty.getLevel();

            if (curr < base || curr == 0) {
                hasLow = true;
            }
            if (curr > base) {
                hasHigh = true;
            }
        }

        level = base;
        if (!hasLow) {
            level = 0;
        } else if (!hasHigh) {
            level += new Random().nextInt(3) + 2;
        }

        if (level < 0) {
            level = 0;
        }
    }

    public int getLevel() {
        return level;
    }

    private void modifyBountyForNearbyFleets() {
        float orbit = hideoutLocation.getLocation().length();

        for (SectorEntityToken other : hideoutLocation.getContainingLocation().getAllEntities()) {
            if (other == null || other.getLocation() == null) {
                continue;
            }
            float orbitDiff = Math.abs(orbit - other.getLocation().length());
            if (orbitDiff <= CARE_ABOUT_RANGE) {
                FactionAPI playerFaction = Global.getSector().getPlayerFaction();
                if (other.getFaction() != null && !other.getFaction().getId().equals(Factions.NEUTRAL) && bountyFaction.isHostileTo(other.getFaction()) && !playerFaction.isHostileTo(other.getFaction())) {
                    // allies present for player vs bounty
                    allies++;
                } else if (other.getFaction() != null && !other.getFaction().getId().equals(Factions.NEUTRAL) && !bountyFaction.isHostileTo(other.getFaction()) && playerFaction.isHostileTo(other.getFaction())) {
                    // more enemies present vs player as well as bounty
                    enemies++;
                }
            }
        }

        if (allies > 0 && enemies == 0) {
            bountyCredits *= 0.75f;

        } else if (allies == 0 && enemies > 0) {
            bountyCredits *= 1.25f;

        } else if (allies > enemies) {
            bountyCredits *= 0.9f;

        } else if (allies < enemies) {
            bountyCredits *= 1.1f;

        } else {
            // no nearby other allies or enemies -- no change!
        }
    }

    private void pickFaction() {
        FactionAPI player = Global.getSector().getPlayerFaction();

        String commFacId = Misc.getCommissionFactionId();
        boolean forceCommissionFaction = true;
        if (commFacId != null && getSharedData().isParticipating(commFacId)) {
            for (EveryFrameScript s : VayraPersonBountyManager.getInstance().getActive()) {
                VayraPersonBountyIntel bounty = (VayraPersonBountyIntel) s;
                if (bounty.faction != null && bounty.faction.getId().equals(commFacId)) {
                    forceCommissionFaction = false;
                }
            }
        } else {
            forceCommissionFaction = false;
        }

        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {

            if (!getSharedData().isParticipating(market.getFactionId())) {
                continue;
            }
            if (market.getSize() < 3) {
                continue;
            }
            if (market.isHidden()) {
                continue;
            }
            if (market.getFaction().isPlayerFaction()) {
                continue;
            }
            if (!market.getFaction().isShowInIntelTab()) {
                continue;
            }

            float weight = market.getSize();
            if (market.hasIndustry(Industries.HIGHCOMMAND)) {
                weight *= 2.5f;
            } else if (market.hasIndustry(Industries.MILITARYBASE)) {
                weight *= 2f;
            } else if (market.hasIndustry(Industries.PATROLHQ)) {
                weight *= 1.5f;
            }

            if (market.getFaction() != null) {
                if (forceCommissionFaction && !market.getFaction().getId().equals(commFacId)) {
                    continue;
                }

                if (market.getFaction().isHostileTo(player)) {
                    weight *= 0.1f;
                }

                if (market.getFactionId().equals(Factions.PIRATES) && !market.getFaction().isHostileTo(player)) {
                    weight *= 2f;
                }

            }

            if (weight > 0) {
                picker.add(market, weight);
            }
        }

        if (picker.isEmpty()) {
            endImmediately();
            return;
        }

        MarketAPI market = picker.pick();
        faction = market.getFaction();
    }

    private void pickReason() {

        if (rareFlagship != null) {

            ShipHullSpecAPI spec = flagship.getVariant().getHullSpec();
            String shipType = spec.getHullNameWithDashClass() + " " + spec.getDesignation().toLowerCase();
            reasonForJerk = rareFlagship.shipSource + " " + aOrAn(shipType) + " " + shipType;

        } else {

            WeightedRandomPicker<String> reasons = new WeightedRandomPicker<>();
            for (String reason : JERK_REASONS) {
                reasons.add(reason);
            }
            reasons.add("crimes");
            String reason = reasons.pick();

            if (reason.contains("$")) {
                FactionAPI replaceFaction;
                if (Math.random() <= 0.69f) {
                    replaceFaction = faction;

                } else {
                    WeightedRandomPicker<FactionAPI> factions = new WeightedRandomPicker<>();
                    for (String facId : SharedData.getData().getPersonBountyEventData().getParticipatingFactions()) {
                        FactionAPI fac = Global.getSector().getFaction(facId);
                        if (fac.isShowInIntelTab() && faction.isAtWorst(fac, RepLevel.FAVORABLE)) {
                            factions.add(fac, faction.getRelationship(facId));
                        }
                    }
                    replaceFaction = factions.pick();
                    if (replaceFaction == null) {
                        replaceFaction = faction;
                    }
                }
                reason = reason.replace("$faction", replaceFaction.getPersonNamePrefix());
                reason = reason.replace("$afaction", aOrAn(replaceFaction.getPersonNamePrefix())
                        + " " + replaceFaction.getPersonNamePrefix());

                if (reason.contains("$market")) {
                    WeightedRandomPicker<MarketAPI> markets = new WeightedRandomPicker<>();
                    for (MarketAPI market : Misc.getFactionMarkets(replaceFaction)) {
                        markets.add(market, market.getSize());
                    }
                    MarketAPI replaceMarket = markets.pick();
                    String replaceMarketName = "a remote outpost";
                    if (replaceMarket != null) {
                        replaceMarketName = replaceMarket.getName();
                    }
                    reason = reason.replace("$market", replaceMarketName);
                }

                if (reason.contains("$crime")) {
                    String replaceCrime;
                    WeightedRandomPicker<String> crimes = new WeightedRandomPicker<>();
                    for (String crime : JERK_CRIMES) {
                        crimes.add(crime);
                    }
                    replaceCrime = crimes.pick();
                    reason = reason.replace("$crime", replaceCrime);
                }

                if (reason.contains("$victim") || reason.contains("$avictim")) {
                    String replaceVictim;
                    if (Math.random() <= 0.69f) {
                        WeightedRandomPicker<String> victims = new WeightedRandomPicker<>();
                        for (String victim : JERK_VICTIMS) {
                            victims.add(victim);
                        }
                        replaceVictim = victims.pick();
                    } else {
                        replaceVictim = replaceFaction.getRank(pickRank(null));
                    }
                    reason = reason.replace("$victim", replaceVictim);
                    reason = reason.replace("$avictim", aOrAn(replaceVictim)
                            + " " + replaceVictim);
                }
            }

            reasonForJerk = reason;
        }
    }

    private String pickRank(Integer personLevel) {

        Map<String, Integer> ranks = new HashMap<>();
        ranks.put(Ranks.SPACE_SAILOR, 5);
        ranks.put(Ranks.SPACE_CHIEF, 8);
        ranks.put(Ranks.SPACE_ENSIGN, 10);
        ranks.put(Ranks.SPACE_LIEUTENANT, 13);
        ranks.put(Ranks.SPACE_COMMANDER, 15);
        ranks.put(Ranks.SPACE_CAPTAIN, 18);
        ranks.put(Ranks.SPACE_ADMIRAL, 20);
        ranks.put(Ranks.GROUND_PRIVATE, 5);
        ranks.put(Ranks.GROUND_SERGEANT, 8);
        ranks.put(Ranks.GROUND_LIEUTENANT, 10);
        ranks.put(Ranks.GROUND_CAPTAIN, 13);
        ranks.put(Ranks.GROUND_MAJOR, 15);
        ranks.put(Ranks.GROUND_COLONEL, 18);
        ranks.put(Ranks.GROUND_GENERAL, 20);
        ranks.put(Ranks.CITIZEN, 5);
        ranks.put(Ranks.PILOT, 10);
        ranks.put(Ranks.AGENT, 15);

        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        for (String rank : ranks.keySet()) {
            float weight;

            if (personLevel != null) {
                int rankDiff = Math.abs(ranks.get(rank) - personLevel);
                weight = 1f - (rankDiff / 15);
            } else {
                weight = 1f;
            }

            picker.add(rank, weight);
        }

        return picker.pick();
    }

    private void pickJerk() {

        WeightedRandomPicker<String> jerkKillWord = new WeightedRandomPicker<>();
        for (String word : JERK_KILL_WORDS) {
            jerkKillWord.add(word);
        }
        wordForKillingJerk = jerkKillWord.pick();

        WeightedRandomPicker<String> jerkDesc = new WeightedRandomPicker<>();
        for (String desc : JERK_DESCS) {
            jerkDesc.add(desc);
        }
        descOfJerk = jerkDesc.pick();

        WeightedRandomPicker<String> jerkType = new WeightedRandomPicker<>();
        for (String type : JERK_TYPES) {
            jerkType.add(type);
        }
        typeOfJerk = jerkType.pick();

        if ((level / 100f) + random() > 0.69f) {
            WeightedRandomPicker<String> placeNames = new WeightedRandomPicker<>();
            for (MarketAPI place : Global.getSector().getEconomy().getMarketsCopy()) {
                placeNames.add(place.getName());
            }
            for (StarSystemAPI place : Global.getSector().getEconomy().getStarSystemsWithMarkets()) {
                placeNames.add(place.getNameWithTypeIfNebula());
            }
            String placeName = placeNames.pick();

            WeightedRandomPicker<String> jerkTitles = new WeightedRandomPicker<>();
            for (String title : JERK_TITLES) {
                jerkTitles.add(title);
            }
            String title = jerkTitles.pick();
            if (random() > 0.5f) {
                titleOfJerk = title + " of " + placeName;
            } else {
                titleOfJerk = placeName + " " + title;
            }
        }

    }

    private void pickBountyFaction() {
        FactionAPI player = Global.getSector().getPlayerFaction();
        String str = "_vayraBountyDiversity";

        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        for (MarketAPI market : Global.getSector().getEconomy().getMarketsCopy()) {

            if (market.getFaction() == null) {
                continue;
            }
            if (market.getFaction().isPlayerFaction()) {
                continue;
            }
            if (!market.getFaction().isShowInIntelTab()) {
                continue;
            }

            float weight = market.getSize();
            Map<String, Object> pd = Global.getSector().getPersistentData();
            if (pd.get(market.getFactionId() + str) == null) {
                pd.put(market.getFactionId() + str, 1f);
            }
            weight *= (float) pd.get(market.getFactionId() + str);

            if (market.hasIndustry(Industries.HIGHCOMMAND)) {
                weight *= 2.5f;
            } else if (market.hasIndustry(Industries.MILITARYBASE)) {
                weight *= 2f;
            } else if (market.hasIndustry(Industries.PATROLHQ)) {
                weight *= 1.5f;
            }

            if (weight > 0) {
                picker.add(market, weight);
            }
        }

        if (picker.isEmpty()) {
            log.error("failed to pick bountyFaction, aborting immediately");
            endImmediately();
            return;
        }

        MarketAPI market = picker.pick();
        bountyFaction = market.getFaction();

        for (FactionAPI f : Global.getSector().getAllFactions()) {
            Map<String, Object> pd = Global.getSector().getPersistentData();
            if (!f.equals(bountyFaction)) {
                if (pd.get(f.getId() + str) == null) {
                    pd.put(f.getId() + str, 1f);
                }
                pd.put(f.getId() + str, (float) pd.get(f.getId() + str) + 1f);
            } else {
                pd.put(f.getId() + str, 1f);
            }
        }
    }

    private void initBountyAmount() {
        float highStabilityMult = 1f;
        float base = Global.getSettings().getFloat("basePersonBounty");
        perLevel = Global.getSettings().getFloat("personBountyPerLevel") * EXTRA_BOUNTY_LEVEL_MULT;

        float random = perLevel * (int) (Math.random() * 15) / 15f;

        bountyCredits = (int) ((base + (perLevel * level) + random) * highStabilityMult);
    }

    private void initPerson() {
        String factionId = bountyFaction.getId();
        if (bountyType == BountyType.DESERTER) {
            factionId = faction.getId();
        }
        int personLevel = (int) (5 + level * 1.5f);
        person = OfficerManagerEvent
                .createOfficer(
                        Global.getSector().getFaction(factionId),
                        personLevel,
                        true
                );

        person.setRankId(pickRank(Math.min(personLevel, 20)));
    }

    private void pickBountyType() {
        WeightedRandomPicker<BountyType> picker = new WeightedRandomPicker<>();
        picker.add(BountyType.HOSTILE, 9f);
        picker.add(BountyType.DESERTER, 1f);
        bountyType = picker.pick();
    }

    private String getTargetDesc() {

        ShipHullSpecAPI spec = flagship.getVariant().getHullSpec();
        String shipType = spec.getHullNameWithDashClass() + " " + spec.getDesignation().toLowerCase();

        String heOrShe = "he";
        String hisOrHer = "his";
        if (person.isFemale()) {
            heOrShe = "she";
            hisOrHer = "her";
        }

        @SuppressWarnings("UnusedAssignment")
        String levelDesc = "";
        int personLevel = person.getStats().getLevel();
        if (personLevel <= 5) {
            levelDesc = "an unremarkable officer";
        } else if (personLevel <= 10) {
            levelDesc = "a capable officer";
        } else if (personLevel <= 15) {
            levelDesc = "a highly capable officer";
        } else {
            levelDesc = "an exceptionally capable officer";
        }

        String skillDesc = "";

        if (person.getStats().getSkillLevel(Skills.OFFICER_MANAGEMENT) > 0) {
            skillDesc = "having a high number of skilled subordinates";
        } else if (person.getStats().getSkillLevel(Skills.ELECTRONIC_WARFARE) > 0) {
            skillDesc = "being proficient in electronic warfare";
//        } else if (person.getStats().getSkillLevel(Skills.FIGHTER_DOCTRINE) > 0) {
        } else if (person.getStats().getSkillLevel(Skills.CARRIER_GROUP) > 0) {
            skillDesc = "a noteworthy level of skill in running carrier operations";
        } else if (person.getStats().getSkillLevel(Skills.COORDINATED_MANEUVERS) > 0) {
            skillDesc = "a high effectiveness in coordinating the maneuvers of ships during combat";
        }

        if (!skillDesc.isEmpty() && levelDesc.contains("unremarkable")) {
            levelDesc = "an otherwise unremarkable officer";
        }

        @SuppressWarnings("UnusedAssignment")
        String fleetDesc = "";
        if (level < 3) {
            fleetDesc = "small";
        } else if (level <= 5) {
            fleetDesc = "medium-sized";
        } else if (level <= 8) {
            fleetDesc = "large";
        } else {
            fleetDesc = "very large";
        }

        String targetDesc;

        if (rareFlagship == null) {
            targetDesc = String.format("%s is in command of a %s fleet and was last seen using %s %s as %s flagship.",
                    person.getName().getFullName(), fleetDesc, aOrAn(shipType), shipType, hisOrHer);
        } else {
            targetDesc = String.format("%s is in command of a %s fleet.",
                    person.getName().getFullName(), fleetDesc);
        }

        if (skillDesc.isEmpty()) {
            targetDesc += String.format(" %s is known to be %s.", Misc.ucFirst(heOrShe), levelDesc);
        } else {
            targetDesc += String.format(" %s is %s known for %s.", Misc.ucFirst(heOrShe), levelDesc, skillDesc);
        }

        return targetDesc;
    }

    public class BountyResult {

        public BountyResultType type;
        public int payment;
        public ReputationAdjustmentResult rep;

        public BountyResult(BountyResultType type, int payment, ReputationAdjustmentResult rep) {
            this.type = type;
            this.payment = payment;
            this.rep = rep;
        }
    }

    @Override
    protected void advanceImpl(float amount) {
        CampaignClockAPI clock = Global.getSector().getClock();
        float days = clock.convertToDays(amount);
        elapsedDays += days;

        if (elapsedDays >= duration && !isDone()) {
            boolean canEnd = fleet == null || !fleet.isInCurrentLocation();
            if (canEnd) {
                log.info(String.format("Ending bounty on %s %s by %s", bountyFaction.getDisplayName(), person.getName().getFullName(), faction.getDisplayName()));
                result = new BountyResult(BountyResultType.END_TIME, 0, null);
                sendUpdateIfPlayerHasIntel(result, true);
                cleanUpFleetAndEndIfNecessary();
                return;
            }
        }

        if (fleet == null) {
            return;
        }
        if (fleet.isInCurrentLocation() && !fleet.getFaction().equals(bountyFaction)) {
            fleet.setFaction(bountyFaction.getId(), true);
        } else if (!fleet.isInCurrentLocation() && !fleet.getFaction().getId().equals(Factions.NEUTRAL)) {
            fleet.setFaction(Factions.NEUTRAL, true);
        }

        if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != person) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, null);
            boolean current = fleet.isInCurrentLocation();
            sendUpdateIfPlayerHasIntel(result, !current);
            cleanUpFleetAndEndIfNecessary();
        }
    }

    @Override
    public float getTimeRemainingFraction() {
        float f = 1f - elapsedDays / duration;
        return f;
    }

    private BountyResult result = null;

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
        cleanUpFleetAndEndIfNecessary();
    }

    private void cleanUpFleetAndEndIfNecessary() {
        if (fleet != null) {
            Misc.makeUnimportant(fleet, "pbe");
            fleet.clearAssignments();
            if (hideoutLocation != null) {
                fleet.getAI().addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, hideoutLocation, 1000000f, null);
            } else {
                fleet.despawn();
            }
            fleet = null; //can't null it because description uses it
        }
        if (!isEnding() && !isEnded()) {
            endAfterDelay();
        }
    }

    private boolean willPay() {
        return true;
    }

    private boolean willRepIncrease() {
        return true;
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {
        if (isDone() || result != null) {
            return;
        }

        if (battle.isInvolved(fleet) && !battle.isPlayerInvolved()) {
            if (fleet.getFlagship() == null || fleet.getFlagship().getCaptain() != person) {
                fleet.setCommander(fleet.getFaction().createRandomPerson());
                result = new BountyResult(BountyResultType.END_OTHER, 0, null);
                sendUpdateIfPlayerHasIntel(result, true);
                cleanUpFleetAndEndIfNecessary();
                return;
            }
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (!battle.isPlayerInvolved() || !battle.isInvolved(fleet) || battle.onPlayerSide(fleet)) {
            return;
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != null && fleet.getFlagship().getCaptain() == person) {
            return;
        }

        int payment = (int) (bountyCredits * battle.getPlayerInvolvementFraction());
        if (payment <= 0) {
            result = new BountyResult(BountyResultType.END_OTHER, 0, null);
            sendUpdateIfPlayerHasIntel(result, true);
            cleanUpFleetAndEndIfNecessary();
            return;
        }

        if (willPay()) {
            log.info(String.format("Paying bounty of %d from faction [%s]", payment, faction.getDisplayName()));

            if (Math.random() <= CRUMB_CHANCE) {
                giveBreadcrumb(playerFleet);
            }

            playerFleet.getCargo().getCredits().add(payment);
            ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                    new RepActionEnvelope(RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                    faction.getId());
            result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep);
            sendUpdateIfPlayerHasIntel(result, false);
        } else if (willRepIncrease()) {
            log.info(String.format("Not paying bounty, but improving rep with faction [%s]", faction.getDisplayName()));
            ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                    new RepActionEnvelope(RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                    faction.getId());
            result = new BountyResult(BountyResultType.END_PLAYER_NO_BOUNTY, payment, rep);
            sendUpdateIfPlayerHasIntel(result, false);
        } else {
            log.info(String.format("Not paying bounty or improving rep with faction [%s]", faction.getDisplayName()));
            result = new BountyResult(BountyResultType.END_PLAYER_NO_REWARD, 0, null);
            sendUpdateIfPlayerHasIntel(result, false);
        }

        getSharedData().reportSuccess();

        cleanUpFleetAndEndIfNecessary();
    }

    public void giveBreadcrumb(CampaignFleetAPI playerFleet) {
        WeightedRandomPicker<SectorEntityToken> picker = new WeightedRandomPicker<>();
        List<StarSystemAPI> systems = Misc.getNearbyStarSystems(playerFleet, 10f);
        for (StarSystemAPI system : systems) {

            // bounties know about salvage
            for (SectorEntityToken other : system.getEntitiesWithTag(Tags.SALVAGEABLE)) {
                if (!other.hasSensorProfile() && !other.isDiscoverable()) {
                    continue;
                }
                if (other == playerFleet) {
                    continue;
                }
                if (!isLargeShipOrNonShip(other)) {
                    continue;
                }
                picker.add(other);
            }

            // bounties know about ruins and habitable worlds
            for (PlanetAPI other : system.getPlanets()) {
                MarketAPI market = other.getMarket();
                if (market == null) {
                    continue;
                }
                if (!market.getSurveyLevel().equals(SurveyLevel.NONE)) {
                    continue;
                }
                boolean hasRuins = market.hasCondition(Conditions.RUINS_EXTENSIVE)
                        || market.hasCondition(Conditions.RUINS_WIDESPREAD)
                        || market.hasCondition(Conditions.RUINS_VAST);
                if (market.getHazardValue() > 1.25f && !hasRuins) {
                    continue;
                }

                picker.add(other);
            }
        }

        SectorEntityToken target = picker.pick();
        if (target != null) {

            String targetName = "a salvageable derelict";
            if (target instanceof PlanetAPI) {
                MarketAPI market = target.getMarket();
                if (market != null) {
                    boolean hasRuins = market.hasCondition(Conditions.RUINS_EXTENSIVE)
                            || market.hasCondition(Conditions.RUINS_WIDESPREAD)
                            || market.hasCondition(Conditions.RUINS_VAST);
                    if (hasRuins) {
                        targetName = "a world with extensive ruins";
                    } else if (market.getHazardValue() <= 1.25f) {
                        targetName = "a habitable world";
                    }
                }
            }

            String located = getLocatedString(target, true);
            String nameForTitle = target instanceof PlanetAPI ? "Hideout" : "Salvage";
            String subject = "Location: " + nameForTitle;

            String intelText = "In the wreckage of " + person.getRank() + " " + person.getName().getLast() + "'s flagship, your crews found a partially accessible memory bank containing information that indicates " + targetName + " is " + located + ".";

            if (target.getCustomPlugin() instanceof DerelictShipEntityPlugin) {
                DerelictShipEntityPlugin dsep = (DerelictShipEntityPlugin) target.getCustomPlugin();
                ShipVariantAPI variant = dsep.getData().ship.variant;
                if (variant == null && dsep.getData().ship.variantId != null) {
                    variant = Global.getSettings().getVariant(dsep.getData().ship.variantId);
                }
                if (variant != null) {
                    String size;
                    if (variant.getHullSize() == ShipAPI.HullSize.FRIGATE
                            || variant.getHullSize() == ShipAPI.HullSize.DESTROYER) {
                        size = "Based on the information, it's likely the derelict is a small ship, a frigate or a destroyer at the largest.";
                    } else {
                        size = "The derelict is likely to be a vessel of at least cruiser size.";
                    }
                    intelText += "\n\n" + size;
                }
            }

            SectorEntityToken start = playerFleet.getContainingLocation().createToken(playerFleet.getLocation().x, playerFleet.getLocation().y);
            BreadcrumbIntel intel = new BreadcrumbIntel(start, target);
            intel.setTitle(subject);
            intel.setText(intelText);
            Global.getSector().getIntelManager().addIntel(intel, false);
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) {
            return;
        }

        if (this.fleet == fleet) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            result = new BountyResult(BountyResultType.END_OTHER, 0, null);
            sendUpdateIfPlayerHasIntel(result, true);
            cleanUpFleetAndEndIfNecessary();
        }
    }

    private String getAlliterativeFleetName(String name) {
        String first = name.substring(0, 1).toLowerCase();
        WeightedRandomPicker<String> alliterative = new WeightedRandomPicker<>();
        WeightedRandomPicker<String> backup = new WeightedRandomPicker<>();
        for (String basterd : JERK_FLEETS) {
            if (basterd.substring(0, 1).equals(first)) {
                alliterative.add(basterd);
            } else {
                backup.add(basterd);
            }
        }

        if (!alliterative.isEmpty()) {
            return alliterative.pick();
        } else {
            return backup.pick();
        }
    }

    private void spawnFleet() {
        String fleetFactionId = bountyFaction.getId();
        if (bountyType == BountyType.DESERTER) {
            fleetFactionId = faction.getId();
        }

        float qf = (float) level / 10f;
        if (qf > 1) {
            qf = 1;
        }

        String fleetName;

        if (Math.random() < 0.666f) {
            fleetName = bountyFaction.getRank(person.getRankId()) + " " + person.getName().getLast() + "'s Fleet";
        } else {
            fleetName = person.getName().getFirst() + "'s " + Misc.ucFirst(getAlliterativeFleetName(person.getName().getFirst()));
        }

        float fp = (5f + (level * 5f)) * 5f;
        fp *= 0.75f + (float) Math.random() * 0.25f;

        FleetParamsV3 params = new FleetParamsV3(
                null,
                hideoutLocation.getLocationInHyperspace(),
                fleetFactionId,
                qf + 0.2f, // qualityOverride
                FleetTypes.PERSON_BOUNTY_FLEET,
                fp, // combatPts
                0f, // freighterPts 
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        params.ignoreMarketFleetSizeMult = true;
        fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null || fleet.isEmpty()) {
            endImmediately();
            if (VAYRA_DEBUG) {
                log.info("generated a null or empty fleet and ended immediately");
            }
            return;
        }

        FleetMemberAPI currFlag = fleet.getFlagship();
        float flagshipRoll = (float) Math.random();
        if (VAYRA_DEBUG) {
            log.info("rolled for rare flagship: " + flagshipRoll);
        }
        if (flagshipRoll <= (VAYRA_DEBUG ? 1f : RARE_BOUNTY_FLAGSHIP_CHANCE)) {
            rareFlagship = VayraPersonBountyManager.pickRareFlagship(currFlag, bountyFaction);

            if (rareFlagship != null) {
                currFlag.setFlagship(false);
                String flagVariant = rareFlagship.variantId;
                FactoryAPI factory = Global.getFactory();
                FleetMemberAPI newFlagship = factory.createFleetMember(FleetMemberType.SHIP, flagVariant);
                // not gonna fuck with IBB-style forced recovery, just give it the smart recovery hullmod
                newFlagship.getVariant().addMod("vayra_bounty_recoverable");
                fleet.getFleetData().addFleetMember(newFlagship);
                fleet.getFleetData().removeFleetMember(currFlag);
                fleet.getFleetData().setFlagship(newFlagship);
                newFlagship.setFlagship(true);
                newFlagship.setCaptain(person);
                fleet.setCommander(person);
                fleet.forceSync();
                newFlagship.getRepairTracker().setCR(newFlagship.getRepairTracker().getMaxCR());
                bountyCredits += ((rareFlagship.diff / 25f) * perLevel); // increase/decrease credits relative to FP change
                bountyCredits += perLevel; // plus one "level" just because the flagship gets a non-autofit variant so it's probably harder
                log.info(String.format("picked rare flagship %s for bounty %s", flagVariant, person.getNameString()));
            } else {
                log.info("couldn't find a rare bounty flagship RIP");
            }
        }

        fleet.setCommander(person);
        fleet.getFlagship().setCaptain(person);
        FleetFactoryV3.addCommanderSkills(person, fleet, null);

        Misc.makeImportant(fleet, "pbe", duration + 20f);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);

        fleet.setNoFactionInName(true);
        fleet.setFaction(Factions.NEUTRAL, true);
        fleet.setName(fleetName);

        fleet.addEventListener(this);

        LocationAPI location = hideoutLocation.getContainingLocation();
        location.addEntity(fleet);
        fleet.setLocation(hideoutLocation.getLocation().x - 500, hideoutLocation.getLocation().y + 500);
        fleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideoutLocation, 1000000f, null);

        flagship = fleet.getFlagship();

        fleet.forceSync();
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    public enum BountyResultType {
        END_PLAYER_BOUNTY,
        END_PLAYER_NO_BOUNTY,
        END_PLAYER_NO_REWARD,
        END_OTHER,
        END_TIME,
    }

    protected void addBulletPoints(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        float initPad = pad;
        if (mode == IntelInfoPlugin.ListInfoMode.IN_DESC) {
            initPad = opad;
        }

        Color tc = getBulletColorForMode(mode);

        bullet(info);
        boolean isUpdate = getListInfoParam() != null;

        if (result == null) {

            if (mode == IntelInfoPlugin.ListInfoMode.IN_DESC) {
                info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(bountyCredits));
                addDays(info, "remaining", duration - elapsedDays, tc);
            } else {
                info.addPara("Target Faction: " + bountyFaction.getDisplayName(), initPad, tc,
                        bountyFaction.getBaseUIColor(), bountyFaction.getDisplayName());
                if (!isEnding()) {
                    int days = (int) (duration - elapsedDays);
                    String daysStr = "days";
                    if (days <= 1) {
                        days = 1;
                        daysStr = "day";
                    }
                    info.addPara("%s reward, %s " + daysStr + " remaining", 0f, tc,
                            h, Misc.getDGSCredits(bountyCredits), "" + days);
                }
            }

            unindent(info);
            return;
        }

        switch (result.type) {
            case END_PLAYER_BOUNTY:
                info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(result.payment));
                CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, faction, null,
                        null, null, info, tc, isUpdate, 0f);
                break;
            case END_PLAYER_NO_BOUNTY:
                CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, faction, null,
                        null, null, info, tc, isUpdate, 0f);
                break;
            case END_PLAYER_NO_REWARD:
                CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, faction, null,
                        null, null, info, tc, isUpdate, 0f);
                break;
            case END_TIME:
                break;
            case END_OTHER:
                break;

        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, IntelInfoPlugin.ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;

        info.addPara(getName(), c, 0f);

        addBulletPoints(info, mode);

    }

    @Override
    public String getSortString() {
        return "Personal Bounty";
    }

    public String getName() {
        String n = person.getName().getFullName();

        if (result != null) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                case END_PLAYER_NO_BOUNTY:
                case END_PLAYER_NO_REWARD:
                    return "Bounty Completed - " + n;
                case END_OTHER:
                case END_TIME:
                    return "Bounty Ended - " + n;
            }
        }

        if (Misc.ucFirst(faction.getEntityNamePrefix()).length() >= 2) {
            return Misc.ucFirst(faction.getEntityNamePrefix()) + " Bounty - " + n;
        } else {
            return "Independent Bounty - " + n;
        }
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        if (faction == null) {
            log.error("tried to get faction for UI colors but it's null");
            return Global.getSector().getFaction(Factions.INDEPENDENT);
        }
        return faction;
    }

    @Override
    public String getSmallDescriptionTitle() {
        return getName();
    }

    @Override
    public void createSmallDescription(TooltipMakerAPI info, float width, float height) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        float pad = 3f;
        float opad = 10f;

        info.addImage(person.getPortraitSprite(), width, 128, opad);

        String killType = wordForKillingJerk;
        String jerkFaction = bountyFaction.getEntityNamePrefix();
        if (jerkFaction.equals("")) {
            jerkFaction = "independent";
        }
        String jerkFactionWithArticle = bountyFaction.getDisplayNameWithArticle();
        String jerkType = aOrAn(descOfJerk) + " " + descOfJerk + " " + jerkFaction + " " + typeOfJerk;
        String jerkName = bountyFaction.getRank(person.getRankId()) + " " + person.getName().getFullName();
        String jerkNameWithTitle = jerkName;
        ShipHullSpecAPI spec = flagship.getVariant().getHullSpec();
        String shipType = spec.getHullNameWithDashClass() + " " + spec.getDesignation().toLowerCase();

        if (titleOfJerk != null) {
            jerkNameWithTitle = jerkName + ", also known as the " + titleOfJerk;
        }

        if (bountyType == BountyType.DESERTER) {
            jerkType = aOrAn(descOfJerk) + " " + descOfJerk + " " + typeOfJerk + " and a deserter to " + jerkFactionWithArticle;
        }

        String has = faction.getDisplayNameHasOrHave();
        info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has
                        + " posted a bounty for the " + killType + " of " + jerkNameWithTitle
                        + ", " + jerkType + ".",
                opad, bountyFaction.getBaseUIColor(), jerkName, titleOfJerk, jerkFactionWithArticle, jerkFaction);

        String her = "her";
        if (person.getGender() == Gender.MALE) {
            her = "his";
        }
        info.addPara("Sources indicate the bounty has been posted in response to "
                        + her + " recent " + reasonForJerk + ".",
                opad, bountyFaction.getBaseUIColor(), shipType);

        String increaseOrDecrease = "";
        String alliesOrEnemies = "";
        if (allies > 0 && enemies == 0) {
            increaseOrDecrease = "decreased significantly";
            alliesOrEnemies = "friendly";

        } else if (allies == 0 && enemies > 0) {
            increaseOrDecrease = "increased significantly";
            alliesOrEnemies = "hostile";

        } else if (allies > enemies) {
            increaseOrDecrease = "decreased slightly";
            alliesOrEnemies = "friendly";

        } else if (allies < enemies) {
            increaseOrDecrease = "increased slightly";
            alliesOrEnemies = "friendly";

        } else {
            // no (or equal) nearby other allies or enemies -- no change!
        }
        if (!alliesOrEnemies.equals("")) {
            info.addPara("The bounty reward has been " + increaseOrDecrease
                    + " relative to the threat posed by this bounty, due to the likely presence of other nearby "
                    + alliesOrEnemies + " elements in-system.", opad, h, "increased", "decreased", "slightly", "significantly", "friendly", "hostile");
        }

        if (result != null) {
            if (result.type == null) {
                try {
                    info.addPara("This bounty is no longer on offer.", opad);
                } catch (NullPointerException npx) {
                    log.error("hey i don't know what the fuck but something's wrong with this bounty");
                }
            } else {
                switch (result.type) {
                    case END_PLAYER_BOUNTY:
                        info.addPara("You have successfully completed this bounty.", opad);
                        break;
                    case END_PLAYER_NO_BOUNTY:
                        info.addPara("You have successfully completed this bounty, but received no "
                                        + "credit reward because of your standing with "
                                        + Misc.ucFirst(faction.getDisplayNameWithArticle()) + ".",
                                opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
                        break;
                    case END_PLAYER_NO_REWARD:
                        info.addPara("You have successfully completed this bounty, but received no "
                                        + "reward because of your standing with "
                                        + Misc.ucFirst(faction.getDisplayNameWithArticle()) + ".",
                                opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());
                        break;
                    default:
                        info.addPara("This bounty is no longer on offer.", opad);
                        break;
                }
            }
        }

        addBulletPoints(info, IntelInfoPlugin.ListInfoMode.IN_DESC);
        if (result == null) {

            if (hideoutLocation != null) {
                SectorEntityToken fake = hideoutLocation.getContainingLocation().createToken(0, 0);
                fake.setOrbit(Global.getFactory().createCircularOrbit(hideoutLocation, 0, 1000, 100));

                String loc = BreadcrumbSpecial.getLocatedString(fake);
                loc = loc.replaceAll("orbiting", "hiding out near");
                loc = loc.replaceAll("located in", "hiding out in");
                String sheIs = "She is";
                if (person.getGender() == Gender.MALE) {
                    sheIs = "He is";
                }
                info.addPara(sheIs + " rumored to be " + loc + ".", opad);
            }

            int cols = 7;
            float iconSize = width / cols;

            if (DebugFlags.PERSON_BOUNTY_DEBUG_INFO) {
                boolean deflate = false;
                if (!fleet.isInflated()) {
                    fleet.setFaction(bountyFaction.getId(), true);
                    fleet.inflateIfNeeded();
                    deflate = true;
                }

                info.addPara("The bounty posting also contains partial intel on the ships under " + her + " command. (DEBUG: full info)", opad);
                info.addShipList(cols, 3, iconSize, getFactionForUIColors().getBaseUIColor(), fleet.getMembersWithFightersCopy(), opad);

                info.addPara("level: " + level, 3f);
                info.addPara("type: " + bountyType.name(), 3f);

                if (deflate) {
                    fleet.deflate();
                }
            } else {
                boolean deflate = false;
                if (!fleet.isInflated()) {
                    fleet.setFaction(bountyFaction.getId(), true);
                    fleet.inflateIfNeeded();
                    deflate = true;
                }

                List<FleetMemberAPI> list = new ArrayList<>();
                Random random = new Random(person.getNameString().hashCode() * 170000L);

                List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
                int max = 7;
                FleetMemberAPI flagCopy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, flagship.getVariant());
                flagCopy.setCaptain(person);
                list.add(flagCopy);
                for (FleetMemberAPI member : members) {
                    if (list.size() >= max) {
                        break;
                    }

                    if (member.isFighterWing()) {
                        continue;
                    }

                    float prob = (float) member.getFleetPointCost() / 20f;
                    prob += (float) max / (float) members.size();
                    if (member.isFlagship()) {
                        prob = 1f;
                    }

                    if (random.nextFloat() > prob) {
                        continue;
                    }

                    if (member.isFlagship()) {
                        continue;
                    }

                    FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
                    list.add(copy);
                }

                if (!list.isEmpty()) {
                    info.addPara("The bounty posting also contains partial intel on some of the ships under " + her + " command.", opad);
                    FactionAPI factionForUI = getFactionForUIColors();
                    if (factionForUI == null) {
                        factionForUI = Global.getSector().getFaction(Factions.INDEPENDENT);
                    }
                    try {
                        info.addShipList(cols, 1, iconSize, factionForUI.getBaseUIColor(), list, opad);
                    } catch (NullPointerException npx) {
                        log.error("something has gone terribly, awfully, horribly wrong");
                    }

                    int num = members.size() - list.size();
                    num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));

                    if (num < 5) {
                        num = 0;
                    } else if (num < 10) {
                        num = 5;
                    } else if (num < 20) {
                        num = 10;
                    } else {
                        num = 20;
                    }

                    if (num > 1) {
                        info.addPara("The intel assessment notes the fleet may contain upwards of %s other ships"
                                + " of lesser significance.", opad, h, "" + num);
                    } else {
                        info.addPara("The intel assessment notes the fleet may contain several other ships"
                                + " of lesser significance.", opad);
                    }
                }

                if (deflate) {
                    fleet.deflate();
                }
            }
        }

    }

    @Override
    public String getIcon() {
        return person.getPortraitSprite();
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add(Tags.INTEL_BOUNTY);
        tags.add(faction.getId());
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        Constellation c = hideoutLocation.getConstellation();
        SectorEntityToken entity = null;
        if (c != null && map != null) {
            entity = map.getConstellationLabelEntity(c);
        }
        if (entity == null) {
            entity = hideoutLocation;
        }
        return entity;
    }

}
