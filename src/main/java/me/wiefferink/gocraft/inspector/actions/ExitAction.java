package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ExitAction extends InventoryAction {

    public ExitAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public ItemStack getItem() {
        ItemStack result = new ItemStack(Material.BIRCH_DOOR_ITEM);
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Stop inspection");
            List<String> lores = new ArrayList<>();
            lores.add(ChatColor.RESET + "" + ChatColor.BLUE + "<Click>");
            meta.setLore(lores);
            result.setItemMeta(meta);
        }
        return result;
    }

    @Override
    public void handleClick() {
        inspection.getInspector().performCommand("gocraft:inspect");
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
