package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import java.util.HashMap;
import java.util.Map;

public class vayra_forever_war extends BaseHullMod {

    public static final String SECRET_BUFF_HULLMOD = "vayra_secret_buff_hullmod";

    // regular stuff
    private static final Map<HullSize, Float> HULL = new HashMap<>();

    static {
        HULL.put(HullSize.FIGHTER, 0f);
        HULL.put(HullSize.FRIGATE, 500f);
        HULL.put(HullSize.DESTROYER, 1000f);
        HULL.put(HullSize.CRUISER, 2000f);
        HULL.put(HullSize.CAPITAL_SHIP, 4000f);
    }
    private static final Map<HullSize, Float> ARMOR = new HashMap<>();

    static {
        ARMOR.put(HullSize.FIGHTER, 0f);
        ARMOR.put(HullSize.FRIGATE, 100f);
        ARMOR.put(HullSize.DESTROYER, 150f);
        ARMOR.put(HullSize.CRUISER, 200f);
        ARMOR.put(HullSize.CAPITAL_SHIP, 250f);
    }
    private static final float SPEED = 10f;
    private static final float MANEUVERABILITY = 0.5f;
    private static final Map<HullSize, Float> FUEL_CAP = new HashMap<>();

    static {
        FUEL_CAP.put(HullSize.FIGHTER, 0f);
        FUEL_CAP.put(HullSize.FRIGATE, 15f);
        FUEL_CAP.put(HullSize.DESTROYER, 30f);
        FUEL_CAP.put(HullSize.CRUISER, 60f);
        FUEL_CAP.put(HullSize.CAPITAL_SHIP, 150f);
    }
    private static final Map<HullSize, Float> FUEL_EFF = new HashMap<>();

    static {
        FUEL_EFF.put(HullSize.FIGHTER, 0f);
        FUEL_EFF.put(HullSize.FRIGATE, 0.5f);
        FUEL_EFF.put(HullSize.DESTROYER, 1f);
        FUEL_EFF.put(HullSize.CRUISER, 2f);
        FUEL_EFF.put(HullSize.CAPITAL_SHIP, 5f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + HULL.get(hullSize).intValue();
        }
        if (index == 1) {
            return "" + ARMOR.get(hullSize).intValue();
        }
        if (index == 2) {
            return "" + (int) SPEED;
        }
        if (index == 3) {
            return "" + (int) (MANEUVERABILITY * 100f) + "%";
        }
        if (index == 4) {
            return "" + FUEL_EFF.get(hullSize);
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getHullBonus().modifyFlat(id, HULL.get(hullSize));
        stats.getArmorBonus().modifyFlat(id, ARMOR.get(hullSize));
        stats.getMaxSpeed().modifyFlat(id, SPEED);
        stats.getDeceleration().modifyMult(id, MANEUVERABILITY);
        stats.getMaxTurnRate().modifyMult(id, MANEUVERABILITY);
        stats.getTurnAcceleration().modifyMult(id, MANEUVERABILITY);
        stats.getFuelMod().modifyFlat(id, FUEL_CAP.get(hullSize));
        stats.getFuelUseMod().modifyFlat(id, FUEL_EFF.get(hullSize));

        if (stats.getVariant() != null && !stats.getVariant().hasHullMod(SECRET_BUFF_HULLMOD)) {
            stats.getVariant().addPermaMod(SECRET_BUFF_HULLMOD);
        }
    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

}
