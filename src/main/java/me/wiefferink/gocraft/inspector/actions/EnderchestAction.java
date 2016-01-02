package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EnderchestAction extends InventoryAction {

    public EnderchestAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return inspection.hasInspected();
    }

    @Override
    public ItemStack getItem() {
        ItemStack result = new ItemStack(Material.ENDER_CHEST);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Open enderchest");
            List<String> lores = new ArrayList<>();
            lores.add(ChatColor.RESET + "" + ChatColor.BLUE + "<Click>");
            meta.setLore(lores);
            result.setItemMeta(meta);
        }
        return result;
    }

    @Override
    public void handleClick() {
        inspection.getInspected().closeInventory(); // close existing
        inspection.getInspector().performCommand("openinv:openender " + inspection.getInspected().getName());
    }

}
