package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.information.providers.*;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

	/**
	 * Add an information provider that has information about a player
	 * @param provider The provider
	 */
	public void addProvider(InformationProvider provider) {
		informationProviders.add(provider);
	}

	/**
	 * Get the information providers
	 * @return The registered information providers
	 */
	public List<InformationProvider> getInformationProviders() {
		return informationProviders;
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
			OfflinePlayer testPlayer = Bukkit.getOfflinePlayer(args[0]);
			if(testPlayer == null || testPlayer.getName() == null || (testPlayer.getLastPlayed() == 0 && !testPlayer.isOnline())) {
				plugin.message(sender, "information-neverPlayed", args[0]);
				return;
			}
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
		InformationRequest request = new InformationRequest(about, sender);
		request.execute();
	}

	public void setupBasicProviders() {
		// TODO setup explicit ordering with InformationProvider
		addProvider(new HealthInfo());
		addProvider(new HungerInfo());
		addProvider(new GamemodeInfo());
		addProvider(new PingInfo());
		addProvider(new SeenInfo());
		addProvider(new BalanceInfo());
		addProvider(new LocationInfo());
		addProvider(new IPInfo());
		addProvider(new InspectInfo());
	}

}
























