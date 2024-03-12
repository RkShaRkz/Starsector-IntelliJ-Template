package data.scripts.campaign;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.PluginPick;
import com.fs.starfarer.api.campaign.*;
import data.scripts.campaign.intel.VayraUniqueBountyInteractionDialogPlugin;
import org.apache.log4j.Logger;

// script originally by DarkRevenant, used under license - which at the time of this writing is:
// Code is free to copy, modify, and redistribute. Attribution must be made to the original creator, DarkRevenant.
public class VayraCampaignPlugin extends BaseCampaignPlugin {

    public static Logger log = Global.getLogger(VayraCampaignPlugin.class);
    //public static final String DYNASECTOR = "dynasector"; // don't fuck with dyna just in case it comes back

    @Override
    public String getId() {
        return "VayraCampaignPlugin";
    }

    @Override
    public PluginPick<InteractionDialogPlugin> pickInteractionDialogPlugin(SectorEntityToken interactionTarget) {
        if (interactionTarget instanceof CampaignFleetAPI
                && interactionTarget.getMemory().contains("$vayra_uniqueBounty")) {
            log.info("triggering VayraCampaignPlugin");
            return new PluginPick<InteractionDialogPlugin>(new VayraUniqueBountyInteractionDialogPlugin(), CampaignPlugin.PickPriority.HIGHEST);
        }
        return null;
    }
}
