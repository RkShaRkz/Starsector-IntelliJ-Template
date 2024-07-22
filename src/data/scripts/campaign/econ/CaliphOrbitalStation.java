package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.listeners.FleetEventListener;
import com.fs.starfarer.api.impl.campaign.econ.impl.OrbitalStation;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.MarketCMD.RaidDangerLevel;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;



public class CaliphOrbitalStation extends OrbitalStation implements FleetEventListener {
        @Override
        public boolean showWhenUnavailable() {
		return false;
	}
        @Override
        public boolean isAvailableToBuild() {
		return false;
	}
        @Override
        public String getUnavailableReason() {
		return "Can not be built";
	}
        @Override
        public String getNameForModifier() {
		return getCurrentName();
	}
        @Override
        public boolean canInstallAICores() {
		return false;
	}
        @Override
	public boolean showShutDown() {
		return false;
	}
        @Override
	public boolean canShutDown() {
		return false;
	}
	
	@Override
	public RaidDangerLevel adjustCommodityDangerLevel(String commodityId, RaidDangerLevel level) {
		return level.next();
	}

	@Override
	public RaidDangerLevel adjustItemDangerLevel(String itemId, String data, RaidDangerLevel level) {
		return level.next();
	}
        
        @Override
	protected void addPostDemandSection(TooltipMakerAPI tooltip, boolean hasDemand, IndustryTooltipMode mode) {
		//if (mode == IndustryTooltipMode.NORMAL && isFunctional()) {
		if (mode != IndustryTooltipMode.NORMAL || isFunctional()) {
			Color h = Misc.getHighlightColor();
			float opad = 10f;
			
			float cr = getCR();
			tooltip.addPara("Caliph combat readiness: %s", opad, h, "" + Math.round(cr * 100f) + "%");
			
			addStabilityPostDemandSection(tooltip, hasDemand, mode);
			
			boolean battlestation = getSpec().hasTag(Industries.TAG_BATTLESTATION);
			boolean starfortress = getSpec().hasTag(Industries.TAG_STARFORTRESS);
			float bonus = DEFENSE_BONUS_BASE;
			if (battlestation) bonus = DEFENSE_BONUS_BATTLESTATION;
			else if (starfortress) bonus = DEFENSE_BONUS_FORTRESS;
			addGroundDefensesImpactSection(tooltip, bonus, Commodities.SUPPLIES);
		}
	}

}





