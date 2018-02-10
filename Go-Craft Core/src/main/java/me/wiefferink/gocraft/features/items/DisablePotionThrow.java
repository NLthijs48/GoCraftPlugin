package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisablePotionThrow extends Feature {

	public DisablePotionThrow() {
		listen("disablePotionThrow");
	}

	// Prevent Potion throw
	@EventHandler(ignoreCancelled = true)
	public void onItemUse(PlayerInteractEvent event) {
		if(inWorld(event)
				&& (event.getMaterial() == Material.SPLASH_POTION || event.getMaterial() == Material.LINGERING_POTION)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				) {
			event.setCancelled(true);
		}
	}
}
