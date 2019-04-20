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

package org.Spazzinq.FlightControl;

import com.earth2me.essentials.Essentials;
import net.minelink.ctplus.CombatTagPlus;
import org.Spazzinq.FlightControl.Hooks.Combat.AntiLogging;
import org.Spazzinq.FlightControl.Hooks.Combat.Combat;
import org.Spazzinq.FlightControl.Hooks.Combat.LogX;
import org.Spazzinq.FlightControl.Hooks.Combat.TagPlus;
import org.Spazzinq.FlightControl.Hooks.Factions.Factions;
import org.Spazzinq.FlightControl.Hooks.Factions.Massive;
import org.Spazzinq.FlightControl.Hooks.Factions.UUIDSavage;
import org.Spazzinq.FlightControl.Hooks.Plot.NewSquared;
import org.Spazzinq.FlightControl.Hooks.Plot.OldSquared;
import org.Spazzinq.FlightControl.Hooks.Plot.Plot;
import org.Spazzinq.FlightControl.Hooks.Towny.BaseTowny;
import org.Spazzinq.FlightControl.Hooks.Towny.Towny;
import org.Spazzinq.FlightControl.Hooks.Vanish.Ess;
import org.Spazzinq.FlightControl.Hooks.Vanish.PremiumSuper;
import org.Spazzinq.FlightControl.Hooks.Vanish.Vanish;
import org.Spazzinq.FlightControl.Multiversion.Particles;
import org.Spazzinq.FlightControl.Multiversion.Regions;
import org.Spazzinq.FlightControl.Multiversion.v13.Particles13;
import org.Spazzinq.FlightControl.Multiversion.v13.Regions13;
import org.Spazzinq.FlightControl.Multiversion.v8.Particles8;
import org.Spazzinq.FlightControl.Multiversion.v8.Regions8;
import org.Spazzinq.FlightControl.Objects.Category;
import org.Spazzinq.FlightControl.Objects.Eval;
import org.Spazzinq.FlightControl.Objects.Sound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;

public final class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    public Config c;
    PluginManager pm = Bukkit.getPluginManager();

    // Msg when command enabled
    private ArrayList<Player> notif = new ArrayList<>();
    ArrayList<Entity> fall = new ArrayList<>();

    private Combat combat = new Combat();
    private BaseTowny towny = new BaseTowny();
    private Plot plot;
    Regions regions;
    Factions fac;
    Particles particles;
    Vanish vanish = new Vanish();


	public void onEnable() {
	    getCommand("flightcontrol").setExecutor(new CMD(this));
	    // Anonymous toggletrail class
	    getCommand("toggletrail").setExecutor((s, cmd, label, args) -> {
            if (s instanceof Player) {
                String uuid = ((Player) s).getUniqueId().toString();
                if (Config.trailPrefs.contains(uuid)) { Config.trailPrefs.remove(uuid); msg(s, Config.eTrail, Config.actionBar); }
                else { Config.trailPrefs.add(uuid); msg(s, Config.dTrail, Config.actionBar); }
            } else getLogger().info("Only players can use this command (the console isn't a player!)");
            return true;
        });

        boolean is13 = getServer().getVersion().contains("1.13");
        // Remember, if you initialize on declaration it doesn't wait for the softdepends first...
        plot = pm.isPluginEnabled("PlotSquared") ? (is13 ? new NewSquared() : new OldSquared()) : new Plot();
        regions = pm.isPluginEnabled("WorldGuard") ? (is13 ? new Regions13() : new Regions8()) : new Regions();
        fac = pm.isPluginEnabled("Factions") ? (pm.isPluginEnabled("MassiveCore") ? new Massive() : new UUIDSavage()) : new Factions();
        particles = is13 ? new Particles13() : (getServer().getVersion().contains("Spigot") ? new Particles8() : null);

        if (pm.isPluginEnabled("CombatLogX")) combat = new LogX();
        else if (pm.isPluginEnabled("CombatTagPlus")) combat = new TagPlus(((CombatTagPlus) pm.getPlugin("CombatTagPlus")).getTagManager());
        else if (pm.isPluginEnabled("AntiCombatLogging")) combat = new AntiLogging();
        if (pm.isPluginEnabled("PremiumVanish") || pm.isPluginEnabled("SuperVanish")) vanish = new PremiumSuper();
        else if (pm.isPluginEnabled("Essentials")) vanish = new Ess((Essentials) pm.getPlugin("Essentials"));

        if (pm.isPluginEnabled("Towny")) towny = new Towny();

        // Load classes
	    c = new Config(this); new Listener(this); new Actionbar(this); new Update(getDescription().getVersion());
	    flyCommand();

        if (Update.exists()) new BukkitRunnable() {
            public void run() { getLogger().info("FlightControl " + Update.newVer() + " is available for update. Perform /fc update to update and " +
                    "visit https://www.spigotmc.org/resources/flightcontrol.55168/ to view the changes (that may affect your configuration)."); }
        }.runTaskLater(this, 40);
    }
	public void onDisable() { c.saveTrails(); }

    void check(Player p) { check(p, p.getLocation()); }
    void check(Player p, Location l) {
        if (!p.hasPermission("flightcontrol.bypass") && !(vanish.vanished(p) && Config.vanishBypass) && p.getGameMode() != GameMode.SPECTATOR) {
            Eval eval = eval(p, l); boolean enable = eval.enable(), disable = eval.disable();

            if (p.getAllowFlight()) { if (disable || !enable) disableFlight(p); }
            else if (enable && !disable) canEnable(p);
        } else if (!p.getAllowFlight()) canEnable(p);
    }
    private Eval eval(Player p, Location l) {
        String world = l.getWorld().getName();
        String region = regions.region(l);
        Eval categories = evalCategories(p), worlds = new Eval(Config.worldBL, Config.worlds.contains(world)),
                regions = new Eval(Config.regionBL, Config.regions.containsKey(world) && Config.regions.get(world).contains(region));
        boolean enable = categories.enable() || plot.flight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ())
                || p.hasPermission("flightcontrol.flyall")
                || p.hasPermission("flightcontrol.fly." + world)
                || region != null && p.hasPermission("flightcontrol.fly." + world + "." + region)
                || worlds.enable() || regions.enable()
                || (Config.ownTown || p.hasPermission("flightcontrol.owntown")) && towny.ownTown(p) && (!Config.townyWar || !towny.wartime()),
                disable = combat.tagged(p) || categories.disable()
                        || plot.dFlight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ())
                        || p.hasPermission("flightcontrol.nofly." + world)
                        || region != null && p.hasPermission("flightcontrol.nofly." + world + "." + region)
                        || worlds.disable() || regions.disable();
        return new Eval(disable, enable, false);
    }

    void debug(Player p) {
        Location l = p.getLocation(); String world = l.getWorld().getName();
        String region = regions.region(l);
        ArrayList<Category> cats = categories(p);
        Eval categories = evalCategories(p), worlds = new Eval(Config.worldBL, Config.worlds.contains(world)),
                regions = new Eval(Config.regionBL, Config.regions.containsKey(world) && Config.regions.get(world).contains(region));
        msg(p, (Config.fac && (cats != null) ? cats + "\n \n" : "") + world + "." + region + "\n" + Config.regions  + "\n \n&a&lEnable" +
                (Config.fac ? "\n&aFC &7» &f" + categories.enable() : "") +
                "\n&aAll &7» &f" + p.hasPermission("flightcontrol.flyall") +
                "\n&aPlot &7» &f" + plot.flight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ()) +
                "\n&aPWorld &7» &f" + p.hasPermission("flightcontrol.fly." + world) +
                "\n&aPRegion &7» &f" + (region != null && p.hasPermission("flightcontrol.fly." + world + "." + region)) +
                "\n&aPTowny &7» &f" + (p.hasPermission("flightcontrol.owntown") && towny.ownTown(p) && (!Config.townyWar || !towny.wartime())) +
                "\n&aCWorld &7» &f" + worlds.enable() +
                "\n&aCRegion &7» &f" + regions.enable() +
                "\n&aCTowny &7» &f" + (Config.ownTown && towny.ownTown(p) && (!Config.townyWar || !towny.wartime())) +
                "\n \n&c&lDisable" +
                (Config.fac ? "\n&cFC &7» &f" + categories.disable() : "") +
                "\n&cCombat &7» &f" + combat.tagged(p) +
                "\n&cPlot &7» &f" + plot.dFlight(world, l.getBlockX(), l.getBlockY(), l.getBlockZ()) +
                "\n&cPWorld &7» &f" + p.hasPermission("flightcontrol.nofly." + world) +
                "\n&cPRegion &7» &f" + (region != null && p.hasPermission("flightcontrol.nofly." + world + "." + region)) +
                "\n&cCWorld &7» &f" + worlds.disable() +
                "\n&cCRegion &7» &f" + regions.disable());
    }

    private void canEnable(Player p) {
	    if (!Config.command) enableFlight(p);
        else if (!notif.contains(p)) { notif.add(p); Sound.play(p, Config.cSound); msg(p, Config.cFlight, Config.actionBar); }
    }
    private void cannotEnable(Player p) { Sound.play(p, Config.nSound); msg(p, Config.nFlight, Config.actionBar); }
    private void enableFlight(Player p) {
        p.setAllowFlight(true);
        if (!Config.everyEnable) Sound.play(p, Config.eSound);
        msg(p, Config.eFlight, Config.actionBar);
    }
    private void disableFlight(Player p) {
        if (Config.command) notif.remove(p);
        if (Config.cancelFall && p.isFlying()) { fall.add(p);
            new BukkitRunnable() { public void run() { fall.remove(p); } }.runTaskLater(this, 300); }
        p.setAllowFlight(false);
        p.setFlying(false);
        Sound.play(p, Config.dSound);
        msg(p, Config.dFlight, Config.actionBar);
    }

    static void msg(CommandSender s, String msg) { msg(s, msg, false); }
    static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            if (s instanceof ConsoleCommandSender) msg = msg.replaceAll("FlightControl &7» ", "[FlightControl] ").replaceAll("»", "-");
            msg = ChatColor.translateAlternateColorCodes('&', msg);
            if (actionBar && s instanceof Player) Actionbar.send((Player) s, msg);
            else s.sendMessage(msg);
        }
    }

    private Eval evalCategories(Player p) {
        ArrayList<Category> cats = categories(p);
        boolean disable = false, enable = false;
        if (cats != null) for (Category c : cats) {
            boolean cat = fac.rel(p, c);
            if (c.blacklist && cat) disable = true; else if (c.blacklist || cat) enable = true;
        }
        return new Eval(disable, enable, false);
    }

    private ArrayList<Category> categories(Player p) {
        ArrayList<Category> c = new ArrayList<>();
        if (Config.fac) {
            for (Map.Entry<String, Category> entry : Config.categories.entrySet())
                if (p.hasPermission("flightcontrol.factions." + entry.getKey())) c.add(entry.getValue());
            return c;
        } return null;
    }

    void flyCommand() {
        try {
            Field cmdMap = Bukkit.getServer().getClass().getDeclaredField("commandMap"), knownCMDS = SimpleCommandMap.class.getDeclaredField("knownCommands");
            Constructor<PluginCommand> plCMD = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            cmdMap.setAccessible(true); knownCMDS.setAccessible(true); plCMD.setAccessible(true);
            CommandMap map = (CommandMap) cmdMap.get(Bukkit.getServer());
            @SuppressWarnings("unchecked") Map<String, Command> kCMDMap = (Map<String, Command>) knownCMDS.get(cmdMap.get(Bukkit.getServer()));
            PluginCommand fly = plCMD.newInstance("fly", this);
            String plName = getDescription().getName();
            if (Config.command) {
                fly.setDescription("Enables flight");
                map.register(plName, fly);
                kCMDMap.put(plName.toLowerCase() + ":fly", fly);
                kCMDMap.put("fly", fly);
                // Anonymous fly class
                fly.setExecutor((s, cmd, label, args) -> {
                    if (s instanceof Player)
                        if (s.hasPermission("essentials.fly") || s.hasPermission("flightcontrol.fly")) {
                            Player p = (Player) s;
                            if (p.getAllowFlight()) { disableFlight(p); notif.add(p); }
                            else { Eval eval = eval(p, p.getLocation()); if (eval.enable() && !eval.disable()) enableFlight(p); else cannotEnable(p); }
                        } else msg(s, Config.permDenied);
                    else getLogger().info("Only players can use this command (the console can't fly, can it?)");
                    return true;
                });
            } else if (getCommand("fly") != null && getCommand("fly").getPlugin() == this) {
                kCMDMap.remove(plName.toLowerCase() + ":fly");
                kCMDMap.remove("fly");
                if (pm.isPluginEnabled("Essentials")) {
                    map.register("Essentials", fly);
                    fly.setExecutor(pm.getPlugin("Essentials"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override public FileConfiguration getConfig() { return c.c; }
}
