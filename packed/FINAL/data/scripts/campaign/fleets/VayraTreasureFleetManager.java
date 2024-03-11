package data.scripts.campaign.fleets;

import com.fs.starfarer.api.FactoryAPI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.ReputationActionResponsePlugin.ReputationAdjustmentResult;
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.econ.ShippingDisruption;
import com.fs.starfarer.api.impl.campaign.fleets.BaseRouteFleetManager;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.CargoQuantityData;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI.EconomyRouteData;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.OptionalFleetData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteData;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;
import java.util.HashMap;
import java.util.Map;

public class VayraTreasureFleetManager extends BaseRouteFleetManager implements FleetEventListener {

    public static final Integer ROUTE_SRC_LOAD = 1;
    public static final Integer ROUTE_TRAVEL_DST = 2;
    public static final Integer ROUTE_TRAVEL_WS = 3;
    public static final Integer ROUTE_RESUPPLY_WS = 4;
    public static final Integer ROUTE_DST_UNLOAD = 5;

    public static final String FACTION_ID = "almighty_dollar";
    public static final FactionAPI FACTION = Global.getSector().getFaction(FACTION_ID);

    public static final String PARENT_ID = Factions.HEGEMONY;
    public static final FactionAPI PARENT = Global.getSector().getFaction(PARENT_ID);
    public static final String DEFAULT_DEST = "chicomoztoc";
    
    public final MarketAPI destMarket = Global.getSector().getEconomy().getMarket(DEFAULT_DEST);

    public static final String SOURCE_ID = "colonialism";
    public static Logger log = Global.getLogger(VayraTreasureFleetManager.class);

    public static final float COMBAT_POINTS = 200f;

    public static Map<CampaignFleetAPI, FleetMemberAPI> GALLEONS = new HashMap<>();

    public VayraTreasureFleetManager() {
        super(300f, 400f);
    }

    protected Object readResolve() {
        return this;
    }

    @Override
    public void advance(float amount) {
        List<MarketAPI> rimMarketList = Misc.getFactionMarkets(FACTION);
        if (rimMarketList.isEmpty()) {
            return;
        }
        if (VAYRA_DEBUG) {
            amount *= 100f;
        }
        super.advance(amount);
    }
    
    @Override
    protected String getRouteSourceId() {
        return SOURCE_ID;
    }

    @Override
    protected int getMaxFleets() {
        return 1; // only one treasure fleet at a time
    }

    @Override
    protected void addRouteFleetIfPossible() {
        MarketAPI from = pickSourceMarket();
        MarketAPI to = pickDestMarket(from);
        if (from != null && to != null) {

            EconomyRouteData data = createData(from, to);
            if (data == null) {
                return;
            }

            log.info("Added treasure fleet from " + from.getName() + " to " + to.getName());

            Long seed = new Random().nextLong();
            String id = getRouteSourceId();

            OptionalFleetData extra = new OptionalFleetData(from);
            extra.factionId = FACTION_ID;

            RouteData route = RouteManager.getInstance().addRoute(id, from, seed, extra, this);
            route.setCustom(data);

            float orbitDays = data.size * (0.75f + (float) Math.random() * 0.5f);
            route.addSegment(new RouteSegment(ROUTE_SRC_LOAD, orbitDays, from.getPrimaryEntity()));
            route.addSegment(new RouteSegment(ROUTE_TRAVEL_DST, from.getPrimaryEntity(), to.getPrimaryEntity()));
            route.addSegment(new RouteSegment(ROUTE_DST_UNLOAD, orbitDays * 0.5f, to.getPrimaryEntity()));

            setDelayAndSendMessage(route);
        }
    }

    protected void setDelayAndSendMessage(RouteData route) {
        EconomyRouteData data = (EconomyRouteData) route.getCustom();

        float delay = data.size * 2.5f;
        delay *= 0.75f + (float) Math.random() * 0.5f;
        delay = (int) delay;
        route.setDelay(delay);

        if (!Factions.PLAYER.equals(route.getFactionId())) {
            // queues itself
            VayraTreasureFleetIntel intel = new VayraTreasureFleetIntel(route);
        }
    }

    public MarketAPI pickSourceMarket() {

        // return the biggest rimward market we can find
        List<MarketAPI> rimMarketList = Misc.getFactionMarkets(FACTION);
        if (!rimMarketList.isEmpty()) {
            int size = 0;
            MarketAPI dest = null;
            for (MarketAPI m : rimMarketList) {
                if (m.getSize() > size) {
                    size = m.getSize();
                    dest = m;
                }
            }
            return dest;
        } else {
            // or null, if there aren't any rimward markets
            return null;
        }
    }

    public MarketAPI pickDestMarket(MarketAPI from) {

        // return null if we're coming from nowhere
        if (from == null) {
            return null;
        }

        // return chicomoztoc if it's still hegemony (and exists)
        if (destMarket != null && destMarket.getFaction().equals(PARENT)) {
            return destMarket;
        }

        // otherwise return the biggest hegemony market we can find
        List<MarketAPI> hegMarketList = Misc.getFactionMarkets(PARENT);
        if (!hegMarketList.isEmpty()) {
            int size = 0;
            MarketAPI dest = null;
            for (MarketAPI m : hegMarketList) {
                if (m.getSize() > size) {
                    size = m.getSize();
                    dest = m;
                }
            }
            return dest;
        } else {
            // or null, if there aren't any hegemony markets
            return null;
        }
    }

    @SuppressWarnings("AssignmentToForLoopParameter")
    public static EconomyRouteData createData(MarketAPI from, MarketAPI to) {

        EconomyRouteData data = new EconomyRouteData();
        data.from = from; // we're coming from where we came from
        data.to = to; // and we're going where we're going
        data.smuggling = false; // the Treasure Fleet does not SMUGGLE

        float legalTotal = 0;

        // for every commodity on the source market...
        List<CommodityOnMarketAPI> relevant = new ArrayList<>();
        for (CommodityOnMarketAPI com : from.getAllCommodities()) {

            // ignore nonecon commodities
            if (com.isNonEcon()) {
                continue;
            }
            
            // ignore ship hulls chicomoztoc doesnt want em
            if (com.getCommodity().getId().equals(Commodities.SHIPS)) {
                continue;
            }

            // exported qty is lower of available and maxSupply
            int exported = Math.min(com.getAvailable(), com.getMaxSupply());

            // mark these as relevant, we'll pick among them later
            // if we explort less than 1 fuhgeddabouddi
            if (exported > 0) {
                relevant.add(com);
            }
        }

        // for all the commodities we marked as relevant...
        for (CommodityOnMarketAPI com : relevant) {

            // store the original in case we swap it for a primary later
            CommodityOnMarketAPI orig = com;

            // exported qty is lower of available and maxSupply
            int exported = Math.min(com.getAvailable(), com.getMaxSupply());

            // if it's a non-primary (stand-in) just use the primary
            if (!com.getCommodity().isPrimary()) {
                com = from.getCommodityData(com.getCommodity().getDemandClass());
            }

            // just gonna add 2 here... it is a TREASURE FLEET after all
            exported += 2;

            data.addDeliver(orig.getId(), exported);
            legalTotal += exported;
        }

        Comparator<CargoQuantityData> comp = new Comparator<CargoQuantityData>() {
            @Override
            public int compare(CargoQuantityData o1, CargoQuantityData o2) {
                if (o1.getCommodity().isPersonnel() && !o2.getCommodity().isPersonnel()) {
                    return 1;
                }
                if (o2.getCommodity().isPersonnel() && !o1.getCommodity().isPersonnel()) {
                    return -1;
                }
                return o2.units - o1.units;
            }
        };
        Collections.sort(data.cargoDeliver, comp);

        if (legalTotal <= 0) {
            return null;
        }

        while (data.cargoDeliver.size() > 4) {
            data.cargoDeliver.remove(4);
        }

        data.size = 10f;

        return data;
    }

    @Override
    public boolean shouldCancelRouteAfterDelayCheck(RouteData route) {
        String factionId = route.getFactionId();

        return factionId != null && route.getMarket() != null
                && route.getMarket().getFaction().isHostileTo(factionId);
    }

    @Override
    public CampaignFleetAPI spawnFleet(RouteData route) {
        Random random = new Random();
        if (route.getSeed() != null) {
            random = new Random(route.getSeed());
        }

        CampaignFleetAPI fleet = createTradeRouteFleet(route, random);
        if (fleet == null) {
            return null;
        }

        fleet.addEventListener(this);

        fleet.addScript(new EconomyFleetAssignmentAI(fleet, route));
        return fleet;
    }

    public static String getFleetTypeIdForTier(float tier, boolean smuggling) {
        return FleetTypes.TRADE;
    }

    public static CampaignFleetAPI createTradeRouteFleet(RouteData route, Random random) {
        EconomyRouteData data = (EconomyRouteData) route.getCustom();

        MarketAPI from = data.from;
        MarketAPI to = data.to;

        float tier = data.size;

        String factionId = route.getFactionId();

        float total = 0f;
        float fuel = 0f;
        float cargo = 0f;
        float personnel = 0f;

        List<CargoQuantityData> all = new ArrayList<>();
        all.addAll(data.cargoDeliver);
        for (CargoQuantityData curr : all) {
            CommoditySpecAPI spec = curr.getCommodity();
            if (spec.isMeta()) {
                continue;
            }

            total += curr.units;
            if (spec.hasTag(Commodities.TAG_PERSONNEL)) {
                personnel += curr.units;
            } else if (spec.getId().equals(Commodities.FUEL)) {
                fuel += curr.units;
            } else {
                cargo += curr.units;
            }
        }

        if (total < 1f) {
            total = 1f;
        }

        float fuelFraction = fuel / total;
        float personnelFraction = personnel / total;
        float cargoFraction = cargo / total;

        if (fuelFraction + personnelFraction + cargoFraction > 0) {
            float mult = 1f / (fuelFraction + personnelFraction + cargoFraction);
            fuelFraction *= mult;
            personnelFraction *= mult;
            cargoFraction *= mult;
        }

        log.info("Creating treasure fleet for market [" + from.getName() + "]");

        float combat = COMBAT_POINTS;
        float freighter = tier * 2f * cargoFraction * 3f;
        float tanker = tier * 2f * fuelFraction * 3f;
        float transport = tier * 2f * personnelFraction * 3f;
        float liner = 0f;

        float utility = 0f;

        String type = FleetTypes.TRADE;

        FleetParamsV3 params = new FleetParamsV3(
                from,
                null, // locInHyper
                factionId,
                route.getQualityOverride(), // qualityOverride
                type,
                combat, // combatPts
                freighter, // freighterPts 
                tanker, // tankerPts
                transport, // transportPts
                liner, // linerPts
                utility, // utilityPts
                1f // qualityBonus
        );
        params.timestamp = route.getTimestamp();
        params.officerLevelBonus = 10;
        params.random = random;
        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);

        if (fleet == null || fleet.isEmpty()) {
            return null;
        }

        fleet.setName("Treasure Fleet");

        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_TRADE_FLEET, true);

        data.cargoCap = fleet.getCargo().getMaxCapacity();
        data.fuelCap = fleet.getCargo().getMaxFuel();
        data.personnelCap = fleet.getCargo().getMaxPersonnel();

        FactoryAPI factory = Global.getFactory();
        FleetMemberAPI flagship = factory.createFleetMember(FleetMemberType.SHIP, "vayra_galleon_rimward");
        fleet.getFleetData().addFleetMember(flagship);
        fleet.getFleetData().setFlagship(flagship);
        fleet.forceSync();
        fleet.getFleetData().sort();
        flagship.getRepairTracker().setCR(flagship.getRepairTracker().getMaxCR());
        GALLEONS.put(fleet, flagship);

        return fleet;

    }

    @Override
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

        RouteData route = RouteManager.getInstance().getRoute(getRouteSourceId(), fleet);
        if (route == null || !(route.getCustom() instanceof EconomyRouteData)) {
            return;
        }

        if (route.isExpired()) {
            return;
        }

        EconomyRouteData data = (EconomyRouteData) route.getCustom();

        float cargoCap = fleet.getCargo().getMaxCapacity();
        float fuelCap = fleet.getCargo().getMaxFuel();
        float personnelCap = fleet.getCargo().getMaxPersonnel();

        float lossFraction = 0.34f;

        boolean returning = false;

        // whether it lost enough carrying capacity to count as an economic loss 
        // of that commodity at destination markets
        boolean lostCargo = data.cargoCap * lossFraction > cargoCap;
        boolean lostFuel = data.fuelCap * lossFraction > fuelCap;
        boolean lostPersonnel = data.personnelCap * lossFraction > personnelCap;

        // set to 0f so that the loss doesn't happen multiple times for a commodity
        if (lostCargo) {
            data.cargoCap = 0f;
        }
        if (lostFuel) {
            data.fuelCap = 0f;
        }
        if (lostPersonnel) {
            data.personnelCap = 0f;
        }

        applyLostShipping(data, returning, lostCargo, lostFuel, lostPersonnel);

        // if it's lost all 3 capacities, also trigger a general shipping capacity loss at market
        boolean allThreeLost = true;
        allThreeLost &= data.cargoCap <= 0f || lostCargo;
        allThreeLost &= data.fuelCap <= 0f || lostFuel;
        allThreeLost &= data.personnelCap <= 0f || lostPersonnel;

        boolean applyAccessLoss = allThreeLost;
        if (applyAccessLoss) {
            ShippingDisruption.getDisruption(data.from).addShippingLost(data.size);
            ShippingDisruption.getDisruption(data.from).notifyDisrupted(ShippingDisruption.ACCESS_LOSS_DURATION);
        }

        // didn't destroy the original flagship
        if (fleet.getFlagship() != GALLEONS.get(fleet)) {
            return;
        }

        int bountyCredits = 500000;
        int payment = (int) (bountyCredits * battle.getPlayerInvolvementFraction());
        if (payment <= 0) {
            return;
        }
        Global.getSector().getPlayerFleet().getCargo().getCredits().add(payment);
        Misc.addCreditsMessage("Hard-copy currency vaults within the Galleon yield %s credits.", payment);
        ReputationAdjustmentResult rep = Global.getSector().adjustPlayerReputation(
                new RepActionEnvelope(RepActions.PERSON_BOUNTY_REWARD, null, null, null, true, false),
                Factions.PIRATES);
        ReputationAdjustmentResult rep2 = Global.getSector().adjustPlayerReputation(
                new RepActionEnvelope(RepActions.COMBAT_AGGRESSIVE, null, null, null, true, false),
                FACTION_ID);
        ReputationAdjustmentResult rep3 = Global.getSector().adjustPlayerReputation(
                new RepActionEnvelope(RepActions.COMBAT_AGGRESSIVE, null, null, null, true, false),
                PARENT_ID);
    }

    public static void applyLostShipping(EconomyRouteData data, boolean returning, boolean cargo, boolean fuel, boolean personnel) {
        if (!cargo && !fuel && !personnel) {
            return;
        }

        int penalty = 2;
        if (!returning) {
            for (CargoQuantityData curr : data.cargoDeliver) {
                CommodityOnMarketAPI com = data.to.getCommodityData(curr.cargo);
                if (!fuel && com.isFuel()) {
                    continue;
                }
                if (!personnel && com.isPersonnel()) {
                    continue;
                }
                if (!cargo && !com.isFuel() && !com.isPersonnel()) {
                    continue;
                }

                com.getAvailableStat().addTemporaryModFlat(
                        ShippingDisruption.ACCESS_LOSS_DURATION,
                        ShippingDisruption.COMMODITY_LOSS_PREFIX + Misc.genUID(), "Recent incoming shipment lost", -penalty);

                ShippingDisruption.getDisruption(data.to).notifyDisrupted(ShippingDisruption.ACCESS_LOSS_DURATION);
            }
        }
    }

    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {

    }

    @Override
    public boolean shouldRepeat(RouteData route) {
        return false;
    }

    @Override
    public void reportAboutToBeDespawnedByRouteManager(RouteData route) {

    }

}
