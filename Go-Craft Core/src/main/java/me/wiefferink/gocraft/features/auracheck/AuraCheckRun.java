package me.wiefferink.gocraft.features.auracheck;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.Callback;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.packetwrapper.WrapperPlayServerEntityDestroy;
import me.wiefferink.gocraft.tools.scheduling.Do;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class AuraCheckRun {
	private AuraCheck manager;
	private HashMap<Integer, Boolean> entitiesSpawned;
	private HashMap<UUID, Set<Integer>> inspectEntities;
	private CommandSender invoker;
	private Player checked;
	private Callback<AuraCheckRunResult> callback;
	private long started;
	private long finished = 9223372036854775807L;
	private boolean allKilled = false;
	private boolean ended = false;

	public AuraCheckRun(AuraCheck manager, Player checked) {
		this.manager = manager;
		this.checked = checked;
		entitiesSpawned = new HashMap<>();
		inspectEntities = new HashMap<>();
	}

	/**
	 * Kill an fake player entity
	 * @param entityId The id of the entity to kill
	 */
	public void kill(int entityId, Player player) {
		if (!player.isOnline()) {
			return;
		}
		WrapperPlayServerEntityDestroy wrapper = new WrapperPlayServerEntityDestroy();
		wrapper.setEntityIds(new int[]{entityId});
		wrapper.sendPacket(player);
	}

	/**
	 * Start the check
	 * @param player The player that wants to receive the result
	 * @param callback The callback to call with the result
	 */
	public void start(CommandSender player, Callback<AuraCheckRunResult> callback) {
		manager.addCheck(checked.getUniqueId(), this);
		invoker = player;
		started = System.currentTimeMillis();
		this.callback = callback;

		int numPlayers = GoCraft.getInstance().getConfig().getInt("auracheck.numberOfFakePlayers");

		int baseDegrees = 360 / (numPlayers - 1);
		int degreeOffset = (int) (Utils.random.nextFloat() * baseDegrees);
		for (int i = 1; i <= numPlayers; i++) {
			int degrees = baseDegrees * i + degreeOffset;
			double radians = Math.toRadians(degrees);
			Location location;
			if (i == 1) {
				location = checked.getLocation().add(0.0D, 2.0D, 0.0D);
			} else {
				location = checked.getLocation().add(2.0D * Math.cos(radians), 0.2D, 2.0D * Math.sin(radians));
			}
			String name = Utils.randomName();
			int entityId = GoCraft.getInstance().getSpecificUtils().sendFakePlayer(location, checked, GoCraft.getInstance().getConfig().getBoolean("auracheck.visiblePlayers"), name);
			entitiesSpawned.put(entityId, false);
			// Spawn fake visible players for inspector
			for (Inspection inspection : GoCraft.getInstance().getInspectionManager().getInspectionsByInspected(checked)) {
				int inspectEntityId = GoCraft.getInstance().getSpecificUtils().sendFakePlayer(location, inspection.getInspector(), true, name);
				UUID uuid = inspection.getInspector().getUniqueId();
				inspectEntities
						.computeIfAbsent(uuid, key -> new HashSet<>())
						.add(inspectEntityId);
			}
		}
		final AuraCheckRun self = this;
		Bukkit.getScheduler().runTaskLater(GoCraft.getInstance(), self::end, GoCraft.getInstance().getConfig().getInt("auracheck.ticksToKill", 10));
	}

	/**
	 * Mark an player as killed
	 * @param entityId The id of the player to mark as killed
	 */
	public void markAsKilled(Integer entityId) {
		if (ended) {
			return;
		}
		if (entitiesSpawned.containsKey(entityId)) {
			entitiesSpawned.put(entityId, true);
			kill(entityId, checked);
		}
		if (!entitiesSpawned.containsValue(false)) {
			finished = System.currentTimeMillis();
		}
	}

	/**
	 * Kill all fake players, and call the callback when results are in
	 */
	public void end() {
		if (ended) {
			return;
		}
		if (!allKilled) {
			allKilled = true;
			for (Entry<Integer, Boolean> entry : entitiesSpawned.entrySet()) {
				if (!entry.getValue()) {
					kill(entry.getKey(), checked);
				}
			}
			killInspectorEntities();
			// Give the player time to send back hit packets (at least 3 ticks, or 3 times his ping)
			Do.syncLater(Math.max(3, Utils.getPing(checked) * 20 * 3 / 1000), this::wrapup);
		}
	}

	/**
	 * End the killaura check (calling the callback)
	 */
	public void wrapup() {
		if (ended) {
			return;
		}
		int killed = 0;
		for (Entry<Integer, Boolean> entry : entitiesSpawned.entrySet()) {
			if (entry.getValue()) {
				killed++;
			} else if (!allKilled) {
				kill(entry.getKey(), checked);
			}
		}
		killInspectorEntities();
		int amount = entitiesSpawned.size();
		entitiesSpawned.clear();
		manager.removeCheck(checked.getUniqueId());
		callback.execute(new AuraCheckRunResult(started, finished, killed, amount, invoker, checked));
		ended = true;
	}

	/**
	 * Kill the fake entities displayed to the inspectors
	 */
	private void killInspectorEntities() {
		// Kill fake players for inspectors
		for (UUID uuid : inspectEntities.keySet()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null && player.isOnline()) {
				for (int entityId : inspectEntities.get(uuid)) {
					kill(entityId, player);
				}
			}
		}
		inspectEntities.clear();
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
