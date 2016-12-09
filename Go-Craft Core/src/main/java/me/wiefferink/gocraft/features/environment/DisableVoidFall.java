package me.wiefferink.gocraft.features.environment;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

/**
 * Teleport the player to spawn if fallen through the world
 */
public class DisableVoidFall extends Feature {

	public DisableVoidFall() {
		listen("disableVoidFall");
	}

	@EventHandler
	public void onPlayerDamage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player)event.getEntity();
		if(inWorld(player) && player.getLocation().getBlockY() <= 0) {
			player.setVelocity(new Vector(0,0,0));
			player.setHealth(player.getMaxHealth());
			if(player.getFallDistance() > 10) { // Prevent message spam
				plugin.message(player, "other-fallenThroughWorld");
			}
			player.setFallDistance(0);
			event.setCancelled(Utils.teleportToLocation(player, Bukkit.getWorld("world").getSpawnLocation(), 15*15*15)); // Prevent infinite loop if not teleported
		}
	}
}
