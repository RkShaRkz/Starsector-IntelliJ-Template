package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.KadurModPlugin.VAYRA_DEBUG;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class vayra_modular_shields extends BaseHullMod {

    public static Logger log = Global.getLogger(vayra_modular_shields.class);

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
    public static ArrayList<String> EXCLUDED_HULLMODS = new ArrayList<>(Arrays.asList(
            HullMods.MAKESHIFT_GENERATOR));

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

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        // setup stuff
        CombatEngineAPI engine = Global.getCombatEngine();

        // change this ship's shield color according to flux level
        Color color;
        if (ship.getShield() != null) {
            color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, Math.min(ship.getFluxLevel(), 1f));
            ship.getShield().setInnerColor(color);
        }

        if (ship.getHullSpec() != null) {
            switch (ship.getHullSpec().getHullId()) {
                case SHIELD_PART_ID: {
                    doShieldEmitterStuff(ship);
                    break;
                } // handle generator
                case SHIELD_GENERATOR_ID: {
                    doShieldGenStuff(ship);
                    break;
                } // handle damage reduction and visuals for everything else
                default: {
                    doEveryOtherPartStuff(ship, amount, engine);
                    break;
                }
            }
        }
    }

    private void doEveryOtherPartStuff(ShipAPI ship, float amount, CombatEngineAPI engine) {
        Color color;

        // set up generators list
        Map<ShipAPI, ShipAPI> storedGenerators; // nongenerator part -> shield generator
        if (engine.getCustomData().get(STORED_GENERATORS_KEY) instanceof Map) {
            storedGenerators = (Map<ShipAPI, ShipAPI>) engine.getCustomData().get(STORED_GENERATORS_KEY);
        } else {
            storedGenerators = new HashMap<>();
        }

        // find the generator
        ShipAPI generator = storedGenerators.get(ship);
        if (generator == null) {
            for (ShipAPI check : CombatUtils.getShipsWithinRange(ship.getLocation(), 1000)) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                        && (ship.equals(check.getParentStation())
                        || (ship.getParentStation() != null
                        && ship.getParentStation().equals(check.getParentStation())))) {
                    generator = check;
                    storedGenerators.put(ship, generator);
                }
            }
        }
        if (generator != null && generator.isAlive() && !generator.getFluxTracker().isOverloaded()) {

            // change the color according to the GENERATOR'S flux level for everything else
            float genFlux = generator.getFluxLevel();
            color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, genFlux);

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
                            true);

                    if (VAYRA_DEBUG) {
                        log.info(String.format("%s triggering skinshield for %s %s damage",
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
                            false);

                    if (VAYRA_DEBUG) {
                        log.info(String.format("%s triggering skinshield for %s %s damage",
                                beam.getWeapon().getId(), beam.getDamage().getDamage() / 10f, beam.getDamage().getType().name()));
                    }
                }
            }
        } else {
            ship.getMutableStats().getArmorDamageTakenMult().unmodify(SHIELD_GENERATOR_ID);
            ship.getMutableStats().getHullDamageTakenMult().unmodify(SHIELD_GENERATOR_ID);
            ship.getMutableStats().getEmpDamageTakenMult().unmodify(SHIELD_GENERATOR_ID);
        }
    }

    private void doShieldGenStuff(ShipAPI ship) {

        CombatEngineAPI engine = Global.getCombatEngine();

        // you're the generator
        ShipAPI generator = ship;

        // set up emitters list
        Map<ShipAPI, List<ShipAPI>> storedEmitters; // shield generator -> shield emitters
        if (engine.getCustomData().get(STORED_EMITTERS_KEY) instanceof Map) {
            storedEmitters = (Map<ShipAPI, List<ShipAPI>>) engine.getCustomData().get(STORED_EMITTERS_KEY);
        } else {
            storedEmitters = new HashMap<>();
        }

        // find the emitters
        List<ShipAPI> emitters = storedEmitters.get(ship);
        if (emitters == null || emitters.isEmpty()) {
            emitters = new ArrayList<>();
            for (ShipAPI check : CombatUtils.getShipsWithinRange(ship.getLocation(), 1000)) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_PART_ID)
                        && ship.getParentStation() != null
                        && ship.getParentStation().equals(check.getParentStation())
                        && !emitters.contains(check)) {
                    emitters.add(check);
                }
            }
            storedEmitters.put(ship, emitters);
        }

        // don't let the generator vent, it looks STUPID
        ship.getMutableStats().getVentRateMult().modifyMult(this.toString(), 0f);

        // get emitter flux
        float storedEmitterHard;
        float storedEmitterFlux;
        Map<ShipAPI, Float> storedHard;
        if (engine.getCustomData().get(STORED_HARD_KEY) instanceof Map) {
            storedHard = (Map<ShipAPI, Float>) engine.getCustomData().get(STORED_HARD_KEY);
        } else {
            storedHard = new HashMap<>();
        }
        if (storedHard.containsKey(ship)) {
            storedEmitterHard = storedHard.get(ship);
        } else {
            storedEmitterHard = 0f;
        }
        Map<ShipAPI, Float> storedFlux;
        if (engine.getCustomData().get(STORED_FLUX_KEY) instanceof Map) {
            storedFlux = (Map<ShipAPI, Float>) engine.getCustomData().get(STORED_FLUX_KEY);
        } else {
            storedFlux = new HashMap<>();
        }
        if (storedFlux.containsKey(ship)) {
            storedEmitterFlux = storedFlux.get(ship);
        } else {
            storedEmitterFlux = 0f;
        }
        float emitterHard = 0f;
        float emitterFlux = 0f;
        for (ShipAPI emitter : emitters) {
            emitterHard += emitter.getFluxTracker().getHardFlux();
            emitterFlux += emitter.getCurrFlux();
        }
        storedHard.put(ship, emitterHard);
        storedFlux.put(ship, emitterFlux);

        // increase/decrease the generator flux, overload is handled by the increaseFlux method
        float hardIncrease = Math.max(0f, emitterHard - storedEmitterHard);
        float softIncrease = Math.max(0f, Math.max(0f, emitterFlux - storedEmitterFlux) - hardIncrease);
        float fluxDecrease = Math.max(0f, storedEmitterFlux - emitterFlux);
        generator.getFluxTracker().decreaseFlux(fluxDecrease);
        generator.getFluxTracker().increaseFlux(softIncrease, false);
        generator.getFluxTracker().increaseFlux(hardIncrease, true);
    }

    private void doShieldEmitterStuff(ShipAPI ship) {

        CombatEngineAPI engine = Global.getCombatEngine();

        // don't let the emitters vent, it looks STUPID
        ship.getMutableStats().getVentRateMult().modifyMult(this.toString(), 0f);

        // set up generators list
        Map<ShipAPI, ShipAPI> storedGenerators; // nongenerator part -> shield generator
        if (engine.getCustomData().get(STORED_GENERATORS_KEY) instanceof Map) {
            storedGenerators = (Map<ShipAPI, ShipAPI>) engine.getCustomData().get(STORED_GENERATORS_KEY);
        } else {
            storedGenerators = new HashMap<>();
        }

        // find the generator
        ShipAPI generator = storedGenerators.get(ship);
        if (generator == null) {
            for (ShipAPI check : CombatUtils.getShipsWithinRange(ship.getLocation(), 1000)) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                        && ship.getParentStation() != null
                        && ship.getParentStation().equals(check.getParentStation())) {
                    generator = check;
                    storedGenerators.put(ship, generator);
                }
            }
        }
        // if the generator's dead, disable the shields
        if (generator == null || generator.getHullLevel() < 0.01f) {
            ship.getShield().setArc(0f);

            // if the generator's overloading, overload the emitters
        } else if (generator.getFluxTracker().isOverloaded() && !ship.getFluxTracker().isOverloaded()) {
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
            log.info(String.format("passing %s damage to shield generator as flux", damage));
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
                        color);
            }
        }
    }
}
