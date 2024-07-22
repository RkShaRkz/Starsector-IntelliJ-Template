package data.scripts.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;

public class CHM_kadur extends BaseHullMod {
        public static final float DAMAGE_REDUCTION = 10f;	
        public static final float HULL_INTEGRITY = 50f;
        
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
			if (ship == playerShip) Global.getCombatEngine().maintainStatusForPlayerShip("KR_FORKADUR", "graphics/icons/hullsys/vayra_forever_war_icon.png", "Remember Kadur!", "-" + (int) DAMAGE_REDUCTION + "% hull damage received", false);
			}
		else {
			stats.getHullDamageTakenMult().unmodifyMult("CHM_kadur");
		}
	}
    
    @Override
    public void addPostDescriptionSection(final TooltipMakerAPI tooltip, final ShipAPI.HullSize hullSize, final ShipAPI ship, final float width, final boolean isForModSpec) {
        tooltip.addPara("%s", 6f, Misc.getGrayColor(), "Remember Kadur. Remember... And FIGHT FOR YOUR LIFE.").italicize();
    }

    @Override
    public Color getBorderColor() {
        return new Color(147, 102, 50, 0);
    }

    @Override
    public Color getNameColor() {
        return Global.getSettings().getDesignTypeColor("Kadur Theocracy") != null ? Global.getSettings().getDesignTypeColor("Kadur Theocracy") : new Color(255,213,133,255);
    }
}
