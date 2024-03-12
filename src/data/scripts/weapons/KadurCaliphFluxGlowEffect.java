package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;
import static data.scripts.hullmods.vayra_modular_shields.FULL_FLUX_COLOR;
import static data.scripts.hullmods.vayra_modular_shields.ZERO_FLUX_COLOR;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

// original script by Nia thanks Nia
public class KadurCaliphFluxGlowEffect implements EveryFrameWeaponEffectPlugin {

    private static final float MAX_JITTER_DISTANCE = 2f;
    private static final float ALPHA_MULT = 3f;

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        // setup stuff
        ShipAPI ship = weapon.getShip();
        if (ship == null || engine == null || !ship.isAlive()) {
            return;
        }
        
        Color baseColor = ZERO_FLUX_COLOR;
        if (ship.getShield() != null) {
            baseColor = ship.getShield().getInnerColor();
        }
        
        // set up glow color according to flux level
        Color newColor = Misc.interpolateColor(baseColor, FULL_FLUX_COLOR, ship.getHardFluxLevel());
        float red = (float) newColor.getRed()/255f;
        float green = (float) newColor.getGreen()/255f;
        float blue = (float) newColor.getBlue()/255f;
        float alpha = Math.min(1f, ship.getFluxLevel() * ALPHA_MULT);
        
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
        if (ship.getFluxLevel() > 0.666) {
            Vector2f randomOffset = MathUtils.getRandomPointInCircle(new Vector2f(weapon.getSprite().getWidth() / 2f, weapon.getSprite().getHeight() / 2f), MAX_JITTER_DISTANCE);
            weapon.getSprite().setCenter(randomOffset.x, randomOffset.y);
        }
    }
}
