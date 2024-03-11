package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class VayraWarhawkModular extends BaseHullMod {
    
    public static final float REFIT_TIME_BONUS = 0.2f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterRefitTimeMult().modifyMult(id, 1f-REFIT_TIME_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "universal";
        }
        if (index == 1) {
            return (int) (100f * REFIT_TIME_BONUS) + "%";
        }
        return null;
    }

}
