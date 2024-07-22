package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

public class VayraBoomerangShieldEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        ShipAPI source = projectile.getSource();
        if (source == null) {
            return;
        }

        float angle = VectorUtils.getAngle(point, source.getLocation());
        WeaponAPI weapon = projectile.getWeapon();
        DamagingProjectileAPI newProj = (DamagingProjectileAPI) engine.spawnProjectile(
                source,
                weapon,
                weapon.getId() + "_copy",
                MathUtils.getPointOnCircumference(point, 10f, angle),
                angle,
                target.getVelocity());
        engine.addPlugin(new KadurBoomerangShieldGuidance(newProj, source));

        String shieldSound = "vayra_shieldreturn";
        if (projectile.getProjectileSpecId().equals("vayra_light_boomerangshield")) {
            shieldSound += "_light";
        }
        Global.getSoundPlayer().playSound(shieldSound, 1f, 1f, point, Misc.ZERO);
    }
}
