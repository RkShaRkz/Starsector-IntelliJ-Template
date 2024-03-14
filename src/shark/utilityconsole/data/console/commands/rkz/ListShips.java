package shark.utilityconsole.data.console.commands.rkz;

import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;
import shark.utilityconsole.util.CommonUtil;

import java.util.List;

public class ListShips implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        // First, break out the args to see what we have here.
        // the syntax should be `listships` and the output should be a list of owned ships with their ID and Names.
        StringBuilder sb = new StringBuilder();
        CommonUtil.getAllPlayerShips(context, new CommonUtil.GetAllShipsListener() {
            @Override
            public void onGetAllShipsCombat(List<ShipAPI> combatShips, List<FleetMemberAPI> fleetMembers) {
                for (FleetMemberAPI ship : fleetMembers) {
                    Console.showMessage(processFleetMember(ship));
                }
            }

            @Override
            public void onGetAllShipsCampaign(List<FleetMemberAPI> campaignShips) {
                for (FleetMemberAPI ship : campaignShips) {
                    Console.showMessage(processFleetMember(ship));
                }
            }
        });

        /*
        if (context.isInCombat()) {
            List<ShipAPI> ships = Global.getCombatEngine().getShips();
            for (ShipAPI ship : ships) {
                if (ship.getOwner() == FleetSide.PLAYER.ordinal()) {
                    // cast to FleetMemberAPI

                }
            }
        } else {
            List<FleetMemberAPI> shipList =
                    Global
                            .getSector()
                            .getPlayerFleet()
                            .getFleetData()
                            .getFleet()
                            .getMembersWithFightersCopy();

            StringBuilder sb = new StringBuilder();
            for (FleetMemberAPI ship : shipList) {
                sb
                        .append("ID: ").append(ship.getId())
                        .append("\t\tHull ID: ").append(ship.getHullId())
                        .append("\t\tName: ").append(ship.getShipName())
                        .append("\n");
            }

            Console.showMessage(sb.toString());
        }
         */

        return CommandResult.SUCCESS;
    }

    private String processFleetMember(FleetMemberAPI ship) {
        StringBuilder retVal = new StringBuilder()
                .append("ID: ").append(ship.getId())
                .append("\t\tHull ID: ").append(ship.getHullId())
                .append("\t\tName: ").append(ship.getShipName())
                .append("\n");

        return retVal.toString();
    }
}
