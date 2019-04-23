package fr.keke142.hypercraftupdater;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class HypercraftUpdaterPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginCommand convertMyWorldCommand = this.getCommand("convertmyworld");

        convertMyWorldCommand.setExecutor(new ConvertMyWorldCommand(this));
    }
}
