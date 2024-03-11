package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.util.Misc;

public class vayra_pirate_xiv extends BaseHullMod {

    private static final float ARMOR_BONUS = 130f;
    private static final float FLUX_HANDLING_MULT = 1.15f;
    private static final int BURN_BONUS = 1;

    /**
     *
     * @param hullSize
     * @param stats
     * @param id
     */
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

        // better armour
        stats.getArmorBonus().modifyFlat(id, (Float) ARMOR_BONUS);

        // better flux stats
        stats.getFluxCapacity().modifyMult(id, FLUX_HANDLING_MULT);
        stats.getFluxDissipation().modifyMult(id, FLUX_HANDLING_MULT);

        // better speed and handling
        stats.getMaxSpeed().modifyMult(id, FLUX_HANDLING_MULT);
        stats.getAcceleration().modifyMult(id, FLUX_HANDLING_MULT);
        stats.getDeceleration().modifyMult(id, FLUX_HANDLING_MULT);
        stats.getTurnAcceleration().modifyMult(id, FLUX_HANDLING_MULT);
        stats.getMaxTurnRate().modifyMult(id, FLUX_HANDLING_MULT);
        
        // +1 burn speed
        stats.getMaxBurnLevel().modifyFlat(id, BURN_BONUS);
    }

    /**
     *
     * @param index
     * @param hullSize
     * @return
     */
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return Misc.getRoundedValue(ARMOR_BONUS);
        }
        if (index == 1) {
            return "" + (int) ((FLUX_HANDLING_MULT - 1f) * 101f);
        }
        if (index == 2) {
            return "" + (int) ((FLUX_HANDLING_MULT - 1f) * 101f);
        }
        return null;
    }

}
