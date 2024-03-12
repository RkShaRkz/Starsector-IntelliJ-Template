package data.scripts.campaign.colonies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.EconomyAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI.SurveyLevel;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Conditions;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.intel.raid.ActionStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseAssignmentAI.FleetActionDelegate;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.colonies.VayraColonialExpeditionIntel.KadurColonialExpeditionOutcome;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static data.scripts.VayraMergedModPlugin.createAdmin;
import static java.lang.Math.random;

public class VayraColonialExpeditionStage4Colonize extends ActionStage implements FleetActionDelegate {

    public static Logger log = Global.getLogger(VayraColonialExpeditionStage4Colonize.class);

    protected MarketAPI target;
    protected boolean playerTargeted = true;
    protected boolean gaveOrders = true; // will be set to false in updateRoutes()
    protected VayraColonialExpeditionIntel colonyIntel = ((VayraColonialExpeditionIntel) this.intel);
    protected float untilColonize = 0f;
    protected boolean checkedFP = false;
    protected PlanetAPI planet;

    public VayraColonialExpeditionStage4Colonize(VayraColonialExpeditionIntel raid, MarketAPI target) {
        super(raid);
        this.target = target;
        this.planet = target.getPlanetEntity();
        untilColonize = (float) (7f + (7f * random()));
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        float days = Misc.getDays(amount);
        untilColonize -= days;

        if (!gaveOrders) {
            gaveOrders = true;
            log.info(String.format("Giving the orbit/colonize order in %s", target.getStarSystem().getNameWithLowercaseType()));
            giveOrbitOrder(getRoutes());
        }
    }

    public void giveOrbitOrder(List<RouteData> routes) {
        for (RouteData route : routes) {

            float orbitDays = untilColonize;

            if (route.getActiveFleet() != null) {
                CampaignFleetAPI fleet = route.getActiveFleet();
                fleet.clearAssignments();
                fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, target.getPrimaryEntity(), orbitDays, "Colonizing");
            } else {
                route.addSegment(new RouteSegment(orbitDays, target.getPrimaryEntity()));
            }
        }
    }

    @Override
    protected void abortIfNeededBasedOnFP(boolean giveReturnOrders) {
        List<RouteData> routes = getRoutes();
        List<RouteData> stragglers = new ArrayList<>();

        if (!enoughMadeIt(routes, stragglers)) {
            log.info(String.format("Decided not enough colonizers made it to %s", target.getStarSystem().getNameWithLowercaseType()));
            status = RaidStageStatus.FAILURE;
            if (giveReturnOrders) {
                giveReturnOrdersToStragglers(routes);
            }
        }
    }

    @Override
    protected boolean enoughMadeIt(List<RouteData> routes, List<RouteData> stragglers) {
        float madeItFP = 0;
        for (RouteData route : RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId())) {
            CampaignFleetAPI fleet = route.getActiveFleet();
            if (fleet != null) {
                float mult = Misc.getAdjustedFP(1f, route.getMarket());
                if (mult < 1) {
                    mult = 1f;
                }
                madeItFP += fleet.getFleetPoints() / mult;
                log.info(String.format("counting %s FP from %s route activefleet", fleet.getFleetPoints(), fleet.getNameWithFaction()));
            } else {
                madeItFP += route.getExtra().fp;
                log.info(String.format("counting %s FP from route itself", route.getExtra().fp));
            }
        }
        log.info(String.format("%s FP of colonizers made it to %s, our threshold is %s", madeItFP, target.getStarSystem().getNameWithLowercaseType(), abortFP));
        return madeItFP >= abortFP;
    }

    @Override
    protected void updateStatus() {

        if (!checkedFP) {
            abortIfNeededBasedOnFP(true);
            checkedFP = true;
        }

        if (status != RaidStageStatus.ONGOING) {

            String outcomeString = "null";

            if (colonyIntel.getOutcome() != null) {
                outcomeString = colonyIntel.getOutcome().name();
            }

            log.info(String.format("Outcome == %s, Raid status == %s, so cutting this updateStatus off short (in %s)", outcomeString, status.name(), target.getStarSystem().getNameWithLowercaseType()));
            return;
        }

        SectorEntityToken entity = target.getPrimaryEntity();
        if (target.isPlayerOwned()
                || (target.getFactionId() != null && target.getFactionId().equals(Factions.PLAYER))
                || (entity.getFaction() != null && entity.getFaction().getId().equals(Factions.PLAYER))) {
            status = RaidStageStatus.FAILURE;
            giveReturnOrdersToStragglers(getRoutes());
            log.info(String.format("Colonization target in %s is player owned? fuck 'em!!!", target.getStarSystem().getNameWithLowercaseType()));
            colonyIntel.setOutcome(KadurColonialExpeditionOutcome.TARGET_ALREADY_COLONIZED);
            colonyIntel.makeHostile();
            colonyIntel.sendOutcomeUpdate();
            return;
        }

        if (target.hasIndustry(Industries.POPULATION)) {
            status = RaidStageStatus.FAILURE;
            giveReturnOrdersToStragglers(getRoutes());
            log.info(String.format("Colonization target in %s apparently has Population industry? Giving up...", target.getStarSystem().getNameWithLowercaseType()));
        }

        if (untilColonize <= 0) {
            abortIfNeededBasedOnFP(true);
            if (status == RaidStageStatus.FAILURE) {
                log.info(String.format("Giving up the ghost in %s -- not enough FP made it", target.getStarSystem().getNameWithLowercaseType()));
                return;
            }

            colonize(target);
        }
    }

    @Override
    public String getRaidActionText(CampaignFleetAPI fleet, MarketAPI market) {
        return "colonizing " + market.getName();
    }

    @Override
    public String getRaidApproachText(CampaignFleetAPI fleet, MarketAPI market) {
        return "moving to colonize " + market.getName();
    }

    public void colonize(MarketAPI market) {

        SectorEntityToken entity = target.getPrimaryEntity();
        if (target.isPlayerOwned()
                || (target.getFactionId() != null && target.getFactionId().equals(Factions.PLAYER))
                || (entity.getFaction() != null && entity.getFaction().getId().equals(Factions.PLAYER))) {
            status = RaidStageStatus.FAILURE;
            giveReturnOrdersToStragglers(getRoutes());
            log.info(String.format("Colonization target in %s is player owned? fuck 'em!!!", target.getStarSystem().getNameWithLowercaseType()));
            colonyIntel.setOutcome(KadurColonialExpeditionOutcome.TARGET_ALREADY_COLONIZED);
            colonyIntel.makeHostile();
            colonyIntel.sendOutcomeUpdate();
            return;
        }

        status = RaidStageStatus.SUCCESS;

        makeTheColony(market);

        if (colonyIntel.getOutcome() != null) {
            if (status == RaidStageStatus.SUCCESS) {
                colonyIntel.sendOutcomeUpdate();
            } else {
                giveReturnOrdersToStragglers(getRoutes());
            }
        }
    }

    @Override
    protected void updateRoutes() {
        resetRoutes();

        ((VayraColonialExpeditionIntel) intel).sendInSystemUpdate();

        gaveOrders = false;
        ((VayraColonialExpeditionIntel) intel).setEnteredSystem(true);

        List<RouteData> routes = RouteManager.getInstance().getRoutesForSource(intel.getRouteSourceId());

        for (RouteData route : routes) {
            if (target.getStarSystem() != null) { // so that fleet may spawn NOT at the target
                route.addSegment(new RouteSegment(5f, target.getStarSystem().getCenter()));
            }
            route.addSegment(new RouteSegment(1000f, target.getPrimaryEntity()));
        }
    }

    @Override
    public void showStageInfo(TooltipMakerAPI info) {
        int curr = intel.getCurrentStage();
        int index = intel.getStageIndex(this);

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        if (status == RaidStageStatus.FAILURE) {
            if (colonyIntel.getOutcome() == KadurColonialExpeditionOutcome.TARGET_ALREADY_COLONIZED) {
                info.addPara("The colonial expedition arrived to find their destination already claimed. The colonization effort is now over.", opad);
            } else {
                info.addPara("The expedition was destroyed before they could colonize "
                        + target.getName() + ". The colonization effort is now over.", opad);
            }
        } else if (status == RaidStageStatus.SUCCESS) {

            if (colonyIntel.getOutcome() == KadurColonialExpeditionOutcome.COLONY_ESTABLISHED) {
                info.addPara("The expedition arrived successfully and established a colony.", opad);
            }
        } else if (curr == index) {
            info.addPara("The colonization of " + target.getName() + " is currently under way.", opad);

        }
    }

    @Override
    public boolean canRaid(CampaignFleetAPI fleet, MarketAPI market) {
        if (colonyIntel.getOutcome() != null) {
            return false;
        }
        return market == target;
    }

    @Override
    public String getRaidPrepText(CampaignFleetAPI fleet, SectorEntityToken from) {
        return "orbiting " + from.getName();
    }

    @Override
    public String getRaidInSystemText(CampaignFleetAPI fleet) {
        return "establishing colony";
    }

    @Override
    public String getRaidDefaultText(CampaignFleetAPI fleet) {
        return "establishing colony";
    }

    @Override
    public boolean isPlayerTargeted() {
        return playerTargeted;
    }

    private MarketAPI makeTheColony(MarketAPI market) {
        String faction = colonyIntel.getFaction().getId();

        planet.setMarket(market);
        market.setPrimaryEntity(planet);

        for (SectorEntityToken entity : market.getConnectedEntities()) {
            entity.setFaction(faction);
        }

        EconomyAPI globalEconomy = Global.getSector().getEconomy();

        market.setPlanetConditionMarketOnly(false);
        market.setFactionId(faction);
        market.getTariff().modifyFlat("generator", market.getFaction().getTariffFraction());
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.setSize(3);
        market.addIndustry(Industries.POPULATION);
        market.reapplyIndustries();

        market.addCondition(Conditions.POPULATION_3);

        market.setSurveyLevel(SurveyLevel.FULL);
        for (MarketConditionAPI condition : market.getConditions()) {
            condition.setSurveyed(true);
        }

        globalEconomy.addMarket(market, false);

        log.info(String.format("Founded colony %s for %s", market.getName(), market.getFaction().getDisplayNameLongWithArticle()));

        log.info(String.format("giving defend order to colony fleets", market.getName(), market.getFaction().getDisplayNameLongWithArticle()));

        for (CampaignFleetAPI fleet : target.getContainingLocation().getFleets()) {
            if (fleet.getFaction().equals(colonyIntel.colonyFaction)) {
                fleet.addAssignment(FleetAssignment.DEFEND_LOCATION, target.getPrimaryEntity(), 69f);
                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, target.getPrimaryEntity(), 42069f);
            }
        }

        createAdmin(market);

        int stage = 0;
        VayraColonialManager.getInstance().putColony(market);

        log.info(String.format("Added %s to the colonies list at stage %s, now building a spaceport", market.getName(), stage));

        return market;
    }

    @Override
    public void performRaid(CampaignFleetAPI fleet, MarketAPI market) {
        colonize(market);
    }
}
