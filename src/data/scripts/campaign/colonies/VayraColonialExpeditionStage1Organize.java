package data.scripts.campaign.colonies;

import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.intel.raid.OrganizeStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.util.Misc;

public class VayraColonialExpeditionStage1Organize extends OrganizeStage {

    public VayraColonialExpeditionStage1Organize(RaidIntel raid, MarketAPI market, float durDays) {
        super(raid, market, durDays);
    }

    @Override
    public void advance(float amount) {
        float days = Misc.getDays(amount);

        elapsed += days;

        statusInterval.advance(days);
        if (statusInterval.intervalElapsed()) {
            updateStatus();
        }
    }

    protected String getForcesString() {
        return "The colonial expedition";
    }

    protected String getRaidString() {
        return "colonial expedition";
    }
}
