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
import com.fs.starfarer.api.util.WeightedRandomPicker;
import java.util.List;
import org.lazywizard.lazylib.combat.CombatUtils;

public class VayraShockwebOnHitEffect implements OnHitEffectPlugin {

    private static final String EMP_SOUND = "tachyon_lance_emp_impact"; // standard EMP arc sound for everything
    private static final float ARC_RANGE = 300f;
    private static final Color ARC_CORE = new Color(235, 235, 255, 220);
    private static final Color ARC_FRINGE = new Color(110, 170, 255, 175);

    private static final float FLECHETTE_ARC_CHANCE = 0.5f; // fractional chance for arc on flechette hit

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {

        if (!shieldHit && target instanceof ShipAPI) {

            float emp = projectile.getEmpAmount();
            float dam = 0;

            if (CombatUtils.getEntitiesWithinRange(point, ARC_RANGE).size() > 0) {

                List<CombatEntityAPI> potentialTargets = CombatUtils.getEntitiesWithinRange(point, ARC_RANGE);
                WeightedRandomPicker<ShipAPI> targets = new WeightedRandomPicker<>();
                ShipAPI source = projectile.getSource();

                for (CombatEntityAPI p : potentialTargets) {
                    if (p.getOwner() != source.getOwner() && p instanceof ShipAPI) {
                        targets.add((ShipAPI) p);
                    }
                }

                String projId = projectile.getProjectileSpecId();

                switch (projId) {
                    case "vayra_shockweb_canister":
                        for (CombatEntityAPI getEm : targets.getItems()) {
                            if (getEm == null) {
                                continue;
                            }
                            engine.spawnEmpArc(
                                    source,
                                    point,
                                    getEm,
                                    getEm,
                                    DamageType.ENERGY,
                                    dam,
                                    emp,
                                    9999f,
                                    EMP_SOUND,
                                    Math.max(emp / 20f, 5f),
                                    ARC_FRINGE,
                                    ARC_CORE);
                        }
                        break;
                    case "vayra_shockweb_flechette":
                        if (Math.random() > FLECHETTE_ARC_CHANCE) {
                            break;
                        }
                        if (!targets.isEmpty()) {
                            CombatEntityAPI getEm = targets.pick();
                            if (getEm == null) {
                                break;
                            }
                            engine.spawnEmpArc(
                                    source,
                                    point,
                                    getEm,
                                    getEm,
                                    DamageType.ENERGY,
                                    dam,
                                    emp,
                                    9999f,
                                    EMP_SOUND,
                                    Math.max(emp / 20f, 5f),
                                    ARC_FRINGE,
                                    ARC_CORE);
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
