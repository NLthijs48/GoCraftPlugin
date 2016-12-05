package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.messages.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class IPInfo extends InformationProvider {

	@Override
	public void show(Player about, CommandSender to) {
		if(!about.isOnline()) {
			return;
		}

		if(!to.hasPermission("gocraft.staff")) {
			return;
		}

		// Show numeric ip and (if available) the hostname
		String numerical = about.getAddress().getAddress().getHostAddress();
		String hostname = about.getAddress().getHostName();
		Message hostnamePart = Message.none();
		if(!hostname.equals(numerical)) {
			hostnamePart = Message.fromKey("information-ipHostname").replacements(hostname);
		}
		plugin.messageNoPrefix(to, "information-ip", numerical, hostnamePart);
	}
}
