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

import org.Spazzinq.FlightControl.Multiversion.Particles;
import org.Spazzinq.FlightControl.Multiversion.v13.Particles13;
import org.Spazzinq.FlightControl.Multiversion.v8.Particles8;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

class Listener implements org.bukkit.event.Listener {
    private static FlightControl pl;
    private Particles particles;

	Listener(FlightControl i) { pl = i;
	particles = pl.is13 ? new Particles13() : new Particles8();
	Bukkit.getPluginManager().registerEvents(this, i); }

	@EventHandler
	private void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL) { Player p = (Player) e.getEntity();
            if (pl.fall.contains(p)) { e.setCancelled(true); pl.fall.remove(p); }
        }
	}

	@EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent e) {
		Player p = e.getPlayer();

		pl.check(p, e.getTo(), false);
		if ((particles instanceof Particles13 || (Config.isSpigot && particles instanceof Particles8)) && Config.flightTrail && !Config.trailPrefs.contains(p.getUniqueId().toString()) &&
                e.getFrom().distance(e.getTo()) > 0 && p.isFlying() && p.getGameMode() != GameMode.SPECTATOR && !pl.vanish.vanished(p)) particles.play(p.getWorld(), p, e.getTo(), e.getFrom());
	}

	@EventHandler private void onCommand(PlayerCommandPreprocessEvent e) { new BukkitRunnable() { public void run() { pl.check(e.getPlayer(), false); } }.runTaskLater(pl, 1);  }
}