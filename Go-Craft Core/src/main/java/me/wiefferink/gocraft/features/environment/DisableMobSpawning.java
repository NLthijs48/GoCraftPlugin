package me.wiefferink.gocraft.features.environment;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class DisableMobSpawning extends Feature {

	public DisableMobSpawning() {
		listen("disableMobSpawning");
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		event.setCancelled(inWorld(event) && event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM); // Allow plugin spawned entities
	}
}
