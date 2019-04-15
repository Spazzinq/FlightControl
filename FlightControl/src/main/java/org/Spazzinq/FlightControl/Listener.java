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

import org.Spazzinq.FlightControl.Objects.Sound;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

final class Listener implements org.bukkit.event.Listener {
    private FlightControl pl;
    private HashMap<Player, BukkitTask> partTasks = new HashMap<>();

	Listener(FlightControl i) { pl = i; Bukkit.getPluginManager().registerEvents(this, i); }

	private void trailCheck(Player p) {
	    if (pl.particles != null)
            partTasks.put(p, new BukkitRunnable() {
                public void run() {
                    if (Config.trail && !Config.trailPrefs.contains(p.getUniqueId().toString()) && !pl.vanish.vanished(p))
                        pl.particles.play(p.getLocation());
                }
            }.runTaskTimerAsynchronously(pl, 0, 4));
    }
    // Fly particles
	@EventHandler private void onFly(PlayerToggleFlightEvent e) {
	    Player p = e.getPlayer();
	    if (e.isFlying()) {
	        if (Config.everyEnable) Sound.play(p, Config.eSound);
	        trailCheck(p);
        }
	    else {
            BukkitTask task = partTasks.remove(p);
            if (task != null) task.cancel();
        }
    }
    @EventHandler private void onGamemode(PlayerGameModeChangeEvent e) {
	    if (e.getNewGameMode() == GameMode.SPECTATOR) {
            BukkitTask task = partTasks.remove(e.getPlayer());
            if (task != null) task.cancel();
        }
	    else if (e.getPlayer().isFlying()) trailCheck(e.getPlayer());
	}

	// Check fly status
	@EventHandler(priority = EventPriority.HIGHEST) private void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();
		pl.check(p, e.getTo());
	}
	@EventHandler private void onLeave(PlayerQuitEvent e) { BukkitTask task = partTasks.remove(e.getPlayer()); if (task != null) task.cancel(); }
	@EventHandler private void onJoin(PlayerJoinEvent e) {
	    Player p = e.getPlayer(); pl.check(p);
	    if (p.isFlying()) new BukkitRunnable() { public void run() { trailCheck(p); } }.runTaskLater(pl, 2);
	}
	@EventHandler private void onCommand(PlayerCommandPreprocessEvent e) { new BukkitRunnable() { public void run() { pl.check(e.getPlayer()); } }.runTaskLater(pl, 1);  }

	// Fall damage prevention
    @EventHandler private void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL) {
            Player p = (Player) e.getEntity();
            if (pl.fall.contains(p)) { e.setCancelled(true); pl.fall.remove(p); }
        }
    }

    // On-the-fly permission management
    @EventHandler private void onWorldLoad(WorldLoadEvent e) {
        String w = e.getWorld().getName();
        // Set default false permission for new world
        Config.defaultPerms(w); for (String rg : pl.regions.regions(e.getWorld())) Config.defaultPerms(w + "." + rg);

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