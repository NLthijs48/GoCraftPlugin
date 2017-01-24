package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.interactivemessenger.processing.Message;

public class IPInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(!request.getAbout().isOnline()) {
			return;
		}

		if(!request.getTo().hasPermission("gocraft.staff")) {
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
}
