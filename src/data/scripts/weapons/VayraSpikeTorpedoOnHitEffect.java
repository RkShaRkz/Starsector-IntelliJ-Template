package data.scripts.weapons;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import java.util.ArrayList;
import java.util.List;
import org.lazywizard.lazylib.combat.CombatUtils;

public class VayraSpikeTorpedoOnHitEffect implements OnHitEffectPlugin {
    
    private static final Color COLOR = new Color(33, 103, 109, 150);
    private static final String CHAFF_ID = "vayra_kadur_chaff";
    private static final float CHAFF_RANGE = 500f;
    private static final float FORCE_MULT = 0.666f; // force applied = base damage amount * this

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        
        float force = projectile.getBaseDamageAmount() * FORCE_MULT;
        CombatUtils.applyForce(target, projectile.getVelocity(), force);

        if (target instanceof ShipAPI) {

            float emp = projectile.getEmpAmount();
            float dam = projectile.getEmpAmount();

            List<MissileAPI> chaff = new ArrayList<>();
            
            for (MissileAPI test : CombatUtils.getMissilesWithinRange(point, CHAFF_RANGE)) {
                if (test.isFlare() 
                        && !test.didDamage() 
                        && !test.isFading() 
                        && !test.isFizzling()
                        && !test.getEngineController().isFlamedOut()
                        && !test.getEngineController().isFlamingOut()
                        && CHAFF_ID.equals(test.getProjectileSpecId())) {
                    chaff.add(test);
                }
            }
            
            for (MissileAPI spend : chaff) {
                engine.spawnEmpArc(projectile.getSource(), spend.getLocation(), spend, target,
                        DamageType.ENERGY,
                        dam,
                        emp, // emp 
                        100000f, // max range 
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        COLOR,
                        COLOR.brighter()
                );
                spend.flameOut();
            }
        }
    }
}