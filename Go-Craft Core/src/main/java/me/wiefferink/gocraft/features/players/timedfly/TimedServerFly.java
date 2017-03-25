package me.wiefferink.gocraft.features.players.timedfly;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

public class TimedServerFly extends Feature {

	public TimedServerFly() {
		command("addtimedfly", "Add a timed reward purchaced by a player", "/addtimedfly <player> <duration amount> <duration identifier>");
		permission("timedfly", "Add timed rewards to players", PermissionDefault.OP);
		listen();
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.timedfly")) {
			plugin.message(sender, "timedfly-noPermission");
			return;
		}

		if(args.length < 3) {
			plugin.message(sender, "timedfly-help");
			return;
		}

		long duration = Utils.durationStringToLong(args[1]+" "+args[2]);
		if(duration <= 0) {
			plugin.message(sender, "timedfly-wrongDuration", args[1]+" "+args[2]);
			return;
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[0]);
		if(offlinePlayer == null || offlinePlayer.getUniqueId() == null || offlinePlayer.getName() == null) {
			plugin.message(sender, "timedfly-wrongPlayer", args[0]);
			return;
		}

		async(() -> {
			// Save new fly reward
			database(session -> {
				GCPlayer buyer = Database.getCreatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName());
				TimedFly fly = new TimedFly(buyer, duration, plugin.getServerId());
				session.save(fly);
			});

			// Execute rewards
			checkRewards();
		});
	}

	private void checkRewards() {
		// TODO get and check rewards
	}

}
