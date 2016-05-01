package me.wiefferink.gocraft.integration;

import me.confuser.banmanager.BanManager;
import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class BanManagerLink {
	BanManager banManager;

	public BanManagerLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("BanManager");
		if (!(plugin instanceof BanManager)) {
			GoCraft.debug("Plugin with name BanManager found, but it is not the correct one");
		} else {
			this.banManager = (BanManager) plugin;
		}
	}

	public BanManager get() {
		return banManager;
	}

}
