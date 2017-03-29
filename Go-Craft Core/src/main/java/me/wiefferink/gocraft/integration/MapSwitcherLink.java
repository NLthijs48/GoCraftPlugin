package me.wiefferink.gocraft.integration;

import me.wiefferink.gocraft.Log;
import nl.evolutioncoding.mapswitcher.MapSwitcher;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class MapSwitcherLink {
	MapSwitcher mapSwitcher;

	public MapSwitcherLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("MapSwitcher");
		if (!(plugin instanceof MapSwitcher)) {
			Log.warn("Plugin with name MapSwitcher found, but it is not the correct one");
		} else {
			this.mapSwitcher = (MapSwitcher) plugin;
		}
	}

	public MapSwitcher get() {
		return mapSwitcher;
	}

}
