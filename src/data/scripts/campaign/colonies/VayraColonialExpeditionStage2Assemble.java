package data.scripts.campaign.colonies;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel.RaidStageStatus;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Logger;

import java.awt.*;

public class VayraColonialExpeditionStage2Assemble extends AssembleStage {

    public static Logger log = Global.getLogger(VayraColonialExpeditionStage2Assemble.class);

    public VayraColonialExpeditionStage2Assemble(RaidIntel raid, SectorEntityToken gatheringPoint) {
        super(raid, gatheringPoint);
    }

    @Override
    protected void updateStatus() {
        VayraColonialExpeditionIntel raidIntel = (VayraColonialExpeditionIntel) intel;
        String from = raidIntel.getFrom().getFactionId();
        String colonyFaction = raidIntel.getFaction().getId();
        String parent = VayraColonialManager.getInstance().colonialParents.get(colonyFaction);
        boolean incorrectParent = (!from.equals(colonyFaction) && !from.equals(parent));
        if (incorrectParent) {
            raidIntel.setOutcome(VayraColonialExpeditionIntel.KadurColonialExpeditionOutcome.EXPEDITION_DESTROYED);
            status = RaidStageStatus.FAILURE;
            giveReturnOrdersToStragglers(getRoutes());
            raidIntel.sendOutcomeUpdate();
            log.info(String.format("oh GOD DAMN it this is the problem isn't it. raidIntel.getFrom.getFactionId = %s, raidIntel.getFaction.getId = %s", raidIntel.getFrom().getFactionId(), raidIntel.getFaction().getId()));
        }
        super.updateStatus();
    }

    @Override
    public void showStageInfo(TooltipMakerAPI info) {
        int curr = intel.getCurrentStage();
        int index = intel.getStageIndex(this);

        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color tc = Misc.getTextColor();
        float pad = 3f;
        float opad = 10f;

        if (status == RaidStageStatus.FAILURE) {
            info.addPara("The colonial expedition has failed to successfully assemble at the rendezvous point. The colonization effort has been cancelled.", opad);
        } else if (curr == index) {
            if (isSourceKnown()) {
                info.addPara("The colonial expedition is currently assembling in the " + gatheringPoint.getContainingLocation().getNameWithLowercaseType() + ".", opad);
            } else {
                info.addPara("The colonial expedition is currently assembling at an unknown location.", opad);
            }
        }
    }

    @Override
    protected String pickNextType() {
        return FleetTypes.MERC_ARMADA;
    }

    @Override
    protected float getFP(String type) {
        float base = 100f;
        if (spawnFP < base * 1.5f) {
            base = spawnFP;
        }
        if (base > spawnFP) {
            base = spawnFP;
        }

        spawnFP -= base;
        return base;
    }
}
