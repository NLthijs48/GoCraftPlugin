package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableXpBottleThrow extends Feature {

	public DisableXpBottleThrow() {
		listen("disableXpBottleThrow");
	}

	// Prevent using xp bottles
	@EventHandler(ignoreCancelled = true)
	public void onItemUse(PlayerInteractEvent event) {
		if(inWorld(event)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& event.getPlayer().getItemInHand().getType() == Material.EXP_BOTTLE && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
}
