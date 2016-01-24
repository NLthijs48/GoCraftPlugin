package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class NCPAction extends InventoryAction {

    public NCPAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return Bukkit.getServer().getPluginManager().getPlugin("NoCheatPlus") != null && inspection.hasInspected() && inspection.getInspected().isOnline();
    }

    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Material.WOOL)
                .setData(14)
                .setName(ChatColor.GREEN + "Check NCP violations")
                .addAction("Click")
                .getItemStack();
    }

    @Override
    public void handleClick() {
        inspection.getInspector().performCommand("nocheatplus:ncp info " + inspection.getInspected().getName());
        inspection.getInspector().closeInventory();
    }

}
