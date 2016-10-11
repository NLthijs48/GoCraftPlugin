package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Callback;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomtpCommand extends Feature {

	public RandomtpCommand() {
		permission("randomtp", "Allows you to teleport to random places");
		command("randomtp", "Teleport to a random location in the current world", "/randomtp", "rtp");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}
		Player player = (Player)sender;
		if(!sender.hasPermission("gocraft.randomtp")) {
			plugin.message(player, "randomtp-noPermission");
			return;
		}
		Utils.teleportRandomly(player, player.getWorld(), Utils.getWorldRadius(player.getWorld()), new Callback<Boolean>() {
			@Override
			public void execute(Boolean teleported) {
				if(teleported) {
					plugin.message(player, "randomtp-success");
					plugin.increaseStatistic("command.rtp."+player.getWorld().getName()+".success");
				} else {
					plugin.message(player, "randomtp-failed");
					plugin.increaseStatistic("command.rtp."+player.getWorld().getName()+".failed");
				}
			}
		});
	}

}
