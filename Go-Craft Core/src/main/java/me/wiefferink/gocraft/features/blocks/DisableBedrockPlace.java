package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

public class DisableBedrockPlace extends Feature {

	public DisableBedrockPlace() {
		listen("disableBedrockPlace");
	}

	// Prevent placing bedrock
	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		event.setCancelled(event.getBlock().getType() == Material.BEDROCK && !event.getPlayer().isOp() && inWorld(event));
	}
}
