package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DisableWitherDamage extends Feature {

	public DisableWitherDamage() {
		listen("disableWitherDamage");
	}

	@EventHandler(ignoreCancelled = true)
	public void onWitherExplosion(EntityExplodeEvent event) {
		if(inWorld(event) && (event.getEntity().getType() == EntityType.WITHER || event.getEntity() instanceof WitherSkull)) {
			event.blockList().clear();
			event.setYield(0);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onWitherDestroyBlocks(EntityChangeBlockEvent event) {
		EntityType type = event.getEntity().getType();
		event.setCancelled(inWorld(event) && (type == EntityType.WITHER || type == EntityType.WITHER_SKULL));
	}
}
