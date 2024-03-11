package data.scripts.hullmods;


import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class vayra_overtaxed_targeting extends BaseHullMod {

	private static final float RANGE_MULT = 0.9f;
	
    /**
     *
     * @param index
     * @param hullSize
     * @return
     */
    @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return (int) ((1f - RANGE_MULT) * 100) + "%";
		return null;
	}
	
    /**
     *
     * @param hullSize
     * @param stats
     * @param id
     */
    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().modifyMult(id, RANGE_MULT);
		stats.getEnergyWeaponRangeBonus().modifyMult(id, RANGE_MULT);
	}
	
}
