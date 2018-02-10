package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.LingeringPotionSplashEvent;
import org.bukkit.event.entity.PotionSplashEvent;

public class DisablePotionSplash extends Feature {

	public DisablePotionSplash() {
		listen("disablePotionSplash");
	}

	// Block effect when a splash potion hits the ground
	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		if(inWorld(event)) {
			event.setCancelled(true);
		}
	}

	// Block effect when a splash lingering potion hits the ground
	@EventHandler(ignoreCancelled = true)
	public void onLingeringPotionSplash(LingeringPotionSplashEvent event) {
		if(inWorld(event)) {
			event.setCancelled(true);
		}
	}
}
