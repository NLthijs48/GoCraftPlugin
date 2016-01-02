package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.Utils;
import org.bukkit.Bukkit;
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
            plugin.message(sender, "inspect-playerOnly");
            return true;
        }
        Player inspector = (Player) sender;
        if (!sender.hasPermission("gocraft.staff")) {
            plugin.message(inspector, "inspect-noPermission");
            return true;
        }
        Inspection inspection = plugin.getInspectionManager().getInspectionByInspector(inspector);
        if (inspection != null) {
            // End current inspection
            inspection.endInspection();
            plugin.message(inspector, "inspect-ended", inspection.getInspected().getName());
        } else {
            if (args.length < 1) {
                plugin.message(inspector, "inspect-help");
                return true;
            }
        }
        if (args.length >= 1) {
            if (Utils.isInPvpArea(inspector) && inspector.getGameMode() == GameMode.SURVIVAL) {
                plugin.message(inspector, "inspect-inNonPVP");
                return true;
            }
            Player inspected = Bukkit.getPlayer(args[0]);
            if (inspected == null) {
                plugin.message(inspector, "inspect-notOnline", args[0]);
                return true;
            }
            /*
			if (inspector.getUniqueId().equals(inspected.getUniqueId())) {
				plugin.message(inspector, "inspect-self");
				return true;
			}
			*/
            if (plugin.getInspectionManager().getInspectionByInspector(inspected) != null) {
                plugin.message(inspector, "inspect-inspection", inspected.getName());
                return true;
            }
            // Start inspection
            inspection = plugin.getInspectionManager().setupInspection(inspector, inspected);
            inspection.startInspection();
            plugin.message(inspector, "inspect-started", inspected.getName());
        }
        return true;
    }

}
