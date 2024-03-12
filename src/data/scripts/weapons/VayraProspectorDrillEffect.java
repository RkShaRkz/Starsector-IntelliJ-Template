package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

public class VayraProspectorDrillEffect implements BeamEffectPlugin {

    private final IntervalUtil fireInterval = new IntervalUtil(0.3f, 0.5f);
    private boolean wasZero = true;

    @Override
    public void advance(float amount, CombatEngineAPI engine, BeamAPI beam) {
        CombatEntityAPI target = beam.getDamageTarget();
        if (target instanceof ShipAPI && beam.getBrightness() >= 1f) {
            float dur = beam.getDamage().getDpsDuration();
            // needed because when the ship is in fast-time, dpsDuration will not be reset every frame as it should be
            if (!wasZero) {
                dur = 0;
            }
            wasZero = beam.getDamage().getDpsDuration() <= 0;
            fireInterval.advance(dur);

            if (fireInterval.intervalElapsed()) {
                boolean hitShield = target.getShield() != null && target.getShield().isWithinArc(beam.getTo());

                if (!hitShield) {
                    Vector2f dir = Vector2f.sub(beam.getTo(), beam.getFrom(), new Vector2f());
                    if (dir.lengthSquared() > 0) {
                        dir.normalise();
                    }
                    dir.scale(50f);
                    Vector2f point = Vector2f.sub(beam.getTo(), dir, new Vector2f());
                    float emp = beam.getDamage().getFluxComponent();
                    float dam = beam.getDamage().getDamage() * 0.1f;
                    engine.spawnEmpArc(
                            beam.getSource(), point, beam.getDamageTarget(), beam.getDamageTarget(),
                            DamageType.ENERGY,
                            dam, // damage
                            emp, // emp 
                            100000f, // max range 
                            "tachyon_lance_emp_impact",
                            beam.getWidth() + 9f,
                            beam.getFringeColor(),
                            beam.getCoreColor()
                    );
                }
            }
        }
    }
}
