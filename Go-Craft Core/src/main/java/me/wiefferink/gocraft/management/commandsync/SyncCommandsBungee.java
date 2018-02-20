package me.wiefferink.gocraft.management.commandsync;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.api.WebClient;
import me.wiefferink.gocraft.api.messages.out.OnlinePlayersResponse;
import me.wiefferink.gocraft.api.messages.out.VoteStatusReponse;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SyncCommandsBungee {

	private volatile boolean shouldRun;
	private ServerSocket server;
	private GoCraftBungee plugin;
	private final Map<String, ClientHandler> servers = Collections.synchronizedMap(new HashMap<>());
	private final Map<String, List<String>> queue = Collections.synchronizedMap(new HashMap<>());

	public SyncCommandsBungee(GoCraftBungee plugin) {
		this.plugin = plugin;
		shouldRun = true;
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			try {
				server = new ServerSocket(9191, 50, InetAddress.getByName("localhost"));
				plugin.getProxy().getScheduler().runAsync(plugin, new ClientListener());
			} catch(IOException e) {
				Log.error("SyncCommands: failed to setup server:", ExceptionUtils.getStackTrace(e));
			}
		});
		plugin.getProxy().getPluginManager().registerCommand(plugin, new SyncServersCommand());
	}

	/**
	 * Stop the SyncCommandsBungee
	 */
	public void stop() {
		shouldRun = false;
		if(server != null) {
			try {
				server.close();
			} catch(IOException e) {
				Log.error("SyncCommands: error while stopping the server:", ExceptionUtils.getStackTrace(e));
			}
		}
	}

	/**
	 * Run a command for the specified server
	 * @param server  The server to run the command on
	 * @param command The command to run
	 */
	public void runCommand(String server, String command) {
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			queue
				.computeIfAbsent(server, key -> Collections.synchronizedList(new ArrayList<>()))
				.add(command);
			ClientHandler handler = servers.get(server);
			if(handler != null) {
				handler.sendCommands();
			}
		});
	}

	/**
	 * Command to sync to servers
	 */
	private class SyncServersCommand extends Command {
		public SyncServersCommand() {
			super("syncserversbungee");
		}

		@Override
		public void execute(CommandSender sender, String[] commandParts) {
			if(!sender.hasPermission("gocraft.admin")) {
				sender.sendMessage("[GoCraftBungee] You do not have permission to use this command.");
				return;
			}

			// Sync to all servers
			String runCommand = "console "+ StringUtils.join(commandParts, " ");
			for(ServerInfo server : plugin.getProxy().getServers().values()) {
				runCommand(server.getName(), runCommand);
			}
			Log.info("SyncCommands["+sender.getName()+"]: executed syncServers command:", runCommand);
			sender.sendMessage("[GoCraftBungee] Command executed on "+plugin.getProxy().getServers().size()+" servers.");
		}
	}

	/**
	 * Listen and accept connecting clients from the Spigot servers
	 */
	private class ClientListener implements Runnable {
		@Override
		public void run() {
			while(shouldRun) {
				try {
					plugin.getProxy().getScheduler().runAsync(plugin, new ClientHandler(server.accept()));
				} catch(IOException e) {
					if(shouldRun) {
						Log.error("SyncCommands: exception while listening for new clients: \n"+ExceptionUtils.getStackTrace(e));
					}
				}
			}
		}
	}

	/**
	 * Maintain connection with a Spigot server and handle communication
	 */
	private class ClientHandler implements Runnable {

		private Socket client;
		private String name = "<no name>";
		private volatile boolean clientRun;
		private PrintWriter out;
		private BufferedReader in;

		public ClientHandler(Socket client) {
			this.client = client;
			clientRun = true;
		}

		@Override
		public void run() {
			try {
				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				String address = client.getInetAddress().getHostName()+":"+client.getPort();
				String[] init = in.readLine().split(" ");
				if(init.length < 4 || !init[0].equals("init")) {
					Log.error("SyncCommands: connection from", address, "did not send init line");
					disconnect();
					return;
				}
				if(servers.containsKey(init[1])) {
					Log.error("SyncCommands:", address, "provided name '"+init[1]+"' which is already connected");
					out.println("no Provided name '"+init[1]+"' is already connected");
					disconnect();
					return;
				}
				name = init[1];
				if(ProxyServer.getInstance().getServerInfo(name) == null) {
					Log.error("SyncCommands: ", address, "provided name '"+name+"' which is not registered as a server in BungeeCord.");
					out.println("no Provided name '"+name+"' which is not registered as a server in BungeeCord.");
					disconnect();
					return;
				}

				if(!init[2].equals(plugin.getGeneralConfig().getString("settings.commandSync.password"))) {
					Log.error("SyncCommands:", name, "provided an invalid password");
					out.println("no Password is incorrect");
					disconnect();
					return;
				}
				String version = plugin.getDescription().getVersion();
				if(!init[3].equals(version)) {
					Log.warn("SyncCommands:", name, "is running version", init[3], "but we are running version", version);
				}
				out.println("connected");
				servers.put(name, this);
				Log.info("SyncCommands["+name+"]: connected");
				plugin.getProxy().getScheduler().runAsync(plugin, this::sendCommands);
			} catch(IOException e) {
				Log.error("SyncCommands: exception while setting up listener for new client:\n", ExceptionUtils.getStackTrace(e));
				return;
			}

			while(shouldRun && clientRun) {
				// Check for errors in the stream
				if(out.checkError()) {
					disconnect();
					continue;
				}

				// Retreive input from server
				try {
					String input = in.readLine();
					if(input == null) {
						disconnect();
						continue;
					}
					String[] split = input.split(" ");
					if(split.length < 1) {
						Log.warn("SyncCommands["+name+"]: received empty input from server:", input);
						continue;
					}

					String type = split[0];
					String command = GoCraftBungee.join(split, " ", 1);
					// TODO fork to async thread before handling command? Otherwise the complete queue is locked until the command is complete

					// Sync to all servers
					if("syncServers".equalsIgnoreCase(type)) {
						String runCommand = "console "+command;
						for(ServerInfo server : plugin.getProxy().getServers().values()) {
							runCommand(server.getName(), runCommand);
						}
						Log.info("SyncCommands["+name+"]: executed syncServers command:", runCommand);
					}

					// Sync to bungee console
					else if("syncBungee".equalsIgnoreCase(type)) {
						boolean result = false;
						try {
							result = plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
						} catch(Exception e) {
							Log.warn("SyncCommands["+name+"]:executing syncBungee command failed:", command+"\n", ExceptionUtils.getStackTrace(e));
						}
						if(!result) {
							Log.error("SyncCommands["+name+"]: executing syncBungee command was not successful:", command);
						} else {
							Log.info("SyncCommands["+name+"]: executed syncBungee command:", command);
						}
					}

					// Switch player to another server: switch <player> <server>
					else if("switch".equalsIgnoreCase(type)) {
						if(split.length < 3) {
							Log.warn("SyncCommands["+name+"]: not enough arguments for switch:", command);
						} else {
							UUID playerUUID = null;
							try {
								playerUUID = UUID.fromString(split[1]);
							} catch(IllegalArgumentException e) {
								Log.warn("SyncCommands["+name+"]: wrong UUID for switch:", command);
							}
							if(playerUUID != null) {
								ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerUUID);
								if(player == null) {
									Log.warn("SyncCommands["+name+"]: player is not online to do switch:", command);
								} else {
									ServerInfo server = ProxyServer.getInstance().getServerInfo(split[2]);
									if(server == null) {
										Log.warn("SyncCommands["+name+"]: could not find server for switch:", command);
									} else {
										player.connect(server, (result, error) -> {
											if(!result) {
												player.sendMessage(
														ChatMessageType.CHAT,
														new ComponentBuilder("[Go-Craft]")
															.color(ChatColor.DARK_GREEN)
															.append(" Could not connect you to "+plugin.getServerName(server.getName()))
															.color(ChatColor.WHITE)
															.create())
												;
											}
											if(error != null) {
												Log.warn("SyncCommands[" + name + "]: Error while trying to switch player from server:", ExceptionUtils.getStackTrace(error));
											}
										});
									}
								}
							}
						}
					}

					// Send new players list to the website
					else if("updatePlayers".equalsIgnoreCase(type)) {
						plugin.getApi().broadcast(new OnlinePlayersResponse());
					}

					else if("updateVoteStatus".equalsIgnoreCase(type)) {
						if(split.length < 2) {
							Log.warn("SyncCommands["+name+"]: no ip given for updateVoteStatus command");
						} else {
							String votedIp = split[1];
							for(WebClient client : plugin.getApi().getClients().values()) {
								if(votedIp.equals(client.getIp())) {
									client.message(new VoteStatusReponse(client));
								}
							}
						}
					}

					// Invalid command
					else {
						Log.warn("SyncCommands["+name+"]: unknown command type:", type, "args:", command);
					}

				} catch(IOException e) {
					if(shouldRun) {
						Log.error("SyncCommands["+name+"]: receiving commands failed:", ExceptionUtils.getStackTrace(e));
						disconnect();
					}
				}
			}

		}

		/**
		 * Disconnect the server and cleanup the thread
		 */
		private void disconnect() {
			servers.remove(name);
			try {
				client.close();
			} catch(IOException ignored) {
			}
			clientRun = false;

			Log.info("SyncCommands["+name+"]: disconnected");
		}


		/**
		 * Send the commands that are in the queue
		 */
		private void sendCommands() {
			// Will send when connected again
			if(!clientRun || !shouldRun) {
				return;
			}

			if(queue.get(name) == null) {
				return;
			}

			// Block our queue
			synchronized(queue.get(name)) {
				Iterator<String> it = queue.get(name).iterator();
				while(it.hasNext()) {
					String command = it.next();
					out.println(command);
					if(out.checkError()) {
						disconnect();
						break; // Not send successfully, try again later
					}
					it.remove(); // Successful send, remove
					//Log.info("SyncCommands["+name+"]: command send:", command);
				}
			}
		}
	}
}
