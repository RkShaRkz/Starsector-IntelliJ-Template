package data.scripts.campaign.econ;

import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MutableCommodityQuantity;
import com.fs.starfarer.api.impl.campaign.econ.BaseHazardCondition;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class vayra_virus_bomb extends BaseHazardCondition {
    //Sleepy parasitic spores... from Tartiflette
    private final int PRODUCTION_MALUS=-1; 
    
    @Override
    public void apply(String id) {
        super.apply(id);
        for(Industry i : market.getIndustries()){
            for(MutableCommodityQuantity c : i.getAllSupply()){
                i.getSupply(c.getCommodityId()).getQuantity().modifyFlat(id, PRODUCTION_MALUS, "Virus Bombarded");
            }
        }
    }
    
    @Override
    protected void createTooltipAfterDescription(TooltipMakerAPI tooltip, boolean expanded) {
        super.createTooltipAfterDescription(tooltip, expanded);
        tooltip.addPara(
                "%s production from all industries.",
                10f, 
                Misc.getHighlightColor(),
                ""+PRODUCTION_MALUS
        );
    }
}
