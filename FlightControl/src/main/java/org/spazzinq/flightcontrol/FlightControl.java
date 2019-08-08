/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
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

import com.earth2me.essentials.Essentials;
import lombok.Getter;
import net.minelink.ctplus.CombatTagPlus;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.api.APIManager;
import org.spazzinq.flightcontrol.commands.FlightControlCommand;
import org.spazzinq.flightcontrol.commands.FlyCommand;
import org.spazzinq.flightcontrol.commands.TempFlyCommand;
import org.spazzinq.flightcontrol.commands.ToggleTrailCommand;
import org.spazzinq.flightcontrol.hooks.combat.*;
import org.spazzinq.flightcontrol.hooks.factions.Factions;
import org.spazzinq.flightcontrol.hooks.factions.Massive;
import org.spazzinq.flightcontrol.hooks.factions.UUIDSavage;
import org.spazzinq.flightcontrol.hooks.plot.NewSquared;
import org.spazzinq.flightcontrol.hooks.plot.OldSquared;
import org.spazzinq.flightcontrol.hooks.plot.Plot;
import org.spazzinq.flightcontrol.hooks.towny.BaseTowny;
import org.spazzinq.flightcontrol.hooks.towny.Towny;
import org.spazzinq.flightcontrol.hooks.vanish.EssentialsVanish;
import org.spazzinq.flightcontrol.hooks.vanish.PremiumSuperVanish;
import org.spazzinq.flightcontrol.hooks.vanish.Vanish;
import org.spazzinq.flightcontrol.multiversion.Particles;
import org.spazzinq.flightcontrol.multiversion.WorldGuard;
import org.spazzinq.flightcontrol.multiversion.v1_13.Particles13;
import org.spazzinq.flightcontrol.multiversion.v1_13.WorldGuard7;
import org.spazzinq.flightcontrol.multiversion.v1_8.Particles8;
import org.spazzinq.flightcontrol.multiversion.v1_8.WorldGuard6;
import org.spazzinq.flightcontrol.objects.Category;
import org.spazzinq.flightcontrol.objects.Evaluation;
import org.spazzinq.flightcontrol.utils.Actionbar;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    @Getter private APIManager apiManager = APIManager.getInstance();
    private PluginManager pm = Bukkit.getPluginManager();
    @Getter File storageFolder = new File(getDataFolder() + File.separator + "data");

    @Getter ConfigManager configManager;
    @Getter FlightManager flightManager;
    @Getter TempFlyManager tempflyManager;
    @Getter TrailManager trailManager;
    @Getter Updater updater;
    @Getter private TempFlyCommand tempFlyCommand;

    WorldGuard worldGuard;
    Particles particles;
    Vanish vanish = new Vanish();
    private BaseTowny towny = new BaseTowny();
    private Combat combat = new Combat();
    private Factions fac;
    private Plot plot;

    private HashSet<String> registeredPerms = new HashSet<>();

	public void onEnable() {
	    // Prepare storage folder
        //noinspection ResultOfMethodCallIgnored
        storageFolder.mkdirs();

        boolean is1_13 = getServer().getBukkitVersion().contains("1.13") || getServer().getBukkitVersion().contains("1.14");

        // Remember, if you initialize on declaration it doesn't wait for the softdepends first...
        plot = pm.isPluginEnabled("PlotSquared") ? (is1_13 ? new NewSquared() : new OldSquared()) : new Plot();
        worldGuard = pm.isPluginEnabled("WorldGuard") ? (is1_13 ? new WorldGuard7() : new WorldGuard6()) : new WorldGuard();
        fac = pm.isPluginEnabled("Factions") ? (pm.isPluginEnabled("MassiveCore") ? new Massive() : new UUIDSavage()) : new Factions();
        particles = is1_13 ? new Particles13() : new Particles8();

        if (pm.isPluginEnabled("CombatLogX")) combat = new LogX();
        else if (pm.isPluginEnabled("CombatTagPlus")) combat = new TagPlus(((CombatTagPlus) pm.getPlugin("CombatTagPlus")).getTagManager());
        else if (pm.isPluginEnabled("AntiCombatLogging")) combat = new AntiLogging();
        else if (pm.isPluginEnabled("CombatLogPro")) combat = new LogPro(pm.getPlugin("CombatLogPro"));

        if (pm.isPluginEnabled("PremiumVanish") || pm.isPluginEnabled("SuperVanish")) vanish = new PremiumSuperVanish();
        else if (pm.isPluginEnabled("Essentials")) vanish = new EssentialsVanish((Essentials) pm.getPlugin("Essentials"));

        if (pm.isPluginEnabled("Towny")) towny = new Towny();

        // Load classes

        // Load FlightManager before all because Config uses it & only needs to initialize pl
        flightManager = new FlightManager(this);
        configManager = new ConfigManager(this);
        // Loads from Config
        trailManager = new TrailManager(this);
        // Loads from FlightManager
        tempflyManager = new TempFlyManager(this);

        new Actionbar();
        new Listener(this);
        updater = new Updater(getDescription().getVersion());

        // Load commands
        tempFlyCommand = new TempFlyCommand(this);
        getCommand("tempfly").setExecutor(tempFlyCommand);
        getCommand("fly").setExecutor(new FlyCommand(this));
        getCommand("flightcontrol").setExecutor(new FlightControlCommand(this));
        getCommand("toggletrail").setExecutor(new ToggleTrailCommand(this));

        if (configManager.autoUpdate) updater.install(Bukkit.getConsoleSender(), true);
        else if (updater.exists()) new BukkitRunnable() {
            public void run() {
                getLogger().info("flightcontrol " + updater.newVer() + " is available for update. Perform \"/fc update\" to update and " + "visit https://www.spigotmc.org/resources/flightcontrol.55168/ to view the feature changes (the config automatically updates).");
            }
        }.runTaskLater(this, 40);

        checkCurrentPlayers();

        new Metrics(this); // bStats
    }

	public void onDisable() {
	    if (trailManager != null) {
	        trailManager.saveTrailPrefs();
        }
	}

	public void reload() {
	    configManager.reloadConfig();
	    trailManager.saveTrailPrefs();
	    trailManager.reloadTrailPrefs();
	    tempflyManager.reloadTempflyData();
	    checkCurrentPlayers();
    }

    // Set for players already online
    private void checkCurrentPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            flightManager.check(p);
            trailManager.trailCheck(p);
            p.setFlySpeed(configManager.flightSpeed);
        }
    }

    Evaluation eval(Player p, Location l) {
        String world = l.getWorld().getName(),
               region = worldGuard.getRegion(l);
        Evaluation categories = evalCategories(p),
                   worlds = new Evaluation(configManager.worldBL,
                           configManager.worlds.contains(world)),
                   regions = new Evaluation(configManager.regionBL,
                           configManager.regions.containsKey(world)
                        && configManager.regions.get(world).contains(region));

        if (region != null) defaultPerms(world + "." + region); // Register new regions dynamically

        boolean enable = categories.enable() || plot.flight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ())
                || p.hasPermission("flightcontrol.flyall")
                || p.hasPermission("flightcontrol.fly." + world)
                || region != null && p.hasPermission("flightcontrol.fly." + world + "." + region)
                || worlds.enable() || regions.enable()
                || (configManager.ownTown || p.hasPermission("flightcontrol.owntown")) && towny.ownTown(p) && !(configManager.townyWar && towny.wartime()),
                disable = combat.tagged(p) || categories.disable()
                        || plot.dFlight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ())
                        || p.hasPermission("flightcontrol.nofly." + world)
                        || region != null && p.hasPermission("flightcontrol.nofly." + world + "." + region)
                        || worlds.disable() || regions.disable();

        if (configManager.useFacEnemyRange && p.getWorld().equals(l.getWorld())) { // TODO Does second boolean actually prevent error from onTP?
            List<Player> worldPlayers = l.getWorld().getPlayers();
            worldPlayers.remove(p);
            List<Entity> nearbyEntities = p.getNearbyEntities(configManager.facEnemyRange, configManager.facEnemyRange, configManager.facEnemyRange);

            if (nearbyEntities.size() <= worldPlayers.size()) {
                for (Entity e : nearbyEntities)
                    if (e instanceof Player) {
                        Player otherP = (Player) e;
                        // Distance is calculated a second time to match the shape of the other distance calculation
                        // (this would be a cube while the other would be a sphere otherwise)
                        if (fac.isEnemy(p, otherP) && l.distance(otherP.getLocation()) <= configManager.facEnemyRange) {
                            if (otherP.isFlying()) flightManager.check(otherP);
                            disable = true;
                        }
                    }
            } else {
                for (Player otherP : worldPlayers)
                    if (fac.isEnemy(p, otherP) && l.distance(otherP.getLocation()) <= configManager.facEnemyRange) {
                        if (otherP.isFlying()) flightManager.check(otherP);
                        disable = true;
                    }
            }
        }
        return new Evaluation(disable, enable, true);
    }

    public void debug(Player p) {
        Location l = p.getLocation();
        String world = l.getWorld().getName(),
               region = worldGuard.getRegion(l);
        ArrayList<Category> cats = categories(p);
        Evaluation categories = evalCategories(p),
                   worlds = new Evaluation(configManager.worldBL, configManager.worlds.contains(world)),
                   regions = new Evaluation(configManager.regionBL, configManager.regions.containsKey(world) && configManager.regions.get(world).contains(region));
        // config options (settings) and permissions that act upon the same function are listed as
        // setting boolean (space) permission boolean
        msg(p, "&a&lFlightControl &f" + getDescription().getVersion() +
                ((fac.isHooked() && (cats != null) ? "\n&eFC &7» &f" + cats : "") +
                "\n&eWG &7» &f" + world + "." + region +
                "\n&eWRLDs &f(&e" + configManager.worldBL + "&f) &7» &f" + configManager.worlds  +
                "\n&eRGs &f(&e" + configManager.regionBL + "&f) &7» &f" + configManager.regions  +
                "\n \n&e&lEnable" +
                "\n&fBypass &7» " + p.hasPermission("flightcontrol.bypass") + " " + flightManager.tempBypassList.contains(p) +
                "\n&fAll &7» " + p.hasPermission("flightcontrol.flyall") +
                (fac.isHooked() ? "\n&fFC &7» " + categories.enable() : "") +
                (plot.isHooked() ? "\n&fPlot &7» " + plot.flight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                "\n&fWorld &7» " + worlds.enable() + " " + p.hasPermission("flightcontrol.fly." + world) +
                "\n&fRegion &7» " + regions.enable() + " " + (region != null && p.hasPermission("flightcontrol.fly." + world + "." + region)) +
                (towny.isHooked() ? "\n&fTowny &7» " + (configManager.ownTown && towny.ownTown(p) && (!configManager.townyWar || !towny.wartime())) + " " +
                        (p.hasPermission("flightcontrol.owntown") && towny.ownTown(p) && (!configManager.townyWar || !towny.wartime())) : "") +
                "\n \n&e&lDisable" +
                (fac.isHooked() ? "\n&fFC &7» " + categories.disable() : "") +
                (combat.isHooked() ? "\n&fCombat &7» " + combat.tagged(p) : "") +
                (plot.isHooked() ? "\n&fPlot &7» " + plot.dFlight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ()) : "") +
                "\n&fWorld &7» " + worlds.disable() + " " + p.hasPermission("flightcontrol.nofly." + world) +
                "\n&fRegion &7» " + regions.disable() + " " + (region != null && p.hasPermission("flightcontrol.nofly." + world + "." + region))).replaceAll("false", "&cfalse").replaceAll("true", "&atrue"));
    }

    private Evaluation evalCategories(Player p) {
        ArrayList<Category> cats = categories(p);
        boolean disable = false,
                enable = false;

        if (cats != null) for (Category c : cats) {
            boolean cat = fac.rel(p, c);
            if (c.blacklist && cat) disable = true; else if (c.blacklist || cat) enable = true;
        }
        return new Evaluation(disable, enable, true);
    }

    private ArrayList<Category> categories(Player p) {
        ArrayList<Category> c = new ArrayList<>();
        if (fac.isHooked()) {
            for (Map.Entry<String, Category> entry : configManager.categories.entrySet())
                if (p.hasPermission("flightcontrol.factions." + entry.getKey())) c.add(entry.getValue());
            return c;
        } return null;
    }

    public static void msg(CommandSender s, String msg) { msg(s, msg, false); }
    public static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            String finalMsg = msg;

            if (s instanceof ConsoleCommandSender) finalMsg = finalMsg.replaceAll("FlightControl &7» ", "[FlightControl] ").replaceAll("»", "-");
            finalMsg = ChatColor.translateAlternateColorCodes('&', finalMsg);

            if (actionBar && s instanceof Player) Actionbar.send((Player) s, finalMsg);
            else s.sendMessage(finalMsg);
        }
    }

    void defaultPerms(String suffix) {
	    if (!registeredPerms.contains(suffix)) {
            registeredPerms.add(suffix);
            if (pm.getPermission("flightcontrol.fly." + suffix) == null)
                pm.addPermission(new Permission("flightcontrol.fly." + suffix, PermissionDefault.FALSE));
            if (pm.getPermission("flightcontrol.nofly." + suffix) == null)
                pm.addPermission(new Permission("flightcontrol.nofly." + suffix, PermissionDefault.FALSE));
        }
    }

//    private void flyCommand() {
//        try {
//            Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap"),
//                  knownCMDS = SimpleCommandMap.class.getDeclaredField("knownCommands");
//            Constructor<PluginCommand> plCMD = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
//            cmdMap.setAccessible(true); knownCMDS.setAccessible(true); plCMD.setAccessible(true);
//            CommandMap map = (CommandMap) cmdMap.get(Bukkit.getServer());
//            @SuppressWarnings("unchecked") Map<String, Command> kCMDMap = (Map<String, Command>) knownCMDS.get(cmdMap.get(Bukkit.getServer()));
//            PluginCommand fly = plCMD.newInstance("fly", this);
//            String plName = getDescription().getName();
//            if (configManager.command) {
//                fly.setDescription("Toggles flight");
//                map.register(plName, fly);
//                kCMDMap.put(plName.toLowerCase() + ":fly", fly);
//                kCMDMap.put("fly", fly);
//                // Anonymous fly class
//                fly.setExecutor(new FlyCommand(this));
//            } else if (getCommand("fly") != null && getCommand("fly").getPlugin() == this) {
//                kCMDMap.remove(plName.toLowerCase() + ":fly");
//                kCMDMap.remove("fly");
//
//                if (pm.isPluginEnabled("Essentials")) {
//                    map.register("Essentials", fly);
//                    fly.setExecutor(pm.getPlugin("Essentials"));
//                    fly.setTabCompleter(pm.getPlugin("Essentials"));
//                }
//            }
//        } catch (Exception e) { e.printStackTrace(); }
//    }


    public float calcActualSpeed(float wrongSpeed) {
        float actualSpeed,
                defaultSpeed = 0.1f,
                maxSpeed = 1f;

        if (wrongSpeed > 10f) wrongSpeed = 10f;
        else if (wrongSpeed < 0.0001f) wrongSpeed = 0.0001f;

        if (wrongSpeed < 1f) actualSpeed = defaultSpeed * wrongSpeed;
        else {
            float ratio = ((wrongSpeed - 1) / 9) * (maxSpeed - defaultSpeed);
            actualSpeed = ratio + defaultSpeed;
        }

        return actualSpeed;
    }

    @Deprecated @Override public FileConfiguration getConfig() {
        return super.getConfig();
    }
}
