package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.KadurModPlugin.VAYRA_DEBUG;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    private static final class JitterData {

        public float timeToLive = 0f;
        public float originalTimeToLive = 0f;
        public float intensity = 0f;
        public float copies = 1f;
        public float range = 0f;

        private JitterData(Float damage) {
            add(damage);
        }

        private void add(float damage) {
            this.intensity = (float) Math.max(this.intensity, Math.min(1f, Math.pow(damage, 0.33f) / 10f));
            this.timeToLive = (float) Math.max(this.timeToLive, Math.pow(damage, 0.4f) / 25f);
            this.originalTimeToLive = Math.max(this.originalTimeToLive, this.timeToLive);
            this.copies = Math.max(this.copies, intensity * JITTER_MAX_COPIES);
            this.range = Math.max(this.range, intensity * JITTER_MAX_RANGE);
        }

        private void age(float amount) {
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
        //CombatEngineAPI engine = Global.getCombatEngine();
        //LocalData localData = getLocalData(engine);
        
        // change this ship's shield color according to flux level
        Color color;
        if (ship.getShield() != null) {
            color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, Math.min(ship.getFluxLevel(), 1f));
            ship.getShield().setInnerColor(color);
        }
        
        if (ship.getParentStation() != null) {
            if (ship.getParentStation().isHoldFire()) {
                ship.giveCommand(ShipCommand.HOLD_FIRE, null, 0);
            }
            if (ship.hasLaunchBays()) {
                if (ship.isPullBackFighters() ^ ship.getParentStation().isPullBackFighters()) {
                    ship.giveCommand(ShipCommand.PULL_BACK_FIGHTERS, null, 0);
                }
                //ship.setPullBackFighters(ship.getParentStation().isPullBackFighters());
                if (ship.getAIFlags() != null && !ship.getParentStation().isPullBackFighters()) {
                    if (((Global.getCombatEngine().getPlayerShip() == ship.getParentStation()) || (ship.getParentStation().getAIFlags() == null))
                            && (ship.getParentStation().getShipTarget() != null)) {
                        ship.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, ship.getParentStation().getShipTarget());
                    } else if ((ship.getParentStation().getAIFlags() != null)
                            && ship.getParentStation().getAIFlags().hasFlag(AIFlags.CARRIER_FIGHTER_TARGET)
                            && (ship.getParentStation().getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET) != null)) {
                        ship.getAIFlags().setFlag(AIFlags.CARRIER_FIGHTER_TARGET, 1f, ship.getParentStation().getAIFlags().getCustom(AIFlags.CARRIER_FIGHTER_TARGET));
                    }
                }
            }
        }

        if (ship.getHullSpec() != null) {
            switch (ship.getHullSpec().getHullId()) {
                case SHIELD_PART_ID: {
                    if (!ship.hasListenerOfClass(EXP3doEveryOtherPartStuff.class)) {ship.addListener(new EXP3doEveryOtherPartStuff());}
                    doShieldEmitterStuff(ship);
                    break;
                } // handle generator
                case SHIELD_GENERATOR_ID: {
                    doShieldGenStuff(ship);
                    break;
                } // handle damage reduction and visuals for everything else
                default: {
                    if (!ship.hasListenerOfClass(EXPdoEveryOtherPartStuff.class)) {ship.addListener(new EXPdoEveryOtherPartStuff());}
                    //doEveryOtherPartStuff(ship, amount, engine, localData); just in case
                    EXP2doEveryOtherPartStuff(ship, amount);
                    if (ship.getParentStation() != null && !ship.getCustomData().containsKey("vayra_apply_dmod") && ship.getParentStation().getHullSpec().getHints().contains(ShipHullSpecAPI.ShipTypeHints.STATION)) {if (ship.getHullSpec().getHullId().equals("vayra_caliph_leftengine") || ship.getHullSpec().getHullId().equals("vayra_caliph_rightengine")) {ship.setHitpoints(ship.getHitpoints()/4f);ship.setHeavyDHullOverlay();}else {ship.setHeavyDHullOverlay();}ship.setCustomData("vayra_apply_dmod", true);}
                    break;
                }
            }
        }
        
    }
    
    //Reapers have this annoying tendency to always make the emitter blow up!
    public static class EXP3doEveryOtherPartStuff implements DamageTakenModifier {
            
            @Override
            public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
                if (!shieldHit) damage.getModifier().modifyMult("vayra_modular_shields", 0.02f);
                return null;
            }
        }
    
    public static class EXPdoEveryOtherPartStuff implements DamageTakenModifier {
            @Override
            public String modifyDamageTaken(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit) {
                Color color;

                    // find the generator 
                    ShipAPI ship;
                    if (target instanceof ShipAPI) {ship = (ShipAPI) target;} else {return null;}
                    ShipAPI generator = (ShipAPI) ship.getCustomData().get(STORED_GENERATORS_KEY); //ShipAPI generator = localData.storedGenerators.get(ship);
                    if (generator == null) {
                        for (ShipAPI check : CombatUtils.getShipsWithinRange(target.getLocation(), 1000)) {
                            if (check.getHullSpec() != null
                                    && check.getHullSpec().getHullId() != null
                                    && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                                    && (ship.equals(check.getParentStation())
                                    || (ship.getParentStation() != null
                                    && ship.getParentStation().equals(check.getParentStation())))) {
                                generator = check;
                                ship.setCustomData(STORED_GENERATORS_KEY, generator);//localData.storedGenerators.put(ship, generator);
                            }
                        }
                    }
                    if (generator != null && generator.isAlive() && !generator.getFluxTracker().isOverloaded()) {

                        // change the color according to the GENERATOR'S flux level for everything else
                        float genFlux = generator.getFluxLevel();
                        color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, genFlux);
                        
                        if (param instanceof DamagingProjectileAPI) {
                            DamagingProjectileAPI proj = (DamagingProjectileAPI) param;
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
                        } else if (param instanceof BeamAPI) {
                            BeamAPI beam = (BeamAPI) param;
                            triggerShield(
                                        ship,
                                        beam.getSource(),
                                        beam.getTo(),
                                        beam.getDamage().getDamage() / 10f,
                                        beam.getDamage().getType(),
                                        generator,
                                        color,
                                        beam.getDamage().isForceHardFlux()); //can't be false since HSA can enable it now :));

                                if (VAYRA_DEBUG) {
                                    log.info(String.format("%s triggering skinshield for %s %s damage",
                                            beam.getWeapon().getId(), beam.getDamage().getDamage() / 10f, beam.getDamage().getType().name()));
                                }
                        }
                        damage.getModifier().modifyMult("vayra_modular_shields", generator.getFluxTracker().isOverloadedOrVenting() ? 1f : Math.max(0.1f, generator.getFluxTracker().getFluxLevel()*0.9f));
                        
                    }
                return null;
            }
        }
    private void EXP2doEveryOtherPartStuff(ShipAPI ship, float amount) {
        Color color;

        // find the generator
        ShipAPI generator = (ShipAPI) ship.getCustomData().get(STORED_GENERATORS_KEY);//localData.storedGenerators.get(ship);
        if (generator == null) {
            for (ShipAPI check : CombatUtils.getShipsWithinRange(ship.getLocation(), 1000)) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                        && (ship.equals(check.getParentStation())
                        || (ship.getParentStation() != null
                        && ship.getParentStation().equals(check.getParentStation())))) {
                    generator = check;
                    ship.setCustomData(STORED_GENERATORS_KEY, generator);//localData.storedGenerators.put(ship, generator);
                }
            }
        }
        if (generator != null && generator.isAlive() && !generator.getFluxTracker().isOverloaded()) {

            // change the color according to the GENERATOR'S flux level for everything else
            float genFlux = generator.getFluxLevel();
            color = Misc.interpolateColor(ZERO_FLUX_COLOR, FULL_FLUX_COLOR, genFlux);

            // run the jitters
            if (ship.getCustomData().containsKey(JITTER_KEY)/*localData.jitters.containsKey(ship)*/) {
                JitterData jitter = (JitterData) ship.getCustomData().get(JITTER_KEY);//localData.jitters.get(ship);
                ship.setJitter(SHIELD_GENERATOR_ID, color.brighter(), jitter.intensity, (int) Math.max(1, jitter.copies), jitter.range);
                ship.setJitterUnder(SHIELD_GENERATOR_ID, color, jitter.intensity, (int) Math.max(1, jitter.copies), jitter.range);
                jitter.age(amount);
                if (jitter.timeToLive <= 0f) {
                    ship.removeCustomData(JITTER_KEY);//localData.jitters.remove(ship);
                }
            }
        }
    }

    private void doShieldGenStuff(ShipAPI ship) {

        // you're the generator
        ShipAPI generator = ship;

        // find the emitters
        List<ShipAPI> emitters = (List<ShipAPI>) ship.getCustomData().get(STORED_EMITTERS_KEY);//localData.storedEmitters.get(ship);
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
            ship.setCustomData(STORED_EMITTERS_KEY, emitters);//localData.storedEmitters.put(ship, emitters);
        }

        // don't let the generator vent, it looks STUPID
        ship.getMutableStats().getVentRateMult().modifyMult(this.toString(), 0f);

        // get emitter flux
        float storedEmitterHard;
        float storedEmitterFlux;
        if (ship.getCustomData().containsKey(STORED_HARD_KEY)/*localData.storedHard.containsKey(ship)*/) {
            storedEmitterHard = (float) ship.getCustomData().get(STORED_HARD_KEY);
        } else {
            storedEmitterHard = 0f;
        }
        if (ship.getCustomData().containsKey(STORED_FLUX_KEY)/*localData.storedFlux.containsKey(ship)*/) {
            storedEmitterFlux = (float) ship.getCustomData().get(STORED_FLUX_KEY);
        } else {
            storedEmitterFlux = 0f;
        }
        float emitterHard = 0f;
        float emitterFlux = 0f;
        for (ShipAPI emitter : emitters) {
            emitterHard += emitter.getFluxTracker().getHardFlux();
            emitterFlux += emitter.getCurrFlux();
        }
        ship.setCustomData(STORED_HARD_KEY, emitterHard);//localData.storedHard.put(ship, emitterHard);
        ship.setCustomData(STORED_FLUX_KEY, emitterFlux);//localData.storedFlux.put(ship, emitterFlux);

        // increase/decrease the generator flux, overload is handled by the increaseFlux method
        float hardIncrease = Math.max(0f, emitterHard - storedEmitterHard);
        float softIncrease = Math.max(0f, Math.max(0f, emitterFlux - storedEmitterFlux) - hardIncrease);
        float fluxDecrease = Math.max(0f, Math.abs(storedEmitterFlux - emitterFlux));
        //float fluxDecrease = Math.max(0f, storedEmitterFlux - emitterFlux);
        //if (hardIncrease > 0.1f) Global.getCombatEngine().addFloatingText(new Vector2f(ship.getLocation().x-200f, ship.getLocation().y), String.valueOf(hardIncrease), 30f, Color.RED, ship, 1f, 0.5f);
        //if (softIncrease > 0.1f) Global.getCombatEngine().addFloatingText(ship.getLocation(), String.valueOf(softIncrease), 30f, Color.YELLOW, ship, 1f, 0.5f); else {Global.getCombatEngine().addFloatingText(ship.getLocation(), String.valueOf((emitterFlux - storedEmitterFlux) - hardIncrease), 15f, Color.YELLOW, ship, 1f, 0.5f);}
        //if (fluxDecrease > 0.1f) Global.getCombatEngine().addFloatingText(new Vector2f(ship.getLocation().x+200f, ship.getLocation().y), String.valueOf(fluxDecrease), 30f, Color.BLUE, ship, 1f, 0.5f); else {Global.getCombatEngine().addFloatingText(new Vector2f(ship.getLocation().x+200f, ship.getLocation().y), String.valueOf(storedEmitterFlux - emitterFlux), 15f, Color.BLUE, ship, 1f, 0.5f);}
        generator.getFluxTracker().decreaseFlux(fluxDecrease);
        generator.getFluxTracker().increaseFlux(softIncrease, false);
        generator.getFluxTracker().increaseFlux(hardIncrease, true);
    }

    private void doShieldEmitterStuff(ShipAPI ship) {

        // don't let the emitters vent, it looks STUPID
        ship.getMutableStats().getVentRateMult().modifyMult(this.toString(), 0f);

        // find the generator
        ShipAPI generator = (ShipAPI) ship.getCustomData().get(STORED_GENERATORS_KEY); //localData.storedGenerators.get(ship);
        if (generator == null) {
            for (ShipAPI check : CombatUtils.getShipsWithinRange(ship.getLocation(), 1000)) {
                if (check.getHullSpec() != null
                        && check.getHullSpec().getHullId() != null
                        && check.getHullSpec().getHullId().equals(SHIELD_GENERATOR_ID)
                        && ship.getParentStation() != null
                        && ship.getParentStation().equals(check.getParentStation())) {
                    generator = check;
                    ship.setCustomData(STORED_GENERATORS_KEY, generator);//localData.storedGenerators.put(ship, generator);
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

    private static void triggerShield(ShipAPI ship, ShipAPI source, Vector2f point, Float damage, DamageType type, ShipAPI generator, Color color, Boolean hard) {
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

        // jitter the ship for a while based on damage dealt
        JitterData jitter = new JitterData(damage);
        ship.setCustomData(JITTER_KEY, jitter);

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
