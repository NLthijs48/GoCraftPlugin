package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class CompassAction extends InventoryAction {

    public CompassAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return inspection.hasInspected();
    }

    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Material.COMPASS)
                .setName(ChatColor.GREEN + "Teleport to target")
                .addAction("Teleport")
                .getItemStack();
    }

    @Override
    public void handleClick() {
        inspection.teleportToInspected();
        inspection.getInspector().closeInventory();
    }

}
