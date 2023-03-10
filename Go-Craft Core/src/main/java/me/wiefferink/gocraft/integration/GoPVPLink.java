package me.wiefferink.gocraft.integration;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gopvp.GoPVP;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class GoPVPLink {
	GoPVP goPVP;

	public GoPVPLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("GoPVP");
		if (!(plugin instanceof GoPVP)) {
			Log.warn("Plugin with name GoPVP found, but it is not the correct one");
		} else {
			this.goPVP = (GoPVP) plugin;
		}
	}

	public GoPVP get() {
		return goPVP;
	}

}
