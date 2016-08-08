package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.messages.Message;
import me.wiefferink.gocraft.tools.Utils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class Rewards extends Feature {

	public Rewards() {
		if(getRewardsSection() == null) {
			return;
		}
		listen();

		// Give currently online people rewards (after a reload for example)
		for(Player player : Bukkit.getOnlinePlayers()) {
			giveRewards(player);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
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
		ConfigurationSection rewardsSection = getRewardsSection();
		List<String> groups = Arrays.asList(GoCraft.getInstance().getPermissionProvider().getPlayerGroups(player));
		for(String key : rewardsSection.getKeys(false)) {
			List<String> targets = Utils.listOrSingle(rewardsSection, key+".target");
			if(targets == null || targets.isEmpty() || GoCraft.getInstance().getLocalStorage().getBoolean("players."+player.getUniqueId().toString()+".rewards."+key)) {
				continue;
			}
			boolean matches = false;
			// Player should match one of the target lines
			for(String target : targets) {
				String[] parts = target.split(", ");
				boolean and = true;
				// Player should match all parts of a target line
				for(String part : parts) {
					if(part.startsWith("rank:") && part.length() > 5) {
						String rank = part.substring(5);
						and &= groups.contains(rank);
					} else if(part.startsWith("uuid:") && part.length() > 5) {
						String uuid = part.substring(5);
						and &= player.getUniqueId().toString().equals(uuid);
					} else {
						GoCraft.warn("Incorrect part '"+part+"' in rewards section "+key);
					}
				}
				matches |= and;
			}
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
						GoCraft.warn("Failed to run command of reward", key, "for player", player.getName()+":", command, stacktrace);
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
			GoCraft.getInstance().getLocalStorage().set("players."+player.getUniqueId().toString()+".rewards."+key, true);
		}
	}

	/**
	 * Get the configurations section that stores the rewards
	 * @return The configurationsection
	 */
	private ConfigurationSection getRewardsSection() {
		return GoCraft.getInstance().getConfig().getConfigurationSection("rewards");
	}

}



















