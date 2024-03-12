package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.util.HashMap;
import java.util.Map;

public class vayra_merchant_marine extends BaseHullMod {

    private static final float MAINTENANCE_MULT = 0.9f;
    private static final float MANEUVER_PENALTY = 0.9f;
    private static final Map<HullSize, Float> ARMOR = new HashMap<>();

    static {
        ARMOR.put(HullSize.FIGHTER, 0f);
        ARMOR.put(HullSize.FRIGATE, 100f);
        ARMOR.put(HullSize.DESTROYER, 150f);
        ARMOR.put(HullSize.CRUISER, 200f);
        ARMOR.put(HullSize.CAPITAL_SHIP, 400f);
    }

    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        // better efficiency
        stats.getMinCrewMod().modifyMult(id, MAINTENANCE_MULT);
        stats.getSuppliesPerMonth().modifyMult(id, MAINTENANCE_MULT);
        stats.getFuelUseMod().modifyMult(id, MAINTENANCE_MULT);

        // worse maneuverability and combat speed
        stats.getAcceleration().modifyMult(id, MANEUVER_PENALTY);
        stats.getDeceleration().modifyMult(id, MANEUVER_PENALTY);
        stats.getTurnAcceleration().modifyMult(id, MANEUVER_PENALTY);
        stats.getMaxTurnRate().modifyMult(id, MANEUVER_PENALTY);
        stats.getMaxSpeed().modifyMult(id, MANEUVER_PENALTY);

        // better armor
        stats.getArmorBonus().modifyFlat(id, ARMOR.get(hullSize));
    }

    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) {
            return Math.round((1f - MAINTENANCE_MULT) * 100f) + "%";
        }
        if (index == 1) {
            return "" + ARMOR.get(hullSize);
        }
        if (index == 2) {
            return Math.round((1f - MANEUVER_PENALTY) * 100f) + "%";
        }
        return null;
    }

}
