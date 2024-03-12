package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;
import com.fs.starfarer.api.impl.campaign.econ.ConditionData;
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
		} else {
			market.getStability().modifyFlat(id, ConditionData.STABILITY_LUDDIC_MAJORITY_PENALTY * 3, "Kadur insurgency");
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

}
