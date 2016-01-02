package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryAction {

    Inspection inspection;

    public InventoryAction(Inspection inspection) {
        this.inspection = inspection;
    }

    /**
     * Get the item that should be displayed in the inspector inventory
     *
     * @return The ItemStack that should be shown
     */
    public abstract ItemStack getItem();

    /**
     * Handle a click event on this item
     */
    public void handleClick() {
        // Do nothing
    }

    /**
     * Determines where in the inventory this item shows up
     *
     * @return The number of the item slot this item shoul show
     */
    public int getItemSlot() {
        return -1; // Automatically align item
    }

    /**
     * Indicates if this action is active for this inspection
     *
     * @return true if this action is active, otherwise false
     */
    public boolean isActive() {
        return true;
    }

    /**
     * Indicates if this action should be updated
     *
     * @return true if the item should be updated, otherwise false
     */
    public boolean doUpdates() {
        return true;
    }
}
