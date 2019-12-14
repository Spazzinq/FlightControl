/*
 * This file is part of FlightControl, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
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
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.api.APIManager;
import org.spazzinq.flightcontrol.command.*;
import org.spazzinq.flightcontrol.hook.HookManager;
import org.spazzinq.flightcontrol.manager.*;
import org.spazzinq.flightcontrol.multiversion.Particles;
import org.spazzinq.flightcontrol.multiversion.current.Particles13;
import org.spazzinq.flightcontrol.multiversion.old.Particles8;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.Region;
import org.spazzinq.flightcontrol.util.ActionbarUtil;

import java.io.File;

public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private APIManager apiManager = APIManager.getInstance();
    private PluginManager pm = Bukkit.getPluginManager();
    @Getter private File storageFolder = new File(getDataFolder() + File.separator + "data");

    @Getter private CategoryManager categoryManager;
    @Getter private ConfigManager configManager;
    @Getter private FlightManager flightManager;
    @Getter private HookManager hookManager;
    @Getter private PlayerManager playerManager;
    @Getter private StatusManager statusManager;
    @Getter private TempFlyManager tempflyManager;
    @Getter private TrailManager trailManager;

    @Getter private Particles particles;

    @Getter private Updater updater;
    @Getter private TempFlyCommand tempFlyCommand;

    public void onEnable() {
	    // Prepare storage folder
        //noinspection ResultOfMethodCallIgnored
        storageFolder.mkdirs();

        boolean is1_13 = getServer().getBukkitVersion().contains("1.13") || getServer().getBukkitVersion().contains("1.14");
        particles = is1_13 ? new Particles13() : new Particles8();
        // Load hooks
        hookManager = new HookManager(this, is1_13);
        // Ensure all plugins load before hook does
        new BukkitRunnable() {
            @Override
            public void run() {
                hookManager.load();
            }
        }.runTaskLater(this, 60);


        // Load classes

        // Load FlightManager before all because Config uses it & only needs to initialize pl
        flightManager = new FlightManager(this);
        categoryManager = new CategoryManager(this);
        configManager = new ConfigManager(this);
        // Resources from Config
        trailManager = new TrailManager(this);
        // Variable in tempflyManager
        playerManager = new PlayerManager(this);
        // Resources from FlightManager
        tempflyManager = new TempFlyManager(this);
        statusManager = new StatusManager(this);

        new ActionbarUtil();
        new Listener(this);
        updater = new Updater(getDescription().getVersion());

        // Load commands
        tempFlyCommand = new TempFlyCommand(this);
        getCommand("tempfly").setExecutor(tempFlyCommand);
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("flightcontrol").setExecutor(new FlightControlCommand(this));
        getCommand("toggletrail").setExecutor(new ToggleTrailCommand(this));
        getCommand("flyspeed").setExecutor(new FlySpeedCommand(this));

        if (configManager.isAutoUpdate()) {
            updater.install(Bukkit.getConsoleSender(), true);
        }
        else if (updater.exists()) {
            new BukkitRunnable() {
                @Override public void run() {
                    getLogger().info("flightcontrol " + updater.newVer() + " is available for update. Perform \"/fc update\" to update and " + "visit https://www.spigotmc.org/resources/flightcontrol.55168/ to view the feature changes (the config automatically updates).");
                }
            }.runTaskLater(this, 50);
        }

        reload();

        new Metrics(this); // bStats
    }

	public void reload() {
        // Prevent permission auto-granting from "*" permission
        for (World w : Bukkit.getWorlds()) {
            String name = w.getName();
            defaultPerms(name);
            for (String rg : getHookManager().getWorldGuard().getRegionNames(w)) {
                defaultPerms(name + "." + rg);
            }
        }

        categoryManager.reloadCategories();
	    configManager.reloadConfig();
        configManager.updateConfig();
        playerManager.reloadPlayerData();
	    tempflyManager.reloadTempflyData();

	    trailManager.disableEnabledTrails();
        for (Player p : Bukkit.getOnlinePlayers()) {
            flightManager.check(p);
            trailManager.trailCheck(p);
        }
    }

    public void debug(Player p) {
	    Location l = p.getLocation();
        World world = l.getWorld();
        String worldName = world.getName(),
                regionName = getHookManager().getWorldGuard().getRegionName(l);
        Region region = new Region(world, regionName);

        if (regionName != null) defaultPerms(worldName + "." + regionName); // Register new regions dynamically

        // TODO Cached category grabbing
        Category category = null;
        for (Category c : getCategoryManager().getCategories()) {
            if (p.hasPermission("flightcontrol.category." + c.getName())) {
                category = c;
            }
        }
        if (category == null) {
            category = getCategoryManager().getGlobal();
        }

        boolean hasWorlds = category.getWorlds() != null,
                hasRegions = category.getRegions() != null,
                hasFactions = category.getFactions() != null;

        // config options (settings) and permissions that act upon the same function are listed as
        // setting boolean (space) permission boolean
        msg(p, "&a&lFlightControl &f" + getDescription().getVersion() +
                "\n&eCategory &7» &f" + category.getName() +
                "\n&eW.RG &7» &f" + worldName + "." + regionName +
                ((hookManager.getFactions().isHooked() && hasFactions ? "\n&eFac &7» &f" + category.getFactions() : "") +
                "\n&eWRLDs &7» &f" + category.getWorlds()  +
                "\n&eRGs &7» &f" +  category.getRegions()  +
                "\n \n&e&lEnable" +
                "\n&fBypass &7» " + p.hasPermission("flightcontrol.bypass") + " " + (getConfigManager().isVanishBypass() && getHookManager().getVanish().vanished(p)) +
                "\n&fTemp &7» " + playerManager.getFlightPlayer(p).hasTempFly() +
                "\n&fAll &7» " + p.hasPermission("flightcontrol.flyall") +
                (hookManager.getFactions().isHooked() && hasFactions ? "\n&fFC &7» " + getHookManager().getFactions().rel(p, category.getFactions().getEnabled()) : "") +
                (hookManager.getPlot().isHooked() ? "\n&fPlot &7» " + hookManager.getPlot().flightAllowed(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                (hasWorlds ? "\n&fWorld &7» " + category.getWorlds().getEnabled().contains(world) + " " + p.hasPermission("flightcontrol.fly." + worldName) : "") +
                (hasRegions ? "\n&fRegion &7» " + category.getRegions().getEnabled().contains(region) + " " + (regionName != null && p.hasPermission("flightcontrol.fly." + worldName + "." + regionName)) : "") +
                (hookManager.getTowny().isHooked() ? "\n&fTowny &7» "+
                        (configManager.isOwnTown() && hookManager.getTowny().ownTown(p) && !(configManager.isTownyWar() && hookManager.getTowny().wartime())) + " " +
                        (p.hasPermission("flightcontrol.owntown") && hookManager.getTowny().ownTown(p) && (!configManager.isTownyWar() || !hookManager.getTowny().wartime())) : "") +
                (hookManager.getLands().isHooked() ? "\n&fLands &7» " +
                        (configManager.isOwnLand() && hookManager.getLands().ownLand(p)) + " " +
                        (p.hasPermission("flightcontrol.ownland") && hookManager.getLands().ownLand(p)) : "") +
                "\n \n&e&lDisable" +
                (hookManager.getFactions().isHooked() ? "\n&fFC &7» " + getHookManager().getFactions().rel(p, category.getFactions().getDisabled()) : "") +
                (hookManager.getCombat().isHooked() ? "\n&fCombat &7» " + hookManager.getCombat().tagged(p) : "") +
                (hookManager.getPlot().isHooked() ? "\n&fPlot &7» " + hookManager.getPlot().flightDenied(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                (hasWorlds ? "\n&fWorld &7» " + category.getWorlds().getDisabled().contains(world) + " " + p.hasPermission("flightcontrol.nofly." + worldName) : "") +
                (hasRegions ? "\n&fRegion &7» " + category.getRegions().getDisabled().contains(region) + " " + (regionName != null && p.hasPermission("flightcontrol.nofly." + worldName + "." + regionName)) : ""))

                            .replaceAll("false", "&cfalse")
                            .replaceAll("true", "&atrue"));
    }

    public static void msg(CommandSender s, String msg) {
	    msg(s, msg, false);
	}
    public static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            String finalMsg = msg;

            if (s instanceof ConsoleCommandSender) {
                finalMsg = finalMsg.replaceAll("FlightControl &7» ", "[FlightControl] ").replaceAll("»", "-");
            }
            finalMsg = ChatColor.translateAlternateColorCodes('&', finalMsg);

            if (actionBar && s instanceof Player) {
                ActionbarUtil.send((Player) s, finalMsg);
            }
            else {
                s.sendMessage(finalMsg);
            }
        }
    }

    public void defaultPerms(String suffix) {
	    setPerm("flightcontrol.fly." + suffix);
	    setPerm("flightcontrol.nofly." + suffix);
    }

    private void setPerm(String permString) {
	    Permission perm = pm.getPermission(permString);

	    if (perm == null) {
	        pm.addPermission(new Permission(permString, PermissionDefault.FALSE));
        } else {
	        perm.setDefault(PermissionDefault.FALSE);
        }
    }

    @Deprecated @Override public FileConfiguration getConfig() {
        return super.getConfig();
    }
}
