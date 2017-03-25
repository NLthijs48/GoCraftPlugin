package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class SafeTeleportCommand extends Feature {

	public SafeTeleportCommand() {
		command("safeteleport", "Teleport to a player to a location in a safe way", "/safeteleport [player] <world> <x> <y> <z> [<yaw> <pitch>]", "safetp");
		permission("safeteleport", "Use the /safeteleport command");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.safeteleport")) {
			plugin.message(sender, "safetetp-noPermission");
			return;
		}

		if(args.length < 4) {
			plugin.message(sender, "safetp-help");
			return;
		}

		// Determine player to teleport
		Player toTeleport;
		int argIndex = 0;
		if(args.length == 5 || args.length == 7) {
			toTeleport = Bukkit.getPlayer(args[argIndex]);
			if(toTeleport == null) {
				plugin.message(sender, "safetp-wrongPlayer", args[argIndex]);
				return;
			}
			argIndex++;
		} else if(sender instanceof Player) {
			toTeleport = (Player)sender;
		} else {
			plugin.message(sender, "safetp-noPlayer");
			return;
		}

		// Check world
		World world = Bukkit.getWorld(args[argIndex]);
		if(world == null) {
			Set<String> worldOptions = new HashSet<>();
			for(World worldOption : Bukkit.getWorlds()) {
				worldOptions.add(worldOption.getName());
			}
			plugin.message(sender, "safetp-wrongWorld", args[argIndex], StringUtils.join(worldOptions, ", "));
			return;
		}
		argIndex++;

		// Check coordinates
		double x, y, z;
		float pitch=toTeleport.getLocation().getPitch(), yaw=toTeleport.getLocation().getYaw();

		// X
		try {
			x = Double.parseDouble(args[argIndex]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "safetp-wrongNumber", "x-coordinate", args[argIndex]);
			return;
		}
		argIndex++;

		// Y
		try {
			y = Double.parseDouble(args[argIndex]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "safetp-wrongNumber", "y-coordinate", args[argIndex]);
			return;
		}
		argIndex++;

		// Z
		try {
			z = Double.parseDouble(args[argIndex]);
		} catch(NumberFormatException e) {
			plugin.message(sender, "safetp-wrongNumber", "z-coordinate", args[argIndex]);
			return;
		}
		argIndex++;

		if(args.length > 5) {
			// Yaw
			try {
				yaw = Float.parseFloat(args[argIndex]);
			} catch(NumberFormatException e) {
				plugin.message(sender, "safetp-wrongNumber", "yaw", args[argIndex]);
				return;
			}
			argIndex++;

			// Pitch
			try {
				pitch = Float.parseFloat(args[argIndex]);
			} catch(NumberFormatException e) {
				plugin.message(sender, "safetp-wrongNumber", "pitch", args[argIndex]);
				return;
			}
		}

		Location location = new Location(world, x, y, z, yaw, pitch);
		if(Utils.teleportToLocation(toTeleport, location, 9*9*9)) {
			plugin.message(sender, "safetp-success", Utils.locationMessage(world, x, y, z, yaw, pitch));
		} else {
			plugin.message(sender, "safetp-failed", Utils.locationMessage(world, x, y, z, yaw, pitch));
		}

	}
}
