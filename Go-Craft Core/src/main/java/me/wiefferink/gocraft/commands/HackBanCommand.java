package me.wiefferink.gocraft.commands;

import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.internal.ormlite.dao.CloseableIterator;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.scheduling.Do;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Calendar;

public class HackBanCommand extends Feature {

	public HackBanCommand() {
		permission("hackban", "Use the /hackban command");
		command("hackban", "Ban a hacker", "/hackban <player> [other reason]", "pvpban");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.hackban")) {
			plugin.message(sender, "hackban-noPermission");
			return;
		}

		if(args.length < 1) {
			plugin.message(sender, "hackban-help");
			return;
		}

		String reason = "Hacking is forbidden!";
		if(args.length > 1) {
			reason = Utils.combineFrom(args, 1, " ");
		}

		// Execute commands
		String target = args[0];
		Log.info("[HackBan]", sender.getName(), "is executing a /hackban on '"+target+"':");
		Player player = Bukkit.getPlayer(target);
		if(player != null) {
			target = player.getName();
			Log.info("  IP address of '"+target+"' is "+player.getAddress().getHostName());
		}

		boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pvp stats "+target);
		success &= Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill "+target);
		success &= Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pvp resetstats "+target);
		success &= Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco reset "+target);
		final boolean finalSuccess = success;
		final String finalTarget = target;
		final String finalReason = reason;
		Do.async(() -> {
			boolean success2 = true;
			long toBan = 0;
			String newReason = "";
			try {
				PlayerBanData currentBan = BmAPI.getCurrentBan(finalTarget);
				if(currentBan != null) {
					plugin.message(sender, "hackban-alreadyBanned", finalTarget);
					return;
				}

				OfflinePlayer offlineTarget = Bukkit.getOfflinePlayer(finalTarget);
				PlayerData playerData = BmAPI.getPlayer(offlineTarget.getUniqueId());
				if(playerData == null) {
					plugin.message(sender, "hackban-noPlayerData", finalTarget);
					return;
				}

				// Calculate bantime
				int timesBanned = 0;
				CloseableIterator<PlayerBanRecord> records = BmAPI.getBanRecords(playerData);
				while(records.hasNext()) {
					PlayerBanRecord ban = records.next();
					Log.debug("  banrecord: createdReason: " + ban.getCreatedReason() + ", createdTime: " + ban.getCreated() + ", reason: " + ban.getReason() + ", expired: " + ban.getExpired() + ", id: " + ban.getId() + ", pastCreatedTime" + ban.getPastCreated());
					if((ban.getExpired() - ban.getPastCreated()) > 86400 // Longer than 1 day
							&& ban.getPastCreated() > 1446336000) { // Newer than 1-11-2015 0:00
						timesBanned++;
						Log.debug("    it counted");
					}
				}
				double multiplier = Math.pow(2, timesBanned); // Amount of times the player is banned for 7 days
				toBan = (long) (multiplier * 604800.0); // 7 days ban times multiplier
				long expires = Calendar.getInstance().getTimeInMillis() / 1000 + toBan;
				newReason = finalReason;
				if(timesBanned != 0) {
					newReason += " [ban #" + (timesBanned + 1) + "]";
				}
				Log.debug("timesBanned=" + timesBanned + ", multiplier=" + multiplier + ", toBan=" + toBan + ", expires=" + expires);

				// Ban by name and ip
				PlayerData actorData;
				if(sender instanceof Player) {
					actorData = BmAPI.getPlayer((Player) sender);
				} else {
					actorData = BmAPI.getConsole();
				}
				BmAPI.ban(playerData, actorData, newReason, expires, true);
				BmAPI.ban(playerData.getIp(), actorData, newReason, expires, true);
			} catch(SQLException e) {
				Log.warn("Something went wrong while executing hackban:", ExceptionUtils.getStackTrace(e));
				success2 = false;
			}

			final String finalNewReason = newReason;
			final boolean finalSuccess2 = success2;
			final long finalToBan = toBan;
			Do.syncLater(20, () -> {
				Player targetPlayer = Bukkit.getPlayer(finalTarget);
				if(targetPlayer != null && targetPlayer.isOnline()) {
					targetPlayer.kickPlayer(finalNewReason);
				}
				if(finalSuccess && finalSuccess2) {
					plugin.message(sender, "hackban-finished", finalTarget, Utils.millisToHumanFormat(finalToBan * 1000));
					GoCraft.getInstance().increaseStatistic("command.hackban.success");
				} else {
					plugin.message(sender, "hackban-failed");
					GoCraft.getInstance().increaseStatistic("command.hackban.failed");
				}
			});
		});
	}
}
