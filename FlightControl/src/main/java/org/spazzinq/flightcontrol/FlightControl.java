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

import static org.spazzinq.flightcontrol.manager.PermissionManager.*;
import static org.spazzinq.flightcontrol.object.FlyPermission.*;
import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private final APIManager apiManager = APIManager.getInstance();
    // Storage management
    @Getter private final File storageFolder = new File(getDataFolder() + File.separator + "data");
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

    private final PluginManager pm = Bukkit.getPluginManager();
    public static final UUID spazzinqUUID = UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c");

    public void onEnable() {
        // Create storage folder
        //noinspection ResultOfMethodCallIgnored
        storageFolder.mkdirs();

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
        }

        // Update check
        if (confManager.isAutoUpdate()) {
            updateManager.installUpdate(Bukkit.getConsoleSender(), true);
        } else if (updateManager.updateExists()) {
            new BukkitRunnable() {
                @Override public void run() {
                    getLogger().info("Yay! Version " + updateManager.getNewVersion() + " is available for update. Perform \"/fc update\" to update and visit https://www.spigotmc.org/resources/55168/ to view the feature changes (the configs automatically update).");
                }
            }.runTaskLater(this, 70);
        }

        // Start file watching service
        new FileWatcher(this, getDataFolder().toPath()).runTaskTimer(this, 0, 10);
        // Start bStats
        new MetricsLite(this);
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
        getCommand("tempfly").setExecutor(new TempFlyCommand(this));
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

            for (String regionName : getHookManager().getWorldGuardHook().getRegionNames(w)) {
                defaultPerms(worldName + "." + regionName);
            }
        }

        categoryManager.reloadCategories();
	    confManager.loadConf();
        langManager.loadLang();
        // At end to allow for any necessary migration
        confManager.updateConfig();

        playerManager.loadPlayerData();

        checkPlayers();
    }

    public void checkPlayers() {
        trailManager.removeEnabledTrails();
        for (Player p : Bukkit.getOnlinePlayers()) {
            flightManager.check(p);
            if (p.isFlying()) {
                trailManager.trailCheck(p);
            }
        }
    }

    // TODO Clean this up
    public void debug(CommandSender s, Player p) {
	    Location l = p.getLocation();
        World world = l.getWorld();
        String worldName = world.getName(),
                regionName = getHookManager().getWorldGuardHook().getRegionName(l);
        Region region = new Region(world, regionName);
        Category category = categoryManager.getCategory(p);

        boolean landsOwnerHasTrusted = false;

        if (hookManager.getLandsHook().isHooked()) {
            Player landsOwner = Bukkit.getPlayer(hookManager.getLandsHook().getOwnerUUID(l));

            landsOwnerHasTrusted = hasPermission(landsOwner, LANDS_TRUSTED);
        }

        if (regionName != null) {
            defaultPerms(worldName + "." + regionName); // Register new regions dynamically
        }

        boolean hasWorlds = category.getWorlds() != null,
                hasRegions = category.getRegions() != null,
                hasFactions = category.getFactions() != null;

        // config options (settings) and permissions that act upon the same function are listed as
        // setting boolean (space) permission boolean
        msg(s, "&a&lFlightControl &f" + getDescription().getVersion() +
                "\n&eTarget &7» &f" + p.getName() +
                "\n&eCategory &7» &f" + category.getName() +
                "\n&eW.RG &7» &f" + worldName + "." + regionName +
                ((hookManager.getFactionsHook().isHooked() && hasFactions ? "\n&eFac &7» &f" + category.getFactions() : "") +
                "\n&eWRLDs &7» &f" + category.getWorlds()  +
                "\n&eRGs &7» &f" +  category.getRegions()  +
                "\n \n&e&lEnable" +
                "\n&fBypass &7» " + hasPermission(p, BYPASS) + " " + (getConfManager().isVanishBypass() && getHookManager().getVanishHook().vanished(p)) +
                "\n&fTemp &7» " + playerManager.getFlightPlayer(p).hasTempFly() +
                "\n&fAll &7» " + hasPermission(p, FLY_ALL) +
                (hookManager.getFactionsHook().isHooked() && hasFactions ? "\n&fFC &7» " + getHookManager().getFactionsHook().rel(p, category.getFactions().getEnabled()) : "") +
                (hookManager.getPlotHook().isHooked() ? "\n&fPlot &7» " + hookManager.getPlotHook().canFly(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                (hasWorlds ? "\n&fWorld &7» " + category.getWorlds().getEnabled().contains(world) + " " + hasPermissionFly(p, worldName) : "") +
                (hasRegions ? "\n&fRegion &7» " + category.getRegions().getEnabled().contains(region) + " " + (regionName != null && hasPermissionFly(p, worldName + "." + regionName)) : "") +
                (hookManager.getTownyHook().isHooked() ? "\n&fTowny &7» "+
                        (confManager.isTownyOwn() && hookManager.getTownyHook().townyOwn(p) && !(confManager.isTownyWarDisable() && hookManager.getTownyHook().wartime())) + " " +
                        (hasPermission(p, TOWNY_OWN) && hookManager.getTownyHook().townyOwn(p) && (!confManager.isTownyWarDisable() || !hookManager.getTownyHook().wartime())) : "") +
                (hookManager.getLandsHook().isHooked() ? "\n&fLands &7» " +
                        (confManager.isLandsOwnEnable() && hookManager.getLandsHook().landsOwn(p)) + " " +
                        (hasPermission(p, LANDS_OWN) && hookManager.getLandsHook().landsOwn(p)) + " " +
                        ((confManager.isLandsOwnEnable() && confManager.isLandsTrusted() || landsOwnerHasTrusted) && hookManager.getLandsHook().landsTrusted(p)) + " " +
                        ((hasPermission(p, LANDS_TRUSTED) || landsOwnerHasTrusted) && hookManager.getLandsHook().landsTrusted(p)) + " " +
                        (landsOwnerHasTrusted) : "") +
                (hookManager.getEnchantmentsHook().isHooked() ? "\n&fEnchants &7» " + hookManager.getEnchantmentsHook().canFly(p) : "") +
                "\n \n&e&lDisable" +
                (hookManager.getFactionsHook().isHooked() ? "\n&fFC &7» " + getHookManager().getFactionsHook().rel(p, category.getFactions().getDisabled()) : "") +
                (hookManager.getCombatHook().isHooked() ? "\n&fCombat &7» " + hookManager.getCombatHook().tagged(p) : "") +
                (hookManager.getPlotHook().isHooked() ? "\n&fPlot &7» " + hookManager.getPlotHook().cannotFly(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                (hasWorlds ? "\n&fWorld &7» " + category.getWorlds().getDisabled().contains(world) + " " + hasPermissionNoFly(p, worldName) : "") +
                (hasRegions ? "\n&fRegion &7» " + category.getRegions().getDisabled().contains(region) + " " + (regionName != null && hasPermissionNoFly(p, worldName + "." + regionName)) : ""))

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
}
