package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetspawnCommand extends Feature {

	public SetspawnCommand() {
		if(config.getBoolean("spawnTeleport")) { // Same as in SpawnTeleport.java
			command("SetGSpawn");
		}
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if (!sender.hasPermission("gocraft.setspawn")) {
			plugin.message(sender, "setspawn-noPermission");
			return;
		}

		if (!(sender instanceof Player)) {
			plugin.message(sender, "setspawn-onlyByPlayers");
			return;
		}
		Player player = (Player) sender;
		plugin.getLocalStorage().set("spawnLocation", Utils.locationToConfig(player.getLocation(), true));
		plugin.saveLocalStorage();
		plugin.message(player, "setspawn-success");
		plugin.increaseStatistic("command.setspawn.used");
	}

}

































