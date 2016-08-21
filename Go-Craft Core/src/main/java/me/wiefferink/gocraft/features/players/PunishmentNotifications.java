package me.wiefferink.gocraft.features.players;

import me.confuser.banmanager.events.*;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.event.EventHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PunishmentNotifications extends Feature {

	public PunishmentNotifications() {
		listen("enablePunishmentNotifications");
	}

	@EventHandler(ignoreCancelled = true)
	public void onBan(PlayerBannedEvent event) {
		String untilPart = "";
		if (event.getBan().getExpires() != 0) {
			untilPart = " until " + timeToString(event.getBan().getExpires() * 1000);
		}
		Utils.sendStaffMessage("Ban", event.getBan().getPlayer().getName() + untilPart + " by " + event.getBan().getActor().getName() + ": " + event.getBan().getReason());
	}

	@EventHandler(ignoreCancelled = true)
	public void onUnban(PlayerUnbanEvent event) {
		Utils.sendStaffMessage("Unban", event.getBan().getPlayer().getName() + " (was banned for: " + event.getBan().getReason() + ")");
	}

	@EventHandler(ignoreCancelled = true)
	public void onMute(PlayerMutedEvent event) {
		String untilPart = "";
		if (event.getMute().getExpires() != 0) {
			untilPart = " until " + timeToString(event.getMute().getExpires() * 1000);
		}
		Utils.sendStaffMessage("Mute", event.getMute().getPlayer().getName() + untilPart + " by " + event.getMute().getActor().getName() + ": " + event.getMute().getReason());
	}

	@EventHandler(ignoreCancelled = true)
	public void onUnmute(PlayerUnmuteEvent event) {
		Utils.sendStaffMessage("Unmute", event.getMute().getPlayer().getName() + " (was muted for: " + event.getMute().getReason() + ")");
	}

	@EventHandler(ignoreCancelled = true)
	public void onWarn(PlayerWarnedEvent event) {
		Utils.sendStaffMessage("Warn", event.getWarning().getPlayer().getName() + " by " + event.getWarning().getActor().getName() + ": " + event.getWarning().getReason());
	}

	private String timeToString(long time) {
		SimpleDateFormat date = new SimpleDateFormat("dd-MM HH:mm");
		return date.format(new Date(time));
	}
}
