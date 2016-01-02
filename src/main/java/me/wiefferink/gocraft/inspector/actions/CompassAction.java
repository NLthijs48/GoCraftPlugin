package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

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
        ItemStack result = new ItemStack(Material.COMPASS);
        ItemMeta meta = result.getItemMeta();
        List<String> lores = new ArrayList<>();
        lores.add(ChatColor.BLUE + "<Click to teleport>");
        if (meta != null) {
            meta.setLore(lores);
            result.setItemMeta(meta);
        }
        inspection.getInspector().setCompassTarget(inspection.getInspected().getLocation());
        return result;
    }

    @Override
    public void handleClick() {
        inspection.teleportToInspected();
    }

}
