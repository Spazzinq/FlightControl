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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.api.APIManager;
import org.spazzinq.flightcontrol.command.*;
import org.spazzinq.flightcontrol.manager.*;
import org.spazzinq.flightcontrol.multiversion.Particle;
import org.spazzinq.flightcontrol.multiversion.current.Particle13;
import org.spazzinq.flightcontrol.multiversion.legacy.Particle8;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.util.CheckUtil;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

/**
 * The main singleton class for FlightControl, a {@link org.bukkit.plugin.java.JavaPlugin JavaPlugin}.
 */
public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private static FlightControl instance;
    @Getter private final APIManager apiManager = APIManager.getInstance();
    // Storage management
    @Getter private final File storageFolder = new File(getDataFolder() + File.separator + "data");
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

    // My Minecraft UUID used to notify me of the plugin version and allow debugging (after granted permission)
    public static final UUID spazzinqUUID = UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c");

    public void onEnable() {
        instance = this;

        // Create storage folder
        storageFolder.mkdirs();

        // Registration
        registerManagers();
        registerCommands();
        new EventListener(this);

        // Load
        load();
        updateManager.checkOnStartup();

        // Start config watching service (on-the-fly editing)
        new PathWatcher(this, getDataFolder().toPath()).runTaskTimer(this, 0, 10);
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

    /**
     * Registers all relevant managers in one nice method.
     */
    private void registerManagers() {
        categoryManager = new CategoryManager(this);
        confManager = new ConfManager(this);
        langManager = new LangManager(this);
        updateManager = new UpdateManager(this);

        boolean v1_13 = false;

        for (int i = 13; i < 18; i++) {
            if (getServer().getBukkitVersion().contains("1." + i)) {
                v1_13 = true;
                break;
            }
        }

        checkManager = new CheckManager(this);
        hookManager = new HookManager(this, v1_13);
        particle = v1_13 ? new Particle13() : new Particle8();

        flightManager = new FlightManager(this);
        playerManager = new PlayerManager(this);
        statusManager = new StatusManager(this);
        factionsManager = new FactionsManager(this);
        trailManager = new TrailManager(this);
    }

    private void registerCommands() {
        getCommand("tempfly").setExecutor(new TempflyCommand(this));
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("flightcontrol").setExecutor(new FlightControlCommand(this));
        getCommand("toggletrail").setExecutor(new ToggleTrailCommand(this));
        getCommand("flyspeed").setExecutor(new FlySpeedCommand(this));
    }

    /**
     * Loads config data, relevant checks, categories, and player data.
     */
    public void load() {
        confManager.loadConf();
        langManager.loadLang();
        // Allow for any necessary config migration from previous versions
        confManager.updateConf();
        langManager.updateLang();
        // Load config first so the check manager knows which checks to use
        checkManager.loadChecks();
        hookManager.loadHooks();
        categoryManager.loadCategories();
        playerManager.loadPlayerData();

        checkPlayers();
    }

    /**
     * Verifies trail and flight access for all online players.
     */
    // TODO MOVE
    public void checkPlayers() {
        trailManager.removeEnabledTrails();
        for (Player p : Bukkit.getOnlinePlayers()) {
            flightManager.check(p);
            if (p.isFlying()) {
                trailManager.trailCheck(p);
            }
        }
    }

    /**
     * Sends debug information about a player's flight status.
     *
     * @param sender the recipient of the debug message
     * @param targetPlayer the target of the debug check
     */
    public void debug(CommandSender sender, Player targetPlayer) {
        Location l = targetPlayer.getLocation();
        World world = l.getWorld();
        String regionName = getHookManager().getWorldGuardHook().getRegionName(l);
        Category category = categoryManager.getCategory(targetPlayer);

        // config options (settings) and permissions that act upon the same function are listed as
        // setting boolean (space) permission boolean
        msg(sender, "&a&lFlightControl &f" + getDescription().getVersion() +
                "\n&eTarget &7» &f" + targetPlayer.getName() +
                "\n&eCategory &7» &f" + category.getName() +
                (hookManager.getWorldGuardHook().isHooked() ? "\n&eW.RG &7» &f" + world.getName() + "." + regionName : "") +
                (hookManager.getFactionsHook().isHooked() ? "\n&eFac &7» &f" + category.getFactions() : "") +
                "\n&eWRLDs &7» &f" + category.getWorlds() +
                (hookManager.getWorldGuardHook().isHooked() ? "\n&eRGs &7» &f" + category.getRegions() : "") +
                ("\n&eBypass &7» &f" + CheckUtil.checkAll(checkManager.getBypassChecks(), targetPlayer, true)));

        statusManager.checkEnable(targetPlayer, sender);
        statusManager.checkDisable(targetPlayer, sender);
    }
}
