package nl.evolutioncoding.gocraft.general;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class DisableMobSpawning implements Listener {
	
	public final String configLine = "disableMobSpawning";
	private GoCraft plugin;
	
	public DisableMobSpawning(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent spawning of mobs
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		if(plugin.onThisWorld(configLine, event.getLocation())
				&& event.getSpawnReason() != SpawnReason.SPAWNER
				&& event.getSpawnReason() != SpawnReason.DEFAULT 
				&& event.getSpawnReason() != SpawnReason.DEFAULT) {
			event.setCancelled(true);
		}
		//plugin.debug("Spawn event, type=" + event.getEntityType() + ", reason=" + event.getSpawnReason() + ", cancelled=" + event.isCancelled());
	}
}
