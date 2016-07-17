package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectCommand implements CommandExecutor {


	public final String configLine = "enableInspecting";
	private GoCraft plugin;

	public InspectCommand(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getCommand("Inspect").setExecutor(this);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return true;
		}
		Player inspector = (Player) sender;
		if (!sender.hasPermission("gocraft.staff")) {
			plugin.message(inspector, "inspect-noPermission");
			return true;
		}
		Inspection inspection = plugin.getInspectionManager().getInspectionByInspector(inspector);
		if (inspection != null && args.length == 0) {
			// Stop existing inspection
			inspection.endInspection();
			if (inspection.hasInspected()) {
				plugin.message(inspector, "inspect-ended", inspection.getInspected().getName());
			} else {
				plugin.message(inspector, "inspect-endedNoTarget");
			}
			return true;
		}

		Player newTarget = null;
		if (args.length > 0) {
			newTarget = Utils.loadPlayer(args[0]);
			// Did not play before
			if (newTarget == null) {
				plugin.message(inspector, "inspect-notAvailable", args[0]);
				return true;
			} else if (plugin.getInspectionManager().getInspectionByInspector(newTarget) != null) {
				// Trying to inspect an inspector
				plugin.message(inspector, "inspect-inspection", newTarget.getName());
				return true;
			} else if (inspector.getUniqueId().equals(newTarget.getUniqueId())) {
				plugin.message(inspector, "inspect-self");
				return true;
			}
		}
		if (inspection != null) {
			// From existing to new target
			inspection.switchToPlayer(newTarget, true);
			if (newTarget != null) {
				plugin.message(inspector, "inspect-started", newTarget.getName());
			} else {
				plugin.message(inspector, "inspect-startedNoTarget");
			}
			return true;
		}

		// New inspection
		if (Utils.isInPvpArea(inspector) && inspector.getGameMode() == GameMode.SURVIVAL) {
			plugin.message(inspector, "inspect-inNonPVP");
			return true;
		}

		// Start inspection
		inspection = plugin.getInspectionManager().setupInspection(inspector, newTarget);
		inspection.startInspection();
		if (newTarget != null) {
			plugin.message(inspector, "inspect-started", newTarget.getName());
		} else {
			plugin.message(inspector, "inspect-startedNoTarget");
		}
		return true;
	}

}
