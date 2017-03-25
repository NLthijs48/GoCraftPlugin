package me.wiefferink.gocraft.ranks;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class RankManager extends Feature {

	public RankManager() {
		command("importRanks", "Import the ranks from PermissionsEx into the database");
		permission("importranks", "Use the /importranks command");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("importRanks")) {
			// TODO stuff
		}
	}

}
