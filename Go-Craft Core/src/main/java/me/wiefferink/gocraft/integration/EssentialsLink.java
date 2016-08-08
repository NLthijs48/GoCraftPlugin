package me.wiefferink.gocraft.integration;

import com.earth2me.essentials.Essentials;
import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class EssentialsLink {
	Essentials essentials;

	public EssentialsLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("Essentials");
		if (!(plugin instanceof Essentials)) {
			GoCraft.warn("Plugin with name Essentials found, but it is not the correct one");
		} else {
			this.essentials = (Essentials) plugin;
		}
	}

	public Essentials get() {
		return essentials;
	}

}
