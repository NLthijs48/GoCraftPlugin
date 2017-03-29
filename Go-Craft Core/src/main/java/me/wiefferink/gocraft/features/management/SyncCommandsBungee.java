package me.wiefferink.gocraft.features.management;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.Log;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Command;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

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

public class SyncCommandsBungee {

	private static final String password = "cbpyiSsZqU6Mu88gqqFcbqLmxZdJNiXr6ePj6TWmUiKQqz67MJMEPUXMaKhX63Sz";

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
			List<String> commands = queue.get(server);
			if(commands == null) {
				commands = Collections.synchronizedList(new ArrayList<String>());
				queue.put(server, commands);
			}
			commands.add(command);
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
					Log.error("SyncCommands:", address, "provided name '"+name+"' which is already connected");
					out.println("no Provided name '"+init[1]+"' is already connected");
					disconnect();
					return;
				}
				name = init[1];
				if(!init[2].equals(password)) {
					Log.error("SyncCommands:", name, "provided an invalid password");
					out.println("no Password is incorrect");
					disconnect();
					return;
				}
				String version = plugin.getDescription().getVersion();
				if(!init[3].equals(version)) {
					Log.error("SyncCommands:", name, "is running version", init[3], "but we are running version", version);
					out.println("no Version incorrect, running version "+version);
					disconnect();
					return;
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
					if(split.length <= 1) {
						Log.warn("SyncCommands["+name+"]: received empty input from server:", input);
						continue;
					}

					String type = split[0];
					String command = GoCraftBungee.join(split, " ", 1);

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

			List<String> commands = queue.get(name);
			if(commands == null) {
				return;
			}

			// Block our queue
			synchronized(commands) {
				Iterator<String> it = commands.iterator();
				while(it.hasNext()) {
					String command = it.next();
					out.println(command);
					if(out.checkError()) {
						disconnect();
						break; // Not send successfully, try again later
					}
					it.remove(); // Successful send, remove
					Log.info("SyncCommands["+name+"]: command send:", command);
				}
			}
		}
	}
}
