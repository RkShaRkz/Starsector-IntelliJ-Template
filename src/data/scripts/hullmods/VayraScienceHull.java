package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class VayraScienceHull extends BaseHullMod {

    private static final float DAMAGE_MULT = 0.9f;
    private static final float SENSOR_MULT = 0.1f;
    private static final float ENERGY_DAMAGE = 300f;
    private static final float EMP_DAMAGE = 900f;
    private static final float RANGE = 666f;
    private static final int COSMETIC_ARCS = 10;

    private final ArrayList<ShipAPI> exploded = new ArrayList<>();

    @Override
    public void init(HullModSpecAPI spec) {
        this.spec = spec;
        exploded.clear();
    }

    // handles applying stat bonuses
    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getArmorDamageTakenMult().modifyMult(id, DAMAGE_MULT);
        stats.getHullDamageTakenMult().modifyMult(id, DAMAGE_MULT);
        stats.getSensorProfile().modifyMult(id, SENSOR_MULT);
        exploded.clear();
    }

    // handles exploding
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if (ship == null || engine == null || engine.isPaused()) {
            return;
        }

        if (!ship.isAlive() && !exploded.contains(ship)) {
            exploded.add(ship);
            List<CombatEntityAPI> targets = CombatUtils.getEntitiesWithinRange(ship.getLocation(), RANGE);
            engine.spawnExplosion(ship.getLocation(), ship.getVelocity(), Color.BLUE.brighter().brighter(), RANGE / 2f, 0.666f);
            for (int i = 0; i < COSMETIC_ARCS; i++) {
                targets.add(null);
            }
            for (CombatEntityAPI target : targets) {
                if (target == null) {
                    target = new SimpleEntity(MathUtils.getRandomPointInCircle(ship.getLocation(), RANGE));
                }
                engine.spawnEmpArc(
                        ship,
                        ship.getLocation(),
                        ship,
                        target,
                        DamageType.ENERGY,
                        ENERGY_DAMAGE,
                        EMP_DAMAGE,
                        9999f,
                        "tachyon_lance_emp_impact",
                        25f,
                        Color.BLUE.brighter().brighter(),
                        Color.WHITE);
            }
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return (int) ((1f - DAMAGE_MULT) * 100) + "%";
        }
        if (index == 1) {
            return (int) ((1f - SENSOR_MULT) * 100) + "%";
        }
        if (index == 2) {
            return "detonate spectacularly in an EMP burst";
        }
        return null;
    }

}
