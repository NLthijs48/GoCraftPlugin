package me.wiefferink.gocraft.features.auracheck;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.VoidCommandSender;
import me.wiefferink.gocraft.tools.packetwrapper.WrapperPlayClientUseEntity;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static me.wiefferink.gocraft.tools.Utils.random;

public class AuraCheck extends Feature {

	private HashMap<UUID, AuraCheckRun> running;
	private boolean isRegistered;
	private static AuraCheck self;

	public AuraCheck() {
		permission("auracheck", "Allow checking players for kill aura");
		command("auracheck", "Check if ap player has kill aura hacks", "/auracheck <player>", "ac");
		listen();

		random = new Random();
		running = new HashMap<>();
		self = this;
		if (GoCraft.getInstance().getConfig().getBoolean("auracheck.enablePeriodicCheck")) {
			Do.syncLater(getNextPeriodicDelay(), this::performAllPlayerCheck);
		}
	}

	@Override
	public void stop() {
		ArrayList<UUID> list = new ArrayList<>(running.keySet()); // Prevent concurrent modification
		for (UUID uuid : list) {
			AuraCheckRun check = removeCheck(uuid);
			check.wrapup();
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onDisconnect(PlayerQuitEvent event) {
		AuraCheckRun check = removeCheck(event.getPlayer().getUniqueId());
		if (check != null) {
			check.wrapup();
		}
	}

	/**
	 * Register a packet listener if it has not been registered yet
	 */
	public void register() {
		if(!isRegistered && running.size() > 0) {
			ProtocolLibrary.getProtocolManager().addPacketListener(
					new PacketAdapter(GoCraft.getInstance(), WrapperPlayClientUseEntity.TYPE) {
						public void onPacketReceiving(PacketEvent event) {
							if(event.getPacketType() == WrapperPlayClientUseEntity.TYPE) {
								WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());
								int entID = packet.getTarget();
								if(packet.getType().equals(EnumWrappers.EntityUseAction.ATTACK)) {
									AuraCheckRun run = running.get(event.getPlayer().getUniqueId());
									if(run != null) {
										run.markAsKilled(entID);
									}
								}
							}
						}
					});
			this.isRegistered = true;
		}
	}

	/**
	 * Unregister the packet listener
	 */
	public void unregister() {
		if(this.running.size() == 0 && isRegistered) {
			// TODO Make this compatible with possible other packet listeners
			ProtocolLibrary.getProtocolManager().removePacketListeners(GoCraft.getInstance());
			isRegistered = false;
		}
	}

	/**
	 * Remove an auracheck run
	 * @param id The id to remove
	 * @return The removed AuraCheckRun
	 */
	public AuraCheckRun removeCheck(UUID id) {
		AuraCheckRun result = running.remove(id);
		unregister();
		return result;
	}

	/**
	 * Add an AuraCheckRun
	 * @param player The player to add it for
	 * @param check The check to add
	 */
	public void addCheck(UUID player, AuraCheckRun check) {
		AuraCheckRun previous = running.get(player);
		if(previous != null) {
			previous.wrapup();
		}
		running.put(player, check);
		register();
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			plugin.message(sender, "ac-help");
			return;
		}
		if(!sender.hasPermission("gocraft.auracheck")) {
			plugin.message(sender, "ac-noPermission");
			return;
		}

		@SuppressWarnings("deprecation")
		List<Player> playerList = Bukkit.matchPlayer(args[0]);
		Player player;
		if (playerList.size() == 0) {
			plugin.message(sender, "general-noPlayer", args[0]);
			return;
		}
		if (playerList.size() == 1) {
			player = playerList.get(0);
		} else {
			// TODO replace by messaging system
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder
					.append("[\"\",{\"text\":\"What player do you mean? (click one)\\n\",\"color\":\"green\"},");
			for (Player p : playerList) {
				stringBuilder
						.append("{\"text\":\""
								+ p.getName()
								+ ", \",\"color\":\"blue\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/auracheck "
								+ p.getName()
								+ "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\""
								+ p.getName()
								+ "\",\"color\":\"dark_purple\"}]}}},");
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			stringBuilder.append("]");
			String json = stringBuilder.toString();
			PacketContainer packet = new PacketContainer(PacketType.Play.Server.CHAT);
			packet.getChatComponents().write(0, WrappedChatComponent.fromJson(json));
			try {
				ProtocolLibrary.getProtocolManager().sendServerPacket((Player) sender, packet);
			} catch (InvocationTargetException e) {
				Log.warn("Something went wrong with the chat packet to choose a target:", ExceptionUtils.getStackTrace(e));
			}
			return;
		}
		if (running.containsKey(player.getUniqueId())) {
			plugin.message(sender, "ac-stillRunning", player.getName());
			return;
		}
		final Player finalPlayer = player;
		AuraCheckRun check = new AuraCheckRun(this, player);
		check.start(sender, result -> {
			String status = null;
			ChatColor color = null;
			if (result.killed == 0) {
				status = "No threat as of yet.";
				color = ChatColor.DARK_GREEN;
			} else if (result.killed == 1) {
				status = "Might be hacking, check head moving and repeat.";
				color = ChatColor.YELLOW;
			} else if (result.killed == 2) {
				status = "Likely hacking, repeat to confirm.";
				color = ChatColor.RED;
			} else if (result.killed == 3) {
				status = "For sure hacking, ban with /hackban.";
				color = ChatColor.DARK_RED;
			} else if (result.killed == 4) {
				status = "Definitely hacking, ban with /hackban.";
				color = ChatColor.DARK_RED;
			}
			result.invoker.sendMessage(ChatColor.BLUE + "[AuraCheck] " + color + ChatColor.BOLD + finalPlayer.getName() + " killed " + result.killed + " out of " + result.spawned + " players.");
			result.invoker.sendMessage(ChatColor.BLUE + "[AuraCheck]" + color + ChatColor.BOLD + " ??? " + ChatColor.RESET + color + status);
			Log.info(finalPlayer.getName()+" killed "+result.killed+" out of "+result.spawned+" (checked by "+sender.getName()+")");
		});
		plugin.increaseStatistic("command.auracheck.used");
	}

	/**
	 * Check all players for using killaura
	 */
	public void performAllPlayerCheck() {
		Do.syncLater(getNextPeriodicDelay(), this::performAllPlayerCheck);
		Do.forAll(Bukkit.getOnlinePlayers(), player -> {
			if(player.isOnline() && player.getGameMode() == GameMode.SURVIVAL) {
				new AuraCheckRun(self, player).start(new VoidCommandSender(), result -> {
					if(result.killed >= GoCraft.getInstance().getConfig().getInt("auracheck.hacksConfirmed")) {
						String baseCommand = GoCraft.getInstance().getConfig().getString("hacksConfirmedCommand");
						Utils.consoleCommand(baseCommand.replace("%player%", result.checked.getName()).replace("%reason%", "Hacking is forbidden! [ac " + result.killed + "/" + result.spawned + "]"));
						Utils.sendStaffMessage("AuraCheck", result.checked.getName() + " got banned: " + result.killed + "/" + result.spawned + ".");
					} else {
						if(result.killed >= GoCraft.getInstance().getConfig().getInt("auracheck.staffChatWarning")) {
							Utils.sendStaffMessage("AuraCheck", result.checked.getName() + " killed " + result.killed + "/" + result.spawned + ", further inspection required.");
						} else if(result.killed >= GoCraft.getInstance().getConfig().getInt("auracheck.consoleLogging")) {
							Log.info("[AuraCheck] staffchat warning for " + result.checked.getName() + ": " + result.killed + "/" + result.spawned + ".");
						}
					}
				});
			}
		});
	}

	/**
	 * Get a random periodic delay
	 * @return Random periodic delay in ticks as defined in the config
	 */
	public int getNextPeriodicDelay() {
		return 20 * Utils.getRandomBetween(GoCraft.getInstance().getConfig().getInt("auracheck.periodicIntervalMin"), GoCraft.getInstance().getConfig().getInt("auracheck.periodicIntervalMax"));
	}

}
