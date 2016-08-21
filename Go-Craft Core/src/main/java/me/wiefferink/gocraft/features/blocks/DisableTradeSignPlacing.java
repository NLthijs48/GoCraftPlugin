package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

public class DisableTradeSignPlacing extends Feature {

	public DisableTradeSignPlacing() {
		listen("disableTradeSignPlacing");
	}

	// Prevent breaking bedrock
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		if(inWorld(event)
				&& (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN)
				&& !event.getPlayer().isOp()) {
			// Check for trade signs
			if (event.getLine(0) != null && event.getLine(0).equalsIgnoreCase("[trade]")) {
				// Check for the spawn area
				Location loc = event.getBlock().getLocation();
				if (!(loc.getBlockX() < 280 && loc.getBlockX() > 130 && loc.getBlockZ() < -80 && loc.getBlockZ() > -235)) {
					event.setCancelled(true);
					plugin.message(event.getPlayer(), "tradesign-blocked");
				}
			}
		}
	}
}
