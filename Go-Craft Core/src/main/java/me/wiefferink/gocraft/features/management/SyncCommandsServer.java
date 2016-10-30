package me.wiefferink.gocraft.features.management;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SyncCommandsServer extends Feature {

	private static final int TRYCONNECT = 200; // Ticks, 10 seconds

	private final List<String> queue = Collections.synchronizedList(new ArrayList<>()); // Queue of commands to send to Bungee
	private boolean shouldRun; // Activated
	private boolean connected; // Connected to bungee
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	private long lastCommunication = 0;
	private BukkitTask reconnectTask; // Indicate if we have scheduled a reconnect task

	public SyncCommandsServer() {
		if(!plugin.getConfig().getBoolean("commandSyncEnabled")) {
			return;
		}
		shouldRun = true;
		reconnectTask = null;
		new BukkitRunnable() {
			@Override
			public void run() {
				connect();
			}
		}.runTaskAsynchronously(plugin);
		command("syncbungee", "Sync a command to the BungeeCord server", "/syncbungee <command...>");
		command("syncservers", "Sync a command to all servers", "/syncservers <command...>");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if(!sender.isOp()) {
			plugin.message(sender, "sync-noPermission");
			return;
		}
		plugin.message(sender, "sync-added", command, StringUtils.join(args, " "));
		runCommand(command, args);
	}

	/**
	 * Send a sync command
	 * @param command The command name
	 * @param args    The arguments containing the command to execute
	 */
	public void runCommand(String command, String[] args) {
		new BukkitRunnable() {
			@Override
			public void run() {
				queue.add(command+" "+StringUtils.join(args, " "));
				sendCommands();
			}
		}.runTaskAsynchronously(plugin);
	}

	@Override
	public void stop() {
		shouldRun = false;
		disconnect(null);
	}

	/**
	 * Connect to the server at the BungeeCord side
	 */
	private void connect() {
		if(connected) {
			return;
		}
		if(!shouldRun) {
			return;
		}

		try {
			int port = plugin.getConfig().getInt("commandSyncPort");
			socket = new Socket(InetAddress.getByName("localhost"), port);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Send our name
			out.println("init "+plugin.getBungeeId()+" "+plugin.getConfig().getString("commandSyncVerification")+" "+plugin.getDescription().getVersion());
			String result = in.readLine();
			if(result.startsWith("no")) {
				GoCraft.error("SyncCommands: error while connecting:", result.substring(result.indexOf(" ")));
				shouldRun = false;
				disconnect();
				return;
			}

			connected = true;
			GoCraft.info("SyncCommands: Connected to server (localhost:"+port+")");

			// Start input/output
			new Thread(new CommandReader()).start();
			sendCommands();
		} catch(IOException e) {
			disconnect("Could not connect to BungeeCord: "+e.getMessage());
		}
	}

	/**
	 * Disconnect from the server (broken link or shutdown)
	 */
	private void disconnect() {
		disconnect("lost connection to BungeeCord");
	}

	/**
	 * Disconnect from the server (broken link or shutdown)
	 * @param message The message to show
	 */
	private void disconnect(String message) {
		if(socket != null) {
			try {
				socket.close();
			} catch(IOException ignored) {
			}
		}
		socket = null;
		out = null;
		in = null;
		connected = false;

		if(message != null) {
			GoCraft.warn("SyncCommands: "+message, shouldRun ? "(scheduling reconnect)" : "");
		}

		if(shouldRun && reconnectTask == null) {
			reconnectTask = new BukkitRunnable() {
				@Override
				public void run() {
					reconnectTask = null;
					connect();
				}
			}.runTaskLaterAsynchronously(plugin, TRYCONNECT);
		}
	}

	/**
	 * Send the commands that are in the queue
	 */
	private void sendCommands() {
		// Will send when connected again
		if(!connected) {
			return;
		}

		// Block the queue
		synchronized(queue) {
			Iterator<String> it = queue.iterator();
			while(it.hasNext()) {
				String command = it.next();
				out.println(command);
				if(out.checkError()) {
					disconnect();
					break; // Not send successfully, try again later
				}
				it.remove(); // Successful send, remove
				GoCraft.info("SyncCommands: command send:", command);
			}
		}
	}


	/**
	 * Reads incoming commands and executes them
	 */
	public class CommandReader implements Runnable {
		public void run() {
			while(connected) {
				// Check for errors in the stream
				if(out.checkError()) {
					disconnect();
					continue;
				}

				// Retreive input from Bungee
				try {
					String input = in.readLine();
					String[] split = input.split(" ");
					if(split.length == 0) {
						GoCraft.warn("SyncCommands: received empty input from Bungee");
						continue;
					}

					String type = split[0];
					String command = StringUtils.join(split, " ", 1, split.length);

					// type "heartbeat" is skipped here
					if("console".equals(type)) {
						boolean result = false;
						Exception exception = null;
						try {
							result = Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), command);
						} catch(Exception e) {
							exception = e;
						}
						if(result) {
							GoCraft.info("SyncCommands: executed command:", command);
						} else {
							GoCraft.warn("SyncCommands: executing command failed:", command+"\n", exception != null ? ExceptionUtils.getStackTrace(exception) : "result is false");
						}
					}
				} catch(IOException e) {
					disconnect();
				}
			}
		}
	}
}