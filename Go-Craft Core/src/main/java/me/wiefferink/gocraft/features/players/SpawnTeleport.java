package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpawnTeleport extends Feature {

	public SpawnTeleport() {
		if(config.getBoolean("spawnTeleport")) {
			command("setgspawn", "Set the spawn location of the server");
			listen();
		}
	}

	// Set the spawn location
	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if(!sender.hasPermission("gocraft.setspawn")) {
			plugin.message(sender, "setspawn-noPermission");
			return;
		}

		if(!(sender instanceof Player)) {
			plugin.message(sender, "setspawn-onlyByPlayers");
			return;
		}
		Player player = (Player)sender;
		plugin.getLocalStorage().set("spawnLocation", Utils.locationToConfig(player.getLocation(), true));
		plugin.saveLocalStorage();
		plugin.message(player, "setspawn-success");
		plugin.increaseStatistic("command.setspawn.used");
	}

	// Spawn the player at the spawnlocation
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(inWorld(event)) {
			ConfigurationSection section = plugin.getLocalStorage().getConfigurationSection("spawnLocation");
			Location location = null;
			if(section != null) {
				location = Utils.configToLocation(section);
			}
			if(location == null) {
				location = Bukkit.getWorld("world").getSpawnLocation();
			}
			event.getPlayer().teleport(location);
		}
	}
}
