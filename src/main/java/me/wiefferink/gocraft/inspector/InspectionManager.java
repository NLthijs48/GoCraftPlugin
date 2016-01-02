package me.wiefferink.gocraft.inspector;

import com.google.common.base.Charsets;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.storage.UTF8Config;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class InspectionManager {

    private Map<UUID, Inspection> currentInspections;
    private final GoCraft plugin;
    private UTF8Config inspectorStorage;
    private File inspectorsFile;
    private boolean updaterRegistered = false;
    private UpdateListener updateListener;

    public InspectionManager(final GoCraft plugin) {
        this.plugin = plugin;
        inspectorsFile = new File(plugin.getDataFolder(), "inspectors.yml");
        currentInspections = new HashMap<>();
        setupListeners();
        loadInspectors();

        // Keep inspectors inside the map
        if (plugin.getMapSwitcherLink() != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Inspection inspection : plugin.getInspectionManager().getCurrentInspections().values()) {
                        if (!plugin.getMapSwitcherLink().get().isInsideCurrentMap(inspection.getInspector())) {
                            plugin.message(inspection.getInspector(), "inspect-outsideMap");
                            inspection.teleportToInspected();
                        }
                    }
                }
            }.runTaskTimer(plugin, 20, 20);
        }
        // Update inventory fully every so often
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Inspection inspection : GoCraft.getInstance().getInspectionManager().getCurrentInspections().values()) {
                    inspection.updateInventory();
                }
            }
        }.runTaskTimer(plugin, 100, 100);
    }

    /**
     * Setup listeners
     */
    public void setupListeners() {
        plugin.getServer().getPluginManager().registerEvents(new QuitJoinListener(this), plugin);
        plugin.getServer().getPluginManager().registerEvents(new InventoryListener(this), plugin);
    }

    /**
     * Setup an inspection session
     *
     * @param inspector The player that is going to be inspecting
     * @param inspected The player that is going to be inspected
     * @return The inspection session
     */
    public Inspection setupInspection(Player inspector, Player inspected) {
        return new Inspection(plugin, inspector, inspected);
    }

    /**
     * Start an inspection, add this inspection to the manager
     *
     * @param inspection Inspection to start and add
     */
    public void addInspection(Inspection inspection) {
        currentInspections.put(inspection.getInspector().getUniqueId(), inspection);
    }

    /**
     * End inspection
     *
     * @param inspection Inspection to end
     */
    public void removeInspection(Inspection inspection) {
        currentInspections.remove(inspection.getInspector().getUniqueId());
    }

    /**
     * Get an inspection session from the inspector
     *
     * @param inspector The inspector to check
     * @return The inspection session that this inspector is in or null if none
     */
    public Inspection getInspectionByInspector(Player inspector) {
        return currentInspections.get(inspector.getUniqueId());
    }

    /**
     * Get the inspections that are ongoing about a player
     *
     * @param inspected The inspected player
     * @return A set of all inspections going on on this player
     */
    public Set<Inspection> getInspectionsByInspected(Player inspected) {
        Set<Inspection> result = new HashSet<>();
        for (Inspection inspection : currentInspections.values()) {
            if (inspection.isInspected(inspected)) {
                result.add(inspection);
            }
        }
        return result;
    }

    /**
     * Get the current inspections
     *
     * @return A map of the inspector UUID to the inspection
     */
    public Map<UUID, Inspection> getCurrentInspections() {
        return currentInspections;
    }

    /**
     * Handle a leave/kick/etc. of a player
     *
     * @param player The player that is gone now
     */
    public void handlePlayerStopped(Player player) {
        if (player == null) {
            return;
        }
        Inspection inspection = getInspectionByInspector(player);
        if (inspection != null) {
            inspection.endInspection();
        }
        for (Inspection inspect : getInspectionsByInspected(player)) {
            plugin.message(inspect.getInspector(), "inspect-inspectedLeft", inspect.getInspected().getName());
            inspect.endInspection();
        }
    }

    /**
     * Restore saved old inventory if that exists (could happen when server crashed while spectating)
     *
     * @param player Player to (possibly) restore an old inventory for
     */
    public void restoreOldInventory(final Player player) {
        if (!getInspectorStorage().contains(player.getUniqueId().toString())) {
            return;
        }
        // Restore gamemode
        String gamemodeString = getInspectorStorage().getString(player.getUniqueId().toString() + ".gamemode");
        GameMode gamemode = GameMode.valueOf(gamemodeString);
        if (gamemode == null) {
            gamemode = GameMode.SURVIVAL;
        }
        player.setGameMode(gamemode);
        // Restore inventory
        ItemStack[] inventory = player.getInventory().getContents();
        for (int i = 0; i < inventory.length; i++) {
            ItemStack item = getInspectorStorage().getItemStack(player.getUniqueId().toString() + ".inventory." + i);
            inventory[i] = item;
        }
        player.getInventory().setContents(inventory);
        // Restore armor
        ItemStack[] armor = player.getInventory().getArmorContents();
        for (int i = 0; i < armor.length; i++) {
            ItemStack item = getInspectorStorage().getItemStack(player.getUniqueId().toString() + ".armor." + i);
            armor[i] = item;
        }
        player.getInventory().setArmorContents(armor);
        // Restore potion effects
        Collection<PotionEffect> potionEffects = player.getActivePotionEffects();
        for (PotionEffect effect : potionEffects) {
            player.removePotionEffect(effect.getType());
        }
        ConfigurationSection section = getInspectorStorage().getConfigurationSection(player.getUniqueId().toString() + ".potioneffects");
        if (section != null) {
            for (String effectString : section.getKeys(false)) {
                PotionEffectType effect = PotionEffectType.getByName(effectString);
                String optionsString = section.getString(effectString);
                GoCraft.debug("potion restore: " + effectString + ", options: " + optionsString);
                if (optionsString == null || effect == null) {
                    GoCraft.debug("  no effect found");
                    continue;
                }
                String[] options = optionsString.split(":");
                if (options.length < 4) {
                    GoCraft.debug("  not enough options");
                    continue;
                }
                int duration, amplifier;
                boolean ambient, particles;
                try {
                    duration = Integer.parseInt(options[0]);
                    amplifier = Integer.parseInt(options[1]);
                } catch (NumberFormatException e) {
                    GoCraft.debug("  not numbers");
                    continue;
                }
                ambient = "true".equalsIgnoreCase(options[2]);
                particles = "true".equalsIgnoreCase(options[3]);
                GoCraft.debug("duration=" + duration + ", amplifier=" + amplifier + ", ambient=" + ambient + ", particles=" + particles);
                final PotionEffect finalEffect = new PotionEffect(effect, duration, amplifier, ambient, particles);
                player.addPotionEffect(finalEffect, false);
            }
        }
        getInspectorStorage().set(player.getUniqueId().toString(), null);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "essentials:vanish " + player.getName() + " off");
        saveInspectors();
    }

    /**
     * Handle a server stop, nicely close current inspections
     */
    public void handleServerStop() {
        for (Inspection inspection : currentInspections.values()) {
            inspection.endInspection();
        }
    }

    /**
     * Check if a player is inspecting
     *
     * @param inspector The player to check
     * @return true if the player is inspecting, otherwise false
     */
    public boolean isInspecting(Player inspector) {
        return getInspectionByInspector(inspector) != null;
    }

    /**
     * Register or deregister updater as needed
     */
    public void registerUpdater() {
        if (currentInspections.isEmpty() && updaterRegistered) {
            HandlerList.unregisterAll(updateListener);
            updaterRegistered = false;
            updateListener = null;
        } else if (!currentInspections.isEmpty() && !updaterRegistered) {
            updateListener = new UpdateListener(plugin);
            plugin.getServer().getPluginManager().registerEvents(updateListener, plugin);
            updaterRegistered = true;
        }
    }


    /**
     * Load the default.yml file
     */
    public void loadInspectors() {
        // Safe the file from the jar to disk if it does not exist
        if (inspectorsFile.exists()) {
            // Load inspectorStorage.yml from the plugin folder
            try (
                    InputStreamReader reader = new InputStreamReader(new FileInputStream(inspectorsFile), Charsets.UTF_8)
            ) {
                inspectorStorage = UTF8Config.loadConfiguration(reader);
            } catch (IOException e) {
                plugin.getLogger().warning("Loading the inspectory inventories failed: " + inspectorsFile.getAbsolutePath());
            }
        }
        if (inspectorStorage == null) {
            inspectorStorage = new UTF8Config();
        }
    }

    /**
     * Save the inspectors config file
     *
     * @return true if the file has been saved correctly, otherwise false
     */
    public boolean saveInspectors() {
        try {
            inspectorStorage.save(inspectorsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save inspector storage file: " + inspectorsFile.toString());
            return false;
        }
        return true;
    }

    /**
     * Get the inspectors config file
     *
     * @return The inspectors config file
     */
    public YamlConfiguration getInspectorStorage() {
        return inspectorStorage;
    }

}
