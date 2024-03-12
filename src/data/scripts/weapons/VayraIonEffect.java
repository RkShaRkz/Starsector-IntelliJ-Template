package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class VayraIonEffect implements OnHitEffectPlugin {

    private static final float ARC_CHANCE = 0.25f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if ((float) Math.random() < ARC_CHANCE && !shieldHit && target instanceof ShipAPI) {

            float dam = projectile.getDamageAmount();
            float emp = projectile.getEmpAmount();

            engine.spawnEmpArc(projectile.getSource(), point, target, target,
                    DamageType.ENERGY,
                    dam,
                    emp, // emp 
                    100000f, // max range 
                    "tachyon_lance_emp_impact",
                    15f, // thickness
                    new Color(25, 100, 155, 255),
                    new Color(255, 255, 255, 255)
            );
        }
    }
}
