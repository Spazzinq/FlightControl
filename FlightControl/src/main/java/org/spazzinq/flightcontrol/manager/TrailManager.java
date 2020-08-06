/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
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

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.util.CheckUtil;

import java.util.HashMap;
import java.util.HashSet;

public class TrailManager {
    private final FlightControl pl;

    @Getter private final HashMap<Player, BukkitTask> particleTasks = new HashMap<>();

    public TrailManager(FlightControl pl) {
        this.pl = pl;
    }

    public void trailCheck(Player p) {
        if (pl.getParticle() != null && pl.getConfManager().isTrail()
                && pl.getPlayerManager().getFlightPlayer(p).trailWanted() && !particleTasks.containsKey(p)) {
            particleTasks.put(p, new BukkitRunnable() {
                @Override public void run() {
                    HashSet<Check> trailChecks = CheckUtil.checkAll(pl.getCheckManager().getTrailChecks(), p);

                    if (trailChecks.isEmpty()) {
                        Location l = p.getLocation();
                        // For some terrible reason the particle spawn locations are never
                        // in the correct spot so you have to delay them...
                        new BukkitRunnable() {
                            @Override public void run() {
                                pl.getParticle().spawn(l);
                            }
                        }.runTaskLater(pl, 2);
                    }
                }
            }.runTaskTimerAsynchronously(pl, 0, 4));
        }
    }

    public void trailRemove(Player p) {
        BukkitTask task = particleTasks.remove(p);

        if (task != null) {
            task.cancel();
        }
    }

    public void removeEnabledTrails() {
        for (BukkitTask tasks : particleTasks.values()) {
            tasks.cancel();
        }
        particleTasks.clear();
    }

    /**
     * Verifies trails for all online players.
     */
    public void checkAllPlayers() {
        removeEnabledTrails();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isFlying()) {
                trailCheck(p);
            }
        }
    }
}
