package data.scripts.console.commands;

import com.fs.starfarer.api.EveryFrameScript;
import data.scripts.campaign.intel.VayraUniqueBountyManager;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;

public class VayraForceHVB implements BaseCommand {

    @Override
    public CommandResult runCommand(String args, CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        VayraUniqueBountyManager manager = VayraUniqueBountyManager.getInstance();
        if (manager == null) {
            Console.showMessage("the VayraUniqueBountyManager instance is missing!");
            return CommandResult.ERROR;
        }

        Console.showMessage("attempting to spawn HVB with ID: " + args);
        EveryFrameScript bounty = manager.forceSpawn(args);
        if (bounty != null) {
            Console.showMessage("it worked!");
            Console.showMessage("unique currentBounties contains: " + manager.getCurrentBountiesList());
            return CommandResult.SUCCESS;
        } else {
            Console.showMessage("it didn't work!");
            return CommandResult.ERROR;
        }
    }
}
