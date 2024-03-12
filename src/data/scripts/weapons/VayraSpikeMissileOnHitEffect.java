package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;

public class VayraSpikeMissileOnHitEffect implements OnHitEffectPlugin {

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (!shieldHit && target instanceof ShipAPI) {

            float emp = projectile.getEmpAmount();
            float dam = 0;
            int arcs = 2;

            for (int i = 1; i < arcs; i++) {
                engine.spawnEmpArc(projectile.getSource(), point, target, target,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp 
                        100000f, // max range 
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        new Color(25, 100, 155, 255),
                        new Color(255, 255, 255, 255)
                );
            }
        }
    }
}