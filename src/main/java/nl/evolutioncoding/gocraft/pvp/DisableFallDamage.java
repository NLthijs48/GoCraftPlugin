package nl.evolutioncoding.gocraft.pvp;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DisableFallDamage implements Listener {
	
	public final String configLine = "disableFallDamage";
	private GoCraft plugin;
	
	public DisableFallDamage(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Disable fall damage
	@EventHandler
	public void onDamage(EntityDamageEvent event) {
		if(plugin.onThisWorld(configLine, event.getEntity())) {
			if(event.getEntity() instanceof Player && event.getCause().equals(DamageCause.FALL)) {
				event.setCancelled(true);
			}
		}
	}
}
