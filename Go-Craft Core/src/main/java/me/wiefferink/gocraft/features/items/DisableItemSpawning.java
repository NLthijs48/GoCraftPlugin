package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ItemSpawnEvent;

public class DisableItemSpawning extends Feature {

	public DisableItemSpawning() {
		listen("disableItemSpawning");
	}

	// Prevent items dropping from breaking a container
	@EventHandler(ignoreCancelled = true)
	public void onItemSpawn(ItemSpawnEvent event) {
		if(inWorld(event)) {
			event.setCancelled(true);
		}
	}
}
