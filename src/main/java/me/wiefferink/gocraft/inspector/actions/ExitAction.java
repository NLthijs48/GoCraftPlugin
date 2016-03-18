package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ExitAction extends InventoryAction {

    public ExitAction(Inspection inspection) {
        super(inspection);
    }

    @Override
    public ItemBuilder getItem() {
        return new ItemBuilder(Material.BARRIER)
                .setName("&2Stop inspection")
                .addAction("Stop");
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
