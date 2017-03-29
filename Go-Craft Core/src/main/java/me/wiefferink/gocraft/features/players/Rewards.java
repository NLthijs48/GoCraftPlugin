package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class Rewards extends Feature {

	public Rewards() {
		if(getRewardsSection() != null && listen()) {
			// Give currently online people rewards (after a reload for example)
			for(Player player : Bukkit.getOnlinePlayers()) {
				giveRewards(player);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(!inWorld(event)) {
			return;
		}
		Player player = event.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				if(player != null && player.isOnline()) {
					giveRewards(player);
				}
			}
		}.runTaskLater(GoCraft.getInstance(), 40L);
	}

	/**
	 * Check if the player should still receive rewards and give them
	 * @param player The player to check for
	 */
	public void giveRewards(Player player) {
		if(!inWorld(player)) {
			return;
		}
		ConfigurationSection rewardsSection = getRewardsSection();
		List<String> groups = Arrays.asList(GoCraft.getInstance().getPermissionProvider().getPlayerGroups(player));
		for(String key : rewardsSection.getKeys(false)) {
			if(GoCraft.getInstance().getLocalStorage().isSet("players."+player.getUniqueId().toString()+".rewards."+key)) {
				continue;
			}
			boolean matches;
			// Check permission
			String permission = rewardsSection.getString(key+".permission");
			matches = (permission != null && player.hasPermission(permission));

			// Check rank match
			List<String> ranks = Utils.listOrSingle(rewardsSection, key+".ranks");
			if(ranks != null) {
				for(String rank : ranks) {
					matches |= groups.contains(rank);
				}
			}

			// Check uuid match
			List<String> uuids = Utils.listOrSingle(rewardsSection, key+".uuids");
			matches |= (uuids != null && uuids.contains(player.getUniqueId().toString()));
			if(!matches) {
				continue;
			}

			// Execute commands
			List<String> commands = Utils.listOrSingle(rewardsSection, key+".commands");
			if(commands != null) {
				for(String command : commands) {
					command = command.replace("%player%", player.getName());
					command = command.replace("%uuid%", player.getUniqueId().toString());
					boolean success;
					String stacktrace = null;
					try {
						success = plugin.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
					} catch(CommandException e) {
						success = false;
						stacktrace = ExceptionUtils.getStackTrace(e);
					}
					if(!success) {
						Log.warn("Failed to run command of reward", key, "for player", player.getName()+":", command, stacktrace);
					}
				}
			}

			// Send message
			List<String> messageList = Utils.listOrSingle(rewardsSection, key+".message");
			if(messageList != null && !messageList.isEmpty()) {
				// Apply replacements
				for(int i = 0; i < messageList.size(); i++) {
					messageList.set(i, messageList.get(i).replace("%player%", player.getName()));
					messageList.set(i, messageList.get(i).replace("%uuid%", player.getUniqueId().toString()));
				}
				// Send message
				Message.fromList(messageList).send(player);
			}

			// Record that reward is given
			SimpleDateFormat time = new SimpleDateFormat(getConfig().getString("signLogTimeFormat"));
			String timeString = time.format(Calendar.getInstance().getTimeInMillis());
			plugin.getLocalStorage().set("players."+player.getUniqueId().toString()+".rewards."+key, timeString);
			plugin.saveLocalStorage();
			Log.info("[Rewards]", player.getName(), "received reward", key);
		}
	}

	/**
	 * Get the configurations section that stores the rewards
	 * @return The configurationsection
	 */
	private ConfigurationSection getRewardsSection() {
		return getConfig().getConfigurationSection("rewards");
	}

}



















