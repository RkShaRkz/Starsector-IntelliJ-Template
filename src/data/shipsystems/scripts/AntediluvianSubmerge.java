package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class AntediluvianSubmerge extends BaseShipSystemScript {
    
    public static final float SHIP_ALPHA_MULT = 0.333f;
    public static final float VULNERABLE_FRACTION = 0f;
	
    public static final float MAX_TIME_MULT = 2f;
	
    protected Object STATUSKEY2 = new Object();
    
    private void maintainStatus(ShipAPI playerShip, ShipSystemStatsScript.State state, float effectLevel) {
	float level = effectLevel;
	float f = VULNERABLE_FRACTION;
	
	ShipSystemAPI cloak = playerShip.getPhaseCloak();
	if (cloak == null) cloak = playerShip.getSystem();
	if (cloak == null) return;
	
	if (level > f) {
		Global.getCombatEngine().maintainStatusForPlayerShip(STATUSKEY2,
				cloak.getSpecAPI().getIconSpriteName(), cloak.getDisplayName(), "DIVE DIVE DIVE", false);
	} else {
	}
    }
    
    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel) {
        ShipAPI ship;
        boolean player;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
            player = ship == Global.getCombatEngine().getPlayerShip();
            id = id + "_" + ship.getId();
        } else {
            return;
        }
        
        if (player) {
            maintainStatus(ship, state, effectLevel);
        }
        
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        
        if (state == ShipSystemStatsScript.State.COOLDOWN || state == ShipSystemStatsScript.State.IDLE) {
            unapply(stats, id);
            return;
        }
        
        float level = effectLevel;
	
	float levelForAlpha = level;
        
        if (state == ShipSystemStatsScript.State.IN || state == ShipSystemStatsScript.State.ACTIVE || state == ShipSystemStatsScript.State.OUT) {
            ship.setPhased(true);
            levelForAlpha = level;
        }
        
        ship.setExtraAlphaMult(1f - (1f - SHIP_ALPHA_MULT) * levelForAlpha);
	ship.setApplyExtraAlphaToEngines(true);
	
	
	float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * levelForAlpha;
	stats.getTimeMult().modifyMult(id, shipTimeMult);
	if (player) {
		Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
	} else {
		Global.getCombatEngine().getTimeMult().unmodify(id);
	}
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        ShipAPI ship;
	if (stats.getEntity() instanceof ShipAPI) {
		ship = (ShipAPI) stats.getEntity();
	} else {
		return;
	}
        
        Global.getCombatEngine().getTimeMult().unmodify(id);
	stats.getTimeMult().unmodify(id);
	
	ship.setPhased(false);
	ship.setExtraAlphaMult(1f);
    }
}