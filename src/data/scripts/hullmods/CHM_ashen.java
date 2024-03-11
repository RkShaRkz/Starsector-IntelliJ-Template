package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_ashen extends BaseHullMod {

    private static final float HIGH_EXP_DAMAGE_MULT = 0.85f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHighExplosiveShieldDamageTakenMult().modifyMult(id, HIGH_EXP_DAMAGE_MULT);
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
            return "" + (int) ((1f - HIGH_EXP_DAMAGE_MULT) * 100f) + "%";
        }
        return null;
    }
}
