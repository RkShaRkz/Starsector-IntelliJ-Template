package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_communist extends BaseHullMod {

    public static final float PEAK_BONUS = 60f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getPeakCRDuration().modifyFlat(id, PEAK_BONUS);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        if (ship.getVariant().hasHullMod("CHM_commission")) {
            ship.getVariant().removeMod("CHM_commission");
        }
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) PEAK_BONUS;
        }
        return null;
    }
}
