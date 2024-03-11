package data.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.SectorAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VayraInventoryStripper implements EveryFrameScript {

    public static final List<String> WEAPON_IDS = new ArrayList<>(Arrays.asList(
            "vayra_lr_mining_laser",
            "vayra_mining_lance",
            "vayra_looted_tpc"));

    public VayraInventoryStripper() {
    }

    @Override
    public void advance(float amount) {
        SectorAPI sector = Global.getSector();
        if (!sector.isPaused()) {
            // should help with script weight if we only do shit while paused, since most stuff only does shit when unpaused
            return;
        }

        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        if (playerFleet != null) {
            List<CargoAPI> cargos = new ArrayList<>();
            if (sector.getPlayerFleet().getCargo() != null) {
                cargos.add(sector.getPlayerFleet().getCargo());
            }
            if (sector.getPlayerFleet().getInteractionTarget() != null && sector.getPlayerFleet().getInteractionTarget().getCargo() != null) {
                cargos.add(sector.getPlayerFleet().getInteractionTarget().getCargo());
            }

            for (String weaponId : WEAPON_IDS) {
                for (CargoAPI c : cargos) {
                    while (c.getNumWeapons(weaponId) > 0) {
                        c.removeWeapons(weaponId, 1);
                    }
                }
            }
        }

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }
}
