package me.wiefferink.gocraft.features;

import me.wiefferink.bukkitdo.Do;
import me.wiefferink.bukkitdo.Run;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public abstract class Feature implements Listener {
	public static GoCraft plugin = GoCraft.getInstance();

	protected String configKey = null;

	/**
	 * Stop actions of the feature
	 */
	public void stopFeature() {
		stop();
	}

	public void stop() {
	}


	/**
	 * Startup actions of the feature
	 */
	public void startFeature() {
		// TODO properly use start/stop instead of constructor?
	}

	/**
	 * Register as listener
	 * @return true if registered, otherwise false
	 */
	protected boolean listen() {
		return listen(null);
	}

	/**
	 * Register as listener
	 * @param configKey The key to check for in the config
	 * @return true if registered, otherwise false
	 */
	protected boolean listen(String configKey) {
		this.configKey = configKey;
		if(configKey == null || (getConfig().isBoolean(configKey) && getConfig().getBoolean(configKey)) || (!getConfig().isBoolean(configKey) && getConfig().isSet(configKey))) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
			return true;
		}
		return false;
	}

	/**
	 * Stop listening to events
	 */
	public void stopListen() {
		HandlerList.unregisterAll(this);
	}

	/**
	 * Register for a certain command
	 * @param name        The command to register for
	 * @param description       The usage of the command
	 */
	protected void command(String name, String description) {
		command(name, description, null);
	}

	/**
	 * Register for a certain command
	 * @param name The command to register for
	 * @param description The description of the command
	 * @param usage The usage of the command
	 * @param aliases The aliases to add for the command
	 */
	protected void command(String name, String description, String usage, String... aliases) {
		// Create new Command instance that proxies the execute() and tabComplete()
		Command newCommand = new Command(name) {
			@Override
			public boolean execute(CommandSender sender, String label, String[] args) {
				onCommand(sender, this, label, args);
				return true;
			}

			@Override
			public List<String> tabComplete(CommandSender sender, String label, String[] args) {
				List<String> result = onTabComplete(sender, this, label, args);
				// Filter and sort the results
				if(result.size() > 0 && args.length > 0) {
					SortedSet<String> set = new TreeSet<>();
					for(String suggestion : result) {
						if(suggestion.toLowerCase().startsWith(args[args.length-1])) {
							set.add(suggestion);
						}
					}
					result.clear();
					result.addAll(set);
				}
				return result;
			}
		};

		// Set settings on the command
		if(usage != null) {
			newCommand.setUsage(usage);
		}
		if(description != null) {
			newCommand.setDescription(description);
		}
		List<String> normalizedAliases = new ArrayList<>();
		if(aliases != null && aliases.length > 0) {
			for(String alias : aliases) {
				if(alias != null) {
					normalizedAliases.add(alias.toLowerCase());
				}
			}
			newCommand.setAliases(normalizedAliases);
		}

		// Register the new command, overriding existing commands
		try {
			// Get commandMap
			Field bukkitCommandMap = Bukkit.getServer().getClass().getDeclaredField("commandMap");
			bukkitCommandMap.setAccessible(true);
			CommandMap commandMap = (CommandMap)bukkitCommandMap.get(Bukkit.getServer());

			// Get knowCommands map from commandMap
			Field knowCommandsField = commandMap.getClass().getDeclaredField("knownCommands");
			knowCommandsField.setAccessible(true);
			@SuppressWarnings("unchecked")
			Map<String, Command> knownCommands = (Map<String, Command>)knowCommandsField.get(commandMap);

			// Remove this command from the map to be able to override it (and possible aliases)
			knownCommands.remove(name.toLowerCase());
			for(String alias : normalizedAliases) {
				knownCommands.remove(alias);
			}

			// Register command
			boolean register = commandMap.register(name, plugin.getName(), newCommand);
			if(!register) {
				Log.error("Could not register command", name, "(another command is already registered with the same name)");
			}
		} catch(NoSuchFieldException|IllegalAccessException|IllegalArgumentException|SecurityException|ClassCastException e) {
			Log.error("Could not register command", name+":", ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Register a permission
	 * @param permission  The permission to register
	 * @param description Description of the permission
	 */
	protected void permission(String permission, String description) {
		permission(permission, description, PermissionDefault.OP);
	}

	/**
	 * Register a permission
	 * @param permission        The permission to register
	 * @param description       Description of the permission
	 * @param permissionDefault Default permission state
	 */
	protected void permission(String permission, String description, PermissionDefault permissionDefault) {
		Permission newPermission = new Permission("gocraft."+permission, description, permissionDefault);
		newPermission.addParent("gocraft.*", true);
		try {
			Bukkit.getPluginManager().addPermission(newPermission);
		} catch(IllegalArgumentException ignored) {
		}
	}

	/**
	 * Check if the feature is enabled on the given world
	 * @param world The world to check
	 * @return true if the feature is enabled in the given world, otherwise false
	 */
	protected boolean inWorld(Object world) {
		List<String> worlds = getConfig().getStringList(configKey+"Worlds");
		if(configKey != null && worlds.size() != 0) {
			String worldString = "";
			if((world instanceof String)) {
				worldString = (String)world;
			} else if((world instanceof World)) {
				worldString = ((World)world).getName();
			} else if((world instanceof Block)) {
				worldString = ((Block)world).getWorld().getName();
			} else if((world instanceof Location)) {
				worldString = ((Location)world).getWorld().getName();
			} else if((world instanceof Entity)) {
				worldString = ((Entity)world).getWorld().getName();
			} else if(world instanceof PlayerEvent) {
				worldString = ((PlayerEvent)world).getPlayer().getWorld().getName();
			} else if(world instanceof BlockEvent) {
				worldString = ((BlockEvent)world).getBlock().getWorld().getName();
			} else if(world instanceof EntityEvent) {
				worldString = ((EntityEvent)world).getEntity().getWorld().getName();
			} else if(world instanceof HangingEvent) {
				worldString = ((HangingEvent)world).getEntity().getWorld().getName();
			} else if(world instanceof VehicleEvent) {
				worldString = ((VehicleEvent)world).getVehicle().getLocation().getWorld().getName();
			} else if(world instanceof WeatherEvent) {
				worldString = ((WeatherEvent)world).getWorld().getName();
			} else if(world instanceof WorldEvent) {
				worldString = ((WorldEvent)world).getWorld().getName();
			} else {
				Log.warn("GoCraft.inWorld: Cannot get world from object:", world.getClass().getName());
			}
			if(!worlds.contains(worldString)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Executed when a registered command is called (to be overridden by an extending class)
	 * @param sender  The CommandSender using the command
	 * @param command The command being executed
	 * @param args    The arguments of the command
	 */
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
	}

	/**
	 * Executed when a CommandSender uses tab completion (to be overridden by an extending class)
	 * @param sender  The CommandSender that uses tab completion
	 * @param command The command that is being completed
	 * @param args    The arguments of the command
	 * @return A list of possible tab completions (will be filtered to match the requested prefix)
	 */
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> result = new ArrayList<>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			result.add(player.getName());
		}
		return result;
	}

	/**
	 * Run a task on the main server thread
	 * @param runnable The BukkitRunnable to run
	 */
	public void sync(Run runnable) {
		Do.sync(runnable);
	}

	/**
	 * Run a task on an asynchronous thread
	 * @param runnable The BukkitRunnable to run
	 */
	public void async(Run runnable) {
		Do.async(runnable);
	}

	/**
	 * Run a timer task on the main server thread
	 *
	 * @param runnable The BukkitRunnable to run
	 */
	public void syncTimer(long period, Run runnable) {
		Do.syncTimer(period, runnable);
	}

	/**
	 * Run a timer task on an asynchronous thread
	 *
	 * @param runnable The BukkitRunnable to run
	 */
	public void asyncTimer(long period, Run runnable) {
		Do.asyncTimer(period, runnable);
	}

	/**
	 * Get the config file
	 * @return The config file
	 */
	public FileConfiguration getConfig() {
		return plugin.getConfig();
	}
}
