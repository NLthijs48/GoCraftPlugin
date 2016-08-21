package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockDispenseEvent;

public class DisableDispensers extends Feature {

	public DisableDispensers() {
		listen("disableDispensers");
	}

	// Prevent dispensing items
	@EventHandler(ignoreCancelled = true)
	public void onBlockDispense(BlockDispenseEvent event) {
		event.setCancelled(inWorld(event));
	}
}
