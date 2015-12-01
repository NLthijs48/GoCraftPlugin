package nl.evolutioncoding.gocraft.pvp;

import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DisablePlayerDamage implements Listener {
	
	public final String configLine = "disablePlayerDamage";
	private GoCraft plugin;
	
	public DisablePlayerDamage(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Disable damage
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(plugin.onThisWorld(configLine, event.getEntity())) {
			if(event.getEntity() instanceof Player) {
				event.setCancelled(true);
				((Player)(event.getEntity())).setHealth(20.0);
			}
		}
	}
}
