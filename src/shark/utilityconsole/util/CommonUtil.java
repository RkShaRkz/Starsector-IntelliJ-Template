package shark.utilityconsole.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import org.lazywizard.console.BaseCommand;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

public class CommonUtil {

    public static interface GetAllShipsListener {

        /**
         * Called when ships are fetched while we're in combat
         * @param combatShips all ships (currently in the combat)
         */
        public void onGetAllShipsCombat(List<ShipAPI> combatShips, List<FleetMemberAPI> fleetMembers);

        public void onGetAllShipsCampaign(List<FleetMemberAPI> campaignShips);
    }

    public static void getAllShips(BaseCommand.CommandContext context, GetAllShipsListener listener) {
        if (context.isInCombat()) {
            List<ShipAPI> combatShips = new ArrayList<ShipAPI>();
            List<FleetMemberAPI> combatFleetMembers = new ArrayList<FleetMemberAPI>();
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                FleetMemberAPI fleetMember = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship).getMember();
                combatShips.add(ship);
                combatFleetMembers.add(fleetMember);
            }
            listener.onGetAllShipsCombat(combatShips, combatFleetMembers);
        } else {
            CampaignFleetAPI fleetAPI = Global.getSector().getPlayerFleet();
            listener.onGetAllShipsCampaign(fleetAPI.getMembersWithFightersCopy());
        }
    }
}
