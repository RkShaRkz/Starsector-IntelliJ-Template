package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraBeamRotorEffect implements EveryFrameWeaponEffectPlugin {

    private static final float SPIN = 420f;
    private static final float MAX_JITTER_DISTANCE = 3f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        ShipAPI ship = weapon.getShip();
        if (ship == null || engine == null || engine.isPaused() || !ship.isAlive()) {
            weapon.getAnimation().setFrame(0);
            weapon.getSprite().setColor(new Color(0,0,0,0));
            return;
        }

        // set up glow color according to hull level
        Color newColor = new Color(250, 150, 0, 255);
        float red = (float) newColor.getRed() / 255f;
        float green = (float) newColor.getGreen() / 255f;
        float blue = (float) newColor.getBlue() / 255f;
        float alpha = 1f;

        // switch to actual sprite if > 0 alpha (to avoid showing in refit screen)
        if (alpha > 0f) {
            weapon.getAnimation().setFrame(1);
        } else {
            weapon.getAnimation().setFrame(0);
        }

        // actually set color
        Color colorToUse = new Color(red, green, blue, alpha);
        weapon.getSprite().setColor(colorToUse);

        // jitter
        if (ship.getHullLevel() < 1f) {
            float jitter = MAX_JITTER_DISTANCE * (1f - ship.getHullLevel());

            Vector2f randomOffset = MathUtils.getRandomPointInCircle(
                    new Vector2f(
                            weapon.getSprite().getWidth() / 2f,
                            weapon.getSprite().getHeight() / 2f),
                    jitter);
            weapon.getSprite().setCenter(randomOffset.x, randomOffset.y);
        }

        float curr = weapon.getCurrAngle();
        float spin = SPIN * amount; // always spin at at LEAST base spin rate/sec
        curr += spin;
        weapon.setCurrAngle(curr);
    }
}
