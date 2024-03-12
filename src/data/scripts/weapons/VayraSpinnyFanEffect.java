package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class VayraSpinnyFanEffect implements EveryFrameWeaponEffectPlugin {

    private static final float SPIN = 180f;

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon.getShip() == null || !weapon.getShip().isAlive()) {
            return;
        }

        float curr = weapon.getCurrAngle();

        curr += amount * SPIN;

        weapon.setCurrAngle(curr);
    }
}
