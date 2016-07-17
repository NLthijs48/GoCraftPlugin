package me.wiefferink.gocraft.features;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;

import java.util.HashSet;

public class Feature implements Listener {
	public HashSet<String> commands = new HashSet<>();
	public static GoCraft plugin = GoCraft.getInstance();

	/**
	 * Stop actions of the feature
	 */
	public void stopFeature() {
		stop();
	}

	public void stop() {
	}

	/**
	 * Startup actions of the feature
	 */
	public void startFeature() {
		if (commands != null && this instanceof CommandExecutor) {
			for (String command : commands) {
				PluginCommand commandClass = plugin.getCommand(command);
				if (commandClass != null) {
					commandClass.setExecutor((CommandExecutor) this);
				} else {
					plugin.getLogger().warning("Command '" + command + "' is not added to plugin.yml and cannot be registered!");
				}
			}
		}
	}

	/**
	 * Register as listener
	 */
	protected void listen() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}
}
