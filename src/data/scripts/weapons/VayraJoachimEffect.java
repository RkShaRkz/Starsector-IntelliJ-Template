// based on Nickescriptes, bless nicke bless 'em
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraJoachimEffect implements EveryFrameWeaponEffectPlugin {
    
    public static Logger log = Global.getLogger(VayraJoachimEffect.class);

    // The maximum degrees the beam can go towards each side
    public static final float DISTANCE_MAX = 30f;

    // full-power burst duration
    public static final float FIRE_TIME = 3f;

    // Base angle (0f)
    private static final List<Float> BASE_ANGLES = new ArrayList<>(Arrays.asList(0f));

    // In-script variables
    private float fireTime = 0;
    private boolean restart = false;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        //Don't run if we are paused, or our weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }
        
        if (restart) {
            for (int i = 0; i < weapon.getSpec().getTurretAngleOffsets().size(); i++) {
                weapon.getSpec().getHardpointAngleOffsets().set(i, BASE_ANGLES.get(i));
                weapon.getSpec().getTurretAngleOffsets().set(i, BASE_ANGLES.get(i));
                weapon.getSpec().getHiddenAngleOffsets().set(i, BASE_ANGLES.get(i));
            }
        }
        
        if (weapon.getChargeLevel() >= 0.99f) {
            restart = false;
            fireTime += amount;
            
            // sweep the beam
            for (int i = 0; i < weapon.getSpec().getTurretAngleOffsets().size(); i++) {
                float move = (2 * i - 1) * (DISTANCE_MAX / FIRE_TIME) * (float) FastTrig.cos(fireTime);
                weapon.getSpec().getHardpointAngleOffsets().set(i, move);
                weapon.getSpec().getTurretAngleOffsets().set(i, move);
                weapon.getSpec().getHiddenAngleOffsets().set(i, move);
            }
            
            // spawn particles
            for (int i = 0; i < 1 + (int) fireTime; i++) {
                Vector2f point = MathUtils.getRandomPointInCone(weapon.getLocation(), 99f, weapon.getCurrAngle() - DISTANCE_MAX / 1.5f, weapon.getCurrAngle() + DISTANCE_MAX / 1.5f);
                Vector2f vel = (Vector2f) VectorUtils.getDirectionalVector(weapon.getLocation(), point).scale(MathUtils.getDistance(point, weapon.getLocation()) * 6.66f);
                float size = (float) (10f + (Math.random() * 10f));
                engine.addHitParticle(
                        point, 
                        vel, 
                        size, 
                        1f, 
                        0.15f, 
                        weapon.getSpec().getGlowColor());
            }
            
        } else if (!restart) {
            fireTime = 0;
            restart = true;
        }
    }
}
