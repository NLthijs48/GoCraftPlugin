package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ChestAction extends InventoryAction {

    public ChestAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return inspection.hasInspected();
    }

    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Material.CHEST)
                .setName(ChatColor.GREEN + "Open inventory")
                .addAction("Click")
                .getItemStack();
    }

    @Override
    public void handleClick() {
        inspection.getInspector().closeInventory(); // close existing
        inspection.getInspector().performCommand("openinv:openinv " + inspection.getInspected().getName());
    }

}
