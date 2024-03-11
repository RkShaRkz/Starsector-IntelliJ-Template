package data.scripts.campaign.bases;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.JumpPointAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.MutableStat.StatMod;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin;
import com.fs.starfarer.api.impl.campaign.DebugFlags;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteLocationCalculator;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.ids.Entities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.BountyResult;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.BountyResultType;
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarData;
import com.fs.starfarer.api.impl.campaign.intel.deciv.DecivTracker;
import com.fs.starfarer.api.impl.campaign.intel.raid.PirateRaidActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.ReturnStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.TravelStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidDelegate;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames;
import com.fs.starfarer.api.impl.campaign.procgen.MarkovNames.MarkovNameResult;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.AddedEntity;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.EntityLocation;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static java.lang.Math.random;
import static data.scripts.VayraModPlugin.VAYRA_DEBUG;
import static data.scripts.VayraModPlugin.createAdmin;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAIDERS;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAID_TIER1_FP;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAID_TIER2_FP;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAID_TIER3_FP;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAID_TIER4_FP;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.RAID_TIER5_FP;
import data.scripts.campaign.bases.VayraRaiderBaseManager.RaiderData;
import java.io.IOException;

public class VayraRaiderBaseIntel extends BaseIntelPlugin implements EveryFrameScript, FleetEventListener,
        EconomyUpdateListener, RaidDelegate {

    public RaiderData data;
    private transient boolean loaded = false;

    public static enum RaiderBaseTier {
        TIER_1_1MODULE,
        TIER_2_1MODULE,
        TIER_3_2MODULE,
        TIER_4_3MODULE,
        TIER_5_3MODULE,
    }

    public static Object BOUNTY_EXPIRED_PARAM = new Object();
    public static Object DISCOVERED_PARAM = new Object();

    public static class BaseBountyData {

        public float bountyElapsedDays = 0f;
        public float bountyDuration = 0;
        public float baseBounty = 0;
        public float repChange = 0;
        public FactionAPI bountyFaction = null;
    }

    public static Logger log = Global.getLogger(VayraRaiderBaseIntel.class);

    protected StarSystemAPI system;
    protected MarketAPI market;
    protected SectorEntityToken entity;

    protected float elapsedDays = 0f;
    protected float duration = 45f;

    protected BaseBountyData bountyData = null;

    protected RaiderBaseTier tier;
    protected RaiderBaseTier matchedStationToTier = null;

    protected IntervalUtil monthlyInterval = new IntervalUtil(20f, 40f);
    protected int raidTimeoutMonths = 0;

    public RaiderData getData() {
        return this.data;
    }

    @SuppressWarnings({"unchecked", "unchecked"})
    public VayraRaiderBaseIntel(StarSystemAPI system, String factionId, RaiderBaseTier tier) {

        if (VAYRA_DEBUG) log.info("attempting to spawn a " + factionId + " raider base in " + system.getNameWithLowercaseType());

        this.system = system;
        this.tier = tier;
        this.data = RAIDERS.get(factionId);

        this.market = Global.getFactory().createMarket(Misc.genUID(), Global.getSector().getFaction(factionId).getDisplayName() + "Raider Base", this.data.raiderBaseSize);
        this.market.setSize(this.data.raiderBaseSize);
        this.market.setHidden(true);

        this.market.setFactionId(factionId);

        this.market.setSurveyLevel(SurveyLevel.FULL);

        this.market.setFactionId(factionId);
        this.market.addCondition("population_" + this.data.raiderBaseSize);
        createAdmin(this.market);

        for (String condition : this.data.raiderBaseConditionsAndIndustries) {
            try {
                this.market.addCondition(condition);
            } catch (RuntimeException e) {
                this.market.addIndustry(condition);
            }
        }

        for (String submarket : this.data.raiderBaseSubmarkets) {
            this.market.addSubmarket(submarket);
        }

        this.market.getTariff().modifyFlat("default_tariff", this.market.getFaction().getTariffFraction());

        LinkedHashMap<LocationType, Float> weights = new LinkedHashMap<>();
        weights.put(LocationType.IN_ASTEROID_BELT, 10f);
        weights.put(LocationType.IN_ASTEROID_FIELD, 10f);
        weights.put(LocationType.IN_RING, 10f);
        weights.put(LocationType.IN_SMALL_NEBULA, 10f);
        weights.put(LocationType.GAS_GIANT_ORBIT, 10f);
        weights.put(LocationType.PLANET_ORBIT, 10f);
        WeightedRandomPicker<EntityLocation> locs = BaseThemeGenerator.getLocations(null, system, null, 100f, weights);
        EntityLocation loc = locs.pick();

        if (loc == null) {
            endImmediately();
            log.error("location was null, fuck it i'm leaving");
            return;
        }

        AddedEntity added = BaseThemeGenerator.addNonSalvageEntity(system, loc, Entities.MAKESHIFT_STATION, factionId);

        if (added == null || added.entity == null) {
            log.error("couldn't add an entity, fuck it i'm leaving");
            endImmediately();
            return;
        }

        this.entity = added.entity;

        String name = generateName(data);
        if (name == null) {
            log.error("couldn't come up with a name, fuck it i'm leaving");
            endImmediately();
            return;
        }

        this.market.setName(name);
        this.entity.setName(name);

        BaseThemeGenerator.convertOrbitWithSpin(this.entity, -5f);

        this.market.setPrimaryEntity(this.entity);
        this.entity.setMarket(this.market);

        this.entity.setSensorProfile(1f);
        this.entity.setDiscoverable(true);
        this.entity.getDetectedRangeMod().modifyFlat("gen", 5000f);

        this.market.setEconGroup(this.market.getId());
        this.market.getMemoryWithoutUpdate().set(DecivTracker.NO_DECIV_KEY, true);

        this.market.reapplyIndustries();

        Global.getSector().getEconomy().addMarket(market, true);

        log.info(String.format("Added raider base in [%s], tier: %s", system.getName(), tier.name()));

        FleetParamsV3 params = new FleetParamsV3(
                this.market,
                this.market.getLocation(), factionId, // quality will always be reduced by non-market-faction penalty, which is what we want 
                null,
                FleetTypes.PATROL_MEDIUM,
                69f, // combatPts
                0f, // freighterPts 
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                2f // qualityMod
        );

        CampaignFleetAPI fleet = PatrolFleetManager.createPatrolFleet(PatrolType.HEAVY, this.market, factionId, this.market.getLocation(), 0);
        if (fleet == null) {
            return;
        }

        SectorEntityToken marketEntity = this.market.getPrimaryEntity();
        marketEntity.getContainingLocation().addEntity(fleet);
        fleet.setLocation(marketEntity.getLocation().x, marketEntity.getLocation().y);

        PatrolFleetManager.PatrolFleetData fleetData = new PatrolFleetManager.PatrolFleetData(fleet, PatrolType.HEAVY);
        fleetData.startingFleetPoints = fleet.getFleetPoints();
        fleetData.sourceMarket = this.market;

        PatrolAssignmentAI ai = new PatrolAssignmentAI(fleet, fleetData);
        fleet.addScript(ai);

        log.info("Spawned patrol fleet [" + fleet.getNameWithFaction() + "] from raider base " + this.market.getName());

        Global.getSector().getIntelManager().addIntel(this, true);
        timestamp = null;

        Global.getSector().getListenerManager().addListener(this);
        Global.getSector().getEconomy().addUpdateListener(this);

        updateTarget();

        try {
            Class<? extends VayraRaiderBaseBarEvent> scriptClass = (Class<? extends VayraRaiderBaseBarEvent>) Global.getSettings().getScriptClassLoader().loadClass(data.raiderBaseBarEventScript);
            log.info("loaded class " + scriptClass);
            VayraRaiderBaseBarEvent script = scriptClass.newInstance();
            log.info("instantiated " + script + " as VayraRaiderBaseBarEvent");
            PortsideBarData.getInstance().addEvent(script);
            log.info("added " + script + " as an event to PortsideBarData.getInstance()");
            script.setIntel(this);
            log.info("set intel of " + script + " to this");
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            log.error("Raider base rumor BaseBarEvent script for "+this.data.raiderFactionId+"  is fucked up and threw a " + ex + " - adding the default one instead");
            PortsideBarData.getInstance().addEvent(new VayraRaiderBaseBarEvent(this));
            log.info("added VayraRaiderBaseBarEvent as an event to PortsideBarData.getInstance()");
            log.info("set intel of VayraRaiderBaseBarEvent to this");
        }

    }

    @Override
    public boolean isHidden() {
        if (super.isHidden()) {
            return true;
        }
        return timestamp == null;
    }

    public float getRaidFP() {
        float base = getBaseRaidFP();
        return base * (0.75f + (float) Math.random() * 0.5f);
    }

    public float getBaseRaidFP() {
        float base = 0f;
        switch (tier) {
            case TIER_1_1MODULE:
                base = RAID_TIER1_FP;
                break;
            case TIER_2_1MODULE:
                base = RAID_TIER2_FP;
                break;
            case TIER_3_2MODULE:
                base = RAID_TIER3_FP;
                break;
            case TIER_4_3MODULE:
                base = RAID_TIER4_FP;
                break;
            case TIER_5_3MODULE:
                base = RAID_TIER5_FP;
                break;
        }
        return base * (0.75f + (float) Math.random() * 0.5f);
    }

    @Override
    public void notifyRaidEnded(RaidIntel raid, RaidStageStatus status) {
        if (status == RaidStageStatus.SUCCESS) {
            raidTimeoutMonths = 0;
        } else {
            float base = getBaseRaidFP();
            float raidFP = raid.getAssembleStage().getOrigSpawnFP();
            raidTimeoutMonths += Math.round(raidFP / base) * 2;
        }
    }

    public void startRaid(StarSystemAPI target, float raidFP) {
        if (target == null) {
            log.info("We were gonna try to raid, but target is null");
            return;
        }
        log.info(String.format("Ok we're gonna try to raid [%s], our Fleet Points: %s", target.getName(), (int) raidFP));
        boolean hasTargets = false;
        for (MarketAPI curr : Misc.getMarketsInLocation(target)) {
            if (curr.getFaction().isHostileTo(getFactionForUIColors())) {
                hasTargets = true;
                log.info(String.format("We're hostile to [%s], raid is a go so far...", target.getName()));
                break;
            }
        }

        if (!hasTargets) {
            log.info("We don't have a target anymore -- or it wasn't hostile");
            return;
        }

        RaidIntel raid = new RaidIntel(target, getFactionForUIColors(), this);

        float successMult = 0.75f;

        JumpPointAPI gather = null;
        @SuppressWarnings("unchecked")
        List<JumpPointAPI> points = system.getEntities(JumpPointAPI.class);
        float min = Float.MAX_VALUE;
        for (JumpPointAPI curr : points) {
            float dist = Misc.getDistance(entity.getLocation(), curr.getLocation());
            if (dist < min) {
                min = dist;
                gather = curr;
            }
        }

        VayraRaiderAssembleStage assemble = new VayraRaiderAssembleStage(raid, gather, this);
        assemble.addSource(market);
        assemble.setSpawnFP(raidFP);
        assemble.setAbortFP(raidFP * successMult);
        raid.addStage(assemble);
        log.info(String.format("Assembling raiders via our RaiderAssembleStage via our BaseIntel script in [%s], market: [%s], FP: [%s]", system.getName(), market.getName(), (int) raidFP));
        for (RouteManager.RouteData route : assemble.getRoutes()) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet != null) {
                log.info(String.format("Assembled raider fleet [%s], FP [%s], size [%s], loc [%s]", fleet.getNameWithFaction(), fleet.getFleetPoints(), fleet.getFleetSizeCount(), fleet.getContainingLocation().getNameWithLowercaseType()));
            }
        }

        SectorEntityToken raidJump = RouteLocationCalculator.findJumpPointToUse(getFactionForUIColors(), target.getCenter());

        TravelStage travel = new TravelStage(raid, gather, raidJump, false);
        travel.setAbortFP(raidFP * successMult);
        raid.addStage(travel);

        PirateRaidActionStage action = new PirateRaidActionStage(raid, target);
        action.setAbortFP(raidFP * successMult);
        raid.addStage(action);

        raid.addStage(new ReturnStage(raid));

        boolean shouldNotify = raid.shouldSendUpdate();
        Global.getSector().getIntelManager().addIntel(raid, !shouldNotify);

    }

    public StarSystemAPI getSystem() {
        return system;
    }

    protected String pickStationType() {
        WeightedRandomPicker<String> stations = new WeightedRandomPicker<>();

        if (getFactionForUIColors().getCustom().has(Factions.CUSTOM_PIRATE_BASE_STATION_TYPES)) {
            try {
                JSONObject json = getFactionForUIColors().getCustom().getJSONObject(Factions.CUSTOM_PIRATE_BASE_STATION_TYPES);
                for (String key : JSONObject.getNames(json)) {
                    stations.add(key, (float) json.optDouble(key, 0f));
                }
            } catch (JSONException e) {
                stations.clear();
            }
        }

        if (stations.isEmpty()) {
            for (String station : data.raiderBaseTypes.keySet()) {
                stations.add(station, data.raiderBaseTypes.get(station));
            }
        }

        String station = null;

        if (!stations.isEmpty()) {
            station = stations.pick();
        }

        return station;
    }

    protected Industry getStationIndustry() {
        for (Industry curr : market.getIndustries()) {
            if (curr.getSpec().hasTag(Industries.TAG_STATION)) {
                return curr;
            }
        }
        return null;
    }

    protected void updateStationIfNeeded() {
        if (matchedStationToTier == tier) {
            return;
        }

        matchedStationToTier = tier;
        monthsAtCurrentTier = 0;

        Industry stationInd = getStationIndustry();

        String currIndId = null;
        if (stationInd != null) {
            currIndId = stationInd.getId();
            market.removeIndustry(stationInd.getId(), null, false);
        }

        if (currIndId == null) {
            currIndId = pickStationType();
        }

        if (currIndId == null) {
            return;
        }

        market.addIndustry(currIndId);

        if (stationInd == null) {
            return;
        }

        stationInd.finishBuildingOrUpgrading();

        CampaignFleetAPI fleet = Misc.getStationFleet(entity);
        if (fleet == null) {
            return;
        }

        List<FleetMemberAPI> members = fleet.getFleetData().getMembersListCopy();
        if (members.size() < 1) {
            return;
        }

        fleet.inflateIfNeeded();

        FleetMemberAPI station = members.get(0);

        WeightedRandomPicker<Integer> picker = new WeightedRandomPicker<>();
        int index = 1; // index 0 is station body
        for (String slotId : station.getVariant().getModuleSlots()) {
            ShipVariantAPI mv = station.getVariant().getModuleVariant(slotId);
            if (Misc.isActiveModule(mv)) {
                picker.add(index, 1f);
            }
            index++;
        }

        float removeMult = 0f;

        switch (tier) {
            case TIER_1_1MODULE:
            case TIER_2_1MODULE:
                removeMult = 0.67f;
                break;
            case TIER_3_2MODULE:
                removeMult = 0.33f;
                break;
            case TIER_4_3MODULE:
            case TIER_5_3MODULE:
                removeMult = 0;
                break;

        }

        int remove = Math.round(picker.getItems().size() * removeMult);
        if (remove < 1 && removeMult > 0) {
            remove = 1;
        }
        if (remove >= picker.getItems().size()) {
            remove = picker.getItems().size() - 1;
        }

        for (int i = 0; i < remove; i++) {
            Integer pick = picker.pickAndRemove();
            if (pick != null) {
                station.getStatus().setHullFraction(pick, 0f);
                station.getStatus().setDetached(pick, true);
                station.getStatus().setPermaDetached(pick, true);
            }
        }
    }

    protected CampaignFleetAPI addedListenerTo = null;

    @Override
    protected void advanceImpl(float amount) {
        float days = Global.getSector().getClock().convertToDays(amount);
        if (getPlayerVisibleTimestamp() == null && entity.isInCurrentLocation() && isHidden()) {
            makeKnown();
            sendUpdateIfPlayerHasIntel(DISCOVERED_PARAM, false);
        }

        if (!sentBountyUpdate && bountyData != null
                && (Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay()
                || (!isHidden() && DebugFlags.SEND_UPDATES_WHEN_NO_COMM))) {
            makeKnown();
            sendUpdateIfPlayerHasIntel(bountyData, false);
            sentBountyUpdate = true;
        }

        CampaignFleetAPI fleet = Misc.getStationFleet(market);
        if (fleet != null && addedListenerTo != fleet) {
            if (addedListenerTo != null) {
                addedListenerTo.removeEventListener(this);
            }
            fleet.addEventListener(this);
            addedListenerTo = fleet;
        }

        if (target != null) {
            if (getAffectedMarkets(target).isEmpty()) {
                clearTarget();
                log.info("Target was empty, clearing target");
            }
        }

        if (VAYRA_DEBUG) {
            days *= 2f;
        }

        monthlyInterval.advance(days);
        if (monthlyInterval.intervalElapsed()) {
            monthsWithSameTarget++;
            raidTimeoutMonths--;
            if (raidTimeoutMonths < 0) {
                raidTimeoutMonths = 0;
            }

            if ((monthsWithSameTarget > 6 && (float) Math.random() < 0.20f) || monthsWithSameTarget > 12 || target == null) {
                updateTarget();
            }
            if (target != null
                    && (float) Math.random() < monthsWithSameTarget * 0.05f
                    && bountyData == null) {
                setBounty();
            }

            boolean allowRandomRaids = VayraRaiderBaseManager.getInstance().getDaysSinceStart() > Global.getSettings().getFloat("noPirateRaidDays");
            if (VAYRA_DEBUG) {
                allowRandomRaids = true;
            }

            if (target != null
                    && (((float) Math.random() < 0.2f && allowRandomRaids)) && raidTimeoutMonths <= 0) {
                startRaid(target, getRaidFP());
                log.info(String.format("Starting raid on [%s], our Fleet Points: %s", target.getName(), (int) getRaidFP()));
                raidTimeoutMonths = 2 + Math.round((float) Math.random() * 3f);
            }

            checkForTierChange();
        }

        if (bountyData != null) {
            boolean canEndBounty = !entity.isInCurrentLocation();
            bountyData.bountyElapsedDays += days;
            if (bountyData.bountyElapsedDays > bountyData.bountyDuration && canEndBounty) {
                endBounty();
            }
        }

        updateStationIfNeeded();
    }

    protected void checkForTierChange() {
        if (bountyData != null) {
            return;
        }
        if (entity.isInCurrentLocation()) {
            return;
        }

        float minMonths = Global.getSettings().getFloat("pirateBaseMinMonthsForNextTier");
        if (monthsAtCurrentTier > minMonths) {
            float prob = (monthsAtCurrentTier - minMonths) * 0.1f;
            if ((float) Math.random() < prob) {
                RaiderBaseTier next = getNextTier(tier);
                if (next != null) {
                    tier = next;
                    updateStationIfNeeded();
                    monthsAtCurrentTier = 0;
                    return;
                }
            }
        }

        monthsAtCurrentTier++;
    }

    protected RaiderBaseTier getNextTier(RaiderBaseTier tier) {
        switch (tier) {
            case TIER_1_1MODULE:
                return RaiderBaseTier.TIER_2_1MODULE;
            case TIER_2_1MODULE:
                return RaiderBaseTier.TIER_3_2MODULE;
            case TIER_3_2MODULE:
                return RaiderBaseTier.TIER_4_3MODULE;
            case TIER_4_3MODULE:
                return RaiderBaseTier.TIER_5_3MODULE;
            case TIER_5_3MODULE:
                return null;
        }
        return null;
    }

    protected RaiderBaseTier getPrevTier(RaiderBaseTier tier) {
        switch (tier) {
            case TIER_1_1MODULE:
                return null;
            case TIER_2_1MODULE:
                return RaiderBaseTier.TIER_1_1MODULE;
            case TIER_3_2MODULE:
                return RaiderBaseTier.TIER_2_1MODULE;
            case TIER_4_3MODULE:
                return RaiderBaseTier.TIER_3_2MODULE;
            case TIER_5_3MODULE:
                return RaiderBaseTier.TIER_4_3MODULE;
        }
        return null;
    }

    public void makeKnown() {
        makeKnown(null);
    }

    public void makeKnown(TextPanelAPI text) {

        if (getPlayerVisibleTimestamp() == null) {
            Global.getSector().getIntelManager().removeIntel(this);
            Global.getSector().getIntelManager().addIntel(this, text == null, text);
        }
    }

    @Override
    public float getTimeRemainingFraction() {
        float f = 1f - elapsedDays / duration;
        return f;
    }

    @Override
    protected void notifyEnding() {
        super.notifyEnding();
        log.info(String.format("Removing raider base at [%s]", system.getName()));
        Global.getSector().getListenerManager().removeListener(this);
        clearTarget();

        Global.getSector().getEconomy().removeMarket(market);
        Global.getSector().getEconomy().removeUpdateListener(this);
        Misc.removeRadioChatter(market);
        market.advance(0f);
    }

    @Override
    protected void notifyEnded() {
        super.notifyEnded();
    }

    protected BountyResult result = null;

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (isEnding()) {
            return;
        }

        if (addedListenerTo != null && fleet == addedListenerTo) {
            Misc.fadeAndExpire(entity);
            endAfterDelay();

            result = new BountyResult(BountyResultType.END_OTHER, 0, null);

            if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE
                    && param instanceof BattleAPI) {
                BattleAPI battle = (BattleAPI) param;
                if (battle.isPlayerInvolved()) {
                    int payment = 0;
                    if (bountyData != null) {
                        payment = (int) (bountyData.baseBounty * battle.getPlayerInvolvementFraction());
                    }
                    if (payment > 0) {
                        Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);

                        CustomRepImpact impact = new CustomRepImpact();
                        impact.delta = bountyData.repChange * battle.getPlayerInvolvementFraction();
                        if (impact.delta < 0.01f) {
                            impact.delta = 0.01f;
                        }
                        ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM,
                                        impact, null, null, false, true),
                                bountyData.bountyFaction.getId());

                        result = new BountyResult(BountyResultType.END_PLAYER_BOUNTY, payment, rep);
                    } else {
                        result = new BountyResult(BountyResultType.END_PLAYER_NO_REWARD, 0, null);
                    }
                }
            }

            @SuppressWarnings("UnusedAssignment")
            boolean sendUpdate = DebugFlags.SEND_UPDATES_WHEN_NO_COMM
                    || result.type != BountyResultType.END_OTHER
                    || Global.getSector().getIntelManager().isPlayerInRangeOfCommRelay();
            sendUpdate = true;
            if (sendUpdate) {
                sendUpdateIfPlayerHasIntel(result, false);
            }

            VayraRaiderBaseManager.getInstance().incrDestroyed();
        }
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

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

        if (bountyData != null && result == null) {
            if (getListInfoParam() != BOUNTY_EXPIRED_PARAM) {
                if (isUpdate || mode != ListInfoMode.IN_DESC) {
                    FactionAPI faction = bountyData.bountyFaction;
                    info.addPara("Bounty faction: " + faction.getDisplayName(), initPad, tc,
                            faction.getBaseUIColor(), faction.getDisplayName());
                    initPad = 0f;
                }
                info.addPara("%s reward", initPad, tc, h, Misc.getDGSCredits(bountyData.baseBounty));
                addDays(info, "remaining", bountyData.bountyDuration - bountyData.bountyElapsedDays, tc);
            }
        }

        if (result != null && bountyData != null) {
            switch (result.type) {
                case END_PLAYER_BOUNTY:
                    info.addPara("%s received", initPad, tc, h, Misc.getDGSCredits(result.payment));
                    CoreReputationPlugin.addAdjustmentMessage(result.rep.delta, bountyData.bountyFaction, null,
                            null, null, info, tc, isUpdate, 0f);
                    break;
                case END_TIME:
                    break;
                case END_OTHER:
                    break;

            }
        }

        unindent(info);
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color c = getTitleColor(mode);
        info.addPara(getName(), c, 0f);
        addBulletPoints(info, mode);
    }

    @Override
    public String getSortString() {
        String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());
        return base + " Camp";
    }

    public String getName() {
        String base = Misc.ucFirst(getFactionForUIColors().getPersonNamePrefix());

        if (getListInfoParam() == bountyData && bountyData != null) {
            return base + " Camp - Bounty Posted";
        } else if (getListInfoParam() == BOUNTY_EXPIRED_PARAM) {
            return base + " Camp - Bounty Expired";
        }

        if (result != null) {
            if (result.type == BountyResultType.END_PLAYER_BOUNTY) {
                return base + " Camp - Bounty Completed";
            } else if (result.type == BountyResultType.END_PLAYER_NO_REWARD) {
                return base + " Camp - Destroyed";
            }
        }

        String name = market.getName();
        if (isEnding()) {
            return base + " Camp - Abandoned";
        }
        if (getListInfoParam() == DISCOVERED_PARAM) {
            return base + " Camp - Discovered";
        }
        if (entity.isDiscoverable()) {
            return base + " Camp - Exact Location Unknown";
        }
        return base + " Camp - " + name;
    }

    @Override
    public FactionAPI getFactionForUIColors() {
        return market.getFaction();
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

        FactionAPI faction = market.getFaction();

        info.addImage(faction.getLogo(), width, 128, opad);

        String has = faction.getDisplayNameHasOrHave();

        info.addPara(Misc.ucFirst(faction.getDisplayNameWithArticle()) + " " + has
                + " established a camp in the "
                + market.getContainingLocation().getNameWithLowercaseType() + ". "
                + "The camp serves as a staging ground for assaults on nearby settlements.",
                opad, faction.getBaseUIColor(), faction.getDisplayNameWithArticleWithoutArticle());

        if (!entity.isDiscoverable()) {
            switch (tier) {
                case TIER_1_1MODULE:
                    info.addPara("It has very limited defensive capabilities "
                            + "and is protected by a few fleets.", opad);
                    break;
                case TIER_2_1MODULE:
                    info.addPara("It has limited defensive capabilities "
                            + "and is protected by a small number of fleets.", opad);
                    break;
                case TIER_3_2MODULE:
                    info.addPara("It has fairly well-developed defensive capabilities "
                            + "and is protected by a considerable number of fleets.", opad);
                    break;
                case TIER_4_3MODULE:
                    info.addPara("It has very well-developed defensive capabilities "
                            + "and is protected by a large number of fleets.", opad);
                    break;
                case TIER_5_3MODULE:
                    info.addPara("It has very well-developed defensive capabilities "
                            + "and is protected by a large number of fleets. Both the "
                            + "camp and the fleets have elite-level equipment.", opad);
                    break;

            }
        } else {
            info.addPara("You have not yet discovered the exact location or capabilities of this camp.", opad);
        }

        info.addSectionHeading("Recent events",
                faction.getBaseUIColor(), faction.getDarkUIColor(), Alignment.MID, opad);

        if (target != null && !getAffectedMarkets(target).isEmpty() && !isEnding()) {
            info.addPara(faction.getDisplayName() + " forces operating from this camp have been targeting the "
                    + target.getNameWithLowercaseType() + ".", opad);
        }

        if (bountyData != null) {
            info.addPara(Misc.ucFirst(bountyData.bountyFaction.getDisplayNameWithArticle()) + " "
                    + bountyData.bountyFaction.getDisplayNameHasOrHave()
                    + " posted a bounty for the destruction of this camp.",
                    opad, bountyData.bountyFaction.getBaseUIColor(),
                    bountyData.bountyFaction.getDisplayNameWithArticleWithoutArticle());

            if (result != null && result.type == BountyResultType.END_PLAYER_BOUNTY) {
                info.addPara("You have successfully completed this bounty.", opad);
            }

            addBulletPoints(info, ListInfoMode.IN_DESC);
        }

        if (result != null) {
            if (result.type == BountyResultType.END_PLAYER_NO_REWARD) {
                info.addPara("You have destroyed this camp.", opad);
            } else if (result.type == BountyResultType.END_OTHER) {
                info.addPara("It is rumored that this camp is no longer operational.", opad);
            }
        }
    }

    @Override
    public String getIcon() {

        String spritePath = data.raiderBaseIntelIcon;

        SpriteAPI sprite = Global.getSettings().getSprite(spritePath);
        // Load sprite if it hasn't been loaded yet - not needed if you add it to settings.json
        if (!loaded) {
            try {
                Global.getSettings().loadTexture(spritePath);
                loaded = true;
            } catch (IOException ex) {
                throw new RuntimeException("Failed to load sprite '" + spritePath + "'!", ex);
            }
        }

        return spritePath;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        if (bountyData != null) {
            tags.add(Tags.INTEL_BOUNTY);
        }
        tags.add(Tags.INTEL_EXPLORATION);

        if (target != null && !Misc.getMarketsInLocation(target, Factions.PLAYER).isEmpty()) {
            tags.add(Tags.INTEL_COLONIES);
        }

        tags.add(market.getFactionId());
        if (bountyData != null) {
            tags.add(bountyData.bountyFaction.getId());
        }
        return tags;
    }

    @Override
    public SectorEntityToken getMapLocation(SectorMapAPI map) {
        if (market.getPrimaryEntity().isDiscoverable()) {
            return system.getCenter();
        }
        return market.getPrimaryEntity();
    }

    protected static String generateName(RaiderData data) {
        MarkovNames.loadIfNeeded();

        MarkovNameResult gen;
        for (int i = 0; i < 10; i++) {
            gen = MarkovNames.generate(null);
            if (gen != null) {
                String test = gen.name;
                if (test.toLowerCase().startsWith("the ")) {
                    continue;
                }
                String p = pickPostfix(data);
                if (p != null && !p.isEmpty()) {
                    test += " " + p;
                }
                if (test.length() > 22) {
                    continue;
                }

                return test;
            }
        }
        return null;
    }

    protected static String pickPostfix(RaiderData data) {
        WeightedRandomPicker<String> post = new WeightedRandomPicker<>();
        for (String name : data.raiderBaseTypeNames) {
            post.add(name);
        }
        return post.pick();
    }

    @Override
    public void commodityUpdated(String commodityId) {
        CommodityOnMarketAPI com = market.getCommodityData(commodityId);
        int curr = 0;
        String modId = market.getId();
        StatMod mod = com.getAvailableStat().getFlatStatMod(modId);
        if (mod != null) {
            curr = Math.round(mod.value);
        }

        int a = com.getAvailable() - curr;
        int d = com.getMaxDemand();
        if (d > a) {
            int supply = Math.max(1, d - a);
            com.getAvailableStat().modifyFlat(modId, supply, "Captured by raiders");
        }
    }

    @Override
    public void economyUpdated() {

        float fleetSizeBonus = 1f;
        float qualityBonus = 0f;
        int light = 0;
        int medium = 0;
        int heavy = 0;

        switch (tier) {
            case TIER_1_1MODULE:
                qualityBonus = 0f;
                fleetSizeBonus = 0.2f;
                break;
            case TIER_2_1MODULE:
                qualityBonus = 0.2f;
                fleetSizeBonus = 0.3f;
                light = 2;
                break;
            case TIER_3_2MODULE:
                qualityBonus = 0.3f;
                fleetSizeBonus = 0.4f;
                light = 2;
                medium = 1;
                break;
            case TIER_4_3MODULE:
                qualityBonus = 0.4f;
                fleetSizeBonus = 0.5f;
                light = 2;
                medium = 2;
                break;
            case TIER_5_3MODULE:
                qualityBonus = 0.5f;
                fleetSizeBonus = 0.75f;
                light = 2;
                medium = 2;
                heavy = 2;
                break;
        }

        market.getStats().getDynamic().getMod(Stats.FLEET_QUALITY_MOD).
                modifyFlatAlways(market.getId(), qualityBonus,
                        "Development level");

        market.getStats().getDynamic().getMod(Stats.COMBAT_FLEET_SIZE_MULT).modifyFlatAlways(market.getId(),
                fleetSizeBonus,
                "Development level");

        String modId = market.getId();
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).modifyFlat(modId, light);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).modifyFlat(modId, medium);
        market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).modifyFlat(modId, heavy);
    }

    @Override
    public boolean isEconomyListenerExpired() {
        return isEnded();
    }

    public MarketAPI getMarket() {
        return market;
    }

    protected void setBounty() {
        bountyData = new BaseBountyData();
        float base = 100000f;
        switch (tier) {
            case TIER_1_1MODULE:
                base = Global.getSettings().getFloat("pirateBaseBounty1");
                bountyData.repChange = 0.02f;
                break;
            case TIER_2_1MODULE:
                base = Global.getSettings().getFloat("pirateBaseBounty2");
                bountyData.repChange = 0.05f;
                break;
            case TIER_3_2MODULE:
                base = Global.getSettings().getFloat("pirateBaseBounty3");
                bountyData.repChange = 0.06f;
                break;
            case TIER_4_3MODULE:
                base = Global.getSettings().getFloat("pirateBaseBounty4");
                bountyData.repChange = 0.07f;
                break;
            case TIER_5_3MODULE:
                base = Global.getSettings().getFloat("pirateBaseBounty5");
                bountyData.repChange = 0.1f;
                break;
        }

        bountyData.baseBounty = base * (0.9f + (float) Math.random() * 0.2f);

        bountyData.baseBounty = (int) (bountyData.baseBounty / 10000) * 10000;

        WeightedRandomPicker<FactionAPI> picker = new WeightedRandomPicker<>();
        for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(target)) {
            if (curr.getFaction().isPlayerFaction()) {
                continue;
            }
            if (affectsMarket(curr)) {
                picker.add(curr.getFaction(), (float) Math.pow(2f, curr.getSize()));
            }
        }

        FactionAPI faction = picker.pick();
        if (faction == null) {
            bountyData = null;
            return;
        }

        if (random() > 0.5) {
            bountyData.bountyFaction = faction;
        } else {
            bountyData.bountyFaction = faction;
        }
        bountyData.bountyDuration = 180f;
        bountyData.bountyElapsedDays = 0f;

        Misc.makeImportant(entity, "baseBounty");

        sentBountyUpdate = false;
    }

    protected boolean sentBountyUpdate = false;

    protected void endBounty() {
        sendUpdateIfPlayerHasIntel(BOUNTY_EXPIRED_PARAM, false);
        bountyData = null;
        sentBountyUpdate = false;
        Misc.makeUnimportant(entity, "baseBounty");
    }

    protected int monthsWithSameTarget = 0;
    protected int monthsAtCurrentTier = 0;
    protected StarSystemAPI target = null;

    @SuppressWarnings("ResultOfObjectAllocationIgnored")
    private void updateTarget() {
        log.info("updating target");
        StarSystemAPI newTarget = pickTarget();
        if (newTarget == target) {
            log.info("new target... same as the old target");
            return;
        }

        clearTarget();
        log.info("target cleared");

        target = newTarget;
        monthsWithSameTarget = 0;

        if (target != null) {
            log.info(String.format("New target is [%s]", newTarget.getNameWithLowercaseType()));
            new VayraRaiderActivityIntel(target, this);
        } else {
            log.info("New target is null... nevermind");
        }
    }

    public StarSystemAPI getTarget() {
        return target;
    }

    protected void clearTarget() {
        if (target != null) {
            target = null;
            monthsWithSameTarget = 0;
        }
    }

    public List<MarketAPI> getAffectedMarkets(StarSystemAPI system) {
        List<MarketAPI> affectedMarket = new ArrayList<>();
        for (MarketAPI curr : Global.getSector().getEconomy().getMarkets(system)) {
            if (!affectsMarket(curr)) {
                continue;
            }
            affectedMarket.add(curr);
        }
        return affectedMarket;
    }

    public boolean affectsMarket(MarketAPI market) {
        if (market.isHidden()) {
            return false;
        }
        return market.getFaction().isHostileTo(this.market.getFaction());
    }

    protected StarSystemAPI pickTarget() {

        boolean targetsInSystem = false;
        WeightedRandomPicker<MarketAPI> picker = new WeightedRandomPicker<>();
        FactionAPI faction = Global.getSector().getFaction(data.raiderFactionId);

        for (MarketAPI targetMarket : Global.getSector().getEconomy().getMarketsCopy()) {

            String targetFactionId = targetMarket.getFactionId();
            float repWeight = -faction.getRelationship(targetFactionId);
            repWeight = Math.max(0f, repWeight);
            
            if (!faction.isHostileTo(targetFactionId)) {
                continue;
            }

            if (repWeight <= 0) {
                continue;
            }

            if (data.raidTargetWeights.containsKey(targetFactionId)) {
                picker.add(targetMarket, targetMarket.getSize() * repWeight * data.raidTargetWeights.get(targetFactionId));
            } else {
                picker.add(targetMarket, targetMarket.getSize() * repWeight);
            }

            if (market.getStarSystem().equals(targetMarket.getStarSystem())) {
                targetsInSystem = true;
            }
        }

        MarketAPI newTargetMarket = picker.pick();
        if (newTargetMarket == null) {
            log.error("target market was null, nevermind");
            return null;
        }
        StarSystemAPI newTarget = newTargetMarket.getStarSystem();

        if (targetsInSystem) {
            newTarget = market.getStarSystem();
        }

        return newTarget;
    }

    @Override
    public List<ArrowData> getArrowData(SectorMapAPI map) {
        if (target == null || target == entity.getContainingLocation()) {
            return null;
        }

        List<ArrowData> arrowDataResult = new ArrayList<>();

        SectorEntityToken entityFrom = entity;
        if (map != null) {
            SectorEntityToken iconEntity = map.getIntelIconEntity(this);
            if (iconEntity != null) {
                entityFrom = iconEntity;
            }
        }

        ArrowData arrow = new ArrowData(entityFrom, target.getCenter());
        arrow.color = getFactionForUIColors().getBaseUIColor();
        arrowDataResult.add(arrow);

        return arrowDataResult;
    }

    public float getAccessibilityPenalty() {
        switch (tier) {
            case TIER_1_1MODULE:
                return 0.1f;
            case TIER_2_1MODULE:
                return 0.2f;
            case TIER_3_2MODULE:
                return 0.3f;
            case TIER_4_3MODULE:
                return 0.4f;
            case TIER_5_3MODULE:
                return 0.5f;
        }
        return 0f;
    }

    public float getStabilityPenalty() {
        switch (tier) {
            case TIER_1_1MODULE:
                return 1f;
            case TIER_2_1MODULE:
                return 1f;
            case TIER_3_2MODULE:
                return 2f;
            case TIER_4_3MODULE:
                return 2f;
            case TIER_5_3MODULE:
                return 3f;
        }
        return 0f;
    }

    public RaiderBaseTier getTier() {
        return tier;
    }

    public SectorEntityToken getEntity() {
        return entity;
    }

}
