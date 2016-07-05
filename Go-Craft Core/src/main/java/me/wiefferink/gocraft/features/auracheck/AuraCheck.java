package me.wiefferink.gocraft.features.auracheck;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.VoidCommandSender;
import me.wiefferink.gocraft.tools.packetwrapper.WrapperPlayClientUseEntity;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static me.wiefferink.gocraft.tools.Utils.random;

public class AuraCheck extends Feature implements CommandExecutor {

	private HashMap<UUID, AuraCheckRun> running;
	private boolean isRegistered;
	private static AuraCheck self;

	public AuraCheck() {
		GoCraft.getInstance().getServer().getPluginManager().registerEvents(this, GoCraft.getInstance());
		random = new Random();
		running = new HashMap<>();
		self = this;
		if (GoCraft.getInstance().getConfig().getBoolean("auracheck.enablePeriodicCheck")) {
			new BukkitRunnable() {
				@Override
				public void run() {
					performAllPlayerCheck();
				}
			}.runTaskLater(GoCraft.getInstance(), getNextPeriodicDelay());
		}
		commands.add("AuraCheck");
	}

	@Override
	public void stop() {
		for (UUID uuid : running.keySet()) {
			AuraCheckRun check = removeCheck(uuid);
			check.wrapup();
		}
	}

	@EventHandler
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
		if (!isRegistered && running.size() > 0) {
			ProtocolLibrary.getProtocolManager().addPacketListener(
					new PacketAdapter(GoCraft.getInstance(), WrapperPlayClientUseEntity.TYPE) {
						public void onPacketReceiving(PacketEvent event) {
							if (event.getPacketType() == WrapperPlayClientUseEntity.TYPE) {
								WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());
								int entID = packet.getTarget();
								if (running.containsKey(event.getPlayer().getUniqueId()) && packet.getType().equals(EnumWrappers.EntityUseAction.ATTACK)) {
									running.get(event.getPlayer().getUniqueId()).markAsKilled(entID);
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
		if (this.running.size() == 0 && isRegistered) {
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
		if (previous != null) {
			previous.wrapup();
		}
		running.put(player, check);
		register();
	}

	public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		if (!sender.hasPermission("gocraft.auracheck")) {
			plugin.message(sender, "ac-noPermission");
			return true;
		}

		@SuppressWarnings("deprecation")
		List<Player> playerList = Bukkit.matchPlayer(args[0]);
		Player player;
		if (playerList.size() == 0) {
			plugin.message(sender, "general-noPlayer", args[0]);
			return true;
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
				GoCraft.getInstance().getLogger().warning("Something went wrong with the chat packet to choose a target:");
				e.printStackTrace();
			}
			return true;
		}
		if (running.containsKey(player.getUniqueId())) {
			plugin.message(sender, "ac-stillRunning", player.getName());
			return true;
		}
		final Player finalPlayer = player;
		AuraCheckRun check = new AuraCheckRun(this, player);
		check.start(sender, new AuraCheckRun.Callback() {
			public void done(AuraCheckRun.AuraCheckRunResult result) {
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
				result.invoker.sendMessage(ChatColor.BLUE + "[AuraCheck]" + color + ChatColor.BOLD + " ► " + ChatColor.RESET + color + status);
				GoCraft.getInstance().getLogger().info(finalPlayer.getName() + " killed " + result.killed + " out of " + result.spawned + " (checked by " + sender.getName() + ")");
			}
		});
		return true;
	}

	/**
	 * Check all players for using killaura
	 */
	public void performAllPlayerCheck() {
		new BukkitRunnable() {
			@Override
			public void run() {
				performAllPlayerCheck();
			}
		}.runTaskLater(GoCraft.getInstance(), getNextPeriodicDelay());

		new BukkitRunnable() {
			private List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

			@Override
			public void run() {
				try {
					if (players.size() <= 0) {
						this.cancel();
						return;
					}
					Player toCheck = players.remove(0);
					if (toCheck != null && toCheck.isOnline() && toCheck.getGameMode() == GameMode.SURVIVAL) {
						new AuraCheckRun(self, toCheck).start(new VoidCommandSender(), new AuraCheckRun.Callback() {
							@Override
							public void done(AuraCheckRun.AuraCheckRunResult result) {
								if (result.killed >= GoCraft.getInstance().getConfig().getInt("auracheck.hacksConfirmed")) {
									String baseCommand = GoCraft.getInstance().getConfig().getString("hacksConfirmedCommand");
									Utils.consoleCommand(baseCommand.replace("%player%", result.checked.getName()).replace("%reason%", "Hacking is forbidden! [ac " + result.killed + "/" + result.spawned + "]"));
									Utils.sendStaffMessage("AuraCheck", result.checked.getName() + " got banned: " + result.killed + "/" + result.spawned + ".");
								} else {
									if (result.killed >= GoCraft.getInstance().getConfig().getInt("auracheck.staffChatWarning")) {
										Utils.sendStaffMessage("AuraCheck", result.checked.getName() + " killed " + result.killed + "/" + result.spawned + ", further inspection required.");
									} else if (result.killed >= GoCraft.getInstance().getConfig().getInt("auracheck.consoleLogging")) {
										GoCraft.getInstance().getLogger().info("[AuraCheck] staffchat warning for " + result.checked.getName() + ": " + result.killed + "/" + result.spawned + ".");
									}
								}
							}
						});
					}
				} catch (Exception e) {
					this.cancel();
					GoCraft.getInstance().getLogger().warning("Something went wrong with automatic aura check:");
					e.printStackTrace();
				}
			}
		}.runTaskTimer(GoCraft.getInstance(), 1, GoCraft.getInstance().getConfig().getInt("auracheck.playerIntervalTime"));
	}

	/**
	 * Get a random periodic delay
	 * @return Random periodic delay in ticks as defined in the config
	 */
	public int getNextPeriodicDelay() {
		return 20 * Utils.getRandomBetween(GoCraft.getInstance().getConfig().getInt("auracheck.periodicIntervalMin"), GoCraft.getInstance().getConfig().getInt("auracheck.periodicIntervalMax"));
	}

}