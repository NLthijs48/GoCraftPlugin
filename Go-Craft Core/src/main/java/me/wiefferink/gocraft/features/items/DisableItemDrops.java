package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DisableItemDrops extends Feature {

	public DisableItemDrops() {
		listen("disableItemDrops");
	}

	// Prevent dropping items from inventory
	@EventHandler(ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		if(inWorld(event) && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
}
