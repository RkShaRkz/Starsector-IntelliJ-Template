package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;

public class VayraRotorEffect implements EveryFrameWeaponEffectPlugin {

    private static final float SPIN = 420f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (engine.isPaused() || weapon.getShip() == null) {
            return;
        }
        
        if (!weapon.getShip().isAlive()) {
            weapon.getSprite().setAlphaMult(0f);
        }

        float curr = weapon.getCurrAngle();
        
        float spin = SPIN * amount; // always spin at base spin rate/sec
        
        curr += spin;

        weapon.setCurrAngle(curr);
    }
}
