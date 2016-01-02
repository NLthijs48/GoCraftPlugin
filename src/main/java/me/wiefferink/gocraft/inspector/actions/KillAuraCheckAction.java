package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class KillAuraCheckAction extends InventoryAction {

    public KillAuraCheckAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return Bukkit.getServer().getPluginManager().getPlugin("AuraCheck") != null && inspection.hasInspected() && inspection.getInspected().isOnline();
    }

    @Override
    public ItemStack getItem() {
        ItemStack result = new ItemStack(Material.EYE_OF_ENDER);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Check KillAura");
            List<String> lores = new ArrayList<>();
            lores.add(ChatColor.RESET + "" + ChatColor.BLUE + "<Click>");
            meta.setLore(lores);
            result.setItemMeta(meta);
        }
        return result;
    }

    @Override
    public void handleClick() {
        inspection.getInspector().performCommand("auracheck:auracheck " + inspection.getInspected().getName());
        inspection.getInspector().closeInventory();
    }

}
