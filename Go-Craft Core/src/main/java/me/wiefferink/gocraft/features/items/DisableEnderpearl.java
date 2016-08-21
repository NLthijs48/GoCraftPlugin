package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableEnderpearl extends Feature {

	public DisableEnderpearl() {
		listen("disableEnderpearl");
	}

	// Prevent using xp bottles
	@EventHandler(ignoreCancelled = true)
	public void onItemUse(PlayerInteractEvent event) {
		if(inWorld(event)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& event.getPlayer().getItemInHand().getType() == Material.ENDER_PEARL && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
}
