package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang.WordUtils;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;

import java.util.Collection;

public class PotionAction extends InventoryAction {

    public PotionAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return inspection.hasInspected();
    }

    @Override
    public ItemBuilder getItem() {
        ItemBuilder result;
        Collection<PotionEffect> potions = inspection.getInspected().getActivePotionEffects();
        if (potions.size() == 0) {
            result = new ItemBuilder(Material.GLASS_BOTTLE).setName("&2No potion effects");
        } else {
            result = new ItemBuilder(Material.POTION).setName("&2" + potions.size() + " active potion effects");
        }
        for (PotionEffect effect : inspection.getInspected().getActivePotionEffects()) {
            result.addLore(ChatColor.BLUE + WordUtils.capitalizeFully(effect.getType().getName().replace("_", " ")) + " " + (effect.getAmplifier() + 1) + " for " + effect.getDuration() / 20 + " seconds");
        }
        return result;
    }

    @Override
    public boolean doUpdates() {
        return true;
    }

}
