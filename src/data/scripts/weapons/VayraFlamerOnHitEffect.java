package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VayraFlamerOnHitEffect implements OnHitEffectPlugin {

    public static final Color FLAME_COLOR = new Color(100, 255, 0, 50);
    public static final float FLAME_SIZE = 35f; // +/- 50%
    public static final float FLAME_DUR = 0.666f;

    public static final float FLUX_PORTION = 0.5f; // fraction of damage dealt as flux to target

    @Override
    //public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (target == null) {
            return;
        }

        if (!shieldHit) {

            Vector2f loc = point != null ? point : projectile.getLocation();
            Vector2f vel = new Vector2f(target.getVelocity().x * 0.45f, target.getVelocity().y * 0.45f);
            float size = (float) ((FLAME_SIZE / 2f) + (Math.random() * FLAME_SIZE));
            engine.spawnExplosion(loc, vel, FLAME_COLOR, size, FLAME_DUR);

            ShipAPI ship = target instanceof ShipAPI ? (ShipAPI) target : null;
            if (ship != null) {
                float fluxDamage = projectile.getDamageAmount() * FLUX_PORTION;
                ship.getFluxTracker().increaseFlux(fluxDamage, false);
            }
        }
    }
}
