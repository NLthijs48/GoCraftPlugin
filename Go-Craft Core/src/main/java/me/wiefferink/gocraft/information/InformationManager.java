package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.messages.Message;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;

public class InformationManager extends Feature {

	private List<InformationProvider> informationProviders;

	public InformationManager() {
		command("information", "Get information about a player", "/info [player]", "info", "stats");
		permission("information", "Uset the information command", PermissionDefault.TRUE);

		informationProviders = new ArrayList<>();
		setupBasicProviders();
	}

	public void addInformationProvider(InformationProvider provider) {
		informationProviders.add(provider);
	}


	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if(!sender.hasPermission("gocraft.information")) {
			plugin.message(sender, "information-noPermission");
			return;
		}

		// Determine target
		Player about;
		if(args.length > 0) {
			about = Utils.loadPlayer(args[0]);
			if(about == null) {
				plugin.message(sender, "information-wrongPlayer", args[0]);
				return;
			}
		} else if(sender instanceof Player) {
			about = (Player)sender;
		} else {
			plugin.message(sender, "information-noPlayer");
			return;
		}

		// Send messages
		plugin.message(sender, "information-header", about.getName());
		for(InformationProvider provider : informationProviders) {
			provider.show(about, sender);
		}
	}

	public void setupBasicProviders() {
		// Health
		addInformationProvider((Player about, CommandSender to) -> {
			String health = "";
			int healthNumber = (int)about.getHealth();
			if(healthNumber < 7) {
				health += ChatColor.RED;
			} else if(healthNumber < 13) {
				health += ChatColor.GOLD;
			} else {
				health += ChatColor.GREEN;
			}
			for(int i = 0; i < 20; i++) {
				if(i == healthNumber) {
					health += ChatColor.GRAY;
				}
				health += "▌";
			}
			plugin.messageNoPrefix(to, "information-itemHealth", health, healthNumber, about.getMaxHealth());
		});

		// Hunger
		addInformationProvider((Player about, CommandSender to) -> {
			String health = "";
			int foodNumber = about.getFoodLevel();
			if(foodNumber < 7) {
				health += ChatColor.RED;
			} else if(foodNumber < 13) {
				health += ChatColor.GOLD;
			} else {
				health += ChatColor.GREEN;
			}
			for(int i = 0; i < 20; i++) {
				if(i == foodNumber) {
					health += ChatColor.GRAY;
				}
				health += "▌";
			}
			Message saturation = Message.none();
			if(about.getSaturation() > 0) {
				saturation = Message.fromKey("information-itemHungerSaturation").replacements(about.getSaturation());
			}
			plugin.messageNoPrefix(to, "information-itemHunger", health, foodNumber, 20.0, saturation);
		});

		// Location
		addInformationProvider((Player about, CommandSender to) -> {
			if(!to.hasPermission("gocraft.staff")) {
				return;
			}

			Location location = about.getLocation();
			plugin.messageNoPrefix(to, "information-itemLocation", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), Math.round(location.getYaw()), Math.round(location.getPitch()));
		});

		// Ip address
		addInformationProvider((Player about, CommandSender to) -> {
			if(!about.isOnline()) {
				return;
			}

			if(!to.hasPermission("gocraft.staff")) {
				return;
			}

			plugin.messageNoPrefix(to, "information-itemIp", about.getAddress().getHostName());
		});
	}

}
























