package me.wiefferink.gocraft.inspector;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.actions.*;
import me.wiefferink.gocraft.utils.Utils;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class Inspection {

    private Player inspector;
    private Player inspected;
    // Things to restore later
    private ItemStack[] inspectorInventory;
    private ItemStack[] inspectorArmor;
    private Collection<PotionEffect> potionEffects;
    private GameMode gamemode;
    private GoCraft plugin;
    private boolean scoreboardType;
    private Map<Integer, InventoryAction> actions;

    public Inspection(GoCraft plugin, Player inspector, Player inspected) {
        this.plugin = plugin;
        this.inspector = inspector;
        this.inspected = inspected;
        this.scoreboardType = true;
    }

    public Inspection(GoCraft plugin, Player inspector) {
        this(plugin, inspector, null);
    }

    /**
     * Get the player that is inspecting
     *
     * @return The inspecting player
     */
    public Player getInspector() {
        return inspector;
    }

    /**
     * Check if a player is the inspector of this inspection
     *
     * @param player The player to check
     * @return true if the player is the inspector of this inspection, otherwise false
     */
    public boolean isInspector(Player player) {
        return player != null && player.getUniqueId().equals(inspector.getUniqueId());
    }

    /**
     * Get the inspected player
     *
     * @return The inspected player
     */
    public Player getInspected() {
        return inspected;
    }

    /**
     * Check if this inspection has a target
     *
     * @return true if this inspection has a target, otherwise false
     */
    public boolean hasInspected() {
        return inspected != null;
    }

    /**
     * Check if a player is the inspected of this inspection
     *
     * @param player The player to check
     * @return true if the player is the inspected of this inspection, otherwise false
     */
    public boolean isInspected(Player player) {
        return hasInspected() && player != null && player.getUniqueId().equals(inspected.getUniqueId());
    }

    /**
     * Start the inspection
     */
    public void startInspection() {
        plugin.getInspectionManager().addInspection(this);
        if (!saveInspectorState()) {
            // Ending inspection, because this is before most thing apply we dont have to restore much
            plugin.getInspectionManager().removeInspection(this);
            return;
        }
        // Turn on vanish
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "essentials:vanish " + getInspector().getName() + " on");
        // Remove current potion effects (could be annoying, will be restoread after)
        for (PotionEffect effect : inspector.getActivePotionEffects()) {
            inspector.removePotionEffect(effect.getType());
        }
        // Teleport to 1 block behind the direction the inspected player is looking at (third person like view)
        this.teleportToInspected();
        inspector.setGameMode(GameMode.SPECTATOR);
        inspector.getInventory().clear();
        // Prepare and select actions
        prepareInventoryActions();

        updateInventory();
        updateArmor();
        updateScoreboard();
        plugin.getInspectionManager().registerUpdater();
    }

    /**
     * Switch to a new inspected player
     *
     * @param newInspected New player
     */
    public void switchToPlayer(Player newInspected) {
        String oldInspected = "nobody";
        if (hasInspected()) {
            oldInspected = inspected.getName();
        }
        inspected = newInspected;
        updateAll();
        plugin.message(inspector, "inspect-switched", oldInspected, inspected.getName());
    }

    /**
     * Setup the inventory actions that will be active for this inspection
     */
    private void prepareInventoryActions() {
        List<InventoryAction> sourceActions = new ArrayList<>();
        sourceActions.add(new CompassAction(this));
        sourceActions.add(new ChestAction(this));
        sourceActions.add(new EnderchestAction(this));
        sourceActions.add(new KillAuraCheckAction(this));
        sourceActions.add(new PotionAction(this));
        sourceActions.add(new NCPAction(this));
        sourceActions.add(new ExitAction(this));
        // Determine layout
        actions = new HashMap<>();
        int currentSlot = 0;
        for (InventoryAction action : sourceActions) {
            if (action.isActive()) {
                int toSlot = action.getItemSlot();
                if (toSlot == -1) {
                    toSlot = currentSlot;
                    currentSlot++;
                }
                actions.put(toSlot, action);
            }
        }
    }

    /**
     * Set the armor slots of the inspector to the inspected players armor
     */
    public void updateArmor() {
        ItemStack[] armor = {null, null, null, null};
        if (hasInspected()) {
            armor = new ItemStack[inspected.getInventory().getArmorContents().length];
            for (int i = 0; i < armor.length; i++) {
                armor[i] = inspected.getInventory().getArmorContents()[i].clone();
            }
            for (ItemStack armorItem : armor) {
                if (armorItem != null) {
                    ItemMeta meta = armorItem.getItemMeta();
                    if (meta != null) {
                        List<String> lores = meta.getLore();
                        if (lores == null) {
                            lores = new ArrayList<>();
                        }
                        lores.add(0, ChatColor.DARK_GREEN + "Armor from " + inspected.getName());
                        meta.setLore(lores);
                        armorItem.setItemMeta(meta);
                    }
                }
            }
        }
        inspector.getInventory().setArmorContents(armor);
    }

    /**
     * Set the inventory to the current status
     */
    public void updateInventory() {
        PlayerInventory inventory = inspector.getInventory();
        // Setup inventory actions
        int currentSlot = 0;
        for (Integer slot : actions.keySet()) {
            InventoryAction action = actions.get(slot);
            if (action.doUpdates() || inventory.getItem(slot) == null) {
                inventory.setItem(slot, action.getItem());
            }
        }
    }

    /**
     * Update the scoreboard containing all the stats from the inspected player
     */
    public void updateScoreboard() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective;
        // Flip to make sure scoreboard does not flicker
        if (scoreboardType) {
            objective = scoreboard.registerNewObjective("inspector", "dummy");
        } else {
            objective = scoreboard.registerNewObjective("inspector2", "dummy");
        }
        scoreboardType = !scoreboardType;
        if (hasInspected()) {
            objective.setDisplayName(ChatColor.GREEN + "▬▬ " + ChatColor.BOLD + inspected.getName() + ChatColor.RESET + ChatColor.GREEN + " ▬▬");

            int currentScore = 0;

            // Display food
            String food = "" + ChatColor.YELLOW;
            int foodNumber = inspected.getFoodLevel();
            for (int i = 0; i < 20; i++) {
                if (i == foodNumber) {
                    food += ChatColor.GRAY;
                }
                food += "I";
            }
            objective.getScore(food).setScore(currentScore);
            currentScore++;

            // Display health
            String health = "";
            int healthNumber = (int) inspected.getHealth();
            if (healthNumber < 7) {
                health += ChatColor.RED;
            } else if (healthNumber < 13) {
                health += ChatColor.GOLD;
            } else {
                health += ChatColor.GREEN;
            }
            for (int i = 0; i < 20; i++) {
                if (i == healthNumber) {
                    health += ChatColor.GRAY;
                }
                health += "I"; // Half-block: ▌
            }
            objective.getScore(health).setScore(currentScore);
            currentScore++;

            // Display ping
            if (getInspected().isOnline()) {
                int ping = getPing(getInspected());
                if (ping >= 1000) {
                    ping = 999;
                }
                objective.getScore(ChatColor.GRAY + "Ping: " + ChatColor.WHITE + ping + " ms").setScore(currentScore);
                currentScore++;
            }

            // Display XP levels
            objective.getScore(ChatColor.GRAY + "XP levels: " + ChatColor.RESET + getInspected().getLevel()).setScore(currentScore);
            currentScore++;

            // Display hours played
            double hours = ((double) getInspected().getStatistic(Statistic.PLAY_ONE_TICK)) / 20 / 3600;
            BigDecimal bigHours = new BigDecimal(hours);
            bigHours = bigHours.setScale(2, RoundingMode.HALF_UP);
            objective.getScore(ChatColor.WHITE + " " + bigHours).setScore(currentScore);
            currentScore++;
            objective.getScore(ChatColor.GRAY + "Hours played:").setScore(currentScore);
            currentScore++;

            // Display money
            double balance = plugin.getEconomy().getBalance(getInspected());
            BigDecimal bigDecimal = new BigDecimal(balance);
            bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
            balance = bigDecimal.doubleValue();
            objective.getScore(" \u20ac" + balance).setScore(currentScore);
            currentScore++;
            objective.getScore(ChatColor.GRAY + "Money:").setScore(currentScore);
            currentScore++;

            // Display kills/deaths
            if (plugin.getGoPVPLink() != null) {
                // Display K/D ratio
                double kd = ((double) plugin.getGoPVPLink().get().getFileManager().getKills(getInspected().getName())) / ((double) plugin.getGoPVPLink().get().getFileManager().getDeaths(getInspected().getName()));
                if (!((Double) kd).isNaN() && !((Double) kd).isInfinite()) {
                    bigDecimal = new BigDecimal(kd);
                    bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
                    kd = bigDecimal.doubleValue();
                    objective.getScore(" " + kd).setScore(currentScore);
                } else {
                    objective.getScore(" -").setScore(currentScore);
                }
                currentScore++;
                objective.getScore(ChatColor.GRAY + "Kills/Deaths:").setScore(currentScore);
                currentScore++;
                // Display deaths
                objective.getScore(ChatColor.RESET + " " + plugin.getGoPVPLink().get().getFileManager().getDeaths(getInspected().getName())).setScore(currentScore);
                currentScore++;
                objective.getScore(ChatColor.GRAY + "Deaths:").setScore(currentScore);
                currentScore++;
                // Display kills
                objective.getScore(ChatColor.WHITE + " " + plugin.getGoPVPLink().get().getFileManager().getKills(getInspected().getName())).setScore(currentScore);
                currentScore++;
                objective.getScore(ChatColor.GRAY + "Kills:").setScore(currentScore);
                currentScore++;

                // Display last seen
                if (!getInspected().isOnline()) {
                    GoCraft.debug("lastPlayed: " + getInspected().getLastPlayed());
                    objective.getScore(" " + Utils.millisToHumanFormat(Calendar.getInstance().getTimeInMillis() - getInspected().getLastPlayed()) + " ago").setScore(currentScore);
                    currentScore++;
                    objective.getScore(ChatColor.GRAY + "Last seen:").setScore(currentScore);
                    //currentScore++;
                }
            }
        } else {
            objective.setDisplayName(ChatColor.GREEN + "▬▬ " + ChatColor.BOLD + "General inspection" + ChatColor.RESET + ChatColor.GREEN + " ▬▬");
            objective.getScore("Inspecting").setScore(1);
        }
        getInspector().setScoreboard(scoreboard);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * Update the inspector about the inspected
     */
    public void updateAll() {
        updateArmor();
        updateInventory();
        updateScoreboard();
    }

    /**
     * Teleport the inspector to the inspected
     */
    public void teleportToInspected() {
        if (!hasInspected()) {
            return;
        }
        Location result = getInspected().getLocation().clone();
        // One block in opposite direction of the direction the inspected is looking at
        result.setPitch(0.0f);
        result = result.subtract(result.getDirection().normalize());
        // One block up
        result.add(0, 1, 0);
        result.setPitch(45);
        this.getInspector().teleport(result);
    }

    /**
     * End this inspection
     */
    public void endInspection() {
        // revert all actions
        if (plugin.getMapSwitcherLink() != null) {
            inspector.teleport(plugin.getMapSwitcherLink().get().getCurrentSpawnLocation());
        } else {
            World spawn = Bukkit.getWorld("world");
            if (spawn != null) {
                inspector.teleport(spawn.getSpawnLocation());
            } else {
                inspector.teleport(inspector.getWorld().getSpawnLocation());
            }
        }
        restoreInspectorState();
        plugin.getInspectionManager().removeInspection(this);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "essentials:vanish " + getInspector().getName() + " off");
        plugin.getInspectionManager().registerUpdater();
    }

    /**
     * Handle a click of the inspector somewhere in his inventory
     *
     * @param slot The slot clicked
     */
    public void handleClick(int slot) {
        InventoryAction action = actions.get(slot);
        if (action != null) {
            action.handleClick();
        }
    }

    /**
     * Store the inspectors state to memory and disk
     */
    public boolean saveInspectorState() {
        // Save inventory to memory and disk
        ItemStack[] inventory = inspector.getInventory().getContents();
        inspectorInventory = new ItemStack[inventory.length];
        for (int i = 0; i < inspectorInventory.length; i++) {
            inspectorInventory[i] = inventory[i];
            plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString() + ".inventory." + i, inspectorInventory[i]);
        }
        ItemStack[] armor = inspector.getInventory().getArmorContents();
        inspectorArmor = new ItemStack[armor.length];
        for (int i = 0; i < inspectorArmor.length; i++) {
            inspectorArmor[i] = armor[i];
            plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString() + ".armor." + i, inspectorArmor[i]);
        }
        // Save active potion effects
        potionEffects = inspector.getActivePotionEffects();
        for (PotionEffect effect : potionEffects) {
            plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString() + ".potioneffects." + effect.getType().getName(), effect.getDuration() + ":" + effect.getAmplifier() + ":" + effect.isAmbient() + ":" + effect.hasParticles());
        }
        // Save gamemode
        gamemode = inspector.getGameMode();
        plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString() + ".gamemode", gamemode.toString());


        // Save config to disk
        if (!plugin.getInspectionManager().saveInspectors()) {
            plugin.message(inspector, "inspect-cancel");
            return false;
        }
        return true;
    }

    /**
     * Restore saved inspector state from the items in memory
     */
    public void restoreInspectorState() {
        // Restore inventory
        inspector.getInventory().setContents(inspectorInventory);
        inspector.getInventory().setArmorContents(inspectorArmor);
        // Restore potion effects
        for (PotionEffect effect : inspector.getActivePotionEffects()) {
            inspector.removePotionEffect(effect.getType());
        }
        inspector.addPotionEffects(potionEffects);
        // Restore gamemode
        inspector.setGameMode(gamemode);


        // Remove from disk storage because everything is restored from memory
        plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString(), null);
        plugin.getInspectionManager().saveInspectors();
        inspector.updateInventory();
    }

    /**
     * Get the ping of a player
     *
     * @param player The player to check
     * @return The ping in ms
     */
    public int getPing(Player player) {
        if (player == null) {
            return -1;
        }
        CraftPlayer cp = (CraftPlayer) player;
        EntityPlayer ep = cp.getHandle();
        return ep.ping;
    }
}
