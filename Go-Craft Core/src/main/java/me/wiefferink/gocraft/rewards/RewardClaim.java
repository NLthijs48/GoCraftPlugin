package me.wiefferink.gocraft.rewards;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;

public class RewardClaim extends Feature {

	public RewardClaim() {
		command("rewards", "Claim pending rewards");
		listen();
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}

		Do.async(() -> giveRewards((Player)sender, true));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Do.asyncLater(20, () -> giveRewards(event.getPlayer(), false));
	}

	@EventHandler
	public void voteEvent(VotifierEvent event) {
		// Delay a bit to make sure the lobby has put the rewards in the database
		Do.syncLater(20, () -> {
			Player player = Bukkit.getPlayer(event.getVote().getUsername());
			if(player != null) {
				Do.async(() -> giveRewards(player, false));
			}
		});
	}

	/**
	 * Find and give rewards to the player
	 * @param player Player to lookup the rewards for and give them
	 */
	public void giveRewards(Player player, boolean notifyNone) {
		if(!player.isOnline()) {
			return;
		}

		List<Reward> pendingRewards = new ArrayList<>();
		Database.run(session -> {
			GCPlayer gcPlayer = Database.getCreatePlayer(player.getUniqueId(), player.getName());

			// Get pending rewards
			pendingRewards.addAll(session.createQuery("FROM Reward WHERE gcPlayer = :player AND completed = false AND failed = false AND server = :server ORDER BY at ASC", Reward.class)
					.setParameter("player", gcPlayer)
					.setParameter("server", plugin.getServerId())
					.setMaxResults(100)
					.getResultList());
		});

		sync(() -> {
			// Cancel if offline
			if(!player.isOnline()) {
				return;
			}

			// Check if any rewards to give
			if(pendingRewards.isEmpty()) {
				if(notifyNone) {
					plugin.message(player, "rewards-none");
				}
				return;
			}

			// Hand out rewards
			for(Reward reward : pendingRewards) {
				switch(reward.getType()) {
					case MONEY:
						double money = reward.getMoney();
						if(money > 0) {
							plugin.getEconomy().depositPlayer(player, money);
							reward.complete();
						} else {
							reward.failed();
							Log.warn("Zero or negative money reward:", reward);
						}
						break;
					case COMMAND:
						String rewardCommand = reward.getCommand();
						if(rewardCommand != null && !rewardCommand.isEmpty()) {
							// TODO check requiredSlots (don't mark as failed when not met, just postpone for next check)
							rewardCommand = rewardCommand.replace("{player}", player.getName());
							boolean commandResult = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand);
							if(commandResult) {
								reward.complete();
							} else {
								reward.failed();
								Log.warn("Failed to execute reward command:", rewardCommand, "player:", player.getName(), "reward:", reward);
							}
						} else {
							reward.failed();
							Log.warn("Missing or empty command:", reward);
						}
						break;
					default:
						Log.warn("Don't know how to give reward of type", reward.getType());
						reward.failed();
				}

				String message = reward.getMessage();
				message = message.replace("{player}", player.getName());
				if(message != null && !message.isEmpty()) {
					plugin.message(player, "rewards-message", message);
				}
			}

			// Mark as complete
			async(() ->
				Database.run(session ->
					pendingRewards.forEach(session::update)
				)
			);
		});
	}
}
