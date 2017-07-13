package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.scheduling.Do;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class TempbanCommand extends Feature {

	private ArrayList<String> tempbanCommands = new ArrayList<>(Arrays.asList(
			"tempban", "banmanager:tempban", "bmtempban", "banmanager:bmtempban",
			"tempbanip", "banmanager:tempbanip", "bmtempbanip", "banmanager:bmtempbanip"));

	public TempbanCommand() {
		listen();
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String fullMessage = event.getMessage();
		boolean hasArguments = fullMessage.contains(" ");
		String command, arguments = "";
		if (hasArguments) {
			command = fullMessage.substring(1, fullMessage.indexOf(" "));
			arguments = fullMessage.substring(fullMessage.indexOf(" ") + 1);
		} else {
			command = fullMessage.substring(1);
		}
		command = command.toLowerCase();
		if (tempbanCommands.contains(command)) {
			if (!event.getPlayer().hasPermission("gocraft.staff")) {
				plugin.message(event.getPlayer(), "staff-noPermission");
				event.setCancelled(true);
				return;
			}
			String reason = null;
			String[] argumentArray = arguments.split(" ");
			if (argumentArray.length < 3) {
				plugin.message(event.getPlayer(), "tempban-help");
				event.setCancelled(true);
				return;
			} else {
				for (int i = 2; i < argumentArray.length; i++) {
					if (reason == null) {
						reason = argumentArray[i];
					} else {
						reason += " " + argumentArray[i];
					}
				}
			}
			boolean success = event.getPlayer().performCommand("banmanager:tempban " + arguments)
					& event.getPlayer().performCommand("banmanager:tempbanip " + arguments);
			final Player finalPlayer = event.getPlayer();
			final String finalReason = reason;
			final String finalTarget = argumentArray[0];
			final String finalLength = argumentArray[1];
			// Delay message to first let the BanManager messages come through
			Do.syncLater(10, () -> {
				if(success) {
					plugin.message(finalPlayer, "tempban-redirected", finalTarget, finalLength, finalReason);
				} else {
					plugin.message(finalPlayer, "tempban-failed");
				}
			});
			event.setCancelled(true);
			plugin.increaseStatistic("command.tempban.redirected");
		}
	}

}
