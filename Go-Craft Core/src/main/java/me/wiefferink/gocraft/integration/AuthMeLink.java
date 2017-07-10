package me.wiefferink.gocraft.integration;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.api.v3.AuthMeApi;
import me.wiefferink.gocraft.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class AuthMeLink {
	private AuthMeApi authmeApi;

	public AuthMeLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("AuthMe");
		if (!(plugin instanceof AuthMe)) {
			Log.warn("Plugin with name BanManager found, but it is not the correct one");
		} else {
			this.authmeApi = AuthMeApi.getInstance();
		}
	}

	public AuthMeApi get() {
		return authmeApi;
	}

}
