package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_rimward extends BaseHullMod {

    private static final Map<HullSize, Float> MAG = new HashMap<>();

    static {
        MAG.put(HullSize.FRIGATE, 10f);
        MAG.put(HullSize.DESTROYER, 25f);
        MAG.put(HullSize.CRUISER, 50f);
        MAG.put(HullSize.CAPITAL_SHIP, 100f);
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getCargoMod().modifyFlat(id, MAG.get(hullSize));
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
            return "" + (MAG.get(HullSize.FRIGATE)).intValue();
        }
        if (index == 1) {
            return "" + (MAG.get(HullSize.DESTROYER)).intValue();
        }
        if (index == 2) {
            return "" + (MAG.get(HullSize.CRUISER)).intValue();
        }
        if (index == 3) {
            return "" + (MAG.get(HullSize.CAPITAL_SHIP)).intValue();
        }
        return null;
    }
}
