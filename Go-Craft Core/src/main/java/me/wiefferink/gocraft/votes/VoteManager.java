package me.wiefferink.gocraft.votes;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.rewards.Reward;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.PageDisplay;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.permissions.PermissionDefault;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class VoteManager extends Feature {

	private SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
	private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM YYYY", Locale.ENGLISH);

	public VoteManager() {
		listen("collectVotes");
		command("votetop", "Vote top 10", "/votetop [year-month] [page]");
		permission("votetop", "Access to the /votetop command", PermissionDefault.TRUE);
		command("vote", "Get the vote link", "/vote", "votelink", "vote-link");

		Do.asyncTimer(20*60*5, this::voteReminder);
	}

	@EventHandler
	public void voteEvent(VotifierEvent event) {
		if(event.getVote().getUsername() == null || event.getVote().getUsername().isEmpty()) {
			Log.warn("Vote with empty username:", event.getVote());
			return;
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getVote().getUsername());
		if(offlinePlayer == null || offlinePlayer.getName() == null) {
			Log.warn("Voting user not found: ", event.getVote().getUsername());
			return;
		}

		async(() -> {
			Database.run(session -> {
				GCPlayer gcPlayer = Database.getCreatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName());

				// Get the last vote done at the same website
				Vote lastVote = session
						.createQuery("FROM Vote WHERE gcPlayer = :gcPlayer AND serviceName = :service ORDER BY at DESC", Vote.class)
						.setParameter("gcPlayer", gcPlayer)
						.setParameter("service", event.getVote().getServiceName())
						.setMaxResults(1)
						.uniqueResult();
				Vote vote = new Vote(
						gcPlayer,
						event.getVote().getServiceName().toLowerCase(),
						event.getVote().getAddress(),
						event.getVote().getTimeStamp()
				);

				if(gcPlayer != null && lastVote != null && (lastVote.getAt().getTime() + 1000 * 60 * 60 * 11) > Calendar.getInstance().getTimeInMillis()) {
					Log.warn("Last vote of", gcPlayer.getName(), "at", event.getVote().getServiceName(), "was too short ago (restricting to once every 11 hours):", lastVote);
					return;
				}

				session.save(vote);
				Log.debug("Received vote:", vote);

				// Setup rewards
				ConfigurationSection servers = plugin.getGeneralConfig().getConfigurationSection("servers");
				if(servers != null) {

					// Rewards for each server
					for(String serverKey : servers.getKeys(false)) {
						ConfigurationSection serverRewards = servers.getConfigurationSection(serverKey + ".voteRewards");
						if(serverRewards == null) {
							continue;
						}

						// Server rewards
						for(String rewardKey : serverRewards.getKeys(false)) {
							ConfigurationSection rewardSection = serverRewards.getConfigurationSection(rewardKey);
							if(rewardSection == null) {
								continue;
							}

							Reward reward = new Reward(gcPlayer, Reward.RewardSource.VOTING, serverKey);
							// Main type
							if(rewardSection.isDouble("money") || rewardSection.isInt("money")) {
								reward.money(rewardSection.getDouble("money"));
							} else if(rewardSection.isString("command")) {
								reward.command(rewardSection.getString("command"));
							} else {
								Log.warn("Vote reward", rewardKey, "for server", serverKey, "has no money or command!");
								continue;
							}

							// Slots condition
							if(rewardSection.isInt("requiredSlots")) {
								reward.requiredSlots(rewardSection.getInt("requiredSlots"));
							}

							// Message
							if(rewardSection.isString("message")) {
								String message = rewardSection.getString("message");
								message = message.replace("{service}", vote.getServiceName());
								reward.message(message);
							}

							session.save(reward);
						}
					}
				}

				// Immediately give rewards if the player is online, using same database transaction
				Player player = Bukkit.getPlayer(offlinePlayer.getUniqueId());
				if(player != null && player.isOnline()) {
					plugin.getRewardClaim().giveRewards(player, false);
				}
			});

			plugin.getSyncCommandsServer().runCommand("updateVoteStatus", event.getVote().getAddress());
		});
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		// Vote link message
		if("vote".equalsIgnoreCase(command.getName())) {
			plugin.message(sender, "vote-link");
			return;
		}

		if(!sender.hasPermission("gocraft.votetop")) {
			plugin.message(sender, "votetop-noPermission");
			return;
		}

		int pageIndex = 0;
		Calendar getAt = Calendar.getInstance();
		getAt.setTime(getMonthStart()); // Move to start of the month, or else changing month from jan -> feb while day is > 28 it would be wrong

		// Detect year-month argument
		String baseCommand = "/votetop";
		if(args.length > 0 && (args.length > 1 || args[0].contains("-"))) {
			String[] yearMonth = args[0].split("-");
			if(yearMonth.length != 2 || !Utils.isNumeric(yearMonth[0]) || !Utils.isNumeric(yearMonth[1])) {
				plugin.message(sender, "votetop-wrongYearMonth", args[0]);
				return;
			}
			getAt.set(Calendar.YEAR, Utils.asNumber(yearMonth[0]));
			getAt.set(Calendar.MONTH, Utils.asNumber(yearMonth[1])-1); // Month starts at 0 for some reason
			pageIndex = 1;
			baseCommand += " "+args[0];
		}

		// Select format
		SimpleDateFormat format = monthFormat;
		if(getAt.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
			format = monthYearFormat;
		}
		String yearMonthText = format.format(getAt.getTime());

		final int finalPageIndex = pageIndex;
		final String finalBaseCommand = baseCommand;
		async(() ->
			Database.run(session -> {
				Date monthStart = getMonthStart(getAt);
				Date monthEnd = getMonthEnd(getAt);

				// Count players that voted in this month
				long playerCount = (long)session.createQuery("SELECT count(distinct gcPlayer) from Vote WHERE at <= :monthEnd AND at >= :monthStart")
						.setParameter("monthStart", monthStart)
						.setParameter("monthEnd", monthEnd)
						.uniqueResult();

				PageDisplay display = new PageDisplay(sender, (int)playerCount, finalBaseCommand) {

					@Override
					public Message renderHeader() {
						return Message.fromKey("votetop-header").replacements(yearMonthText).prefix();
					}

					@Override
					public Message renderEmpty() {
						return Message.fromKey("votetop-empty").replacements(yearMonthText).prefix();
					}

					@Override
					public boolean renderItems(int itemStart, int itemEnd) {
						// Fetch the players and vote counts
						@SuppressWarnings("unchecked")
						List<Object[]> playerVoteCounts = session.createQuery(
								"SELECT votePlayer.name, count(*) " +
										"FROM Vote vote " +
										"INNER JOIN vote.gcPlayer as votePlayer " +
										"WHERE vote.at <= :monthEnd AND vote.at >= :monthStart " +
										"GROUP BY votePlayer " +
										"ORDER BY count(*) DESC, votePlayer.name ASC")
								.setParameter("monthStart", monthStart)
								.setParameter("monthEnd", monthEnd)
								.setMaxResults(itemEnd - itemStart + 1)
								.setFirstResult(itemStart).getResultList();
						int index = itemStart+1;
						for(Object[] playerAndCount : playerVoteCounts) {
							message(Message.fromKey("votetop-item").replacements(index, playerAndCount[0], playerAndCount[1]));
							index++;
						}
						return true;
					}
				}.renderPage(args.length > finalPageIndex ? args[finalPageIndex] : null);

				sync(display::show);
			})
		);
	}

	/**
	 * Send out vote reminders (to be called async)
	 */
	private void voteReminder() {
		Database.run(session -> {
			List<Player> toMessage = new ArrayList<>();
			for(Player player : new HashSet<>(Bukkit.getOnlinePlayers())) {
				Date lastVoted = session.createQuery("" +
						"select max(vote.at) " +
						"from " +
						"Vote as vote " +
						"where " +
						"vote.gcPlayer = :player ", Date.class)
						.setParameter("player", Database.getPlayer(player.getUniqueId(), player.getName()))
						.getSingleResult();

				// Add to message list if not voted for a day
				Date dayAgo = new Date(Calendar.getInstance().getTimeInMillis() - 1000*60*60*24);
				if(lastVoted == null || lastVoted.before(dayAgo)) {
					toMessage.add(player);
				}
			}

			Message message = Message.fromKey("vote-now");
			Do.sync(() -> toMessage.forEach(message::send));
		});
	}

	/**
	 * Get the start of the month
	 * @param at Date to get the start from
	 * @return Start of the month
	 */
	public static Date getMonthStart(Calendar at) {
		at.set(Calendar.DAY_OF_MONTH, at.getActualMinimum(Calendar.DAY_OF_MONTH));
		at.set(Calendar.HOUR_OF_DAY, 0);
		at.set(Calendar.MINUTE, 0);
		at.set(Calendar.SECOND, 0);
		at.set(Calendar.MILLISECOND, 0);
		return at.getTime();
	}

	/**
	 * Get the start of the current month
	 * @return Start of the current month
	 */
	public static Date getMonthStart() {
		return getMonthStart(Calendar.getInstance());
	}

	/**
	 * Get the end of the month
	 * @param at Date to get the end from
	 * @return End of the month
	 */
	public static Date getMonthEnd(Calendar at) {
		at.set(Calendar.DAY_OF_MONTH, at.getActualMaximum(Calendar.DAY_OF_MONTH));
		at.set(Calendar.HOUR_OF_DAY, 23);
		at.set(Calendar.MINUTE, 59);
		at.set(Calendar.SECOND, 59);
		at.set(Calendar.MILLISECOND, 999);
		return at.getTime();
	}

	/**
	 * Get the end of the current month
	 * @return End of the current month
	 */
	public static Date getMonthEnd() {
		return getMonthEnd(Calendar.getInstance());
	}

}
