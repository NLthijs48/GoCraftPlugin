package nl.evolutioncoding.gocraft.commands;

import nl.evolutioncoding.gocraft.GoCraft;
import nl.evolutioncoding.gocraft.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetspawnCommand implements CommandExecutor {

	public final String configLine = "spawnTeleport"; // Same as in SpawnTeleport.java
	private GoCraft plugin;
	
	public SetspawnCommand(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getCommand("SetGSpawn").setExecutor(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.setspawn")) {
			plugin.message(sender, "setspawn-noPermission");
			return true;
		}
		
		if(!(sender instanceof Player)) {
			plugin.message(sender, "setspawn-onlyByPlayers");
			return true;
		}		
		Player player = (Player)sender;
		plugin.getLocalStorage().set("spawnLocation", Utils.locationToConfig(player.getLocation(), true));
		plugin.saveLocalStorage();
		plugin.message(player, "setspawn-success");		
		return true;
	}

}

































