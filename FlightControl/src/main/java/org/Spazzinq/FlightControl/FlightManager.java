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

import org.Spazzinq.FlightControl.Objects.Evaluation;
import org.Spazzinq.FlightControl.Objects.Sound;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

final class FlightManager {
    private static FlightControl pl;
    static HashMap<Player, BukkitTask> partTasks = new HashMap<>();
    // Msg when command enabled
    static ArrayList<Player> notif = new ArrayList<>();
    static ArrayList<Entity> fall = new ArrayList<>();
    static ArrayList<Player> tempBypass = new ArrayList<>();

    FlightManager(FlightControl pl) { FlightManager.pl = pl; }

    // MANAGE FLIGHT
    static void check(Player p) { check(p, p.getLocation()); }
    static void check(Player p, Location l) {
        if (!(p.hasPermission("flightcontrol.bypass") || tempBypass.contains(p) || (pl.vanish.vanished(p) && Config.vanishBypass) || p.getGameMode() == GameMode.SPECTATOR)) {
            Evaluation eval = pl.eval(p, l); boolean enable = eval.enable(), disable = eval.disable();

            if (p.getAllowFlight()) { if (disable || !enable) disableFlight(p); }
            else if (enable && !disable) canEnable(p);
        } else if (!p.getAllowFlight()) canEnable(p);
    }

    static private void canEnable(Player p) {
        if (!Config.command) enableFlight(p);
        else if (!notif.contains(p)) { notif.add(p); Sound.play(p, Config.cSound); FlightControl.msg(p, Config.cFlight, Config.actionBar); }
    }
    static void cannotEnable(Player p) { Sound.play(p, Config.nSound); FlightControl.msg(p, Config.nFlight, Config.actionBar); }
    static void enableFlight(Player p) {
        p.setAllowFlight(true);
        if (!Config.everyEnable) Sound.play(p, Config.eSound);
        FlightControl.msg(p, Config.eFlight, Config.actionBar);
    }
    static void disableFlight(Player p) {
        if (Config.command) notif.remove(p);
        if (Config.cancelFall && p.isFlying()) { fall.add(p);
            new BukkitRunnable() { public void run() { fall.remove(p); } }.runTaskLater(pl, 300); }
        p.setAllowFlight(false);
        p.setFlying(false);
        trailRemove(p);
        Sound.play(p, Config.dSound);
        FlightControl.msg(p, Config.dFlight, Config.actionBar);
    }

    // TRAIL STUFF
    static void trailCheck(Player p) {
        if (pl.particles != null && Config.trail && !Config.trailPrefs.contains(p.getUniqueId().toString())) {
            partTasks.put(p, new BukkitRunnable() {
                @Override public void run() {
                    if (!(p.getGameMode() == GameMode.SPECTATOR || pl.vanish.vanished(p) || p.hasPotionEffect(PotionEffectType.INVISIBILITY))) {
                        Location l = p.getLocation();
                        // For some terrible reason the locations are never in the correct spot so you have to time them later
                        new BukkitRunnable() { @Override public void run() { pl.particles.spawn(l); } }.runTaskLater(pl, 2);
                    }

                }
            }.runTaskTimerAsynchronously(pl, 0, 4));
        }

    }

    static void trailRemove(Player p) {
        BukkitTask task = partTasks.remove(p); if (task != null) task.cancel();
    }
    static void disableEnabledTrails() {
        Iterator<Player> it = partTasks.keySet().iterator();
        // Throws a ConcurrentModificationException as a for-each
        //noinspection WhileLoopReplaceableByForEach
        while (it.hasNext()) trailRemove(it.next());
    }
}
