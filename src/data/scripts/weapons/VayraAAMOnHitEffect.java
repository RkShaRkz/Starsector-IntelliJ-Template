package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class VayraAAMOnHitEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (!shieldHit && target instanceof ShipAPI) {

            ShipAPI test = (ShipAPI) target;
            if (test.getHullSize() == HullSize.FIGHTER) {

                float dam = projectile.getDamageAmount();
                engine.applyDamage(target, point, dam, DamageType.HIGH_EXPLOSIVE, 0f, false, false, projectile.getSource(), true);
                Global.getSoundPlayer().playSound("explosion_flak", 1f, 1f, point, Misc.ZERO);
            }
        }
    }
}
