package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.sessions.BungeeSession;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.interactivemessenger.processing.Message;

// TODO implement ip history/breakdown
public class IPInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(!request.getTo().hasPermission("gocraft.staff")) {
			return;
		}

		if(!request.getAbout().isOnline()) {
			return;
		}

		// Show numeric ip and (if available) the hostname
		String numerical = request.getAbout().getAddress().getAddress().getHostAddress();
		String hostname = request.getAbout().getAddress().getHostName();
		Message hostnamePart = Message.empty();
		if(!hostname.equals(numerical)) {
			hostnamePart = Message.fromKey("information-ipHostname").replacements(hostname);
		}
		request.message(Message.fromKey("information-ip").replacements(numerical, hostnamePart));
	}

	@Override
	public void showAsync(InformationRequest request) {
		if(!request.getTo().hasPermission("gocraft.staff")) {
			return;
		}

		// showSync already shows it
		if(request.getAbout().isOnline()) {
			return;
		}

		GCPlayer player = Database.getPlayer(request.getAbout().getUniqueId(), request.getAbout().getName());
		if(player != null) {
			BungeeSession lastBungeeSession = player.getLastBungeeSession();
			if(lastBungeeSession != null) {
				request.message(Message.fromKey("information-ip").replacements(lastBungeeSession.getPlayerIp(), Message.empty()));
			}
		}
	}
}
