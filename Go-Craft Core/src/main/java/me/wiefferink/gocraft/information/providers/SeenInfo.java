package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.sessions.BungeeSession;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.sessions.ServerSession;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.interactivemessenger.processing.Message;

public class SeenInfo extends InformationProvider {

	@Override
	public void showAsync(InformationRequest request) {
		Database.run(session -> {
			// Don't show status of staff members to normal players (vanish protection)
			// TODO investigate if we can allow this when not inspecting
			if(request.getAbout().hasPermission("gocraft.staff") && !request.getTo().hasPermission("gocraft.staff")) {
				return;
			}

			GCPlayer player = Database.getPlayer(request.getAbout().getUniqueId(), request.getAbout().getName());
			if(player == null) {
				return;
			}

			BungeeSession lastSession = player.getLastBungeeSession();
			if(lastSession != null) {
				Message history = Message.fromKey("information-onlineHistory").replacements(request.getAbout().getName());
				// Still online
				if(lastSession.getLeft() == null) {
					ServerSession lastServerSession = lastSession.getLastServerSession();
					String server = "dont know";
					if(lastServerSession != null) {
						server = lastServerSession.getServerName();
					}
					// TODO able to click servername
					request.message(Message.fromKey("information-nowOnline").replacements(server, history));
				}
				// Offline
				else {
					request.message(Message.fromKey("information-lastOnline")
							.replacements(
									Utils.agoMessage(lastSession.getLeft().getTime()),
									history
							)
					);
				}
			}
		});
	}
}
