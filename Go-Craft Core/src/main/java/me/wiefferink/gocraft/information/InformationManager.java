package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.information.providers.*;
import me.wiefferink.gocraft.tools.Utils;
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

	public void addProvider(InformationProvider provider) {
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
		addProvider(new HealthInfo());
		addProvider(new HungerInfo());
		addProvider(new GamemodeInfo());
		addProvider(new LocationInfo());
		addProvider(new IPInfo());
	}

}
























