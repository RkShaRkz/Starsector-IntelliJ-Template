package shark.utilityconsole.commands.rkz;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.console.BaseCommand;

import java.util.List;

public class temp implements BaseCommand {
    @Override
    public CommandResult runCommand(@NotNull String args, @NotNull BaseCommand.CommandContext context) {
        List<WeaponSpecAPI> guns = Global.getSettings().getAllWeaponSpecs();
        for (WeaponSpecAPI gun : guns) {
            gun.getDamageType();

            gun.getMaxRange();
            gun.getMaxAmmo();
            gun.getType();
            gun.getDamageType();
            gun.getTurnRate();
            gun.getSize();
        }
        return null;
    }
}
