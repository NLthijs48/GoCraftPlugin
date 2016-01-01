package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;

public class TempbanCommand implements Listener {
	
	public final String configLine = "redirectTempban";
	private GoCraft plugin;
	private ArrayList<String> tempbanCommands = new ArrayList<>(Arrays.asList(
			"tempban", "banmanager:tempban", "bmtempban", "banmanager:bmtempban",
			"tempbanip", "banmanager:tempbanip", "bmtempbanip", "banmanager:bmtempbanip"));	
	
	public TempbanCommand(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String fullMessage = event.getMessage();
		boolean hasArguments = fullMessage.contains(" ");
		String command, arguments = "";
		if(hasArguments) {
			command = fullMessage.substring(1, fullMessage.indexOf(" "));
			arguments = fullMessage.substring(fullMessage.indexOf(" ") +1);
		} else {
			command = fullMessage.substring(1);
		}
		command = command.toLowerCase();
		if(tempbanCommands.contains(command)) {
			if(!event.getPlayer().hasPermission("gocraft.staff")) {
				plugin.message(event.getPlayer(), "staff-noPermission");
				event.setCancelled(true);
				return;
			}
			String reason = null;
			String[] argumentArray = arguments.split(" ");
			if(argumentArray.length < 3) {
				plugin.message(event.getPlayer(), "tempban-help");
				event.setCancelled(true);
				return;
			} else {
				for(int i=2; i<argumentArray.length; i++) {
					if(reason == null) {
						reason = argumentArray[i];
					} else {
						reason += " " + argumentArray[i];
					}
				}
			}
			boolean success = event.getPlayer().performCommand("banmanager:tempban " + arguments);
			success = success & event.getPlayer().performCommand("banmanager:tempbanip " + arguments);
			final boolean finalSuccess = success;
			final Player finalPlayer = event.getPlayer();
			final String finalReason = reason;
			final String finalTarget = argumentArray[0];
			final String finalLength = argumentArray[1];
			// Delay message to first let the BanManager messages come through
			new BukkitRunnable() {
				@Override
				public void run() {
					if(finalSuccess) {
						plugin.message(finalPlayer, "tempban-redirected", finalTarget, finalLength, finalReason);
					} else {
						plugin.message(finalPlayer, "tempban-failed");
					}
				}
			}.runTaskLater(plugin, 10L);
			event.setCancelled(true);
		}
	}

}
