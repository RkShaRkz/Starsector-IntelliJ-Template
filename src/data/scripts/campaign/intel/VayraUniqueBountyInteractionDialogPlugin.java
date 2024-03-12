package data.scripts.campaign.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl;
import org.apache.log4j.Logger;

// script originally by DarkRevenant, used under license - which at the time of this writing is:
// Code is free to copy, modify, and redistribute. Attribution must be made to the original creator, DarkRevenant.

public class VayraUniqueBountyInteractionDialogPlugin extends FleetInteractionDialogPluginImpl {

    public static Logger log = Global.getLogger(VayraUniqueBountyInteractionDialogPlugin.class);

    public VayraUniqueBountyInteractionDialogPlugin() {
        this(null);
    }

    public VayraUniqueBountyInteractionDialogPlugin(FleetInteractionDialogPluginImpl.FIDConfig params) {
        super(params);
        log.info("triggering VayraUniqueBountyInteractionDialogPlugin");
        context = new VayraUniqueBountyFleetEncounterContext();
    }
}
