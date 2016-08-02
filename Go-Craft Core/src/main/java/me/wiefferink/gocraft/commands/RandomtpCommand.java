package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Callback;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RandomtpCommand extends Feature implements CommandExecutor {

	public RandomtpCommand() {
		commands.add("RandomTp");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return true;
		}
		Player player = (Player)sender;
		if(!sender.hasPermission("gocraft.randomtp")) {
			plugin.message(player, "randomtp-noPermission");
			return true;
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
		return true;
	}

}
