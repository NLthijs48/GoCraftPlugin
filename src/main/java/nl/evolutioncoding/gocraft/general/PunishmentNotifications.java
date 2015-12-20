package nl.evolutioncoding.gocraft.general;

import me.confuser.banmanager.events.*;
import nl.evolutioncoding.gocraft.GoCraft;
import nl.evolutioncoding.gocraft.utils.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class PunishmentNotifications implements Listener {

	private GoCraft plugin;

	public PunishmentNotifications(GoCraft plugin) {
		this.plugin = plugin;
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void onBan(PlayerBannedEvent event) {
		Utils.displayStaffMessage("Ban", event.getBan().getPlayer().getName()+" until "+timeToString(event.getBan().getExpires())+" by "+event.getBan().getActor().getName()+": "+event.getBan().getReason());
	}

	@EventHandler
	public void onUnban(PlayerUnbanEvent event) {
		Utils.displayStaffMessage("Unban", event.getBan().getPlayer().getName()+" (was banned for: "+event.getBan().getReason()+")");
	}

	@EventHandler
	public void onMute(PlayerMutedEvent event) {
		Utils.displayStaffMessage("Mute", event.getMute().getPlayer().getName()+" until "+timeToString(event.getMute().getExpires())+" by "+event.getMute().getActor().getName()+": "+event.getMute().getReason());
	}

	@EventHandler
	public void onUnmute(PlayerUnmuteEvent event) {
		Utils.displayStaffMessage("Unmute", event.getMute().getPlayer().getName()+" (was muted for: "+event.getMute().getReason()+")");
	}

	@EventHandler
	public void onWarn(PlayerWarnedEvent event) {
		Utils.displayStaffMessage("Warn", event.getWarning().getPlayer().getName()+" by "+event.getWarning().getActor().getName()+": "+event.getWarning().getReason());
	}

	private String timeToString(long time) {
		SimpleDateFormat date = new SimpleDateFormat("dd-MM HH:mm");
		return date.format(new Date(time));
	}
}