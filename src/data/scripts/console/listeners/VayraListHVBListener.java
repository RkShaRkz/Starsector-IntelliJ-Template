package data.scripts.console.listeners;

import data.scripts.campaign.intel.VayraUniqueBountyManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.BaseCommand.CommandResult;
import org.lazywizard.console.CommandListener;
import org.lazywizard.console.Console;

public class VayraListHVBListener implements CommandListener {

    @Override
    public boolean onPreExecute(@NotNull String command, @NotNull String args, @NotNull BaseCommand.CommandContext context, boolean alreadyIntercepted) {
        // Return true to tell the console that you want to take over from the base command
        boolean hvb = "hvb".equalsIgnoreCase(args)
                || "hvbs".equalsIgnoreCase(args)
                || "highvaluebounties".equalsIgnoreCase(args)
                || "high_value_bounties".equalsIgnoreCase(args);

        return "list".equals(command) && hvb;
    }

    @Override
    public CommandResult execute(@NotNull String command, @NotNull String args, @NotNull BaseCommand.CommandContext context) {
        VayraUniqueBountyManager manager = VayraUniqueBountyManager.getInstance();
        if (manager == null) {
            Console.showMessage("the VayraUniqueBountyManager instance is missing!");
            return CommandResult.ERROR;
        } else {
            Console.showMessage("unique bounties contains: " + manager.getBountiesList());
            Console.showMessage("unique currentBounties contains: " + manager.getCurrentBountiesList());
            Console.showMessage("unique spentBounties contains: " + manager.getSpentBountiesList());
            return CommandResult.SUCCESS;
        }
    }

    @Override
    public void onPostExecute(@NotNull String command, @NotNull String args, @NotNull CommandResult result, @NotNull BaseCommand.CommandContext context, @Nullable CommandListener interceptedBy) {
        // Unnecessary in most cases
    }
}
