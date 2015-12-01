package nl.evolutioncoding.gocraft.blocks;

import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class DisableTradeSignPlacing implements Listener {
	
	public final String configLine = "disableTradeSignPlacing";
	private GoCraft plugin;
	
	public DisableTradeSignPlacing(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent breaking bedrock
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onSignChange(SignChangeEvent event) {
		if(plugin.onThisWorld(this.configLine, event.getBlock())
				&& (event.getBlock().getType() == Material.SIGN || event.getBlock().getType() == Material.SIGN_POST || event.getBlock().getType() == Material.WALL_SIGN)
				&& !event.getPlayer().isOp()) {
			// Check for trade signs
			if(event.getLine(0) != null && event.getLine(0).equalsIgnoreCase("[trade]")) {
				// Check for the spawn area
				Location loc = event.getBlock().getLocation();
				if(!(loc.getBlockX() < 280 && loc.getBlockX() > 130 && loc.getBlockZ() < -80 && loc.getBlockZ() > -235)) {
					event.setCancelled(true);
					plugin.message(event.getPlayer(), "tradesign-blocked");
				}
			}			
		}		
	}
}
