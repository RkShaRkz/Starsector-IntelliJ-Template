package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_mandate extends BaseHullMod {

    private static final Map<HullSize, Float> RANGE_BONUS = new HashMap<>();

    static {
        RANGE_BONUS.put(HullSize.FRIGATE, 10f);
        RANGE_BONUS.put(HullSize.DESTROYER, 10f);
        RANGE_BONUS.put(HullSize.CRUISER, 5f);
        RANGE_BONUS.put(HullSize.CAPITAL_SHIP, 5f);
    }
    private static final Map<HullSize, Float> ARMOR_PENALTY = new HashMap<>();

    static {
        ARMOR_PENALTY.put(HullSize.FRIGATE, -75f);
        ARMOR_PENALTY.put(HullSize.DESTROYER, -100f);
        ARMOR_PENALTY.put(HullSize.CRUISER, -125f);
        ARMOR_PENALTY.put(HullSize.CAPITAL_SHIP, -150f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, RANGE_BONUS.get(hullSize));
        stats.getEnergyWeaponRangeBonus().modifyPercent(id, RANGE_BONUS.get(hullSize));
        stats.getArmorBonus().modifyFlat(id, ARMOR_PENALTY.get(hullSize));
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
            return "" + (RANGE_BONUS.get(HullSize.FRIGATE)).intValue();
        }
        if (index == 1) {
            return "" + (RANGE_BONUS.get(HullSize.DESTROYER)).intValue();
        }
        if (index == 2) {
            return "" + (RANGE_BONUS.get(HullSize.CRUISER)).intValue();
        }
        if (index == 3) {
            return "" + (RANGE_BONUS.get(HullSize.CAPITAL_SHIP)).intValue();
        }
        if (index == 4) {
            return "" + (ARMOR_PENALTY.get(HullSize.FRIGATE)).intValue();
        }
        if (index == 5) {
            return "" + (ARMOR_PENALTY.get(HullSize.DESTROYER)).intValue();
        }
        if (index == 6) {
            return "" + (ARMOR_PENALTY.get(HullSize.CRUISER)).intValue();
        }
        if (index == 7) {
            return "" + (ARMOR_PENALTY.get(HullSize.CAPITAL_SHIP)).intValue();
        }
        return null;
    }
}
