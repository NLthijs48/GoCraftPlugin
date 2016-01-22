package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

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
        return new ItemBuilder(Material.EYE_OF_ENDER)
                .setName(ChatColor.GREEN + "Check KillAura")
                .addAction("Click")
                .getItemStack();
    }

    @Override
    public void handleClick() {
        inspection.getInspector().performCommand("auracheck:auracheck " + inspection.getInspected().getName());
        inspection.getInspector().closeInventory();
    }

}
