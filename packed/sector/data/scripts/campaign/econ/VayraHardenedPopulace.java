package data.scripts.campaign.econ;

import com.fs.starfarer.api.impl.campaign.econ.BaseMarketConditionPlugin;

public class VayraHardenedPopulace extends BaseMarketConditionPlugin {

    /**
     *
     * @param id
     */
    @Override
    public void apply(String id) {
        market.getStability().modifyFlat(id, 3, this.getName());
    }

    /**
     *
     * @param id
     */
    @Override
    public void unapply(String id) {
    }
}
