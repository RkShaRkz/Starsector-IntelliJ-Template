package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class VayraSlowAutoforge extends BaseHullMod {

    public static String MR_DATA_KEY = "vayra_slow_autoforge_data_key";

    public static final float RELOAD_TIME = 25f;

    public static class PeriodicMissileReloadData {

        IntervalUtil interval = new IntervalUtil(RELOAD_TIME, RELOAD_TIME);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return "Domain-era missile autoforge";
        }
        if (index == 1) {
            return "reloads the magazines";
        }
        if (index == 2) {
            return "one fifth";
        }
        if (index == 3) {
            return "" + (int) RELOAD_TIME;
        }
        return null;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        super.advanceInCombat(ship, amount);

        if (!ship.isAlive()) {
            return;
        }

        CombatEngineAPI engine = Global.getCombatEngine();

        String key = MR_DATA_KEY + "_" + ship.getId();
        PeriodicMissileReloadData data = (PeriodicMissileReloadData) engine.getCustomData().get(key);
        if (data == null) {
            data = new PeriodicMissileReloadData();
            engine.getCustomData().put(key, data);
        }

        boolean advance = false;
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.getType() != WeaponType.MISSILE) {
                continue;
            }

            if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
                advance = true;
            }
        }

        if (advance) {
            data.interval.advance(amount);
            if (data.interval.intervalElapsed()) {
                for (WeaponAPI w : ship.getAllWeapons()) {
                    if (w.getType() != WeaponType.MISSILE) {
                        continue;
                    }

                    if (w.usesAmmo() && w.getAmmo() < w.getMaxAmmo()) {
                        int reload = (int) Math.max(1f, (float) w.getMaxAmmo() / 5f);
                        w.setAmmo(w.getAmmo() + reload);
                    }
                }
            }
        } else {
            data.interval.setElapsed(0f);
        }
    }

}
