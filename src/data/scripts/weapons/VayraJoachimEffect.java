// based on Nickescriptes, bless nicke bless 'em
package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.apache.log4j.Logger;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static data.scripts.util.MiscUtils.getMaximumWeaponSpecAngleOffsetsSize;

public class VayraJoachimEffect implements EveryFrameWeaponEffectPlugin {

    public static Logger log = Global.getLogger(VayraJoachimEffect.class);

    // The maximum degrees the beam can go towards each side
    public static final float DISTANCE_MAX = 30f;

    // full-power burst duration
    public static final float FIRE_TIME = 3f;

    // Base angle (0f)
    private static final List<Float> BASE_ANGLES = new ArrayList<>(Collections.singletonList(0f));

    // In-script variables
    private float fireTime = 0;
    private boolean restart = false;

    /**
     * Returns the <i>index</i> from <i>list</i> if it's within bounds, for OutOfBound indices returns the last element
     *
     * @param list the list to get an element from
     * @param index the index of the element to get
     * @return the list[index] element or list[size-1] element if index >= list.size()
     */
    private float safeGetFromArrayList(List<Float> list, int index) {
        float retVal = 0;
        if (index < list.size()) {
            retVal = list.get(index);
        } else {
            retVal = list.get(list.size() - 1);
        }

        return retVal;
    }

    /**
     * Generates the move array for a given {@link WeaponAPI} by looking at it's:
     * {@link WeaponSpecAPI#getTurretAngleOffsets()},
     * {@link WeaponSpecAPI#getHardpointAngleOffsets()}
     * {@link WeaponSpecAPI#getHiddenAngleOffsets()}
     * <br>
     * taking the max of all three sizes and generating a move array out of that by the specified formula.
     *
     * @param weapon the weapon for which to generate the move array
     * @return the move array
     */
    private float[] generateMoveArray(WeaponAPI weapon) {
        // First, figure out how many items we have
        int size = getMaximumWeaponSpecAngleOffsetsSize(weapon);

        // now that we know how large the random array should be, lets create it
        float[] retVal = new float[size];
        for (int i = 0; i < size; i++) {
            retVal[i] =  (2 * i - 1) * (DISTANCE_MAX / FIRE_TIME) * (float) FastTrig.cos(fireTime);
        }

        return retVal;
    }



    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        // Don't run if we are paused, or our weapon is null
        if (engine.isPaused() || weapon == null) {
            return;
        }

        if (restart) {
            int maxOffsetSize = getMaximumWeaponSpecAngleOffsetsSize(weapon);
            for (int i = 0; i < maxOffsetSize; i++) {
                if (i < weapon.getSpec().getTurretAngleOffsets().size()) {
                    weapon.getSpec().getTurretAngleOffsets().set(i, safeGetFromArrayList(BASE_ANGLES, i));
                }
                if (i < weapon.getSpec().getHardpointAngleOffsets().size()) {
                    weapon.getSpec().getHardpointAngleOffsets().set(i, safeGetFromArrayList(BASE_ANGLES, i));
                }
                if (i < weapon.getSpec().getHiddenAngleOffsets().size()) {
                    weapon.getSpec().getHiddenAngleOffsets().set(i, safeGetFromArrayList(BASE_ANGLES, i));
                }
            }
        }

        if (weapon.getChargeLevel() >= 0.99f) {
            restart = false;
            fireTime += amount;

            // sweep the beam
            float[] moveArray = generateMoveArray(weapon);
            int maxOffsetSize = getMaximumWeaponSpecAngleOffsetsSize(weapon);
            for (int i = 0; i < maxOffsetSize; i++) {
                if (i < weapon.getSpec().getTurretAngleOffsets().size()) {
                    weapon.getSpec().getTurretAngleOffsets().set(i, moveArray[i]);
                }
                if (i < weapon.getSpec().getHardpointAngleOffsets().size()) {
                    weapon.getSpec().getHardpointAngleOffsets().set(i, moveArray[i]);
                }
                if (i < weapon.getSpec().getHiddenAngleOffsets().size()) {
                    weapon.getSpec().getHiddenAngleOffsets().set(i, moveArray[i]);
                }
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
