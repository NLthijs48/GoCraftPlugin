package me.wiefferink.gocraft.inspector;

import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.GoCraft;
import org.bukkit.GameMode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class UpdateListener implements Listener {
	private final GoCraft plugin;
	public static final boolean running = false;

	public UpdateListener(GoCraft plugin) {
		this.plugin = plugin;
	}

	// Update scoreboard when taking damage
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onPlayerDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			final Player player = (Player) event.getEntity();
			// After damage is applied
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Inspection inspection : plugin.getInspectionManager().getInspectionsByInspected(player)) {
						inspection.updateScoreboard();
					}
				}
			}.runTaskLater(plugin, 1L);
			// Prevent damage to inspector
			Inspection inspection = plugin.getInspectionManager().getInspectionByInspector(player);
			if (inspection != null) {
				event.setCancelled(true);
				inspection.teleportToSpawn();
				player.setHealth(player.getMaxHealth());
				player.setFoodLevel(20);
				plugin.message(player, "inspect-damage");
			}
		}
	}

	// Update after respawn
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onRespawn(PlayerRespawnEvent event) {
		final Player finalPlayer = event.getPlayer();
		Do.sync(() -> {
			for(Inspection inspection : plugin.getInspectionManager().getInspectionsByInspected(finalPlayer)) {
				inspection.updateAll();
				inspection.teleportToInspected();
			}
		});
	}

	// Update when regaining health
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onHealthRegen(EntityRegainHealthEvent event) {
		handleEntityEvent(event);
	}

	private void handleEntityEvent(EntityEvent event) {
		if(!(event.getEntity() instanceof Player)) {
			return;
		}

		if (!plugin.getInspectionManager().getInspectionsByInspected((Player) event.getEntity()).isEmpty()) {
			Player player = (Player) event.getEntity();
			Do.sync(() -> plugin.getInspectionManager().getInspectionsByInspected(player)
					.forEach(Inspection::updateScoreboard));
		}
	}

	// Update on change of food level
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onFoodChange(FoodLevelChangeEvent event) {
		handleEntityEvent(event);
	}

	// Update on potion change
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		Set<Player> players = new HashSet<>();
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				players.add((Player) entity);
			}
		}
		if (!players.isEmpty()) {
			final Set<Player> finalPlayers = players;
			Do.sync(() -> finalPlayers
				.forEach(player -> plugin.getInspectionManager().getInspectionsByInspected(player)
					.forEach(Inspection::updateInventory)
				)
			);
		}
	}

	// Update on item breakage
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onItemBreak(PlayerItemBreakEvent event) {
		if (!plugin.getInspectionManager().getInspectionsByInspected(event.getPlayer()).isEmpty()) {
			final Player finalPlayer = event.getPlayer();
			Do.sync(() -> plugin.getInspectionManager().getInspectionsByInspected(finalPlayer)
				.forEach(Inspection::updateArmor)
			);
		}
	}

	// Disable sign clicking when in inspect mode
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onSignClick(PlayerInteractEvent event) {
		if (!plugin.getInspectionManager().isInspecting(event.getPlayer())) {
			return;
		}
		boolean clickingBlock = event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK;
		event.setCancelled(clickingBlock);
	}

	// Teleport inspectors to their target if the target teleports
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onTargetTeleport(PlayerTeleportEvent event) {
		// Ignore cancelled events and teleport of less than 3 blocks
		if (event.isCancelled() || (event.getFrom().getWorld().equals(event.getTo().getWorld()) && event.getFrom().distanceSquared(event.getTo()) < 9)) {
			return;
		}
		Player player = event.getPlayer();
		for (final Inspection inspection : plugin.getInspectionManager().getCurrentInspections().values()) {
			// Check if the inspection has the same target as the event
			if (inspection.hasInspected() && inspection.getInspected().getUniqueId().equals(player.getUniqueId())) {
				Do.sync(() -> {
					inspection.teleportToInspected();
					plugin.message(inspection.getInspector(), "inspect-teleportedToInspected", inspection.getInspected().getName());
				});
			}
		}
	}

	// Ensure flying is always on by cancelling toggling it off
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onToggleFlight(PlayerToggleFlightEvent event) {
		if (!event.isFlying() && event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
			event.setCancelled(true);
		}
	}

	// Ensure flying is on by reenabling it immediately
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onToggleFlightMonitor(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		if (!event.isFlying() && !event.isCancelled() && event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
			Do.sync(() -> {
				if(player.isOnline()) {
					player.setFlying(true);
				}
			});
		}
	}
}
























