package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;
import me.wiefferink.gocraft.sessions.BungeeSession;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.sessions.ServerSession;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Database;
import org.hibernate.Session;

public class SeenInfo extends InformationProvider {

	@Override
	public void showAsync(InformationRequest request) {
		Session session = Database.getSession();

		// Don't show status of staff members to normal players (vanish protection)
		// TODO investigate if we can allow this when not inspecting
		if(request.getAbout().hasPermission("gocraft.staff") && !request.getTo().hasPermission("gocraft.staff")) {
			return;
		}

		GCPlayer gcPlayer = Database.getCreatePlayer(request.getAbout().getUniqueId(), request.getAbout().getName());
		BungeeSession lastSession = session.createQuery("FROM BungeeSession WHERE gcPlayer = :player ORDER BY joinedBungee DESC", BungeeSession.class)
				.setParameter("player", gcPlayer)
				.setMaxResults(1)
				.uniqueResult();
		if(lastSession != null) {
			Message history = Message.fromKey("information-onlineHistory").replacements(request.getAbout().getName());
			// Still online
			if(lastSession.getLeft() == null) {
				ServerSession serverSession = session.createQuery("FROM ServerSession WHERE bungeeSession = :bungeeSession ORDER BY joinedServer DESC", ServerSession.class)
						.setParameter("bungeeSession", lastSession)
						.setMaxResults(1)
						.uniqueResult();
				String server = "dont know";
				if(serverSession != null) {
					server = serverSession.getServerName();
				}
				// TODO able to click servername
				request.message(Message.fromKey("information-nowOnline").replacements(server, history));
			}
			// Offline
			else {
				// TODO show server?
				request.message(Message.fromKey("information-lastOnline")
						.replacements(
								Utils.agoMessage(lastSession.getLeft().getTime()),
								history
						)
				);
			}
		}

		Database.closeSession();
	}
}
