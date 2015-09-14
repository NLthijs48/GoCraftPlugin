package nl.evolutioncoding.gocraft.general;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SpawnTeleport implements Listener {
	
	public final String configLine = "spawnTeleport";
	private GoCraft plugin;
	
	public SpawnTeleport(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent rain and thunder
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(plugin.onThisWorld(configLine, event.getPlayer())) {
			event.getPlayer().teleport(Bukkit.getWorld("world").getSpawnLocation());
			/*
			final Player player = event.getPlayer();
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			    @Override
			    public void run() {
			        player.teleport(Bukkit.getWorld("world").getSpawnLocation());
			    }
			}, 10L);
			*/
		}
	}
}
