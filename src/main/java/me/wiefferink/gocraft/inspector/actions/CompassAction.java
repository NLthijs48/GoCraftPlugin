package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import org.bukkit.Material;

public class CompassAction extends InventoryAction {

    public CompassAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public boolean isActive() {
        return inspection.hasInspected();
    }

    @Override
    public ItemBuilder getItem() {
        return new ItemBuilder(Material.COMPASS)
                .setName("&2Teleport to target")
                .addAction("Teleport");
    }

    @Override
    public void handleClick() {
        inspection.teleportToInspected();
        inspection.getInspector().closeInventory();
    }

}
