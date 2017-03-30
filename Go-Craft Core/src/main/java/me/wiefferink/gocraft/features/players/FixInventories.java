package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.io.File;
import java.io.IOException;

public class FixInventories extends Feature {
	public FixInventories() {
		listen();
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerJoin(AsyncPlayerPreLoginEvent event) {
		String basePath = Bukkit.getServer().getWorldContainer().getAbsolutePath()+File.separator+"world"+File.separator+"playerdata"+File.separator;
		String uuid = event.getUniqueId().toString().toLowerCase();
		File offlineData = new File(basePath+uuid+".dat.offline-read");
		if(offlineData.exists()) {
			File normalData = new File(basePath+uuid+".dat");
			File emptyData = new File(basePath+uuid+".dat.empty");
			// Delete empty data from possible previous recovery
			if(emptyData.exists()) {
				try {
					FileDeleteStrategy.FORCE.delete(emptyData);
				} catch(IOException e) {
					Log.error("Could not delete emptyData file to fix inventory:", emptyData.getAbsolutePath(), ExceptionUtils.getStackTrace(e));
				}
			}
			// Move current player file to empty file
			if(normalData.exists() && !normalData.renameTo(emptyData)) {
				Log.error("Could not move current player file to empty file to fix inventory: from:", normalData.getAbsolutePath()+", to:", emptyData.getAbsolutePath());
			}
			// Move offline data to player file location
			if(!offlineData.renameTo(normalData)) {
				Log.error("Could not move offline player data file to proper location to fix inventory: from:", offlineData.getAbsolutePath()+", to:", normalData.getAbsolutePath());
			}
			Log.warn("Inventory of", event.getName(), "("+event.getUniqueId()+")", "has automatically been fixed");
			GoCraft.getInstance().increaseStatistic("administration.fixedinventory");
		}

	}
}
