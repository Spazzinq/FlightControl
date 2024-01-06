/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
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

    public TrailManager() {
        pl = FlightControl.getInstance();
    }

    /**
     * Checks whether the player's trail should be enabled.
     * @param p the target player
     */
    public void trailCheck(Player p) {
        boolean conditions = pl.getParticle() != null
                && pl.getConfManager().isTrailEnabled()
                && pl.getPlayerManager().getFlightPlayer(p).isTrailWanted()
                && !particleTasks.containsKey(p);

        if (conditions) {
            enableTrail(p);
        } else {
            disableTrail(p);
        }
    }

    /**
     * Enables the player trail regardless of status.
     * @param p the target player
     */
    public void enableTrail(Player p) {
        particleTasks.put(p, new BukkitRunnable() {
            @Override public void run() {
                HashSet<Check> trailChecks = CheckUtil.evaluate(pl.getCheckManager().getNoTrailChecks(), p);

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

    /**
     * Disables the player trail regardless of status.
     * @param p the target player
     */
    public void disableTrail(Player p) {
        BukkitTask task = particleTasks.remove(p);

        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Disables all player trails regardless of status.
     */
    public void disableAllTrails() {
        for (BukkitTask tasks : particleTasks.values()) {
            tasks.cancel();
        }
        particleTasks.clear();
    }

    /**
     * Disables all player trails then re-checks all online players.
     */
    public void checkAllPlayers() {
        disableAllTrails();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isFlying()) {
                trailCheck(p);
            }
        }
    }
}
