/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
 *
 * Copyright (cFile) 2019 Spazzinq
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

import org.Spazzinq.FlightControl.Objects.Sound;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.Spazzinq.FlightControl.FlightManager.*;

final class Listener implements org.bukkit.event.Listener {
    private FlightControl pl;

	Listener(FlightControl i) { pl = i; Bukkit.getPluginManager().registerEvents(this, i); }

    // Check fly status
    @EventHandler(priority = EventPriority.HIGHEST) private void onMove(PlayerMoveEvent e) { FlightManager.check(e.getPlayer(), e.getTo()); }
    // Fly particles
    @EventHandler private void onToggleFly(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();
        if (e.isFlying()) {
            trailCheck(p);
            if (Config.everyEnable) Sound.play(p, Config.eSound);
        } else trailRemove(p);
    }
    // Because onMove doesn't trigger right after a TP
    @EventHandler private void onTP(PlayerTeleportEvent e) { FlightManager.check(e.getPlayer(), e.getTo()); }
	@EventHandler private void onQuit(PlayerQuitEvent e) { trailRemove(e.getPlayer()); }
	@EventHandler private void onJoin(PlayerJoinEvent e) {
	    Player p = e.getPlayer(); FlightManager.check(p);
	    if (p.isFlying()) new BukkitRunnable() { public void run() { trailCheck(p); } }.runTaskLater(pl, 5);
	    p.setFlySpeed(Config.flightSpeed);
	}
	// Because commands might affect permissions/fly
	@EventHandler private void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (e.getMessage().toLowerCase().startsWith("/fly") && !Config.command &&
                (p.isOp() || p.hasPermission("flightcontrol.admin"))) {
            e.setCancelled(true);
            for (CommandSender s : Arrays.asList(Bukkit.getConsoleSender(), p))
                FlightControl.msg(s, "&e&lFlightControl &7» &e" +
                    "You tried to use /fly while the \"command\" setting in the config is disabled! By default, FlightControl automatically enables and disables flight " +
                    "without any commands. Because you used /fly, FlightControl has &aautomatically enabled the command setting&e. If you wish to disable the \"command\" setting again, " +
                    "perform &f/fc command &eor &fdisable &ethe option in the config.");
            CMD.toggleCommand(p);
        }
	    new BukkitRunnable() { public void run() {
            FlightManager.check(p);
	        if (p.isFlying() && !partTasks.containsKey(p)) new BukkitRunnable() { public void run() { trailCheck(p); } }.runTask(pl);
	        else if (!p.isFlying() && partTasks.containsKey(p)) trailRemove(p);
	    } }.runTask(pl);
	}

	// Fall damage prevention
    @EventHandler private void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL &&
            FlightManager.fall.remove(e.getEntity())) e.setCancelled(true);
    }
    // On-the-fly permission management
    @EventHandler private void onWorldLoad(WorldLoadEvent e) {
        String w = e.getWorld().getName();
        // Set default false permission for new world
        FlightControl.defaultPerms(w); for (String rg : pl.regions.regions(e.getWorld())) FlightControl.defaultPerms(w + "." + rg);

        ConfigurationSection worldsCS = Config.load(pl.getConfig(),"worlds");
        if (worldsCS != null) {
            List<String> type = worldsCS.getStringList(Config.worldBL ? "disable" : "enable");
            if (type != null && type.contains(w)) Config.worlds.add(w);
        }

        ConfigurationSection regionsCS = Config.load(pl.getConfig(),"regions");
        if (regionsCS != null) {
            ConfigurationSection dE = regionsCS.getConfigurationSection(Config.regionBL ? "disable" : "enable");
            if (dE.isList(w)) {
                ArrayList<String> rgs = new ArrayList<>(); for (String rg : dE.getStringList(w)) if (pl.regions.hasRegion(w, rg)) rgs.add(rg);
                Config.regions.put(w, rgs);
            }
        }
    }
}