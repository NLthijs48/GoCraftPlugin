package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;

public class DisablePotionSplash implements Listener {

	public final String configLine = "disablePotionSplash";
	private GoCraft plugin;

	public DisablePotionSplash(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Block effect when a splash potion hits the ground
	@EventHandler
	public void onPotionSplash(PotionSplashEvent event) {
		if (plugin.onThisWorld(configLine, event.getEntity())) {
			event.setCancelled(true);
		}
	}
}
