package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

public class DisablePlayerDamage extends Feature {

	public DisablePlayerDamage() {
		listen("disablePlayerDamage");
	}

	// Disable damage
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if(inWorld(event) && event.getEntity() instanceof Player) {
			event.setCancelled(true);
			((Player)(event.getEntity())).setHealth(20.0);
		}
	}
}
