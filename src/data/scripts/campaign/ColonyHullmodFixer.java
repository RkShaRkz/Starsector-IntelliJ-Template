package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.util.*;

import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;

public class ColonyHullmodFixer implements EveryFrameScript {

    public static Logger log = Global.getLogger(ColonyHullmodFixer.class);
    private static final Map<String, List<String>> COLONY_FACTIONS_HULLMODS = new HashMap<>();

    static {
        // The RVTC knows ships and weapons with the `mercantile` tag
        // and applies the faction hullmod to ships that have hull IDs ending in `vayra_mercantile` OR have the `vayra_merchant_marine` tag
        COLONY_FACTIONS_HULLMODS.put("almighty_dollar", new ArrayList<>(Arrays.asList("vayra_mercantile", "vayra_merchant_marine")));

        // The Ashen Keepers know ships and weapons with the `ashen` and `ashen_rare` tags
        // and applies the faction hullmod to ships that have hull IDs ending in `vayra_ashen` OR have the `vayra_ashen_bulwark` tag
        COLONY_FACTIONS_HULLMODS.put("ashen_keepers", new ArrayList<>(Arrays.asList("vayra_ashen", "vayra_ashen_bulwark")));

        // The PDPRC knows ships and weapons with the `revolutionary` tag
        // and applies the faction hullmod to ships that have hull IDs ending in `vayra_revolutionary` OR have the `vayra_red_army` tag
        COLONY_FACTIONS_HULLMODS.put("communist_clouds", new ArrayList<>(Arrays.asList("vayra_revolutionary", "vayra_red_army")));

        // The Research Mandate knows ships and weapons with the `science` tag and will learn the `science_post_ai` tag after some time
        // and applies the faction hullmod to ships that have hull IDs ending in `vayra_science` OR have the `vayra_science_hull` tag
        COLONY_FACTIONS_HULLMODS.put("science_fuckers", new ArrayList<>(Arrays.asList("vayra_science", "vayra_science_hull")));

        // The Stormhawk Republic knows ships and weapons with the `warhawk` tag
        // and applies the faction hullmod to ships that have hull IDs ending in `vayra_warhawk` OR have the `vayra_warhawk_modular` tag
        COLONY_FACTIONS_HULLMODS.put("warhawk_republic", new ArrayList<>(Arrays.asList("vayra_warhawk", "vayra_warhawk_modular")));
    }

    private final IntervalUtil timer = new IntervalUtil(1f, 1f);

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    @Override
    public void advance(float amount) {

        // do fleets
        List<CampaignFleetAPI> fleets = Misc.getNearbyFleets(Global.getSector().getPlayerFleet(), 1000);
        fleets.add(Global.getSector().getPlayerFleet());
        for (CampaignFleetAPI fleet : fleets) {
            for (FleetMemberAPI ship : fleet.getFleetData().getMembersInPriorityOrder()) {
                for (String factionId : COLONY_FACTIONS_HULLMODS.keySet()) {
                    String suffix = COLONY_FACTIONS_HULLMODS.get(factionId).get(0);
                    String hullmod = COLONY_FACTIONS_HULLMODS.get(factionId).get(1);
                    if ((ship.getHullId().endsWith(suffix) || ship.getHullSpec().hasTag(hullmod)) && !ship.getVariant().getHullMods().contains(hullmod)) {
                        //ship.getHullSpec().addBuiltInMod(hullmod);
                        ship.getVariant().addPermaMod(hullmod);
                        if (VAYRA_DEBUG) {
                            log.info("added hullmod to ship from " + suffix);
                        }
                    }
                }
            }
        }

        // only check markets once per second
        timer.advance(amount);
        if (timer.intervalElapsed()) {
            // do markets
            for (String factionId : COLONY_FACTIONS_HULLMODS.keySet()) {
                String suffix = COLONY_FACTIONS_HULLMODS.get(factionId).get(0);
                String hullmod = COLONY_FACTIONS_HULLMODS.get(factionId).get(1);
                for (MarketAPI market : Misc.getFactionMarkets(Global.getSector().getFaction(factionId))) {
                    Set<CargoAPI> allCargo = new HashSet<>();
                    if (market.hasSubmarket(Submarkets.SUBMARKET_OPEN)) {
                        allCargo.add(market.getSubmarket(Submarkets.SUBMARKET_OPEN).getCargo());
                    }
                    if (market.hasSubmarket(Submarkets.GENERIC_MILITARY)) {
                        allCargo.add(market.getSubmarket(Submarkets.GENERIC_MILITARY).getCargo());
                    }
                    if (market.hasSubmarket(Submarkets.SUBMARKET_BLACK)) {
                        allCargo.add(market.getSubmarket(Submarkets.SUBMARKET_BLACK).getCargo());
                    }
                    if (market.hasSubmarket(Submarkets.SUBMARKET_STORAGE)) {
                        allCargo.add(market.getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo());
                    }
                    for (CargoAPI cargo : allCargo) {
                        for (FleetMemberAPI ship : cargo.getMothballedShips().getMembersInPriorityOrder()) {
                            if ((ship.getHullId().endsWith(suffix) || ship.getHullSpec().hasTag(hullmod)) && !ship.getVariant().getHullMods().contains(hullmod)) {
                                //ship.getHullSpec().addBuiltInMod(hullmod);
                                ship.getVariant().addPermaMod(hullmod);
                                if (VAYRA_DEBUG) {
                                    log.info("added hullmod to mothballed ship in market owned by " + factionId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
