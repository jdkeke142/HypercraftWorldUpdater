package fr.keke142.worldupdater;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldUpdaterPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginCommand convertMyWorldCommand = this.getCommand("convertmyworld");

        convertMyWorldCommand.setExecutor(new ConvertMyWorldCommand(this));
    }
}
