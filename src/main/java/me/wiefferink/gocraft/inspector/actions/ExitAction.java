package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ExitAction extends InventoryAction {

    public ExitAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public ItemStack getItem() {
        return new ItemBuilder(Material.BIRCH_DOOR_ITEM)
                .setName(ChatColor.GREEN + "Stop inspection")
                .addAction("Click")
                .getItemStack();
    }

    @Override
    public void handleClick() {
        inspection.endInspection();
        final Player finalInspector = inspection.getInspector();
        new BukkitRunnable() {
            @Override
            public void run() {
                finalInspector.closeInventory();
            }
        }.runTaskLater(GoCraft.getInstance(), 1);
    }

    @Override
    public int getItemSlot() {
        return 8;
    }

}
