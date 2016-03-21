package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {

	private GoCraft plugin;

	public ShopCommand(GoCraft plugin) {
		this.plugin = plugin;
		plugin.getCommand("Shop").setExecutor(this);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (plugin.getShop() == null) {
			plugin.message(sender, "shop-notEnabled");
			return true;
		}

		if (!(sender instanceof Player)) {
			plugin.message(sender, "shop-playerOnly");
			return true;
		}
		Player player = (Player) sender;
		plugin.getShop().open(player);
		return true;
	}

}
