package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_stormhawk extends BaseHullMod {

    public static final float STRIKE_DAMAGE_BONUS = 20f;
    public static final float WING_RANGE_BONUS = 500f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getDamageToFighters().modifyPercent(id, STRIKE_DAMAGE_BONUS);
        //stats.getFighterWingRange().modifyFlat(id, WING_RANGE_BONUS);
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
            return "+" + (int) STRIKE_DAMAGE_BONUS + "%";
        }
        if (index == 1) {
            return "+" + (int) WING_RANGE_BONUS;
        }
        return null;
    }
}
