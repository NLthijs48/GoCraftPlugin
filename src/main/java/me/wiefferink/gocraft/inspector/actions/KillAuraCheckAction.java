package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class KillAuraCheckAction extends InventoryAction {

    public KillAuraCheckAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return Bukkit.getServer().getPluginManager().getPlugin("AuraCheck") != null && inspection.hasInspected() && inspection.getInspected().isOnline();
    }

    @Override
    public ItemBuilder getItem() {
        return new ItemBuilder(Material.EYE_OF_ENDER)
                .setName("&2Check KillAura")
                .addAction("Click");
    }

    @Override
    public void handleClick() {
        inspection.getInspector().performCommand("auracheck:auracheck " + inspection.getInspected().getName());
        inspection.getInspector().closeInventory();
    }

}
