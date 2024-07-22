package data.scripts.campaign.econ;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ConditionData;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import java.util.Arrays;


public class KadurMajority extends BaseMarketConditionPlugin {

	private static final String [] kadurFactions = new String [] {
		"kadur_remnant",
	};

    /**
     *
     * @param id
     */
    @Override
	public void apply(String id) {
		if (Arrays.asList(kadurFactions).contains(market.getFactionId())) {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_BONUS, "Kadur fellowship");
		} else if (!Misc.getFactionMarkets("kadur_remnant").isEmpty()) {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_PENALTY, "Kadur insurgency");
		}

	}

    /**
     *
     * @param id
     */
    @Override
	public void unapply(String id) {
		market.getStability().unmodify(id);
		
	}
        
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        if (Arrays.asList(kadurFactions).contains(market.getFactionId())) {
            tooltip.addPara(
                "%s stability",
                10f, 
                Misc.getHighlightColor(),
                "+"+Misc.getRoundedValue(ConditionData.STABILITY_LUDDIC_MAJORITY_BONUS)
        );
	} else if (!Misc.getFactionMarkets(Global.getSector().getFaction("kadur_remnant"), null).isEmpty()) {
            tooltip.addPara(
                "%s stability",
                10f, 
                Misc.getHighlightColor(),
                ""+Misc.getRoundedValue(ConditionData.STABILITY_LUDDIC_MAJORITY_PENALTY)
        );
        } else {
            tooltip.addPara(
                "No stability change for Kadurans are no longer a significant presence in the Persean Sector.",
                10f, 
                Misc.getHighlightColor()
        );
        
        }
        
    }

}
