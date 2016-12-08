package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OldHunger extends Feature {

	public static final long TIME_BETWEEN_HEAL = 3900; // Milliseonds between food regens
	private Map<UUID, Long> lastRegens;

	public OldHunger() {
		if(listen("oldHunger")) {
			lastRegens = new HashMap<>();
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHealthRegen(EntityRegainHealthEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}

		Player player = (Player)event.getEntity();
		//GoCraft.debug("Health regen of", player.getName(), "amount:", event.getAmount(), "cause:", event.getRegainReason());

		if(event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
			return;
		}

		long lastRegen = 0;
		if(lastRegens.containsKey(player.getUniqueId())) {
			lastRegen = lastRegens.get(player.getUniqueId());
		}

		long now = Calendar.getInstance().getTimeInMillis();
		if((now-lastRegen) < TIME_BETWEEN_HEAL) {
			event.setCancelled(true);
		} else {
			event.setAmount(1); // Saturation based regen has an amount between 0.2 an 1 based on how saturated you are
			lastRegens.put(player.getUniqueId(), now);
		}
	}
}
