package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

public class DisableBlockBreaking extends Feature {

	public DisableBlockBreaking() {
		listen("disableBlockBreaking");
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(!event.getPlayer().isOp() && inWorld(event));
	}
}
