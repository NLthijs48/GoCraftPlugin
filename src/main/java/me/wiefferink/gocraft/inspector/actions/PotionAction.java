package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class PotionAction extends InventoryAction {

    public PotionAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return inspection.hasInspected();
    }

    @Override
    public ItemStack getItem() {
        // Add a lore for each potion effect the player has
        List<String> lores = new ArrayList<>();
        for (PotionEffect effect : inspection.getInspected().getActivePotionEffects()) {
            lores.add(ChatColor.RESET + "" + ChatColor.BLUE + WordUtils.capitalizeFully(effect.getType().getName().replace("_", " ")) + " " + (effect.getAmplifier() + 1) + " for " + effect.getDuration() / 20 + " seconds");
        }
        ItemStack result;
        if (lores.isEmpty()) {
            result = new ItemStack(Material.GLASS_BOTTLE);
        } else {
            result = new ItemStack(Material.POTION);
        }
        ItemMeta meta = result.getItemMeta();
        if (meta != null) {
            if (lores.isEmpty()) {
                meta.setDisplayName(ChatColor.GREEN + "No potion effects");
            } else {
                meta.setDisplayName(ChatColor.GREEN + "" + lores.size() + " active potion effects");
            }
            meta.setLore(lores);
            result.setItemMeta(meta);
        }
        return result;
    }

    @Override
    public boolean doUpdates() {
        return true;
    }

}
