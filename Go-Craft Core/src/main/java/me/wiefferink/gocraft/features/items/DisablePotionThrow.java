package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
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
		int potionData = event.getPlayer().getItemInHand().getDurability() % 32768;
		if(inWorld(event)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& potionData / 16384 > 0) {
			event.setCancelled(true);
		}
	}
}
