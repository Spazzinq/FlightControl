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
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.api.APIManager;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.command.*;
import org.spazzinq.flightcontrol.manager.*;
import org.spazzinq.flightcontrol.multiversion.ParticleManager;
import org.spazzinq.flightcontrol.multiversion.current.ParticleManager13;
import org.spazzinq.flightcontrol.multiversion.old.ParticleManager8;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.VersionType;

import java.io.File;
import java.util.UUID;

import static org.spazzinq.flightcontrol.manager.LangManager.msg;

public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private APIManager apiManager = APIManager.getInstance();
    private PluginManager pm = Bukkit.getPluginManager();
    @Getter private File storageFolder = new File(getDataFolder() + File.separator + "data");

    // Storage management
    @Getter private CategoryManager categoryManager;
    @Getter private ConfManager confManager;
    @Getter private LangManager langManager;
    @Getter private UpdateManager updateManager;
    // Multi-version management
    @Getter private HookManager hookManager;
    @Getter private ParticleManager particleManager;
    // In-game management
    @Getter private FlightManager flightManager;
    @Getter private PlayerManager playerManager;
    @Getter private StatusManager statusManager;
    @Getter private TrailManager trailManager;

    @Getter private TempFlyCommand tempFlyCommand;

    public static final UUID spazzinqUUID = UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c");

    public void onEnable() {
        // Create storage folder
        //noinspection ResultOfMethodCallIgnored
        storageFolder.mkdirs();

        // TODO Can we just initialize on declare now?
        registerManagers();
        new Listener(this);
        registerCommands();

        // Ensure all hooks load before managers do
        hookManager.load();
        reloadManagers();

        if (updateManager.getVersion().getVersionType() == VersionType.BETA) {
            getLogger().warning(" \n  _       _       _       _       _       _\n" +
                    " ( )     ( )     ( )     ( )     ( )     ( )\n" +
                    "  X       X       X       X       X       X\n" +
                    "-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,\n" +
                    "      X       X       X       X       X       X\n" +
                    "     (_)     (_)     (_)     (_)     (_)     (_)\n" +
                    " \nVersion " + updateManager.getVersion() + " is currently unstable and should not be run on a production server.\n" +
                    "Thanks for being a FlightControl beta tester!\n \n" +
                    "  _       _       _       _       _       _\n" +
                    " ( )     ( )     ( )     ( )     ( )     ( )\n" +
                    "  X       X       X       X       X       X\n" +
                    "-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,\n" +
                    "      X       X       X       X       X       X\n" +
                    "     (_)     (_)     (_)     (_)     (_)     (_)\n");
        } else {
            // Update check
            if (confManager.isAutoUpdate()) {
                updateManager.install(Bukkit.getConsoleSender(), true);
            } else if (updateManager.exists()) {
                new BukkitRunnable() {
                    @Override public void run() {
                        getLogger().info("Yay! Version " + updateManager.getNewVersion() + " is available for update. Perform \"/fc update\" to update and visit https://www.spigotmc.org/resources/flightcontrol.55168/ to view the feature changes (the config automatically updates).");
                    }
                }.runTaskLater(this, 70);
            }
        }

        // Start FileWatcher
        new FileWatcher(this, getDataFolder().toPath()).runTaskTimer(this, 0, 10);

        new Metrics(this); // bStats
    }

    private void registerManagers() {
        categoryManager = new CategoryManager(this);
        confManager = new ConfManager(this);
        langManager = new LangManager(this);
        updateManager = new UpdateManager(getDescription().getVersion());

        boolean is1_13 = getServer().getBukkitVersion().contains("1.13")
                || getServer().getBukkitVersion().contains("1.14")
                || getServer().getBukkitVersion().contains("1.15");
        hookManager = new HookManager(this, is1_13);
        particleManager = is1_13 ? new ParticleManager13() : new ParticleManager8();

        flightManager = new FlightManager(this);
        playerManager = new PlayerManager(this);
        statusManager = new StatusManager(this);
        trailManager = new TrailManager(this);
    }

    private void registerCommands() {
        tempFlyCommand = new TempFlyCommand(this);
        getCommand("tempfly").setExecutor(tempFlyCommand);
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("flightcontrol").setExecutor(new FlightControlCommand(this));
        getCommand("toggletrail").setExecutor(new ToggleTrailCommand(this));
        getCommand("flyspeed").setExecutor(new FlySpeedCommand(this));
    }

	public void reloadManagers() {
        // Prevent permission auto-granting from "*" permission
        for (World w : Bukkit.getWorlds()) {
            String worldName = w.getName();
            defaultPerms(worldName);

            for (String regionName : getHookManager().getWorldGuard().getRegionNames(w)) {
                defaultPerms(worldName + "." + regionName);
            }
        }

        categoryManager.reloadCategories();
	    confManager.reloadConf();
        langManager.reloadLang();
        // At end to allow for any necessary migration
        confManager.updateConfig();

        playerManager.reloadPlayerData();

        checkPlayers();
    }

    public void checkPlayers() {
        trailManager.disableEnabledTrails();
        for (Player p : Bukkit.getOnlinePlayers()) {
            flightManager.check(p);
            if (p.isFlying()) {
                trailManager.trailCheck(p);
            }
        }
    }

    public void debug(Player p) {
	    Location l = p.getLocation();
        World world = l.getWorld();
        String worldName = world.getName(),
                regionName = getHookManager().getWorldGuard().getRegionName(l);
        Region region = new Region(world, regionName);
        Category category = categoryManager.getCategory(p);

        if (regionName != null) defaultPerms(worldName + "." + regionName); // Register new regions dynamically

        boolean hasWorlds = category.getWorlds() != null,
                hasRegions = category.getRegions() != null,
                hasFactions = category.getFactions() != null;

        // TODO Make this cleaner?
        // config options (settings) and permissions that act upon the same function are listed as
        // setting boolean (space) permission boolean
        msg(p, "&a&lFlightControl &f" + getDescription().getVersion() +
                "\n&eCategory &7» &f" + category.getName() +
                "\n&eW.RG &7» &f" + worldName + "." + regionName +
                ((hookManager.getFactions().isHooked() && hasFactions ? "\n&eFac &7» &f" + category.getFactions() : "") +
                "\n&eWRLDs &7» &f" + category.getWorlds()  +
                "\n&eRGs &7» &f" +  category.getRegions()  +
                "\n \n&e&lEnable" +
                "\n&fBypass &7» " + p.hasPermission("flightcontrol.bypass") + " " + (getConfManager().isVanishBypass() && getHookManager().getVanish().vanished(p)) +
                "\n&fTemp &7» " + playerManager.getFlightPlayer(p).hasTempFly() +
                "\n&fAll &7» " + p.hasPermission("flightcontrol.flyall") +
                (hookManager.getFactions().isHooked() && hasFactions ? "\n&fFC &7» " + getHookManager().getFactions().rel(p, category.getFactions().getEnabled()) : "") +
                (hookManager.getPlot().isHooked() ? "\n&fPlot &7» " + hookManager.getPlot().flightAllowed(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                (hasWorlds ? "\n&fWorld &7» " + category.getWorlds().getEnabled().contains(world) + " " + p.hasPermission("flightcontrol.fly." + worldName) : "") +
                (hasRegions ? "\n&fRegion &7» " + category.getRegions().getEnabled().contains(region) + " " + (regionName != null && p.hasPermission("flightcontrol.fly." + worldName + "." + regionName)) : "") +
                (hookManager.getTowny().isHooked() ? "\n&fTowny &7» "+
                        (confManager.isOwnTown() && hookManager.getTowny().ownTown(p) && !(confManager.isTownyWar() && hookManager.getTowny().wartime())) + " " +
                        (p.hasPermission("flightcontrol.owntown") && hookManager.getTowny().ownTown(p) && (!confManager.isTownyWar() || !hookManager.getTowny().wartime())) : "") +
                (hookManager.getLands().isHooked() ? "\n&fLands &7» " +
                        (confManager.isOwnLand() && hookManager.getLands().ownLand(p)) + " " +
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

    public void defaultPerms(String suffix) {
	    setPerm("flightcontrol.fly." + suffix);
	    setPerm("flightcontrol.nofly." + suffix);
    }

    private void setPerm(String permString) {
	    Permission perm = pm.getPermission(permString);

	    if (perm == null) {
	        pm.addPermission(new Permission(permString, PermissionDefault.FALSE));
        }
    }

    @Deprecated @Override public FileConfiguration getConfig() {
        return super.getConfig();
    }
}
