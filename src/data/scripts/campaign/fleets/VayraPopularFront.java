package data.scripts.campaign.fleets;

import java.util.Random;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.VayraModPlugin.VAYRA_DEBUG;
import static data.scripts.campaign.fleets.VayraPopularFrontManager.JOINT_FACTION;
import static data.scripts.campaign.fleets.VayraPopularFrontManager.POSSIBLE_ALLIES;
import org.apache.log4j.Logger;

public class VayraPopularFront extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

    public static Logger log = Global.getLogger(VayraPopularFront.class);

    // maximum patrols per level
    private static final int BASE_LIGHT = 3;
    private static final int BASE_MEDIUM = 2;
    private static final int BASE_HEAVY = 1;

    // FP (100-150% of this, multiplied by 5, multiplied by level)
    private static final float LIGHT_FP = 4f;
    private static final float MEDIUM_FP = 8f;
    private static final float HEAVY_FP = 16f;

    private final WeightedRandomPicker<String> allies = new WeightedRandomPicker<>();

    private int level = 1;

    public String pickFaction() {
        if (allies.isEmpty()) {
            for (String faction : POSSIBLE_ALLIES) {
                if (Global.getSector().getFaction(faction) != null) {
                    allies.add(faction);
                }
            }
        }
        return allies.pickAndRemove();
    }

    @Override
    public boolean isHidden() {
        return !POSSIBLE_ALLIES.contains(market.getFactionId());
    }

    @Override
    public boolean isFunctional() {
        return super.isFunctional() && POSSIBLE_ALLIES.contains(market.getFactionId());
    }

    @Override
    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.SHIPS, size - 2);
        demand(Commodities.HAND_WEAPONS, size - 1);

        supply(Commodities.SUPPLIES, size - 1);
        supply(Commodities.FUEL, size - 1);
        supply(Commodities.METALS, size - 1);
        supply(Commodities.CREW, size - 2);
        supply(Commodities.MARINES, size - 2);
        supply(Commodities.ORGANICS, size - 2);
        supply(Commodities.DOMESTIC_GOODS, size - 2);
        supply(Commodities.RARE_METALS, size - 2);

        modifyStabilityWithBaseMod();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), true, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), true, -1);

        if (!isFunctional()) {
            supply.clear();
            unapply();
        }
    }

    @Override
    public void unapply() {
        super.unapply();

        MemoryAPI memory = market.getMemoryWithoutUpdate();
        Misc.setFlagWithReason(memory, MemFlags.MARKET_PATROL, getModId(), false, -1);
        Misc.setFlagWithReason(memory, MemFlags.MARKET_MILITARY, getModId(), false, -1);

        unmodifyStabilityWithBaseMod();
    }

    @Override
    protected boolean hasPostDemandSection(boolean hasDemand, Industry.IndustryTooltipMode mode) {
        return mode != Industry.IndustryTooltipMode.NORMAL || isFunctional();
    }

    @Override
    protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, Industry.IndustryTooltipMode mode) {
        if (mode != Industry.IndustryTooltipMode.NORMAL || isFunctional()) {
            addStabilityPostDemandSection(tooltip, hasDemand, mode);
        }
    }

    @Override
    protected int getBaseStabilityMod() {
        return 2;
    }

    @Override
    public String getNameForModifier() {
        if (getSpec().getName().contains("HQ")) {
            return getSpec().getName();
        }
        return Misc.ucFirst(getSpec().getName());
    }

    @Override
    protected Pair<String, Integer> getStabilityAffectingDeficit() {
        return getMaxDeficit(Commodities.SUPPLIES, Commodities.FUEL, Commodities.SHIPS, Commodities.HAND_WEAPONS);
    }

    @Override
    public String getCurrentImage() {
        return super.getCurrentImage();
    }

    @Override
    public boolean isDemandLegal(CommodityOnMarketAPI com) {
        return true;
    }

    @Override
    public boolean isSupplyLegal(CommodityOnMarketAPI com) {
        return true;
    }

    protected IntervalUtil tracker = new IntervalUtil(Global.getSettings().getFloat("averagePatrolSpawnInterval") * 0.7f,
            Global.getSettings().getFloat("averagePatrolSpawnInterval") * 1.3f);

    protected float returningPatrolValue = 0f;

    @Override
    protected void buildingFinished() {
        super.buildingFinished();

        tracker.forceIntervalElapsed();
    }

    @Override
    protected void upgradeFinished(Industry previous) {
        super.upgradeFinished(previous);

        tracker.forceIntervalElapsed();
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (Global.getSector().getEconomy().isSimMode()) {
            return;
        }

        if (!isFunctional()) {
            return;
        }

        float days = Global.getSector().getClock().convertToDays(amount);

        float spawnRate = 1f;
        float rateMult = market.getStats().getDynamic().getStat(Stats.COMBAT_FLEET_SPAWN_RATE_MULT).getModifiedValue();
        spawnRate *= rateMult;

        float extraTime = 0f;
        if (returningPatrolValue > 0) {
            // apply "returned patrols" to spawn rate, at a maximum rate of 1 interval per day
            float interval = tracker.getIntervalDuration();
            extraTime = interval * days;
            returningPatrolValue -= days;
            if (returningPatrolValue < 0) {
                returningPatrolValue = 0;
            }
        }
        if (VAYRA_DEBUG) {
            days *= 5f;
        }
        tracker.advance(days * spawnRate + extraTime);

        if (tracker.intervalElapsed()) {
            
            revealIfCommissioned();

            int newLevel = 0;
            if (market.hasIndustry(Industries.PATROLHQ) && !market.getIndustry(Industries.PATROLHQ).isDisrupted()) {
                newLevel += 1;
            }
            if (market.hasIndustry(Industries.MILITARYBASE) && !market.getIndustry(Industries.MILITARYBASE).isDisrupted()) {
                newLevel += 2;
            }
            if (market.hasIndustry(Industries.HIGHCOMMAND) && !market.getIndustry(Industries.HIGHCOMMAND).isDisrupted()) {
                newLevel += 3;
            }
            level = newLevel;

            String sid = getRouteSourceId();

            int light = getCount(PatrolType.FAST);
            int medium = getCount(PatrolType.COMBAT);
            int heavy = getCount(PatrolType.HEAVY);

            int maxLight = level * BASE_LIGHT;
            int maxMedium = level * BASE_MEDIUM;
            int maxHeavy = level * BASE_HEAVY;

            WeightedRandomPicker<PatrolType> picker = new WeightedRandomPicker<>();
            picker.add(PatrolType.HEAVY, maxHeavy - heavy);
            picker.add(PatrolType.COMBAT, maxMedium - medium);
            picker.add(PatrolType.FAST, maxLight - light);

            if (picker.isEmpty()) {
                return;
            }

            PatrolType type = picker.pick();
            PatrolFleetData custom = new PatrolFleetData(type);

            OptionalFleetData extra = new OptionalFleetData(market);
            extra.fleetType = type.getFleetType();

            RouteData route = RouteManager.getInstance().addRoute(sid, market, Misc.genRandomSeed(), extra, this, custom);
            float patrolDays = 35f + (float) Math.random() * 10f;

            route.addSegment(new RouteSegment(patrolDays, market.getPrimaryEntity()));
        }
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {
    }

    @Override
    public boolean shouldRepeat(RouteData route) {
        return false;
    }

    public int getCount(PatrolType... types) {
        int count = 0;
        for (RouteData data : RouteManager.getInstance().getRoutesForSource(getRouteSourceId())) {
            if (data.getCustom() instanceof PatrolFleetData) {
                PatrolFleetData custom = (PatrolFleetData) data.getCustom();
                for (PatrolType type : types) {
                    if (type == custom.type) {
                        count++;
                        break;
                    }
                }
            }
        }
        return count;
    }

    public int getMaxPatrols(PatrolType type) {
        if (type == PatrolType.FAST) {
            int base = (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_LIGHT_MOD).computeEffective(0);
            return base;
        }
        if (type == PatrolType.COMBAT) {
            int base = (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_MEDIUM_MOD).computeEffective(0);
            return base;
        }
        if (type == PatrolType.HEAVY) {
            int base = (int) market.getStats().getDynamic().getMod(Stats.PATROL_NUM_HEAVY_MOD).computeEffective(0);
            return base;
        }
        return 0;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        return false;
    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        if (!isFunctional()) {
            return;
        }

        if (reason == FleetDespawnReason.REACHED_DESTINATION) {
            RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
            if (route.getCustom() instanceof PatrolFleetData) {
                PatrolFleetData custom = (PatrolFleetData) route.getCustom();
                if (custom.spawnFP > 0) {
                    float fraction = fleet.getFleetPoints() / custom.spawnFP;
                    returningPatrolValue += fraction;
                }
            }
        }
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {

        PatrolFleetData custom = (PatrolFleetData) route.getCustom();
        PatrolType type = custom.type;

        Random random = route.getRandom();

        float combat = 0f;
        float tanker = 0f;
        float freighter = 0f;
        String fleetType = type.getFleetType();
        switch (type) {
            case FAST:
                combat = Math.round(LIGHT_FP + random.nextFloat() * (LIGHT_FP / 2f)) * 5f;
                break;
            case COMBAT:
                combat = Math.round(MEDIUM_FP + random.nextFloat() * (MEDIUM_FP / 2f)) * 5f;
                tanker = Math.round(random.nextFloat()) * MEDIUM_FP;
                break;
            case HEAVY:
                combat = Math.round(HEAVY_FP + random.nextFloat() * (HEAVY_FP / 2f)) * 5f;
                tanker = Math.round(random.nextFloat()) * HEAVY_FP;
                freighter = Math.round(random.nextFloat()) * HEAVY_FP;
                break;
        }

        String pickedFactionId = pickFaction();
        FactionAPI pickedFaction = Global.getSector().getFaction(pickedFactionId);

        FleetParamsV3 params = new FleetParamsV3(
                market,
                null, // loc in hyper; don't need if have market
                pickedFactionId, // String factionId
                route.getQualityOverride(), // quality override
                fleetType,
                combat, // combatPts
                freighter, // freighterPts 
                tanker, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod - since the Lion's Guard is in a different-faction market, counter that penalty
        );
        params.timestamp = route.getTimestamp();
        params.random = random;
        params.modeOverride = Misc.getShipPickMode(market);
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        if (!fleet.getFaction().getId().equals(JOINT_FACTION)) {
            fleet.setFaction(JOINT_FACTION, true);
            fleet.setNoFactionInName(true);
            String name = Misc.ucFirst(pickedFaction.getEntityNamePrefix()) + " Allied " + fleet.getName();
            fleet.setName(name);
        }

        fleet.addEventListener(this);

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);

        if (type == PatrolType.FAST || type == PatrolType.COMBAT) {
            fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_CUSTOMS_INSPECTOR, true);
        }

        String postId = Ranks.POST_PATROL_COMMANDER;
        String rankId = Ranks.SPACE_COMMANDER;
        switch (type) {
            case FAST:
                rankId = Ranks.SPACE_LIEUTENANT;
                break;
            case COMBAT:
                rankId = Ranks.SPACE_COMMANDER;
                break;
            case HEAVY:
                rankId = Ranks.SPACE_CAPTAIN;
                break;
        }

        fleet.getCommander().setPostId(postId);
        fleet.getCommander().setRankId(rankId);

        market.getContainingLocation().addEntity(fleet);
        fleet.setFacing((float) Math.random() * 360f);
        // this will get overridden by the patrol assignment AI, depending on route-time elapsed etc
        fleet.setLocation(market.getPrimaryEntity().getLocation().x, market.getPrimaryEntity().getLocation().y);

        fleet.addScript(new PatrolAssignmentAIV4(fleet, route));

        if (custom.spawnFP <= 0) {
            custom.spawnFP = fleet.getFleetPoints();
        }

        return fleet;
    }

    public String getRouteSourceId() {
        return getMarket().getId() + "_" + "vanguard";
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

    private void revealIfCommissioned() {
        boolean commission = JOINT_FACTION.equals(Misc.getCommissionFactionId());
        
        if (commission) {
            market.getPrimaryEntity().setDiscoverable(false);
        }
    }
}
