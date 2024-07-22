package data.scripts.campaign.vayra_rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import exerelin.utilities.NexConfig;
import exerelin.utilities.NexFactionConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class vayra_k7 extends BaseCommandPlugin {

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, Map<String, MemoryAPI> memoryMap) {
        if (dialog == null) {
            return false;
        }
        if (Global.getSettings().getMissionScore("vayra_k007") > 0 && Global.getSettings().getModManager().isModEnabled("nexerelin")) {
            if (NexConfig.getFactionConfig("kadur_remnant").getStartFleetSet(NexFactionConfig.StartFleetType.GRAND_FLEET.name()) != null) {
                for (int i = 1; i <= NexConfig.getFactionConfig("kadur_remnant").getStartFleetSet(NexFactionConfig.StartFleetType.GRAND_FLEET.name()).getNumFleets(); i++) {
                    if ("vayra_caliph_revenant".equals(NexConfig.getFactionConfig("kadur_remnant").getStartFleetSet(NexFactionConfig.StartFleetType.GRAND_FLEET.name()).getFleet(i).get(0))) {
                        break;
                    }
                    if ((i) == NexConfig.getFactionConfig("kadur_remnant").getStartFleetSet(NexFactionConfig.StartFleetType.GRAND_FLEET.name()).getNumFleets()) {
                        List<String> caliphFleet = new ArrayList<>(1);
                        caliphFleet.add("vayra_caliph_revenant");
                        NexConfig.getFactionConfig("kadur_remnant").getStartFleetSet(NexFactionConfig.StartFleetType.GRAND_FLEET.name()).addFleet(caliphFleet);
                        break;
                    }
                }
            }
        }
        return true;
    }
}
