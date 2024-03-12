package data.scripts.campaign;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.OrbitAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Submarkets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.apache.log4j.Logger;

import java.util.*;

public class VayraAbandonedStationAndLeagueSubfactionBonker implements EveryFrameScript {

    private boolean finished = false;
    public static Logger log = Global.getLogger(VayraAbandonedStationAndLeagueSubfactionBonker.class);

    private static final String INDUSTRY_ID = "vayra_league_subfaction_spawner";
    private static final String LEAGUE_ID = "persean";
    private static final List<String> MARKET_IDS = new ArrayList<>(Arrays.asList(
            "kazeron",
            "laicaille_habitat",
            "mazalot",
            "salamanca",
            "fikenhild",
            "athulf"
    ));

    private static final List<String> ENTITIES = new ArrayList<>(Collections.singletonList(
            "vayra_procgen_abandonedstation" // unique ID, should probably be unique across mods so a prefix is a good idea
    ));

    @Override
    public void advance(float amount) {

        if (finished) {
            // if we're already done, just don't bother
            Global.getSector().removeScript(this);

        } else {
            for (MarketAPI market : Misc.getFactionMarkets(Global.getSector().getFaction(LEAGUE_ID))) {
                if (MARKET_IDS.contains(market.getId())) {
                    market.addIndustry(INDUSTRY_ID);
                }
            }


            for (String thing : ENTITIES) {

                // generic setup, done once for each thing
                StarSystemAPI system = pickSystem();
                float dist = 333f;
                Set<SectorEntityToken> exclude = new HashSet<>();
                OrbitAPI orbit = BaseThemeGenerator.pickAnyLocation(new Random(), system, dist, exclude).orbit;
                SectorEntityToken entity = null;

                // custom, entity-specific setup
                // wanna put a case in here for each thing in your list
                switch (thing) {
                    case "vayra_procgen_abandonedstation":
                        entity = system.addCustomEntity(
                                thing, // unique ID
                                "Derelict Station", // in-game display name
                                "station_side06", // entity type id from custom_entities.json
                                "neutral" // faction ID of entity (should proooobably be Neutral)
                        );
                        // more custom setup stuff, in this case specific to this entity
                        entity.setCustomDescriptionId("vayra_procgen_abandonedstation"); // set this up as a CUSTOM type in descriptions.csv
                        entity.setInteractionImage("illustrations", "abandoned_station2"); // illustration ID, just comment line out to use default
                        Misc.setAbandonedStationMarket(entity + "market", entity);
                        entity.getMarket().getSubmarket(Submarkets.SUBMARKET_STORAGE).getCargo().addMothballedShip(FleetMemberType.SHIP, "afflictor_d_pirates_Strike", null);
                        entity.setCustomDescriptionId("vayra_abandoned_station");
                        break;

                    default:
                        break;
                }

                // more generic setup done once for each thing
                if (entity == null || orbit == null) {
                    // null check in case we forgot to put a case statement above for this entity
                    log.error("entity was " + entity + ", orbit was " + orbit + ", not spawning " + thing);
                    return;
                } else {
                    entity.setOrbit(orbit);
                    orbit.setEntity(entity);
                }
            }

            // once we're done all the things, tell the script to fuck off
            finished = true;
        }
    }

    // here's where we set weights for systems to pick or whatever
    public StarSystemAPI pickSystem() {
        WeightedRandomPicker<StarSystemAPI> systems = new WeightedRandomPicker<>();
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float weight = 0f;
            for (String tag : system.getTags()) {
                switch (tag) {
                    case Tags.THEME_MISC:
                        weight += 3f;
                        break;
                    case Tags.THEME_MISC_SKIP:
                        weight += 3f;
                        break;
                    case Tags.THEME_RUINS:
                        weight += 3f;
                        break;
                    case Tags.THEME_REMNANT_NO_FLEETS:
                        weight += 1f;
                        break;
                    case Tags.THEME_REMNANT_DESTROYED:
                        weight += 1f;
                        break;
                    default:
                        break;
                }
            }
            if (weight > 0f) {
                systems.add(system, weight);
            }
        }
        return systems.pick();
    }

    @Override
    public boolean isDone() {
        return finished;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }
}
