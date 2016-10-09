package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand extends Feature {

	public ShopCommand() {
		command("Shop");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if (plugin.getShop() == null) {
			plugin.message(sender, "shop-notEnabled");
			return;
		}

		if (!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}
		Player player = (Player) sender;
		plugin.getShop().open(player);
	}

}
