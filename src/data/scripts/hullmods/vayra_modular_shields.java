package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import data.scripts.util.MiscUtils;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.List;
import java.util.*;

import static data.scripts.VayraMergedModPlugin.VAYRA_DEBUG;

public class vayra_modular_shields extends BaseHullMod {

    public static Logger logger = Global.getLogger(vayra_modular_shields.class);

    public static final String SHIELD_GENERATOR_ID = "vayra_caliph_shieldgenerator";
    public static final String SHIELD_PART_ID = "vayra_caliph_shieldpart";
    public static final float OVERLOAD_MULT = 1.5f;
    public static final Color ZERO_FLUX_COLOR = new Color(33, 106, 109, 255);
    public static final Color FULL_FLUX_COLOR = new Color(109, 33, 33, 255);

    public static final float SOUND_THRESHOLD_HEAVY = 500f;
    public static final float SOUND_THRESHOLD_SOLID = 100f;
    public static final float ARC_THRESHOLD = 25f;
    public static final float JITTER_MAX_RANGE = 25f;
    public static final int JITTER_MAX_COPIES = 15;

    public static final String STORED_FLUX_KEY = "vayra_caliph_stored_flux_key";
    public static final String STORED_HARD_KEY = "vayra_caliph_stored_hard_key";
    public static final String PROJ_KEY = "vayra_caliph_stored_proj_key";
    public static final String JITTER_KEY = "vayra_caliph_stored_jitter_key";
    public static final String STORED_GENERATORS_KEY = "vayra_caliph_stored_generator_key";
    public static final String STORED_EMITTERS_KEY = "vayra_caliph_stored_emitter_key";

    // sound that plays when you try to put on an excluded hullmod or weapon
    public static String ERROR_SOUND = "vayra_note1";

    // excluded hullmods
    public static ArrayList<String> EXCLUDED_HULLMODS = new ArrayList<>(Collections.singletonList(
            HullMods.MAKESHIFT_GENERATOR));

    private static final boolean UTIL_LOG = false;
    private static final boolean FLUX_LOG = false;

    public static final class JitterData {

        public float timeToLive = 0f;
        public float originalTimeToLive = 0f;
        public float intensity = 0f;
        public float copies = 1f;
        public float range = 0f;

        public JitterData(Float damage) {
            add(damage);
        }

        public void add(float damage) {
            this.intensity = (float) Math.max(this.intensity, Math.min(1f, Math.pow(damage, 0.33f) / 10f));
            this.timeToLive = (float) Math.max(this.timeToLive, Math.pow(damage, 0.4f) / 25f);
            this.originalTimeToLive = Math.max(this.originalTimeToLive, this.timeToLive);
            this.copies = Math.max(this.copies, intensity * JITTER_MAX_COPIES);
            this.range = Math.max(this.range, intensity * JITTER_MAX_RANGE);
        }

        public void age(float amount) {
            this.timeToLive -= amount;
            amount /= this.originalTimeToLive;
            this.intensity -= amount * this.intensity;
            this.copies -= amount * this.copies;
            this.range -= amount * this.range;
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "rotating shield emitters";
        }
        if (index == 1) {
            return "force-transfer skinshield system";
        }
        if (index == 2) {
            return "shield generator";
        }
        if (index == 3) {
            return "overloaded or destroyed";
        }
        return null;
    }

    // increase overload duration
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getOverloadTimeMod().modifyMult(id, OVERLOAD_MULT);
    }

    // handles removing excluded hullmods
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        // remove excluded mods, play a sound if we do
        ArrayList<String> delete = new ArrayList<>();
        for (String excluded : EXCLUDED_HULLMODS) {
            if (ship.getVariant().hasHullMod(excluded)) {
                delete.add(excluded);
            }
        }
        for (String toDelete : delete) {
            ship.getVariant().removeMod(toDelete);
            Global.getSoundPlayer().playUISound(ERROR_SOUND, 1f, 1f);
        }
    }

    private void utilLog(String log) {
        if (UTIL_LOG) logger.info(log);
    }

    private void fluxLog(String log) {
        if (FLUX_LOG) logger.info(log);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        // setup stuff
        CombatEngineAPI engine = Global.getCombatEngine();

        if (ship.getHullSpec() != null) {
            // Find the generator
            ShipAPI generator = findGenerator(ship, engine);
            if (generator == null) {
                logger.error("no generator found for ship "+ship+", returning...");
                return;
            }
            List<ShipAPI> emitters = findEmitters(generator, engine);
            switch (ship.getHullSpec().getHullId()) {
                case SHIELD_PART_ID: {
                    // Handle emitters
                    doShieldEmitterStuff(ship, generator);
                    break;
                }
                case SHIELD_GENERATOR_ID: {
                    // handle generator
                    doShieldGenStuff(ship, emitters, engine);
                    break;
                }
                default: {
                    // handle damage reduction and visuals for everything else
                    doEveryOtherPartStuff(ship, amount, generator, engine);
                    break;
                }
            }
        }
    }

    private ShipAPI findGenerator(ShipAPI ship, CombatEngineAPI engine) {
        // First, try getting generator directly from the cached data
        Map<FleetMemberAPI, ShipAPI> storedGenerators;
        if (engine.getCustomData().get(STORED_GENERATORS_KEY) instanceof Map) {
            storedGenerators = (Map<FleetMemberAPI, ShipAPI>) engine.getCustomData().get(STORED_GENERATORS_KEY);
        } else {
            storedGenerators = new HashMap<>();
        }
        utilLog("storedGenerators.size(): "+storedGenerators.size());
        // find the generator
        FleetMemberAPI shipsFleetMember = getFleetMember(ship);
        ShipAPI generator = storedGenerators.get(shipsFleetMember);
        if (generator == null) {
            // Now try getting generator directly since we have no cached data
            List<ShipAPI> shipsWithin1000range = CombatUtils.getShipsWithinRange(ship.getLocation(), 1000);

            for (ShipAPI check : shipsWithin1000range) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                        && ship.getParentStation() != null
                        && ship.getParentStation().equals(check.getParentStation())
                ) {
                    generator = check;
                    storedGenerators.put(shipsFleetMember, generator);
                }
            }

            // If we still didn't find the generator, try the alternative approach - from any other part
            if (generator == null) {
                for (ShipAPI check : shipsWithin1000range) {
                    if (check.getHullSpec() != null
                            && check.getHullSpec().getHullId() != null
                            && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                            &&
                            (
                                    ship.equals(check.getParentStation())
                                            ||
                                            (
                                                    ship.getParentStation() != null
                                                            && ship.getParentStation().equals(check.getParentStation())
                                            )
                            )
                    ) {
                        generator = check;
                        storedGenerators.put(shipsFleetMember, generator);
                    }
                }
            }
        }

        // sanitize the list from dead or no longer existing fleetmembers
        Set<FleetMemberAPI> keysToRemove = new HashSet<>();
        // Fill up the to-remove set first
        for (FleetMemberAPI key : storedGenerators.keySet()) {
            CombatFleetManagerAPI playerFleet = engine.getFleetManager(FleetSide.PLAYER);
            CombatFleetManagerAPI enemyFleet = engine.getFleetManager(FleetSide.ENEMY);
            if (playerFleet.getShipFor(key) != null && !playerFleet.getShipFor(key).isAlive()) keysToRemove.add(key);
            if (enemyFleet.getShipFor(key) != null && !enemyFleet.getShipFor(key).isAlive()) keysToRemove.add(key);
            if (key == null) keysToRemove.add(key);
        }
        // then remove the keys that need to be removed from the saved list
        for (FleetMemberAPI key : keysToRemove) {
            storedGenerators.remove(key);
        }
        utilLog("storedGenerators after sanitizing: "+storedGenerators);

        // Finally, save the generator map
        engine.getCustomData().put(STORED_GENERATORS_KEY, storedGenerators);

        return generator;
    }

    private List<ShipAPI> findEmitters(ShipAPI generator, CombatEngineAPI engine) {
        // set up emitters list
        Map<FleetMemberAPI, List<ShipAPI>> storedEmitters; // shield generator -> shield emitters
        if (engine.getCustomData().get(STORED_EMITTERS_KEY) instanceof Map) {
            storedEmitters = (Map<FleetMemberAPI, List<ShipAPI>>) engine.getCustomData().get(STORED_EMITTERS_KEY);
        } else {
            storedEmitters = new HashMap<>();
        }

        utilLog("storedEmitters.size(): "+storedEmitters.size());
        // find the emitters
        FleetMemberAPI generatorFleetMember = getFleetMember(generator);
        List<ShipAPI> emitters = storedEmitters.get(generatorFleetMember);
        if (emitters == null || emitters.isEmpty()) {
            emitters = new ArrayList<>();
            for (ShipAPI check : CombatUtils.getShipsWithinRange(generator.getLocation(), 1000)) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_PART_ID)
                        && generator.getParentStation() != null
                        && generator.getParentStation().equals(check.getParentStation())
                        && !emitters.contains(check)) {
                    emitters.add(check);
                }
            }
            storedEmitters.put(generatorFleetMember, emitters);
        }

        // sanitize the list from dead or no longer existing fleetmembers
        Set<FleetMemberAPI> keysToRemove = new HashSet<>();
        // Fill up the to-remove set first
        for (FleetMemberAPI key : storedEmitters.keySet()) {
            CombatFleetManagerAPI playerFleet = engine.getFleetManager(FleetSide.PLAYER);
            CombatFleetManagerAPI enemyFleet = engine.getFleetManager(FleetSide.ENEMY);
            if (playerFleet.getShipFor(key) != null && !playerFleet.getShipFor(key).isAlive()) keysToRemove.add(key);
            if (enemyFleet.getShipFor(key) != null && !enemyFleet.getShipFor(key).isAlive()) keysToRemove.add(key);
            if (key == null) keysToRemove.add(key);
        }
        // then remove the keys that need to be removed from the saved list
        for (FleetMemberAPI key : keysToRemove) {
            storedEmitters.remove(key);
        }

        utilLog("storedEmitters after sanitizing: "+storedEmitters);

        // Also save emitters since they're only used here
        engine.getCustomData().put(STORED_EMITTERS_KEY, storedEmitters);

        return emitters;
    }

    private FleetMemberAPI getFleetMember(ShipAPI ship) {
        FleetMemberAPI shipsFleetMember = CombatUtils.getFleetMember(ship);
        if (shipsFleetMember == null) {
            logger.warn("CombatUtils.getFleetMember() returned null for this ship!\tship: "+ship);
            shipsFleetMember = ship.getFleetMember();
        }
        if (shipsFleetMember == null) {
            logger.error("ShipAPI.getFleetMember() returned null for this ship as well!\tship: "+ship);
            // Maybe we're a module?
            if (ship.getParentStation() != null) {
                ShipAPI parent = ship.getParentStation();
                // Please excuse the recursive call but it should be safe to do for modules I think/hope...
                shipsFleetMember = getFleetMember(parent);

                if (shipsFleetMember == null) {
                    logger.error("getFleetMember() returned null for this ship's parent as well!\tparent: "+parent);
                }
            }
        }

        return shipsFleetMember;
    }

    private void doEveryOtherPartStuff(ShipAPI ship, float amount, ShipAPI generator, CombatEngineAPI engine) {
        if (generator != null && generator.isAlive() && !generator.getFluxTracker().isOverloaded()) {
            // change the color according to the GENERATOR'S flux level for everything else
            float genFlux = generator.getFluxLevel();
            Color color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, genFlux);

            // get the jitterlist
            Map<ShipAPI, JitterData> jitters;
            if (engine.getCustomData().get(JITTER_KEY) instanceof Map) {
                jitters = (Map<ShipAPI, JitterData>) engine.getCustomData().get(JITTER_KEY);
            } else {
                jitters = new HashMap<>();
            }

            // run the jitters
            if (jitters.containsKey(ship)) {
                JitterData jitter = jitters.get(ship);
                ship.setJitter(SHIELD_GENERATOR_ID, color.brighter(), jitter.intensity, (int) Math.max(1, jitter.copies), jitter.range);
                ship.setJitterUnder(SHIELD_GENERATOR_ID, color, jitter.intensity, (int) Math.max(1, jitter.copies), jitter.range);
                jitter.age(amount);
                if (jitter.timeToLive <= 0f) {
                    jitters.remove(ship);
                }
            }

            // set armor/hull to take 90% less damage
            ship.getMutableStats().getArmorDamageTakenMult().modifyMult(SHIELD_GENERATOR_ID, 0.1f);
            ship.getMutableStats().getHullDamageTakenMult().modifyMult(SHIELD_GENERATOR_ID, 0.1f);
            ship.getMutableStats().getEmpDamageTakenMult().modifyMult(SHIELD_GENERATOR_ID, 0.1f);

            // make sure we have a list of active projectiles
            List<DamagingProjectileAPI> projs;
            if (engine.getCustomData().get(PROJ_KEY) instanceof List) {
                projs = (List<DamagingProjectileAPI>) engine.getCustomData().get(PROJ_KEY);
            } else {
                projs = new ArrayList<>();
            }

            // loop through all projectiles
            for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(ship.getLocation(), 1000)) {
                // ignore ones that already did damage
                if (proj.didDamage()) {
                    continue;
                }

                // add new ones to our list of active projectiles
                if (!projs.contains(proj)) {
                    projs.add(proj);
                }
            }

            List<DamagingProjectileAPI> toRemove = new ArrayList<>();

            // loop through all ACTIVE projectiles in our list
            for (DamagingProjectileAPI proj : projs) {
                // trigger only on projectiles that have dealt damage, are still in play, and hit this ship (or module)
                if (proj.didDamage() && ship.equals(proj.getDamageTarget()) && engine.isEntityInPlay(proj)) {

                    // trigger the shield effect stuff
                    triggerShield(
                            ship,
                            proj.getSource(),
                            proj.getLocation(),
                            proj.getDamageAmount(),
                            proj.getDamageType(),
                            generator,
                            color,
                            true
                    );

                    if (VAYRA_DEBUG) {
                        logger.info(String.format("doEveryOtherPartStuff()\t%s triggering skinshield for %s %s damage",
                                proj.getWeapon().getId(), proj.getBaseDamageAmount(), proj.getDamageType().name()));
                    }
                } else if (proj.didDamage() || proj.isFading() || !engine.isEntityInPlay(proj)) {
                    // remove the projectiles from the "active" list as they are now spent
                    toRemove.add(proj);
                }
            }
            for (DamagingProjectileAPI proj : toRemove) {
                projs.remove(proj);
            }

            // now do more or less the same thing for beams
            for (BeamAPI beam : engine.getBeams()) {
                if (beam.didDamageThisFrame() && ship.equals(beam.getDamageTarget()) && beam.getBrightness() > 0f) {
                    triggerShield(
                            ship,
                            beam.getSource(),
                            beam.getTo(),
                            beam.getDamage().getDamage() / 10f,
                            beam.getDamage().getType(),
                            generator,
                            color,
                            false
                    );

                    if (VAYRA_DEBUG) {
                        logger.info(String.format("doEveryOtherPartStuff()\t%s triggering skinshield for %s %s damage",
                                beam.getWeapon().getId(), beam.getDamage().getDamage() / 10f, beam.getDamage().getType().name()));
                    }
                }
            }

            // And finally, save the projectiles because we're obviously always fetching them
            engine.getCustomData().put(PROJ_KEY, projs);
            // Save the jitters also since we might have removed some
            engine.getCustomData().put(JITTER_KEY, jitters);
        } else {
            ship.getMutableStats().getArmorDamageTakenMult().unmodify(SHIELD_GENERATOR_ID);
            ship.getMutableStats().getHullDamageTakenMult().unmodify(SHIELD_GENERATOR_ID);
            ship.getMutableStats().getEmpDamageTakenMult().unmodify(SHIELD_GENERATOR_ID);
        }
    }

    private void doShieldGenStuff(ShipAPI generator, List<ShipAPI> emitters, CombatEngineAPI engine) {
        // don't let the generator vent, it looks STUPID
        generator.getMutableStats().getVentRateMult().modifyMult(this.toString(), 0f);

        // get emitter flux
        float storedEmitterHard;
        float storedEmitterFlux;
        Map<ShipAPI, Float> storedHard;
        if (engine.getCustomData().get(STORED_HARD_KEY) instanceof Map) {
            storedHard = (Map<ShipAPI, Float>) engine.getCustomData().get(STORED_HARD_KEY);
        } else {
            storedHard = new HashMap<>();
        }
        if (storedHard.containsKey(generator)) {
            storedEmitterHard = storedHard.get(generator);
        } else {
            storedEmitterHard = 0f;
        }
        Map<ShipAPI, Float> storedFlux;
        if (engine.getCustomData().get(STORED_FLUX_KEY) instanceof Map) {
            storedFlux = (Map<ShipAPI, Float>) engine.getCustomData().get(STORED_FLUX_KEY);
        } else {
            storedFlux = new HashMap<>();
        }
        if (storedFlux.containsKey(generator)) {
            storedEmitterFlux = storedFlux.get(generator);
        } else {
            storedEmitterFlux = 0f;
        }
        fluxLog("doShieldGenStuff()\tstoredHard map: "+storedHard+"\tstoredFlux map: "+storedFlux);
        float emitterHard = 0f;
        float emitterFlux = 0f;
        for (ShipAPI emitter : emitters) {
            fluxLog("doShieldGenStuff()\tEmitter "+emitter+":\thard flux: "+emitter.getFluxTracker().getHardFlux()+", current flux: "+emitter.getCurrFlux());
            emitterHard += emitter.getFluxTracker().getHardFlux();
            emitterFlux += emitter.getCurrFlux();
        }
        // If any of these two are very small numbers, e.g. 1E-45 (0.fourty zeroes then 1) just round them to 0
        if (emitterHard < 0.00001f) emitterHard = 0f;
        if (emitterFlux < 0.00001f) emitterFlux = 0f;
        utilLog("doShieldGenStuff()\tstoring emitterHard: "+emitterHard+", emitterFlux: "+emitterFlux+"\t\tgenerator max: "+ generator.getFluxTracker().getMaxFlux());
        storedHard.put(generator, emitterHard);
        storedFlux.put(generator, emitterFlux);

        fluxLog("doShieldGenStuff()\tgenerator currentFlux: "+generator.getFluxTracker().getCurrFlux()+", generator hardFlux: "+generator.getFluxTracker().getHardFlux());

        // increase/decrease the generator flux, overload is handled by the increaseFlux method
        float hardIncrease = Math.max(0f, emitterHard - storedEmitterHard);
        float softIncrease = Math.max(0f, Math.max(0f, emitterFlux - storedEmitterFlux) - hardIncrease);
        float fluxDecrease = Math.max(0f, storedEmitterFlux - emitterFlux);
        fluxLog("doShieldGenStuff()\tstoredEmitterHard: "+storedEmitterHard+", storedEmitterFlux: "+storedEmitterFlux+"\thardIncrease: "+hardIncrease+", softIncrease: "+softIncrease+", fluxDecrease: "+fluxDecrease);
        generator.getFluxTracker().decreaseFlux(fluxDecrease);
        generator.getFluxTracker().increaseFlux(softIncrease, false);
        generator.getFluxTracker().increaseFlux(hardIncrease, true);

        // Finally, put in the maps because they weren't being stored and we'll always be making a new map
        engine.getCustomData().put(STORED_HARD_KEY, storedHard);
        engine.getCustomData().put(STORED_FLUX_KEY, storedFlux);
    }

    private void doShieldEmitterStuff(ShipAPI ship, ShipAPI generator) {
        // change this ship's shield color according to flux level
        if (ship.getShield() != null) {
            Color color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, Math.min(ship.getFluxLevel(), 1f));
            ship.getShield().setInnerColor(color);
        }

        // don't let the emitters vent, it looks STUPID
        ship.getMutableStats().getVentRateMult().modifyMult(this.toString(), 0f);

        // if the generator's dead, disable the shields
        if (generator == null || generator.getHullLevel() < 0.01f) {
            ship.getShield().setArc(0f);
        } else if (generator.getFluxTracker().isOverloaded() && !ship.getFluxTracker().isOverloaded()) {
            // if the generator's overloading, overload the emitters
            ship.getFluxTracker().beginOverloadWithTotalBaseDuration(generator.getFluxTracker().getOverloadTimeRemaining() / OVERLOAD_MULT);
        }
    }

    private void triggerShield(ShipAPI ship, ShipAPI source, Vector2f point, Float damage, DamageType type, ShipAPI generator, Color color, Boolean hard) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null) {
            return;
        }

        // multiply damage as if shield was hit
        switch (type) {
            case KINETIC:
                damage *= 2f;
                break;
            case HIGH_EXPLOSIVE:
                damage *= 0.5f;
                break;
            case FRAGMENTATION:
                damage *= 0.25f;
                break;
            default:
                break;
        }

        // play energy weapon hitting shield sound based on damage
        float volume = 1f;
        if (damage >= SOUND_THRESHOLD_HEAVY) {
            Global.getSoundPlayer().playSound("hit_shield_heavy_gun", 1f, volume, point, ship.getVelocity());
        } else if (damage >= SOUND_THRESHOLD_SOLID) {
            Global.getSoundPlayer().playSound("hit_shield_solid_gun", 1f, volume, point, ship.getVelocity());
        } else {
            // if it's below the solid hit threshold, modulate the volume even lower based on damage
            volume = Math.max(damage / SOUND_THRESHOLD_SOLID, 0.5f); // min 0.5 volume
            Global.getSoundPlayer().playSound("hit_shield_light_gun", 1f, volume, point, ship.getVelocity());
        }

        // get the jitterlist
        Map<ShipAPI, JitterData> jitters;
        if (engine.getCustomData().get(JITTER_KEY) instanceof Map) {
            jitters = (Map<ShipAPI, JitterData>) engine.getCustomData().get(JITTER_KEY);
        } else {
            jitters = new HashMap<>();
        }

        // jitter the ship for a while based on damage dealt
        JitterData jitter = new JitterData(damage);
        jitters.put(ship, jitter);

        // increase the generator flux by the amount of damage dealt
        generator.getFluxTracker().increaseFlux(damage, hard);

        if (VAYRA_DEBUG) {
            logger.info(String.format("triggerShield()\tpassing %s damage to shield generator as flux", damage));
        }

        // create cosmetic EMP arcs to the shield generator for everything over a reasonable amount of damage
        if (damage >= ARC_THRESHOLD) {
            float width = (float) Math.pow(damage, 0.2) * 10f;
            int arcCount = (int) Math.max(1f, width / 10);
            Vector2f loc = MathUtils.getRandomPointInCircle(generator.getLocation(), generator.getCollisionRadius() / 1.5f);
            while (!CollisionUtils.isPointWithinBounds(loc, generator)) {
                loc = MathUtils.getRandomPointInCircle(generator.getLocation(), generator.getCollisionRadius() / 1.5f);
            }
            for (int i = 0; i < arcCount; i++) {
                engine.spawnEmpArcPierceShields(
                        source,
                        point,
                        ship,
                        new SimpleEntity(loc),
                        DamageType.ENERGY,
                        0f,
                        0f,
                        10000f,
                        "tachyon_lance_emp_impact",
                        width,
                        color.brighter(),
                        color
                );
            }
        }

        // Save the jitters since we might have put some new ones in
        engine.getCustomData().put(JITTER_KEY, jitters);
    }
}
