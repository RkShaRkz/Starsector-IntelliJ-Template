package data.scripts.util;

import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;

/**
 * Bunch of small copypastable methods belonging everywhere but nowhere in specific
 */
public class MiscUtils {
    /**
     * Returns the maximum size of {@link WeaponAPI} angle offsets by looking at it's:
     * {@link WeaponSpecAPI#getTurretAngleOffsets()},
     * {@link WeaponSpecAPI#getHardpointAngleOffsets()}
     * {@link WeaponSpecAPI#getHiddenAngleOffsets()}
     *
     * @param weapon the weapon to lookup
     * @return the max size of these three things
     */
    public static int getMaximumWeaponSpecAngleOffsetsSize(WeaponAPI weapon) {
        int size = 0;
        size = Math.max(size, weapon.getSpec().getTurretAngleOffsets().size());
        size = Math.max(size, weapon.getSpec().getHardpointAngleOffsets().size());
        size = Math.max(size, weapon.getSpec().getHiddenAngleOffsets().size());

        return size;
    }
}
