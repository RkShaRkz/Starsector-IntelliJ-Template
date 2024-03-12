package data.scripts.weapons;

import com.fs.starfarer.api.combat.BeamAPI;
import com.fs.starfarer.api.combat.BeamEffectPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class VayraJoachimOnHitEffect implements BeamEffectPlugin {
    
    private static final float HARD_FLUX_PERCENT = 10f;

    private final IntervalUtil fireInterval = new IntervalUtil(0.5f, 0.6f);
    private boolean wasZero = true;
    
    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {

            boolean shieldHit = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());
            if (shieldHit) {
                ShipAPI fucko = (ShipAPI) target;
                fucko.getFluxTracker().increaseFlux((fucko.getMaxFlux() / 100) * HARD_FLUX_PERCENT * amount, true);
                
                float dur = beam.getDamage().getDpsDuration();
                // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
                if (!wasZero) {
                    dur = 0;
                }
                wasZero = beam.getDamage().getDpsDuration() <= 0;
                fireInterval.advance(dur);
                
                if (fireInterval.intervalElapsed()) {
                    Vector2f dir = Vector2f.sub(beam.getTo(), beam.getFrom(), new Vector2f());
                    if (dir.lengthSquared() > 0) {
                        dir.normalise();
                    }
                    dir.scale(50f);
                    Vector2f point = Vector2f.sub(beam.getTo(), dir, new Vector2f());
                    float dam = beam.getWeapon().getDamage().getDamage();
                    engine.spawnEmpArcPierceShields(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam * 0.25f, // damage
                            dam * 0.5f, // emp 
                            100000f, // max range 
                            "tachyon_lance_emp_impact",
                            beam.getWidth() + 5f,
                            beam.getFringeColor(),
                            beam.getCoreColor()
                    );
                }
            }
        }
    }
}
