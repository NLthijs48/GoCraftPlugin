package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableBooks extends Feature {

	public DisableBooks() {
		listen("disableBooks");
	}

	// Prevent using xp bottles
	@EventHandler(ignoreCancelled = true)
	public void onItemUse(PlayerInteractEvent event) {
		if(inWorld(event)
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& event.getPlayer().getItemInHand().getType() == Material.BOOK_AND_QUILL && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onBookEdit(PlayerEditBookEvent event) {
		if(!event.getPlayer().isOp() && inWorld(event)) {
			event.setCancelled(true);
			Player Player = event.getPlayer();
			Player.getInventory().remove(Material.BOOK);
			Player.getInventory().remove(Material.BOOK_AND_QUILL);
		}
	}
}
