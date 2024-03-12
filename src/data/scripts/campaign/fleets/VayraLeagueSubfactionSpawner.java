package data.scripts.campaign.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.econ.impl.MilitaryBase.PatrolFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.PatrolType;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.PatrolAssignmentAIV4;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteFleetSpawner;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static data.scripts.VayraMergedModPlugin.LEAGUE_SUBFACTIONS;

public class VayraLeagueSubfactionSpawner extends BaseIndustry implements RouteFleetSpawner, FleetEventListener {

    public static Logger log = Global.getLogger(VayraLeagueSubfactionSpawner.class);

    public static final String FACTION = "persean";
    public static final Map<String, String> SPAWN_FACTIONS = new HashMap<>();

    static {
        SPAWN_FACTIONS.put("kazeron", "vayra_kazeron");
        SPAWN_FACTIONS.put("laicaille_habitat", "vayra_kazeron");
        SPAWN_FACTIONS.put("mazalot", "vayra_mazalot");
        SPAWN_FACTIONS.put("salamanca", "vayra_mazalot");
        SPAWN_FACTIONS.put("fikenhild", "vayra_westernesse");
        SPAWN_FACTIONS.put("athulf", "vayra_westernesse");
    }

    // maximum patrols per level
    private static final int BASE_LIGHT = 2;
    private static final int BASE_MEDIUM = 1;
    private static final int BASE_HEAVY = 1;

    // FP (actual FP, +/- 25%, +50% per level, * market fleet size modifier)
    private static final float LIGHT_FP = 35f;
    private static final float MEDIUM_FP = 75f;
    private static final float HEAVY_FP = 150f;

    private int level = 1;

    @Override
    public boolean isHidden() {
        return !FACTION.contains(market.getFactionId()) || !LEAGUE_SUBFACTIONS;
    }

    @Override
    public boolean isFunctional() {
        return super.isFunctional() && FACTION.contains(market.getFactionId()) && LEAGUE_SUBFACTIONS;
    }

    @Override
    public void apply() {
        super.apply(true);

        int size = market.getSize();

        demand(Commodities.SUPPLIES, size - 1);
        demand(Commodities.FUEL, size - 1);
        demand(Commodities.SHIPS, size - 1);

        supply(Commodities.CREW, size);

        demand(Commodities.HAND_WEAPONS, size);
        supply(Commodities.MARINES, size);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HAND_WEAPONS);
        applyDeficitToProduction(1, deficit, Commodities.MARINES);

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
        return 1;
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
        tracker.advance(days * spawnRate + extraTime);

        if (tracker.intervalElapsed()) {

            log.info("patrol spawn timer elapsed at " + market.getId());

            int newLevel = 1;
            if (market.hasIndustry(Industries.PATROLHQ) && !market.getIndustry(Industries.PATROLHQ).isDisrupted()) {
                newLevel += 0;
            }
            if (market.hasIndustry(Industries.MILITARYBASE) && !market.getIndustry(Industries.MILITARYBASE).isDisrupted()) {
                newLevel += 1;
            }
            if (market.hasIndustry(Industries.HIGHCOMMAND) && !market.getIndustry(Industries.HIGHCOMMAND).isDisrupted()) {
                newLevel += 2;
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
                combat = (float) ((LIGHT_FP * 0.75f) + ((LIGHT_FP / 2f) * Math.random()));
                break;
            case COMBAT:
                combat = (float) ((MEDIUM_FP * 0.75f) + ((MEDIUM_FP / 2f) * Math.random()));
                tanker = Math.round(random.nextFloat()) * MEDIUM_FP;
                break;
            case HEAVY:
                combat = (float) ((HEAVY_FP * 0.75f) + ((HEAVY_FP / 2f) * Math.random()));
                tanker = Math.round(random.nextFloat()) * HEAVY_FP;
                freighter = Math.round(random.nextFloat()) * HEAVY_FP;
                break;
        }
        if (level >= 3f) {
            combat *= 2f;
        } else if (level >= 2f) {
            combat *= 1.5f;
        }

        String factionId = SPAWN_FACTIONS.get(market.getPrimaryEntity().getId());
        FactionAPI pickedFaction = Global.getSector().getFaction(factionId);
        if (factionId == null || pickedFaction == null) {
            log.error("factionId " + factionId + " didn't match to an extant faction, aborting");
            return null;
        }

        FleetParamsV3 params = new FleetParamsV3(
                market,
                null, // loc in hyper; don't need if have market
                factionId, // String factionId
                null, // quality override
                fleetType,
                combat, // combatPts
                freighter, // freighterPts 
                tanker, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                market.getShipQualityFactor() // qualityMod
        );
        params.timestamp = route.getTimestamp();
        params.random = random;
        params.modeOverride = Misc.getShipPickMode(market);
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        if (!fleet.getFaction().getId().equals(FACTION)) {
            fleet.setFaction(FACTION, true);
            fleet.setNoFactionInName(true);
            String name = Misc.ucFirst(pickedFaction.getEntityNamePrefix()) + " " + fleet.getName();
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
        return getMarket().getId() + "_" + "vayra_perseansubfactionpatrol";
    }

    @Override
    public boolean isAvailableToBuild() {
        return false;
    }

    @Override
    public boolean showWhenUnavailable() {
        return false;
    }

}
