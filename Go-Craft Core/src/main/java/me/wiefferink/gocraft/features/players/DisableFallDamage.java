package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class DisableFallDamage extends Feature {

	public DisableFallDamage() {
		listen("disableFallDamage");
	}

	// Disable fall damage
	@EventHandler(ignoreCancelled = true)
	public void onDamage(EntityDamageEvent event) {
		if(inWorld(event) && event.getEntity() instanceof Player && event.getCause().equals(DamageCause.FALL)) {
			event.setCancelled(true);
		}
	}
}
