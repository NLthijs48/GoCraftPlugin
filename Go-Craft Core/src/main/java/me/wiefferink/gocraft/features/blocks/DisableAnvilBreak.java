package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableAnvilBreak extends Feature {

	public DisableAnvilBreak() {
		listen("disableAnvilBreak");
	}

	// Prevent anvil breaking
	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled = true)
	public void onAnvilUse(PlayerInteractEvent event) {
		if(inWorld(event) && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = event.getClickedBlock();
			if(block.getType() == Material.ANVIL) {
				/* Direction by looking there and then placing
				 * Direction\type:	normal	damaged	broken
				 * East				0		4		8
				 * South			1		5		9
				 * West				2		6		10
				 * North			3		7		11
				 */
				block.setData((byte)(block.getData()%4));
			}
		}
	}
}
