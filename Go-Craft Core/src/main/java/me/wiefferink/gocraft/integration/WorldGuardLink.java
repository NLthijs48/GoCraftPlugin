package me.wiefferink.gocraft.integration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.wiefferink.gocraft.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class WorldGuardLink {
	WorldGuardPlugin worldGuard;

	public WorldGuardLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (!(plugin instanceof WorldGuardPlugin)) {
			Log.warn("Plugin with name WorldGuard found, but it is not the correct one");
		} else {
			this.worldGuard = (WorldGuardPlugin) plugin;
		}
	}

	public WorldGuardPlugin get() {
		return worldGuard;
	}
}
