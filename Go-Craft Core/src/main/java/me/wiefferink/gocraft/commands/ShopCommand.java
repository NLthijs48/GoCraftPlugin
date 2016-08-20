package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand extends Feature {

	public ShopCommand() {
		command("Shop");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (plugin.getShop() == null) {
			plugin.message(sender, "shop-notEnabled");
			return true;
		}

		if (!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return true;
		}
		Player player = (Player) sender;
		plugin.getShop().open(player);
		return true;
	}

}
