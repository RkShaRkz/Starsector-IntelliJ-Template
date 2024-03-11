package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

public class VayraKazeron extends BaseHullMod {

    private static final float ARMOR_BONUS = 100f;
    private static final float WEAPON_ENGINE_HEALTH_BONUS = 100f;
    private static final float OVERLOAD_TIME_PERCENT = -25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        stats.getArmorBonus().modifyFlat(id, ARMOR_BONUS);
        stats.getWeaponHealthBonus().modifyFlat(id, WEAPON_ENGINE_HEALTH_BONUS);
        stats.getEngineHealthBonus().modifyFlat(id, WEAPON_ENGINE_HEALTH_BONUS);
        stats.getOverloadTimeMod().modifyPercent(id, OVERLOAD_TIME_PERCENT);

    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return Misc.getRoundedValue(ARMOR_BONUS);
        }
        if (index == 1) {
            return Misc.getRoundedValue(WEAPON_ENGINE_HEALTH_BONUS);
        }
        if (index == 2) {
            return "" + (int) (-OVERLOAD_TIME_PERCENT) + "%";
        }
        return null;
    }

}
