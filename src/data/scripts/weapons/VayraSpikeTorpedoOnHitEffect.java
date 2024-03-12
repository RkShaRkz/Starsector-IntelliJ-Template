package data.scripts.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

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