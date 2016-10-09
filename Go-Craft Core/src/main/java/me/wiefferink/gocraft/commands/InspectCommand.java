package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InspectCommand extends Feature {

	public InspectCommand() {
		if(config.getBoolean("enableInspecting")) {
			command("Inspect");
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if (!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}
		Player inspector = (Player) sender;
		if (!sender.hasPermission("gocraft.staff")) {
			plugin.message(inspector, "inspect-noPermission");
			return;
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
			return;
		}

		Player newTarget = null;
		if (args.length > 0) {
			newTarget = Utils.loadPlayer(args[0]);
			// Did not play before
			if (newTarget == null) {
				plugin.message(inspector, "inspect-notAvailable", args[0]);
				return;
			} else if (plugin.getInspectionManager().getInspectionByInspector(newTarget) != null) {
				// Trying to inspect an inspector
				plugin.message(inspector, "inspect-inspection", newTarget.getName());
				return;
			} else if (inspector.getUniqueId().equals(newTarget.getUniqueId())) {
				plugin.message(inspector, "inspect-self");
				return;
			}
		}
		if (inspection != null) {
			// From existing to new target
			inspection.switchToPlayer(newTarget, true);
			plugin.increaseStatistic("command.inspect.switchTarget");
			if (newTarget != null) {
				plugin.message(inspector, "inspect-started", newTarget.getName());
			} else {
				plugin.message(inspector, "inspect-startedNoTarget");
			}
			return;
		}

		// New inspection
		if (Utils.isInPvpArea(inspector) && inspector.getGameMode() == GameMode.SURVIVAL) {
			plugin.message(inspector, "inspect-inNonPVP");
			return;
		}

		// Start inspection
		inspection = plugin.getInspectionManager().setupInspection(inspector, newTarget);
		inspection.startInspection();
		GoCraft.debug("Inspect: starting inspection by command for", inspector.getName());
		if (newTarget != null) {
			plugin.message(inspector, "inspect-started", newTarget.getName());
			plugin.increaseStatistic("command.inspect.withTarget");
		} else {
			plugin.message(inspector, "inspect-startedNoTarget");
			plugin.increaseStatistic("command.inspect.withoutTarget");
		}
	}

}
