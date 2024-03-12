package shark.utilityconsole.data.console.commands.rkz;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import com.fs.starfarer.api.mission.FleetSide;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.Console;

public class RefillCR implements BaseCommand {
    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        CommandResult retVal;

        // IF args are empty, refill all ships CR
        // IF args are nonempty, refill just that ship's CR
        // IF args is == "list" then just list the names/ids/hull ids of all fleet ships
        if (args.isEmpty()) {
            // Iterate through all ships on the combat map and refill their CR
            retVal = refillAllShipsCR(context);
        } else if (args.equalsIgnoreCase("list")) {
            retVal = listShipNames();
        } else {
            retVal = refillShipsCR(args, context);
        }

        return retVal;
    }

    private CommandResult refillAllShipsCR(CommandContext context) {
        if (context.isInCombat()) {
            for (ShipAPI ship : Global.getCombatEngine().getShips()) {
                if (ship.getOwner() == FleetSide.PLAYER.ordinal()) {
                    FleetMemberAPI fleetMember = Global.getCombatEngine().getFleetManager(ship.getOwner()).getDeployedFleetMember(ship).getMember();
                    RepairTrackerAPI shipsRepairAPI = fleetMember.getRepairTracker();
                    shipsRepairAPI.setCR(Math.max(shipsRepairAPI.getCR(), shipsRepairAPI.getMaxCR()));
                    ship.setCurrentCR(Math.max(shipsRepairAPI.getCR(), shipsRepairAPI.getMaxCR()));
                }
            }
        } else {
            CampaignFleetAPI fleetAPI = Global.getSector().getPlayerFleet();
            for (FleetMemberAPI fleetMemberAPI : fleetAPI.getMembersWithFightersCopy()) {
                RepairTrackerAPI shipsRepairAPI = fleetMemberAPI.getRepairTracker();
                shipsRepairAPI.setCR(Math.max(shipsRepairAPI.getCR(), shipsRepairAPI.getMaxCR()));
            }
        }

        return CommandResult.SUCCESS;
    }

    private CommandResult refillShipsCR(String args, CommandContext context) {
        CommandResult retVal;
        FleetMemberAPI shipToRefill = null;

        // We will use these to give console feedback to the user which ship was refilled.
        boolean usedName = false, usedId = false, usedHullId = false;

        CampaignFleetAPI fleetAPI = Global.getSector().getPlayerFleet();
        // Figure out whether args is
        // - a name
        // - an ID
        // - a hull ID
        // then find the ship that matches it, and refill it.
        //
        // if a ship was found, return success. otherwise return failure.
        for (FleetMemberAPI fleetMember : fleetAPI.getMembersWithFightersCopy()) {
            // for each fleet member, check for name, then ID, then hull ID in that order.
            // if it matches, break and refill that ship. if it doesn't match and we didn't
            // find a suitable ship, return failure.

            if (fleetMember.getShipName().equalsIgnoreCase(args)) {
                usedName = true;
                shipToRefill = fleetMember;
                break;
            }

            if (fleetMember.getId().equalsIgnoreCase(args)) {
                usedId = true;
                shipToRefill = fleetMember;
                break;
            }

            if (fleetMember.getHullId().equalsIgnoreCase(args)) {
                usedHullId = true;
                shipToRefill = fleetMember;
                break;
            }
        }

        // After looping, if ship is non-null, refill and print a message to the console
        // otherwise just fail and say there was no such ship
        if (shipToRefill == null) {
            Console.showMessage("Could not find ship with Name, ID or Hull ID of " + args);
            retVal = CommandResult.ERROR;
        } else {
            if (context.isInCombat()) {

                // Get the CombatEngineAPI reference
                CombatEngineAPI engine = Global.getCombatEngine();

                // Get the ShipAPI reference from the CombatFleetManagerAPI
                ShipAPI shipInstance = engine.getFleetManager(FleetSide.PLAYER).getShipFor(shipToRefill);
                if (shipInstance.getOwner() == FleetSide.PLAYER.ordinal()) {
                    FleetMemberAPI fleetMember = Global.getCombatEngine().getFleetManager(shipInstance.getOwner()).getDeployedFleetMember(shipInstance).getMember();
                    RepairTrackerAPI shipsRepairAPI = fleetMember.getRepairTracker();
                    shipsRepairAPI.setCR(Math.max(shipsRepairAPI.getCR(), shipsRepairAPI.getMaxCR()));
                    shipInstance.setCurrentCR(Math.max(shipsRepairAPI.getCR(), shipsRepairAPI.getMaxCR()));
                }
            } else {
                RepairTrackerAPI shipsRepairAPI = shipToRefill.getRepairTracker();
                shipsRepairAPI.setCR(shipsRepairAPI.getMaxCR());
            }

            retVal = CommandResult.SUCCESS;

            StringBuilder sb = new StringBuilder();
            if (usedName) {
                sb.append("Refilled CR of ship with name: ").append(args);
            } else if (usedId) {
                sb.append("Refilled CR of ship with ID: ").append(args);
            } else if (usedHullId) {
                sb.append("Refilled CR of ship with Hull ID: ").append(args);
            } else {
                // Actually, lets not throw any exceptions, and return bad syntax in this case.
                retVal = CommandResult.BAD_SYNTAX;
            }

            Console.showMessage(sb.toString());
        }

        return retVal;
    }

    private CommandResult listShipNames() {
        StringBuilder sb = new StringBuilder();
        CampaignFleetAPI fleetAPI = Global.getSector().getPlayerFleet();
        for (FleetMemberAPI fleetMember : fleetAPI.getMembersWithFightersCopy()) {
            sb
                    .append(fleetMember.getShipName())
                    .append("\t\tID: ").append(fleetMember.getId())
                    .append("\t\tHull ID: ").append(fleetMember.getHullId())
                    .append("\n");
        }
        Console.showMessage(sb.toString());

        return CommandResult.SUCCESS;
    }
}
