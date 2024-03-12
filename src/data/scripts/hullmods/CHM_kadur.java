package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class CHM_kadur extends BaseHullMod {
        public static final float DAMAGE_REDUCTION = 10f;	
        public static final float HULL_INTEGRITY = 30f;
        
    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
                if (ship.getVariant().hasHullMod("CHM_commission")) {
                    ship.getVariant().removeMod("CHM_commission");
                }   
    }	
    @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
                if (index == 0) return "-" + (int) DAMAGE_REDUCTION + "%";
                if (index == 1) return "" + (int) HULL_INTEGRITY + "%";
		return null;
	}
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive()) return;
		MutableShipStatsAPI stats = ship.getMutableStats();
			
		ShipAPI playerShip = Global.getCombatEngine().getPlayerShip();
		if (ship.getHitpoints() < (ship.getMaxHitpoints() * HULL_INTEGRITY * 0.01f)) {
			stats.getHullDamageTakenMult().modifyMult("CHM_kadur" , 1f - (DAMAGE_REDUCTION * 0.01f));
			if (ship == playerShip) Global.getCombatEngine().maintainStatusForPlayerShip("KR_FORKADUR", "graphics/icons/hullsys/vayra_forever_war_icon", "Remember Kadur!", "-" + (int) DAMAGE_REDUCTION + "% hull damage received", false);
			}
		else {
			stats.getHullDamageTakenMult().modifyMult("CHM_kadur" , 1f);
		}
	}
}
