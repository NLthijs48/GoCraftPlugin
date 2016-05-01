package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpawnTeleport implements Listener {

	public final String configLine = "spawnTeleport"; // Same as in SetspawnCommand.java
	private GoCraft plugin;

	public SpawnTeleport(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Spawn the player at the spawnlocation
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (plugin.onThisWorld(configLine, event.getPlayer())) {
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
