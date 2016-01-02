package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.List;

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
        Wool wool = new Wool(DyeColor.RED);
        ItemStack result = wool.toItemStack(1);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Check NCP violations");
            List<String> lores = new ArrayList<>();
            lores.add(ChatColor.RESET + "" + ChatColor.BLUE + "<Click>");
            meta.setLore(lores);
            result.setItemMeta(meta);
        }
        return result;
    }

    @Override
    public void handleClick() {
        inspection.getInspector().performCommand("nocheatplus:ncp info " + inspection.getInspected().getName());
        inspection.getInspector().closeInventory();
    }

}
