package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

public class DisableBedrockBreak extends Feature {

	public DisableBedrockBreak() {
		listen("disableBedrockBreak");
	}

	// Prevent breaking bedrock
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		event.setCancelled(event.getBlock().getType() == Material.BEDROCK && !event.getPlayer().isOp() && inWorld(event));
	}
}
