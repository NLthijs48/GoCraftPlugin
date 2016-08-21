package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpawnTeleport extends Feature {

	public SpawnTeleport() {
		listen("spawnTeleport");
	}

	// Spawn the player at the spawnlocation
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(inWorld(event)) {
			ConfigurationSection section = plugin.getLocalStorage().getConfigurationSection("spawnLocation");
			Location location = null;
			if (section != null) {
				location = Utils.configToLocation(section);
			}
			if (location == null) {
				location = Bukkit.getWorld("world").getSpawnLocation();
			}
			event.getPlayer().teleport(location);
		}
	}
}
