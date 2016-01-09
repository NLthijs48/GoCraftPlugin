package me.wiefferink.gocraft.inspector;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.List;


public class InventoryListener implements Listener {
    InspectionManager manager;

    public InventoryListener(InspectionManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        //GoCraft.debug("clicktype=" + event.getClick() + ", rawSlot=" + event.getRawSlot() + ", slot=" + event.getSlot() + ", action=" + event.getAction() + ", clicker=" + event.getWhoClicked().getName());
        if (event.getWhoClicked() instanceof Player && manager.getInspectionByInspector((Player) event.getWhoClicked()) != null) {
            if (event.getClickedInventory() instanceof PlayerInventory) {
                // Pass clicks to inventory actions
                int slot = event.getSlot();
                //GoCraft.debug("size="+event.getView().getTopInventory().getSize()+", getcontents.length="+event.getView().getTopInventory().getContents().length);
                if (event.getView().getTopInventory().getSize() == 41) { // OpenInv uses incorrect size, and throws everything off: https://github.com/Jikoo/OpenInv/blob/master/src/com/lishid/openinv/internal/v1_8_R3/SpecialPlayerInventory.java
                    slot -= 4;
                }
                manager.getInspectionByInspector((Player) event.getWhoClicked()).handleClick(slot);
            } else {
                // Overlay click closes inventory
                if (event.getRawSlot() == -999 && event.getAction() == org.bukkit.event.inventory.InventoryAction.NOTHING) {
                    //GoPVP.debug("closing inventory...");
                    event.getWhoClicked().closeInventory();
                }
            }
        }
    }


    // Check for leftclicking a player, then switch inspection to that one
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (manager.isInspecting(player)) {
            BlockIterator blockIterator = new BlockIterator(player, 20);
            List<Block> blocks = new ArrayList<>();
            while (blockIterator.hasNext()) {
                Block block = blockIterator.next();
                if (!block.getType().isSolid()) {
                    blocks.add(block);
                }
            }
            Player closest = null;
            for (Player checkPlayer : Bukkit.getOnlinePlayers()) {
                if (checkPlayer.equals(player) || checkPlayer.getGameMode() == GameMode.SPECTATOR || manager.getInspectionByInspector(checkPlayer) != null) {
                    continue;
                }
                if (player.getWorld().getName().equals(checkPlayer.getWorld().getName()) && player.getLocation().distanceSquared(checkPlayer.getLocation()) < 100) {
                    for (Block block : blocks) {
                        if (block.getLocation().distanceSquared(checkPlayer.getLocation()) < 0.7 || block.getLocation().distanceSquared(checkPlayer.getEyeLocation()) < 0.7) {
                            closest = checkPlayer;
                        }
                    }
                }
            }
            if (closest != null) {
                manager.getInspectionByInspector(player).switchToPlayer(closest);
            }
        }
    }
}























