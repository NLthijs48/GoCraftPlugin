package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DisableWitherDamage implements Listener {

	public final String configLine = "disableWitherDamage";
	private GoCraft plugin;

	public DisableWitherDamage(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	@EventHandler
	public void onWitherExplosion(EntityExplodeEvent event) {
		if (event.getEntity().getType() == EntityType.WITHER || event.getEntity() instanceof WitherSkull) {
			event.blockList().clear();
			event.setYield(0);
		}
	}

	@EventHandler
	public void onWitherDestroyBlocks(EntityChangeBlockEvent event) {
		EntityType type = event.getEntity().getType();
		event.setCancelled(type == EntityType.WITHER || type == EntityType.WITHER_SKULL);
	}
}
