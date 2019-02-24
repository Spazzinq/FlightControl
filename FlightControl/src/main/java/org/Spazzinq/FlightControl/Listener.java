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

import org.Spazzinq.FlightControl.Multiversion.v13.Particles13;
import org.Spazzinq.FlightControl.Multiversion.v8.Particles8;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

class Listener implements org.bukkit.event.Listener {
    private FlightControl pl;
    private boolean particles;

	Listener(FlightControl i) {
	    pl = i; Bukkit.getPluginManager().registerEvents(this, i);
	    particles = pl.particles instanceof Particles13 || (Config.isSpigot && pl.particles instanceof Particles8);
	}

	@EventHandler
    private void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL) {
            Player p = (Player) e.getEntity();
            if (pl.fall.contains(p)) { e.setCancelled(true); pl.fall.remove(p); }
        }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		pl.check(p, e.getTo(), false);
		if (particles && Config.trail && !Config.trailPrefs.contains(p.getUniqueId().toString()) &&
                e.getFrom().distance(e.getTo()) > 0 && p.isFlying() && p.getGameMode() != GameMode.SPECTATOR && !pl.vanish.vanished(p)) pl.particles.play(p.getWorld(), p, e.getTo(), e.getFrom());
	}

	@EventHandler private void onJoin(PlayerJoinEvent e) { pl.check(e.getPlayer()); }
	@EventHandler private void onCommand(PlayerCommandPreprocessEvent e) { new BukkitRunnable() { public void run() { pl.check(e.getPlayer()); } }.runTaskLater(pl, 1);  }
    @EventHandler private void onWorldLoad(WorldLoadEvent e) {
        String w = e.getWorld().getName();
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