package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.gocraft.tools.PageDisplay;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeenCommand extends Feature {

	public SeenCommand() {
		command("seen", "Check the history of players", "/seen [player]");
		permission("seen", "Check the online history of a player");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if(!sender.hasPermission("gocraft.seen")) {
			plugin.message(sender, "seen-noPermission");
			return;
		}

		// Get the player we want to know about
		OfflinePlayer about;
		if(args.length > 0) {
			about = Bukkit.getOfflinePlayer(args[0]);
			if(about == null || about.getName() == null) {
				plugin.message(sender, "seen-wrongPlayer", args[0]);
				return;
			}
		} else {
			plugin.message(sender, "seen-help");
			return;
		}

		// Get the record of the player
		async(() -> {
			Session session = Database.getSession();

			// Count number of records
			GCPlayer gcPlayer = Database.getPlayer(about.getUniqueId());
			if(gcPlayer == null) {
				plugin.message(sender, "seen-noRecords", about.getName());
				return;
			}

			long sessionCount = (long)(session.createQuery("SELECT count(*) FROM BungeeSession WHERE gcPlayer = :player").setParameter("player", gcPlayer).uniqueResult());
			PageDisplay display = new PageDisplay(sender, (int)sessionCount, "/seen "+about.getName()) {

				@Override
				public Message renderHeader() {
					// TODO click playername for /info
					return Message.fromKey("seen-header").replacements(about.getName()).prefix();
				}

				@Override
				public Message renderEmpty() {
					return Message.fromKey("seen-noRecords").replacements(about.getName()).prefix();
				}

				@Override
				public boolean renderItems(int itemStart, int itemEnd) {
					//GoCraft.debug("renderItems: start:", itemStart, "end:", itemEnd, "count:", sessionCount);

					// Get the BungeeSession objects for this page
					@SuppressWarnings("unchecked")
					List<BungeeSession> bungeeSessions = session.createQuery("FROM BungeeSession WHERE gcPlayer = :player ORDER BY joinedBungee DESC", BungeeSession.class)
							.setParameter("player", gcPlayer)
							.setMaxResults(itemEnd-itemStart+1)
							.setFirstResult(itemStart)
							.getResultList();
					// Get the ServerSession objects that belong to the BungeSession objects
					@SuppressWarnings("unchecked")
					List<ServerSession> allServerSessions = session.createQuery("FROM ServerSession WHERE bungeeSession in (:bungeeSessions) ORDER BY joinedServer DESC", ServerSession.class)
							.setParameterList("bungeeSessions", bungeeSessions)
							.setMaxResults(itemEnd-itemStart+1)
							.setFirstResult(itemStart)
							.getResultList();
					// Bundle the ServerSession objects for the BungeeSession objects
					Map<BungeeSession, List<ServerSession>> mapped = new HashMap<>();
					for(ServerSession serverSession : allServerSessions) {
						mapped.computeIfAbsent(serverSession.getBungeeSession(), k -> new ArrayList<>())
								.add(serverSession);
					}
					// Construct the lines in the history
					for(BungeeSession bungeeSession : bungeeSessions) {

						// Build start, end, length
						String left = "Now";
						Message length = Message.empty();
						if(bungeeSession.getLeft() != null) {
							left = Utils.shorTimeString(bungeeSession.getLeft().getTime());
							length = Message.fromKey("seen-itemLength")
									.replacements(Utils.millisToHumanFormat(bungeeSession.getLeft().getTime()-bungeeSession.getJoined().getTime()));
						}

						// Build hover
						Message hover = Message.empty();

						List<ServerSession> serverSessions = mapped.get(bungeeSession);
						if(serverSessions == null || serverSessions.isEmpty()) {
							hover.append(Message.fromKey("seen-hoverNone"));
						} else {
							for(ServerSession serverSession : serverSessions) {
								String leftServer = "Now";
								Message lengthServer = Message.empty();
								if(serverSession.getLeft() != null) {
									leftServer = Utils.shorTimeString(serverSession.getLeft().getTime());
									lengthServer = Message.fromKey("seen-itemLength")
											.replacements(Utils.millisToHumanFormat(serverSession.getLeft().getTime()-serverSession.getJoined().getTime()));
								}
								hover.append(Message.fromKey("seen-hoverLine")
										.replacements(Utils.shorTimeString(serverSession.getJoined().getTime()), leftServer, serverSession.getServerName(), lengthServer));
							}
						}

						// Build final line
						message(Message.fromKey("seen-item")
								.replacements(hover, Utils.shorTimeString(bungeeSession.getJoined().getTime()), left, length));
					}
					return true;
				}
			}.renderPage(args.length > 1 ? args[1] : null);

			Database.closeSession();

			sync(display::show);
		});
	}
}
