package me.wiefferink.gocraft.features;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;

public class Feature implements Listener, CommandExecutor {
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
	}

	/**
	 * Register as listener
	 */
	protected void listen() {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
	}


	/**
	 * Register for a certain command
	 * @param name The command to register for
	 */
	protected void command(String name) {
		PluginCommand command = plugin.getCommand(name);
		if(command != null) {
			command.setExecutor(this);
		} else {
			GoCraft.warn("Could not register for command: "+name);
		}
	}
}
