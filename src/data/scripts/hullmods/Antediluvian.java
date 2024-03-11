package data.scripts.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class Antediluvian extends BaseHullMod {

	public static final float DEGRADE_INCREASE_PERCENT = 50f;
	
        @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getCRLossPerSecondPercent().modifyPercent(id, DEGRADE_INCREASE_PERCENT);
		stats.getSensorProfile().modifyMult(id, 0.1f);
	}
	
        @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "greatly reduce";
		if (index == 1) return "" + (int) DEGRADE_INCREASE_PERCENT + "%";
		return null;
	}

        @Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship != null && (ship.getHullSpec().getNoCRLossTime() < 10000 || ship.getHullSpec().getCRLossPerSecond() > 0); 
	}
}
