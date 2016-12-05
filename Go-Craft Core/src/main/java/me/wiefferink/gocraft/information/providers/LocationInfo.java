package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LocationInfo extends InformationProvider {

	@Override
	public void show(Player about, CommandSender to) {
		if(!to.hasPermission("gocraft.staff")) {
			return;
		}

		Location location = about.getLocation();
		plugin.messageNoPrefix(to, "information-itemLocation", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), Math.round(location.getYaw()), Math.round(location.getPitch()), about.getName());
	}
}
