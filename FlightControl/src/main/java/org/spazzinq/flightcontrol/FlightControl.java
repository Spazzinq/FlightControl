/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.spazzinq.flightcontrol;

import lombok.Getter;
import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.command.*;
import org.spazzinq.flightcontrol.manager.*;
import org.spazzinq.flightcontrol.multiversion.Particle;
import org.spazzinq.flightcontrol.multiversion.current.Particle13;
import org.spazzinq.flightcontrol.multiversion.legacy.Particle8;

import java.util.UUID;

/**
 * The main singleton class for FlightControl, a {@link org.bukkit.plugin.java.JavaPlugin JavaPlugin}.
 */
public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private static FlightControl instance;
    // Storage management
    @Getter private CategoryManager categoryManager;
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
    // Misc. management
    @Getter private PermissionManager permissionManager;

    // Used for support (checking plugin version, debugging after granted permission)
    public static final UUID spazzinqUUID = UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c");

    public void onEnable() {
        instance = this;

        // Registration
        registerManagers();
        registerCommands();
        new EventListener(this);

        // Load and check
        load();
        new BukkitRunnable() {
            @Override public void run() {
                updateManager.checkForUpdate();
            }
        }.runTaskAsynchronously(this);

        // Start bStats
        new MetricsLite(this, 4704); // 4704 = plugin ID
    }

    @Override public void onDisable() {
        playerManager.savePlayerData();

        // Just in case the task isn't automatically cancelled
        for (Player p : Bukkit.getOnlinePlayers()) {
            trailManager.trailRemove(p);
        }
    }

    private void registerManagers() {
        // TODO Change name
        boolean v1_13 = false;

        for (int i = 13; i < 18; i++) {
            if (getServer().getBukkitVersion().contains("1." + i)) {
                v1_13 = true;
                break;
            }
        }

        categoryManager = new CategoryManager();
        confManager = new ConfManager();
        langManager = new LangManager();
        updateManager = new UpdateManager();

        checkManager = new CheckManager();
        hookManager = new HookManager(v1_13);
        particle = v1_13 ? new Particle13() : new Particle8();

        flightManager = new FlightManager();
        playerManager = new PlayerManager();
        statusManager = new StatusManager();
        factionsManager = new FactionsManager();
        trailManager = new TrailManager();

        permissionManager = new PermissionManager();
    }

    private void registerCommands() {
        getCommand("tempfly").setExecutor(new TempflyCommand());
        getCommand("fly").setExecutor(new FlyCommand());
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
        // Check connected players
        flightManager.checkAllPlayers();
        trailManager.checkAllPlayers();

        // Start config watching service (on-the-fly editing)
        if (confManager.isAutoReload()) {
            new PathWatcher(this, getDataFolder().toPath()).runTaskTimer(this, 0, 10);
        }

    }
}
