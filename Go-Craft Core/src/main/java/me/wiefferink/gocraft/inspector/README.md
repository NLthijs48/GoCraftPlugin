# Inspector
Contains the implementation of `/inspect`.

* For each inspection an instance of `Inspection` is created keeping information about the target.
* The inventory of the player is saved to disk, and replaced by a row of `InventoryAction` instances.
* Each `InventoryAction` shows a button and performs an action when clicked.
* After the inspection the original inventory and player state is restored.