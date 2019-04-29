package fr.keke142.worldupdater;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldUpdaterPlugin extends JavaPlugin {
    private PluginManager pluginManager;

    @Override
    public void onEnable() {
        pluginManager = this.getServer().getPluginManager();

        PluginCommand convertMyWorldCommand = this.getCommand("convertmyworld");

        convertMyWorldCommand.setExecutor(new ConvertMyWorldCommand(this));
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }
}
