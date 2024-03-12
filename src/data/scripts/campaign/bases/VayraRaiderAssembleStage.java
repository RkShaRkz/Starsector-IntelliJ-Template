package data.scripts.campaign.bases;

import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.impl.campaign.intel.raid.AssembleStage;
import com.fs.starfarer.api.impl.campaign.intel.raid.RaidIntel;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import static com.fs.starfarer.api.impl.campaign.fleets.FleetFactory.MercType.*;
import static data.scripts.campaign.bases.VayraRaiderBaseManager.*;

public class VayraRaiderAssembleStage extends AssembleStage {

    protected BaseIntelPlugin base;

    public VayraRaiderAssembleStage(RaidIntel raid, SectorEntityToken gatheringPoint, BaseIntelPlugin base) {
        super(raid, gatheringPoint);
        this.base = base;
    }

    @Override
    protected String pickNextType() {
        WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
        if (!SCOUT.fleetType.equals(prevType)) {
            picker.add(SCOUT.fleetType);
        }
        if (!BOUNTY_HUNTER.fleetType.equals(prevType) && spawnFP >= FP_BOUNTY_HUNTER) {
            picker.add(BOUNTY_HUNTER.fleetType);
        }
        if (!PRIVATEER.fleetType.equals(prevType) && spawnFP >= FP_PRIVATEER) {
            picker.add(PRIVATEER.fleetType);
        }
        if (!PATROL.fleetType.equals(prevType) && spawnFP >= FP_PATROL) {
            picker.add(PATROL.fleetType);
        }
        prevType = picker.pick();
        if (prevType == null) {
            prevType = FleetTypes.PATROL_SMALL;
        }
        return prevType;
    }

    @Override
    protected float getFP(String type) {
        float raidFPBase = FP_SCOUT;
        if (SCOUT.fleetType.equals(type)) {
            raidFPBase = FP_SCOUT;
        } else if (BOUNTY_HUNTER.fleetType.equals(type)) {
            raidFPBase = FP_BOUNTY_HUNTER;
        } else if (PRIVATEER.fleetType.equals(type)) {
            raidFPBase = FP_PRIVATEER;
        } else if (PATROL.fleetType.equals(type)) {
            raidFPBase = FP_PATROL;
        }
        raidFPBase *= (1f + ((float) Math.random() - 0.5f) * 0.5f);
        if (raidFPBase > spawnFP) {
            raidFPBase = spawnFP;
        }
        spawnFP -= raidFPBase;
        if (spawnFP < FP_SCOUT * 0.5f) {
            raidFPBase += spawnFP;
            spawnFP = 0f;
        }

        return raidFPBase;
    }

    @Override
    public boolean isSourceKnown() {
        return base.isPlayerVisible();
    }

}
