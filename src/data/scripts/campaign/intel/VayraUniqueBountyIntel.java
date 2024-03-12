package data.scripts.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.characters.FullName.Gender;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin;
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.Constellation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.SalvageSpecialAssigner.ShipRecoverySpecialCreator;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition;
import com.fs.starfarer.api.impl.campaign.shared.PersonBountyEventData;
import com.fs.starfarer.api.impl.campaign.shared.SharedData;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.intel.VayraUniqueBountyManager.UniqueBountyData;
import org.apache.log4j.Logger;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3.*;
import static com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.addSalvageEntity;
import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;

public class VayraUniqueBountyIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener {

    public static Logger log = Global.getLogger(VayraUniqueBountyIntel.class);

    // identifiers for bountyData
    public String bountyId;
    public final UniqueBountyData bountyData;

    private float bountyCredits = 0;

    private FactionAPI faction;
    private FactionAPI bountyFaction;
    private PersonAPI person;
    private CampaignFleetAPI fleet;
    private FleetMemberAPI flagship;
    private String levelDesc;
    private String skillDesc;
    private String fleetDesc;
    private String shipType;

    private SectorEntityToken hideoutLocation = null;

    private final int level = 0;

    public static final String HVB_HOSTILE = "hvb_hostile";

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

    // guess we'll need this later
    public static PersonBountyEventData getSharedData() {
        return SharedData.getData().getPersonBountyEventData();
    }

    // setup stuff
    // this constructor is the normal one
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public VayraUniqueBountyIntel() {

        // pick which bounty to spawn and set it to know this is its intel
        bountyData = pickData();
        if (bountyData == null) {
            endImmediately();
            log.info("aborting UNIQUE bounty due to no eligible picks");
            return;
        }

        // fuck off if we shouldn't have tried to spawn a bounty - we only do this if we're not being overridden
        Global.getSector().getIntelManager().removeAllThatShouldBeRemoved();
        if (!VAYRA_DEBUG && (VayraUniqueBountyManager.getInstance().getActiveCount() >= VayraUniqueBountyManager.getInstance().getMaxConcurrent())) {
            log.info(String.format("I should never have come here and now I'm gonna pay. (UNIQUE bounties are full) [%s/%s]",
                    VayraUniqueBountyManager.getInstance().getActiveCount(), VayraUniqueBountyManager.getInstance().getMaxConcurrent()));
            endImmediately();
            return;
        }

        init();
        bountyData.setIntel(this);
    }

    // this is the one for if we have an override
    public VayraUniqueBountyIntel(String forceSpawn) {

        bountyData = pickData(forceSpawn);

        if (bountyData == null) {
            endImmediately();
            log.info("aborting UNIQUE bounty WITH OVERRIDE because i was fed a malformed ID or something else broke");
            return;
        }

        init();
        bountyData.setIntel(this);
    }

    private void init() {
        if (isDone()) {
            return;
        }

        // load any variables from the bountyData that are important to us and set up other things
        bountyId = bountyData.bountyId;
        faction = Global.getSector().getFaction(bountyData.factionId);
        bountyFaction = Global.getSector().getFaction(bountyData.bountyFactionId);
        bountyCredits = bountyData.creditReward;
        hideoutLocation = VayraBountySharedMethods.pickHideoutLocation(bountyFaction);
        initPerson();

        // do some nullchecks
        if (bountyData == null) {
            log.error("missing bountyData (how did i lose it?), aborting");
            endImmediately();

            return;
        }
        if (hideoutLocation == null) {
            log.error("hideoutLocation was null, aborting");
            endImmediately();

            return;
        }
        if (bountyFaction == null) {
            log.error("missing bountyFaction, aborting");
            endImmediately();

            return;
        }
        if (faction == null) {
            log.error("missing faction, aborting");
            endImmediately();

            return;
        }
        if (person == null) {
            log.error("missing person, aborting");
            endImmediately();

            return;
        }
        if (bountyId == null) {
            log.error("missing bountyId (how the fuck did you do this), aborting");
            endImmediately();

            return;
        }

        // actually spawn the fleet
        spawnFleet();

        // another nullcheck
        if (flagship == null || flagship.getVariant() == null) {
            log.error("missing flagship (what, how?), aborting");
            endImmediately();
            return;
        }

        ShipHullSpecAPI spec = flagship.getVariant().getHullSpec();
        shipType = spec.getHullNameWithDashClass() + " " + spec.getDesignation().toLowerCase();

        // do the thing, i guess!
        log.info("Starting UNIQUE bounty " + bountyId + " by faction " + faction.getId() + " for " + person.getNameString() + " from faction " + bountyFaction.getId());
        VayraUniqueBountyManager manager = VayraUniqueBountyManager.getInstance();
        manager.spendBounty(bountyId);
        Global.getSector().getIntelManager().queueIntel(this);

        if (HVB_HOSTILE.equals(faction.getId())) {
            for (FactionAPI other : Global.getSector().getAllFactions()) {
                faction.setRelationship(other.getId(), 0f);
            }
            faction.setRelationship(Factions.PLAYER, -1f);
        }
    }

    // picks a random eligible bountyData, returns null if there aren't any
    private UniqueBountyData pickData() {
        return pickData(null);
    }

    private UniqueBountyData pickData(String forceSpawn) {

        WeightedRandomPicker<UniqueBountyData> candidates = new WeightedRandomPicker<>();
        VayraUniqueBountyManager manager = VayraUniqueBountyManager.getInstance();

        // if we have an override, just use that
        if (forceSpawn != null) {
            if (manager.getBounty(forceSpawn) == null) {
                log.error("the bounty manager doesn't know what " + forceSpawn + " is");
                return null;
            }
            return manager.getBounty(forceSpawn);
        }

        // otherwise pick one normally
        for (String id : manager.getBountiesList()) {
            UniqueBountyData data = manager.getBounty(id);
            if (data.conditionsMet()) {
                candidates.add(data);
            }
        }

        if (candidates.isEmpty()) {
            log.info("no eligible unique bounties, returning null");
            return null;
        }

        log.info("picking unique bounty");

        UniqueBountyData picked = candidates.pick();
        log.info("picked unique bounty: " + picked.bountyId);
        return picked;
    }

    private void initPerson() {
        person = OfficerManagerEvent.createOfficer(
                bountyFaction,
                bountyData.level,
//                true,
                false
        );

        person.getName().setGender(bountyData.gender);
        person.getName().setFirst(bountyData.firstName);
        person.getName().setLast(bountyData.lastName);
        person.setRankId(bountyData.rank);
        // might as well load the portrait now if we haven't already done it through a faction or settings.json
        try {
            Global.getSettings().loadTexture(bountyData.portrait);
        } catch (IOException ex) {
            log.error("Failed to load portrait sprite: " + bountyData.portrait);
        }
        person.setPortraitSprite(bountyData.portrait);
        String personality = bountyData.captainPersonality;
        if (personality == null) {
            personality = Personalities.AGGRESSIVE;
        }
        person.setPersonality(personality);
    }

    private String getTargetDesc() {

        if (person == null) {
            log.error("tried to getTargetDesc but person was null");
            return "SOMETHING HAS GONE TERRIBLY WRONG";
        }
        if (person.getStats() == null) {
            log.error("tried to getTargetDesc but person.getStats() was null");
            return "SOMETHING HAS GONE TERRIBLY WRONG";
        }

        String heOrShe = "he";
        String hisOrHer = "his";
        if (person.isFemale()) {
            heOrShe = "she";
            hisOrHer = "her";
        }

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

        if (person.getStats().getSkillLevel(Skills.OFFICER_MANAGEMENT) > 0) {
            skillDesc = "having a high number of skilled subordinates";
        } else if (person.getStats().getSkillLevel(Skills.ELECTRONIC_WARFARE) > 0) {
            skillDesc = "being proficient in electronic warfare";
//        } else if (person.getStats().getSkillLevel(Skills.FIGHTER_DOCTRINE) > 0) {
        } else if (person.getStats().getSkillLevel(Skills.CARRIER_GROUP) > 0) {
            skillDesc = "a noteworthy level of skill in running carrier operations";
        } else if (person.getStats().getSkillLevel(Skills.COORDINATED_MANEUVERS) > 0) {
            skillDesc = "a high effectiveness in coordinating the maneuvers of ships during combat";
        } else {
            skillDesc = "nothing, really";
        }

        if (!skillDesc.isEmpty() && levelDesc.contains("unremarkable")) {
            levelDesc = "an otherwise unremarkable officer";
        }

        if (level < 3) {
            fleetDesc = "small";
        } else if (level <= 5) {
            fleetDesc = "medium-sized";
        } else if (level <= 8) {
            fleetDesc = "large";
        } else {
            fleetDesc = "very large";
        }

        String targetDesc = String.format("%s is in command of a %s fleet and personally commands the %s, a %s, as %s flagship.",
                bountyFaction.getRank(person.getRankId()) + " " + person.getName().getFullName(), fleetDesc, bountyData.flagshipName, shipType, hisOrHer);

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

    protected BountyResult result = null;

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
        cleanUpFleetAndEndIfNecessary();
    }

    protected void cleanUpFleetAndEndIfNecessary() {
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
        VayraUniqueBountyManager.getInstance().getActive().remove(this);
        VayraUniqueBountyManager.getInstance().removeCurrentBounty(bountyId);
        if (!isEnding() && !isEnded()) {
            endAfterDelay();
        }
    }

    protected boolean willPay() {
        return true;
    }

    protected boolean willRepIncrease() {
        return true;
    }

    public static List<String> getSpecialItemIds() {
        final List<String> items = new ArrayList<>();
        for (SpecialItemSpecAPI spec : Global.getSettings().getAllSpecialItemSpecs()) {
            items.add(spec.getId());
        }
        return items;
    }

    public void addSpecial(String id) {

        // Prevent crashes when spawning data-less blueprints
        try {
            Global.getSector().getPlayerFleet().getCargo().addSpecial(new SpecialItemData(id, null), 1f);
            log.info("added special item [" + id + "]");
        } catch (Exception ex) {
            log.error("failed to add special item [" + id + "]");
        }
    }

    private void autoRecoverDerelict(CampaignFleetAPI fleet) {
        float roll = (float) Math.random();
        float chance = bountyData.chanceToAutoRecover;
        SectorEntityToken derelict = null;
        LocationAPI location = fleet.getContainingLocation();

        for (SectorEntityToken salvageObject : location.getEntitiesWithTag(Tags.SALVAGEABLE)) {
            if (salvageObject.getCustomPlugin() instanceof DerelictShipEntityPlugin) {

                try {
                    DerelictShipEntityPlugin p = (DerelictShipEntityPlugin) salvageObject.getCustomPlugin();
                    PerShipData psd = p.getData().ship;
                    String check = Global.getSettings().getVariant(bountyData.flagshipVariantId).getHullSpec().getHullId();
                    String hullId = null;

                    if (psd.variant != null) {
                        hullId = psd.variant.getHullSpec().getHullId();
                    } else if (psd.variantId != null) {
                        ShipVariantAPI variant = Global.getSettings().getVariant(psd.variantId);
                        hullId = variant.getHullSpec().getHullId();
                    }

                    if (check.equals(hullId)) {
                        derelict = salvageObject;
                    }

                } catch (NullPointerException npx) {
                    log.error("fucked up somewhere trying to scan for recoverable unique bounty derelict");
                }
            }
        }

        if (derelict == null && roll <= chance) {
            derelict = addDerelictShip(location, bountyData.flagshipVariantId, ShipCondition.WRECKED, true);
        } else {
            derelict = addDerelictShip(location, bountyData.flagshipVariantId, ShipCondition.WRECKED, false);
        }
        derelict.setLocation(fleet.getLocation().x, fleet.getLocation().y);
        Misc.makeImportant(derelict, "vayra_unique_bounty");
        derelict.removeTag(Tags.EXPIRES);
    }

    private SectorEntityToken addDerelictShip(LocationAPI location,
                                              String variantId,
                                              ShipCondition condition,
                                              boolean recoverable) {
        DerelictShipData params = new DerelictShipData(new PerShipData(variantId, condition), false);
        SectorEntityToken ship = addSalvageEntity(location, Entities.WRECK, Factions.NEUTRAL, params);
        ship.setDiscoverable(true);

        if (recoverable) {
            ShipRecoverySpecialCreator creator = new ShipRecoverySpecialCreator(null, 0, 0, false, null, null);
            Misc.setSalvageSpecial(ship, creator.createSpecial(ship, null));
        }
        return ship;
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

                // ROLL AUTORECOVER CHANCE AND SPAWN UNIQUE SHIP AS NON-DESPAWNING RECOVERABLE DERELICT IF IT PASSES
                autoRecoverDerelict(fleet);

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

        log.info(String.format("Paying bounty of %d from faction [%s]", payment, faction.getDisplayName()));

        if (bountyData != null && bountyData.specialItemRewards != null && !bountyData.specialItemRewards.isEmpty()) {
            List<String> rewards = bountyData.specialItemRewards;
            log.info("attempting to dispense rewards " + rewards);
            for (String item : rewards) {
                addSpecial(item);
            }
        }

        playerFleet.getCargo().getCredits().add(payment);
        ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new RepActionEnvelope(RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                faction.getId());
        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep);
        sendUpdateIfPlayerHasIntel(result, false);

        getSharedData().reportSuccess();

        cleanUpFleetAndEndIfNecessary();
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isDone() || result != null) {
            return;
        }

        if (fleet == fleet) {
            fleet.setCommander(fleet.getFaction().createRandomPerson());
            result = new BountyResult(BountyResultType.END_OTHER, 0, null);
            sendUpdateIfPlayerHasIntel(result, true);
            cleanUpFleetAndEndIfNecessary();
        }
    }

    private void spawnFleet() {

        String fleetName = bountyData.fleetName;

        FleetParamsV3 params = new FleetParamsV3(
                null,
                hideoutLocation.getLocationInHyperspace(),
                bountyFaction.getId(),
                2f, // qualityOverride
                FleetTypes.PERSON_BOUNTY_FLEET,
                0f, 0f, 0f, 0f, 0f, 0f, 0f
        );
        params.ignoreMarketFleetSizeMult = true;

        // spawn flagship and handpicked variants
        CampaignFleetAPI newFleet = createFleet(params); // nonrandom fleet, flagship and preset variants
        if (newFleet == null || newFleet.isEmpty()) {
            log.error("fleet spawned empty, possibly due to missing flagship - aborting");
            endImmediately();
            return;
        }

        // spawn random filler ships if needed/desired
        int currPts = newFleet.getFleetPoints();
        int extraPts = 0;
        int min = bountyData.minimumFleetFP;
        log.info("pregenerated fleet is " + currPts + " FP, minimum fleet FP is " + min + ", maximum fleet FP is " + min);
        if (currPts < min) {
            extraPts += min - currPts;  // if we're below the minimum, add FP until we aren't
            log.info("adding " + extraPts + " extra FP of random ships to hit minimum without exceeding maximum");
        }
        int playerPts = Global.getSector().getPlayerFleet().getFleetPoints();
        float scale = bountyData.playerFPScalingFactor;
        log.info("player fleet is " + playerPts + " FP, scaling factor is " + scale + " for total of " + (int) (playerPts * scale) + " FP");
        if (currPts + extraPts < playerPts * scale) {
            extraPts += (playerPts * scale) - currPts;  // if we're below the player scaling factor, add FP until we aren't
        }
        int max = bountyData.maximumFleetFP;
        log.info("maximum fleet FP is " + max);
        if (currPts + extraPts > max) {
            extraPts = max - currPts; // if we're above the max, don't be
            if (extraPts < 0) {
                extraPts = 0; // if we'd add negative FP, don't
            }
        }
        log.info("adding " + extraPts + " additional FP of random shit");

        // actually spawn the extra stuff
        params.combatPts = extraPts;
        params.doNotPrune = true;
        CampaignFleetAPI extraFleet = FleetFactoryV3.createFleet(params);

        // if we're all jumbled up, sort us out
        if (extraFleet != null && !extraFleet.isEmpty()) {
            List<FleetMemberAPI> holding = new ArrayList<>();
            for (FleetMemberAPI mem : extraFleet.getFleetData().getMembersInPriorityOrder()) {
                holding.add(mem);
            }
            for (FleetMemberAPI held : holding) {
                extraFleet.getFleetData().removeFleetMember(held);
                newFleet.getFleetData().addFleetMember(held);
            }
        }

        // fix up our flagship and captain/admiral and stuff
        if (!newFleet.getFlagship().equals(flagship)) {
            newFleet.getFlagship().setFlagship(false);
            flagship.setFlagship(true);
        }
        flagship.setCaptain(person);
        flagship.setShipName(bountyData.flagshipName);
        newFleet.setCommander(person);
        FleetFactoryV3.addCommanderSkills(person, fleet, null);

        Misc.makeImportant(newFleet, "pbe", 69420f);
        newFleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PIRATE, true);
        newFleet.getMemoryWithoutUpdate().set(MemFlags.FLEET_NO_MILITARY_RESPONSE, true);

        newFleet.setNoFactionInName(true);
        newFleet.setFaction(Factions.NEUTRAL, true);
        newFleet.setName(fleetName);

        newFleet.addEventListener(this);

        LocationAPI location = hideoutLocation.getContainingLocation();
        location.addEntity(newFleet);
        newFleet.setLocation(hideoutLocation.getLocation().x - 500, hideoutLocation.getLocation().y + 500);
        newFleet.getAI().addAssignment(FleetAssignment.ORBIT_AGGRESSIVE, hideoutLocation, 1000000f, null);
        newFleet.getMemoryWithoutUpdate().set("$vayra_uniqueBounty", true);
        newFleet.getMemoryWithoutUpdate().set("$vayraUniqueBountyGreeting", bountyData.greetingText);

        newFleet.getFleetData().syncIfNeeded();
        List<FleetMemberAPI> members = newFleet.getFleetData().getMembersListCopy();
        for (FleetMemberAPI member : members) {
            member.getRepairTracker().setCR(member.getRepairTracker().getMaxCR());
        }
    }

    public CampaignFleetAPI createFleet(FleetParamsV3 params) {

        // create fake market and set ship quality
        MarketAPI market = Global.getFactory().createMarket("fake", "fake", 5);
        market.getStability().modifyFlat("fake", 10000);
        market.setFactionId(params.factionId);
        SectorEntityToken token = Global.getSector().getHyperspace().createToken(0, 0);
        market.setPrimaryEntity(token);
        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).modifyFlat("fake", BASE_QUALITY_WHEN_NO_MARKET);
        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlat("fake", 1f);
        params.source = market;

        // set faction
        String factionId = params.factionId;

        // create the fleet object
        CampaignFleetAPI newFleet = createEmptyFleet(factionId, params.fleetType, market);
        fleet = newFleet;
        newFleet.getFleetData().setOnlySyncMemberLists(true);

        Random random = new Random();
        if (params.random != null) {
            random = params.random;
        }

        // actually add all the ships
        FleetMemberAPI flag = addToFleet(bountyData.flagshipVariantId, newFleet, random);
        if (flag == null) {
            log.error(bountyData.flagshipVariantId + " not found - this just won't work");
            return null;
        }
        flagship = flag;
        // hullmod no longer required - doing IBB-style forced recovery instead
        //flag.getVariant().addMod("vayra_bounty_recoverable");
        if (bountyData.fleetVariantIds != null) {
            for (String v : bountyData.fleetVariantIds) {
                FleetMemberAPI test = addToFleet(v, newFleet, random);
                if (test == null) {
                    log.warn(v + " not found, skipping that variant");
                }
            }
        }

        if (params.withOfficers) {
            // if this is using the "hostile" faction generate indie officers instead so they don't have HVB faces lol
            if ("hvb_hostile".equals(params.factionId)) {
                params.factionId = Factions.INDEPENDENT;
                addCommanderAndOfficers(
                        newFleet,
                        params,
//                        0,
                        random
                );
                params.factionId = "hvb_hostile"; // and set it back after, clean up after yourself
                // otherwise go ahead and use the correct faction
            } else {
                addCommanderAndOfficers(
                        newFleet,
                        params,
//                        0,
                        random
                );
            }
        }

        newFleet.forceSync();

        if (newFleet.getFleetData().getNumMembers() <= 0
                || newFleet.getFleetData().getNumMembers() == newFleet.getNumFighters()) {
        }
        params.source = null;

        newFleet.setInflater(null); // no autofit

        newFleet.getFleetData().setOnlySyncMemberLists(false);

        return newFleet;
    }

    protected static FleetMemberAPI addToFleet(String variant, CampaignFleetAPI fleet, Random random) {

        FleetMemberAPI member;
        ShipVariantAPI test = Global.getSettings().getVariant(variant);
        if (test == null) {
            return null;
        }

        member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, test);
        String name = fleet.getFleetData().pickShipName(member, random);
        member.setShipName(name);
        fleet.getFleetData().addFleetMember(member);
        return member;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    public enum BountyResultType {
        END_PLAYER_BOUNTY,
        END_OTHER,
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
                info.addPara("This bounty will not expire", pad, tc, h);
            } else {
                info.addPara("Target Faction: " + bountyFaction.getDisplayName(), initPad, tc,
                        bountyFaction.getBaseUIColor(), bountyFaction.getDisplayName());
                if (!isEnding()) {
                    info.addPara("%s reward", 0f, tc,
                            h, Misc.getDGSCredits(bountyCredits));
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
        return "High-Value Bounty";
    }

    public String getName() {
        String n = person.getName().getFullName();

        if (result != null) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                case END_OTHER:
            }
        }

        return "High-Value Bounty - " + n;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
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

        info.addPara(bountyData.intelText, opad);

        if (result != null) {
            if (null == result.type) {
                info.addPara("This bounty is no longer on offer.", opad);
            } else {
                switch (result.type) {
                    case END_PLAYER_BOUNTY:
                        info.addPara("You have successfully completed this bounty.", opad);
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

                String her = "her";
                if (person.getGender() == Gender.MALE) {
                    her = "his";
                }
                info.addPara("The bounty posting also contains partial intel on the ships under " + her + " command. (DEBUG: full info)", opad);
                info.addShipList(cols, 3, iconSize, getFactionForUIColors().getBaseUIColor(), fleet.getMembersWithFightersCopy(), opad);

                info.addPara("level: " + level, 3f);

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
                    String her = "her";
                    if (person.getGender() == Gender.MALE) {
                        her = "his";
                    }
                    info.addPara(getTargetDesc() + " The bounty posting also contains intel on some of the ships under " + her + " command.",
                            opad, bountyFaction.getBaseUIColor(), bountyFaction.getRank(person.getRankId()) + " " + person.getName().getFullName(), fleetDesc, bountyData.flagshipName, shipType);
                    info.addShipList(cols, 1, iconSize, getFactionForUIColors().getBaseUIColor(), list, opad);

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
