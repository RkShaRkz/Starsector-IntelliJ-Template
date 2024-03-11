package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import static data.scripts.VayraMergedModPlugin.BOUNTY_DURATION;
import static data.scripts.VayraMergedModPlugin.BOUNTY_SOFT_MAX_DIST;
import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;
import java.util.List;
import org.apache.log4j.Logger;

public class VayraBountySharedMethods {

    public static Logger log = Global.getLogger(VayraBountySharedMethods.class);
    public static final float HOSTILE_AVOID_RANGE = 3000f;
    public static final String USED_FOR_BOUNTY_KEY = "$vayra_used_for_bounty";

    protected static SectorEntityToken pickHideoutLocation(FactionAPI bountyFaction) {
        WeightedRandomPicker<StarSystemAPI> systemPicker = new WeightedRandomPicker<>();
        SectorEntityToken hideoutLocation = null;

        log.info("trying to pick hideout");

        // main loop through star systems
        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            float mult = 0f;

            // skip pulsars
            if (system.hasPulsar()) {
                continue;
            }

            // these are the tags for systems we want
            if (system.hasTag(Tags.THEME_MISC_SKIP)) {
                mult = 1f;
            } else if (system.hasTag(Tags.THEME_MISC)) {
                mult = 1.5f;
            } else if (system.hasTag(Tags.THEME_REMNANT_NO_FLEETS)) {
                mult = 1.5f;
            } else if (system.hasTag(Tags.THEME_RUINS)) {
                mult = 2f;
            } else if (system.hasTag(Tags.THEME_REMNANT_DESTROYED)) {
                mult = 1.5f;
            } else if (system.hasTag(Tags.THEME_CORE_UNPOPULATED)) {
                mult = 1f;
            }

            // if it has any econ markets, skip the system
            for (MarketAPI market : Misc.getMarketsInLocation(system)) {
                if (!market.isPlanetConditionMarketOnly()) {
                    mult = 0f;
                    break;
                }
            }

            // skip systems within our minimum range
            float distToPlayer = Misc.getDistanceToPlayerLY(system.getLocation());
            float noSpawnRange = Global.getSettings().getFloat("personBountyNoSpawnRangeAroundPlayerLY");
            if (distToPlayer < noSpawnRange) {
                if (VAYRA_DEBUG) {
                    log.info(system.getId() + " within min spawn range from player, skipping");
                }
                continue;
            }
            
            // weight systems lower if they're too far away
            if (distToPlayer > BOUNTY_SOFT_MAX_DIST) {
                mult = (float) BOUNTY_SOFT_MAX_DIST / distToPlayer;
            }

            // skip systems that have been used recently
            if (system.getMemoryWithoutUpdate().contains(USED_FOR_BOUNTY_KEY)) {
                continue;
            }

            // skip systems with 0 or lower weight multiplier (optimization?)
            if (mult <= 0) {
                continue;
            }

            // now time to weight the systems based on planets
            List<PlanetAPI> planets = system.getPlanets();
            float weight = planets.size();

            // skip planet-less systems
            if (weight <= 0) {
                if (VAYRA_DEBUG) {
                    log.info(system.getId() + " has no planets, skipping");
                }
                continue;
            }

            // secondary loop through planets
            for (PlanetAPI planet : planets) {

                // skip the star, lol
                if (planet.isStar()) {
                    weight -= 1f;
                    continue;
                }

                // weight the planets by hazard, we like systems with low-hazard hideouts
                MarketAPI market = planet.getMarket();
                if (market == null) {
                    Misc.initConditionMarket(planet);
                    market = planet.getMarket();
                }
                if (market != null) {
                    float h = market.getHazardValue();
                    if (h <= 0.5f) {
                        weight += 5f;
                    } else if (h <= 0.75f) {
                        weight += 3f;
                    } else if (h <= 1f) {
                        weight += 1f;
                    }
                }
            }

            // skip systems that have no valid planets after we're done checking them all
            if (weight <= 0) {
                if (VAYRA_DEBUG) {
                    log.info(system.getId() + " has no VALID planets, skipping");
                }
                continue;
            }

            // finally, add it to the picker
            systemPicker.add(system, weight * mult);
        }

        // try (up to 30 times) to actually pick a hideout
        boolean valid = false;
        int tries = 0;
        while (!valid && tries < 30) {
            StarSystemAPI system = systemPicker.pick();
            if (system != null) {
                tries++;
                log.info("try # " + tries + " to pick actual hideout planet in " + system.getId());
                if (VAYRA_DEBUG) {
                    log.info("picked " + system.getId() + " for hideout system, checking if valid and picking hideout planet");
                }
                WeightedRandomPicker<PlanetAPI> picker = new WeightedRandomPicker<>();
                List<PlanetAPI> planets = system.getPlanets();
                if (planets.isEmpty()) {
                    log.error(system.getId() + " has no planets, which should give it a weight of zero - how did we get here?");
                }
                for (PlanetAPI planet : system.getPlanets()) {

                    // skip planets that already have a bounty
                    if (planet.getMemoryWithoutUpdate().contains(USED_FOR_BOUNTY_KEY)) {
                        log.info(planet.getId() + " already has a bounty around it or did recently, skipping");
                        continue;
                    }
                    if (planet.isStar()) {
                        if (VAYRA_DEBUG) {
                            log.info(system.getId() + " - " + planet.getId() + " is star, skipping");
                        }
                        continue;
                    }
                    if (planet.getFaction() != null && !planet.getFaction().getId().equals(Factions.NEUTRAL) && bountyFaction.isHostileTo(planet.getFaction())) {
                        if (VAYRA_DEBUG) {
                            log.info(system.getId() + " - " + planet.getId() + " is " + planet.getFaction().getId() + " which is hostile to bountyFaction, skipping");
                        }
                        continue;
                    }
                    if (planet.getLocation() == null) {
                        log.error("planet location was null, skipping");
                        continue;
                    }
                    float orbit = planet.getLocation().length();
                    boolean hostileTooClose = false;
                    for (PlanetAPI other : system.getPlanets()) {
                        float orbitDiff;
                        try {
                            orbitDiff = Math.abs(orbit - other.getLocation().length());
                        } catch (NullPointerException npx) {
                            log.error("orbitDiff was null, wtf");
                            continue;
                        }
                        if (orbitDiff < HOSTILE_AVOID_RANGE
                                && other.getFaction() != null
                                && !other.getFaction().getId().equals(Factions.NEUTRAL)
                                && bountyFaction.isHostileTo(other.getFaction())) {
                            hostileTooClose = true;
                        }
                    }
                    if (hostileTooClose) {
                        if (VAYRA_DEBUG) {
                            log.info(system.getId() + " - " + planet.getId() + " has hostiles within " + HOSTILE_AVOID_RANGE + ", skipping");
                        }
                        continue;
                    }
                    picker.add(planet);
                    if (VAYRA_DEBUG) {
                        log.info(system.getId() + " - " + planet.getId() + " added to actual hideout picker");
                    }
                }
                hideoutLocation = picker.pick();
                if (hideoutLocation == null) {
                    log.error("FUCK! Picked an invalid hideout!");
                    return null;
                } else {
                    valid = true;
                }
            } else {
                log.error("picked null system, oops (what, how)");
            }
        }

        if (!valid && tries >= 30) {
            log.error("couldn't pick a valid hideout, giving up");
            return null;
        }

        if (hideoutLocation == null) {
            log.error("invalid hideout, giving up, also we should have given up earlier");
            return null;
        } else {
            log.info("picked " + hideoutLocation.getContainingLocation().getId() + " - " + hideoutLocation.getId() + " as valid hideout! surely nobody will find us there");
            hideoutLocation.getMemoryWithoutUpdate().set(USED_FOR_BOUNTY_KEY, true, BOUNTY_DURATION);
            hideoutLocation.getContainingLocation().getMemoryWithoutUpdate().set(USED_FOR_BOUNTY_KEY, true, BOUNTY_DURATION);
            return hideoutLocation;
        }
    }
}
