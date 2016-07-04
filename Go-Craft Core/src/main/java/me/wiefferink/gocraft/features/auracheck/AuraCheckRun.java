package me.wiefferink.gocraft.features.auracheck;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.packetwrapper.WrapperPlayServerEntityDestroy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map.Entry;

public class AuraCheckRun {
	private AuraCheck manager;
	private HashMap<Integer, Boolean> entitiesSpawned = new HashMap<>();
	private CommandSender invoker;
	private Player checked;
	private Callback callback;
	private long started;
	private long finished = 9223372036854775807L;
	private boolean allKilled = false;

	public AuraCheckRun(AuraCheck manager, Player checked) {
		this.manager = manager;
		this.checked = checked;
	}

	/**
	 * Kill an fake player entity
	 * @param entityId The id of the entity to kill
	 */
	public void kill(int entityId) {
		if (!checked.isOnline()) {
			return;
		}
		WrapperPlayServerEntityDestroy wrapper = new WrapperPlayServerEntityDestroy();
		wrapper.setEntityIds(new int[]{entityId});
		wrapper.sendPacket(checked);
	}

	/**
	 * Start the check
	 * @param player The player that wants to receive the result
	 * @param callback The callback to call with the result
	 */
	public void start(CommandSender player, Callback callback) {
		manager.addCheck(checked.getUniqueId(), this);
		invoker = player;
		started = System.currentTimeMillis();
		this.callback = callback;

		int numPlayers = GoCraft.getInstance().getConfig().getInt("auracheck.numberOfFakePlayers");
		for (int i = 1; i <= numPlayers; i++) {
			int degrees = 360 / (numPlayers - 1) * i;
			double radians = Math.toRadians(degrees);
			Location location;
			if (i == 1) {
				location = checked.getLocation().add(0.0D, 2.0D, 0.0D);
			} else {
				location = checked.getLocation().add(2.0D * Math.cos(radians), 0.2D, 2.0D * Math.sin(radians));
			}
			int entityId = GoCraft.getInstance().getSpecificUtils().sendFakePlayer(location, checked, GoCraft.getInstance().getConfig().getBoolean("auracheck.visiblePlayers"), Utils.randomName());
			entitiesSpawned.put(entityId, false);
		}
		final AuraCheckRun self = this;
		Bukkit.getScheduler().runTaskLater(GoCraft.getInstance(), new Runnable() {
			public void run() {
				self.end();
			}
		}, GoCraft.getInstance().getConfig().getInt("auracheck.ticksToKill", 10));
	}

	/**
	 * Mark an player as killed
	 * @param entityId The id of the player to mark as killed
	 */
	public void markAsKilled(Integer entityId) {
		if (entitiesSpawned.containsKey(entityId)) {
			entitiesSpawned.put(entityId, true);
			kill(entityId);
		}
		if (!entitiesSpawned.containsValue(false)) {
			finished = System.currentTimeMillis();
		}
	}

	/**
	 * Kill all fake players, and call the callback when results are in
	 */
	public void end() {
		if (!allKilled) {
			allKilled = true;
			for (Entry<Integer, Boolean> entry : entitiesSpawned.entrySet()) {
				if (!entry.getValue()) {
					kill(entry.getKey());
				}
			}
			// Give the player a second to send back hit packets
			new BukkitRunnable() {
				@Override
				public void run() {
					wrapup();
				}
			}.runTaskLater(GoCraft.getInstance(), 40L);
		}
	}

	/**
	 * End the killaura check (calling the callback)
	 */
	public void wrapup() {
		int killed = 0;
		for (Entry<Integer, Boolean> entry : entitiesSpawned.entrySet()) {
			if (entry.getValue()) {
				killed++;
			} else if (!allKilled) {
				kill(entry.getKey());
			}
		}
		int amount = entitiesSpawned.size();
		entitiesSpawned.clear();
		manager.removeCheck(checked.getUniqueId());
		callback.done(new AuraCheckRunResult(started, finished, killed, amount, invoker, checked));
	}

	/**
	 * Callback interface to perform a callback after the check is done
	 */
	public interface Callback {
		/**
		 * Called when the auracheck is done
		 * @param result The result of the auracheck
		 */
		void done(AuraCheckRunResult result);
	}

	public class AuraCheckRunResult {
		public long startTime = 0;
		public long endTime = 0;
		public int killed = 0;
		public int spawned = 0;
		public CommandSender invoker = null;
		public Player checked = null;

		public AuraCheckRunResult(long startTime, long endTime, int killed, int spawned, CommandSender invoker, Player checked) {
			this.startTime = startTime;
			this.endTime = endTime;
			this.killed = killed;
			this.spawned = spawned;
			this.invoker = invoker;
			this.checked = checked;
		}
	}
}