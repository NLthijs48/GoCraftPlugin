package nl.evolutioncoding.gocraft.items;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableBooks implements Listener {
	
	public final String configLine = "disableBooks";
	private GoCraft plugin;
	
	public DisableBooks(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent using xp bottles
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(plugin.onThisWorld(configLine, event.getPlayer())
				&& (event.getAction() == Action.RIGHT_CLICK_AIR 
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK) 
				&& event.getPlayer().getItemInHand().getType() == Material.BOOK_AND_QUILL && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBookEdit(PlayerEditBookEvent event) {
		if(!event.getPlayer().isOp()) {
			event.setCancelled(true);
			Player Player = event.getPlayer();
			Player.getInventory().remove(Material.BOOK);
			Player.getInventory().remove(Material.BOOK_AND_QUILL);
		}
	}
}
