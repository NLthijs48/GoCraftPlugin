package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.storage.Database;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class FixGCPlayer extends Feature {

	public FixGCPlayer() {
		listen();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		// Check if name and uuid are still consistent
		async(() ->
			database(session -> {
				String name = event.getPlayer().getName();
				UUID uuid = event.getPlayer().getUniqueId();
				GCPlayer player = Database.getPlayer(name, uuid);
				if(player == null) {
					Log.warn("Joining player does not have GCPlayer:", name, uuid);
					return;
				}
				String oldName = player.getName();
				String oldUuid = player.getUniqueId();

				// Update name and uuid if not the same
				if(!name.equals(oldName) || !uuid.toString().equals(oldUuid)) {
					player.setName(name);
					player.setUniqueId(uuid.toString());
					session.update(player);
					Log.warn("Corrected GCPlayer uuid/name from", oldName, oldUuid, "to", player);
				}
			})
		);
	}

}
