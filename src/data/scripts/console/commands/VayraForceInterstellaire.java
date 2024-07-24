package data.scripts.console.commands;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import data.scripts.VayraMergedModPlugin;
import data.scripts.campaign.fleets.VayraPopularFrontManager;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;
import org.lazywizard.console.CommonStrings;
import org.lazywizard.console.Console;
import org.lwjgl.util.vector.Vector2f;

public class VayraForceInterstellaire implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        if (context != CommandContext.CAMPAIGN_MAP) {
            Console.showMessage(CommonStrings.ERROR_CAMPAIGN_ONLY);
            return CommandResult.WRONG_CONTEXT;
        }

        if (VayraMergedModPlugin.POPULAR_FRONT_ENABLED) {
            Console.showMessage("Popular front was enabled, maybe interstellaire is causing shit");
        } else {
            Console.showMessage("Popular front was disabled, interstellaire doesn't really exist");
            Console.showMessage("Turning on POPULAR_FRONT and VAYRA_DEBUG and VayraPopularFrontManager.LOG_TO_CONSOLE");
            VayraMergedModPlugin.VAYRA_DEBUG = true;
            VayraMergedModPlugin.POPULAR_FRONT_ENABLED = true;
            VayraPopularFrontManager.LOG_TO_CONSOLE = true;
        }

        SectorEntityToken test = Global.getSector().getEntityById(VayraPopularFrontManager.STATION_ID);
        if (test == null) {
            Console.showMessage("Interstellaire doesn't exist yet");
        } else {
            LocationAPI loc = test.getContainingLocation();
            Vector2f vector = test.getLocation();
            Console.showMessage("Interstellaire exists in location: "+loc+", at position: "+vector);
        }

        return CommandResult.SUCCESS;
    }
}
