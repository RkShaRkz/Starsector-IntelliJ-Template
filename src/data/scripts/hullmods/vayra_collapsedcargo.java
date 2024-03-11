package data.scripts.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vayra_collapsedcargo extends BaseHullMod {

    public static final float CARGO_ARMOR = 1f;                 // 100% of cargo converted to armor
    public static final float CARGO_MULT = 0.1f;		// 10% cargo space
    public static final float PPT_MULT = 0.75f;                 // 25% less peak time
    
    public static final Map<HullSize, Float> CAP_MAP = new HashMap<>();
    static {
        CAP_MAP.put(HullSize.FRIGATE, 100f);
        CAP_MAP.put(HullSize.DESTROYER, 300f);
        CAP_MAP.put(HullSize.CRUISER, 900f);
        CAP_MAP.put(HullSize.CAPITAL_SHIP, 2000f);
    }

    private final Map<String, Float> STORAGE_MAP = new HashMap<>();

    /**
     *
     * @param hullSize
     * @param stats
     * @param id
     */
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        if (STORAGE_MAP.get(id) != null) {

            float MODIFIER = Math.min(STORAGE_MAP.get(id),CAP_MAP.get(hullSize));

            stats.getCargoMod().modifyMult(id, CARGO_MULT);
            stats.getArmorBonus().modifyFlat(id, (MODIFIER));
            stats.getPeakCRDuration().modifyMult(id, PPT_MULT);
        }

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

        float CARGO = ship.getHullSpec().getCargo();
        STORAGE_MAP.put(id, CARGO);

        // If we're civ-grade and this isn't the free version, remove it and give us the freebie
        if (ship.getVariant().hasHullMod("vayra_collapsedcargo") & ship.getVariant().hasHullMod("civgrade")) {
            ship.getVariant().removeMod("vayra_collapsedcargo");
            ship.getVariant().addMod("vayra_collapsedcargo_free");
        }

        // If we're NOT civ-grade and this IS the free version... How did you do that? Don't do that.
        if (ship.getVariant().hasHullMod("vayra_collapsedcargo_free") & !ship.getVariant().hasHullMod("civgrade")) {
            ship.getVariant().removeMod("vayra_collapsedcargo_free");
        }

    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {

        if (index == 0) {
            return "a number of points equal to its base cargo capacity";
        }
        if (index == 1) {
            return "" + CAP_MAP.get(hullSize);
        }
        if (index == 2) {
            return "" + Math.round((1f - CARGO_MULT) * 100f);
        }
        if (index == 3) {
            return "" + Math.round((1f - PPT_MULT) * 100f);
        }
        if (index == 4) {
            return "0";
        }
        return null;

    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return !ship.getVariant().hasHullMod("expanded_cargo_holds") && !ship.getVariant().hasHullMod("converted_bay") && !ship.getVariant().hasHullMod("vayra_extremely_converted_hangar") ;
    }

    @Override
    public String getUnapplicableReason(ShipAPI ship) {

        if (ship.getVariant().getHullMods().contains("expanded_cargo_holds")) {
            return "Incompatible with Expanded Cargo Holds";
        }
        
        if (ship.getVariant().getHullMods().contains("converted_bay")) {
            return "Incompatible with Converted Cargo Bay";
        }

        return null;
    }

}
