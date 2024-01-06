/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol;

import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.command.*;
import org.spazzinq.flightcontrol.manager.*;
import org.spazzinq.flightcontrol.multiversion.Particle;
import org.spazzinq.flightcontrol.multiversion.current.ParticleNewAPI;
import org.spazzinq.flightcontrol.multiversion.legacy.ParticleOldAPI;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

/**
 * The main singleton class for FlightControl, a {@link org.bukkit.plugin.java.JavaPlugin JavaPlugin}.
 */
public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private static FlightControl instance;
    // Storage management
    @Getter private CategoryManager categoryManager;
    @Getter private RewardManager rewardManager;
    @Getter private ConfManager confManager;
    @Getter private LangManager langManager;
    @Getter private UpdateManager updateManager;
    // Multi-version management
    @Getter private CheckManager checkManager;
    @Getter private HookManager hookManager;
    @Getter private Particle particle;
    // In-game management
    @Getter private FlightManager flightManager;
    @Getter private PlayerManager playerManager;
    @Getter private StatusManager statusManager;
    @Getter private FactionsManager factionsManager;
    @Getter private TrailManager trailManager;
    @Getter private StickybarManager stickybarManager;
    // Misc. management
    @Getter private PermissionManager permissionManager;

    // Used for support (after admin grants permission)
    public static final UUID spazzinqUUID = UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c");

    public void onEnable() {
        instance = this;

        // Registration
        registerManagers();
        new EventListener();
        // Load and check
        load();
        new BukkitRunnable() {
            @Override public void run() {
                updateManager.checkForUpdate(Bukkit.getConsoleSender());
            }
        }.runTaskLater(this, 70);

        // Start bStats
        new Metrics(this, 4704); // 4704 = plugin ID
    }

    @Override public void onDisable() {
        playerManager.savePlayerData();

        trailManager.disableAllTrails();
        stickybarManager.disableAllStickybars();
    }

    private void registerManagers() {
        boolean isNewSpigotAPI = false;

        for (int i = 13; i < 21; i++) {
            if (getServer().getBukkitVersion().contains("1." + i)) {
                isNewSpigotAPI = true;
                break;
            }
        }

        categoryManager = new CategoryManager();
        rewardManager = new RewardManager();
        confManager = new ConfManager();
        langManager = new LangManager();
        updateManager = new UpdateManager();

        checkManager = new CheckManager();
        hookManager = new HookManager(isNewSpigotAPI);
        particle = isNewSpigotAPI ? new ParticleNewAPI() : new ParticleOldAPI();

        flightManager = new FlightManager();
        playerManager = new PlayerManager();
        statusManager = new StatusManager();
        factionsManager = new FactionsManager();
        trailManager = new TrailManager();
        stickybarManager = new StickybarManager();

        permissionManager = new PermissionManager();
    }

    // Always not null because of plugin.yml
    public void registerCommands() {
        getCommand("tempfly").setExecutor(new TempflyCommand());
        // Dependent on ConfManager
        registerFlyCommand();
        getCommand("flightcontrol").setExecutor(new FlightControlCommand());
        getCommand("toggletrail").setExecutor(new ToggleTrailCommand());
        getCommand("flyspeed").setExecutor(new FlySpeedCommand());
    }

    /**
     * Loads config data, relevant checks, categories, and player data.
     */
    public void load() {
        confManager.load();
        langManager.load();
        // Load config first so the check manager knows which checks to use
        checkManager.loadChecks();
        hookManager.loadHooks();
        categoryManager.loadCategories();
        playerManager.loadPlayerData();
        rewardManager.loadRewards();
        // Check connected players
        flightManager.checkAllPlayers();
        trailManager.checkAllPlayers();
        stickybarManager.checkAllPlayers();

        // Register commands
        registerCommands();

        // Start config watching service (on-the-fly editing)
        if (confManager.isAutoReload()) {
            new PathWatcher(this, getDataFolder().toPath()).runTaskTimer(this, 0, 10);
        }

    }

    private void registerFlyCommand() {
        String flyCmdName = confManager.getFlyCommandName().toLowerCase();

        try {
            // Reflection to add command
            Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap"), knownCmds = SimpleCommandMap.class.getDeclaredField("knownCommands");
            Constructor<PluginCommand> plCmd = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            cmdMap.setAccessible(true); knownCmds.setAccessible(true); plCmd.setAccessible(true);
            CommandMap map = (CommandMap) cmdMap.get(Bukkit.getServer());
            @SuppressWarnings("unchecked") Map<String, Command> kCMDMap = (Map<String, Command>) knownCmds.get(cmdMap.get(Bukkit.getServer()));
            PluginCommand fly = plCmd.newInstance(flyCmdName, this);
            String plName = getDescription().getName();

            fly.setDescription("Toggles flight");
            fly.setAliases(Arrays.asList("fcfly", "ffly"));
            map.register(plName, fly);
            kCMDMap.put(plName.toLowerCase() + ":" + flyCmdName, fly);
            kCMDMap.put(flyCmdName, fly);

            // Try catch handles null
            getCommand(flyCmdName).setExecutor(new FlyCommand());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
