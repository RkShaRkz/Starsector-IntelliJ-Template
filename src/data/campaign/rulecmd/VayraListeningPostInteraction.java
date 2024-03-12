package data.campaign.rulecmd;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.VayraLoreObjectsFramework;
import data.scripts.campaign.VayraLoreObjectsFramework.LoreObjectData;
import data.scripts.campaign.intel.VayraListeningPostIntel;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.List;
import java.util.Map;

public class VayraListeningPostInteraction extends BaseCommandPlugin {

    public static Logger log = Global.getLogger(VayraListeningPostInteraction.class);

    @Override
    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Misc.Token> params, final Map<String, MemoryAPI> memoryMap) {

        if (dialog == null) {
            return false;
        }

        TextPanelAPI text = dialog.getTextPanel();
        Color t = Misc.getTextColor();
        Color h = Misc.getHighlightColor();
        Color b = Misc.getNegativeHighlightColor();

        SectorEntityToken entity = dialog.getInteractionTarget();
        LoreObjectData data = VayraLoreObjectsFramework.getInstance().getDataFor(entity);

        text.addPara(data.text);

        String dataKey = "$" + data.uniqueId;
        if (!Global.getSector().getMemory().contains(dataKey)) {
            Global.getSector().getMemoryWithoutUpdate().set(dataKey, true);
            VayraListeningPostIntel intel = new VayraListeningPostIntel(data);
            Global.getSector().getIntelManager().addIntel(intel);
            log.info("created intel for " + data.uniqueId);
        } else {
            log.info("already had intel for " + data.uniqueId + ", not creating one");
        }

        return true;
    }
}
