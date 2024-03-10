package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class VayraWesternesse extends BaseHullMod {

    private static final float BALLISTIC_ROF_BONUS_PERCENT = 15f;
    private static final float BEAM_DAMAGE_BONUS_PERCENT = 15f;
    private static final float MANEUVERABILITY_BONUS_PERCENT = 50f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_ROF_BONUS_PERCENT);
        stats.getBeamWeaponDamageMult().modifyPercent(id, BEAM_DAMAGE_BONUS_PERCENT);
        
        stats.getAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS_PERCENT * 2f);
        stats.getDeceleration().modifyPercent(id, MANEUVERABILITY_BONUS_PERCENT);
        stats.getTurnAcceleration().modifyPercent(id, MANEUVERABILITY_BONUS_PERCENT * 2f);
        stats.getMaxTurnRate().modifyPercent(id, MANEUVERABILITY_BONUS_PERCENT);

    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) (BALLISTIC_ROF_BONUS_PERCENT) + "%";
        }
        if (index == 1) {
            return "" + (int) (BEAM_DAMAGE_BONUS_PERCENT) + "%";
        }
        if (index == 2) {
            return "" + (int) (MANEUVERABILITY_BONUS_PERCENT) + "%";
        }
        return null;
    }

}
