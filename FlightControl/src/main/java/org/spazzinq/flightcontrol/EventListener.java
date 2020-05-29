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

package org.spazzinq.flightcontrol;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.api.objects.Sound;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

@SuppressWarnings("unused")
final class EventListener implements org.bukkit.event.Listener {
    private final FlightControl pl;

    EventListener(FlightControl pl) {
        this.pl = pl;
        Bukkit.getPluginManager().registerEvents(this, pl);
    }

    // Check fly status
    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMove(PlayerMoveEvent e) {
        // Save performance
        if (e.getFrom().getBlockX() != e.getTo().getBlockX()
                || e.getFrom().getBlockY() != e.getTo().getBlockY()
                || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
            pl.getFlightManager().check(e.getPlayer());
        }
    }

    // Fly particles
    @EventHandler private void onToggleFly(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();

        if (e.isFlying()) {
            pl.getTrailManager().trailCheck(p);
            if (pl.getConfManager().isEveryEnable()) {
                Sound.play(p, pl.getConfManager().getEnableSound());
            }
        } else {
            pl.getTrailManager().trailRemove(p);
            if (pl.getConfManager().isEveryDisable()) {
                Sound.play(p, pl.getConfManager().getDisableSound());
            }
        }
    }

    // Because onMove doesn't trigger right after a TP
    @EventHandler private void onTP(PlayerTeleportEvent e) {
        // Prevent calling on login because another handler takes care of that
        if (e.getCause() != PlayerTeleportEvent.TeleportCause.UNKNOWN) {
            Player p = e.getPlayer();

            pl.getFlightManager().check(p);

            // Fixes bug where particles remain when not supposed so
            if (!p.getAllowFlight()) {
                pl.getTrailManager().trailRemove(p);
            }
        }
    }

    @EventHandler private void onQuit(PlayerQuitEvent e) {
        pl.getTrailManager().trailRemove(e.getPlayer());
    }

    @EventHandler private void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        // Notify me of server version & hooks (debugging)
        if (FlightControl.spazzinqUUID.equals(p.getUniqueId())) {
            new BukkitRunnable() {
                @Override public void run() {
                    msg(p, "&e&lFlightControl &7» &eVersion &f" + pl.getDescription().getVersion() + " &eis currently" +
                            " running on this server. " + pl.getCheckManager().getHookedMsg());
                }
            }.runTaskLater(pl, 40);
        }

        // Notify of updates
        if (p.isOp()) {
            new BukkitRunnable() {
                @Override public void run() {
                    pl.getUpdateManager().notifyUpdate(p);
                }
            }.runTaskLater(pl, 40);
        }

        // Load FlightPlayer data
        p.setFlySpeed(pl.getPlayerManager().getFlightPlayer(p).getActualFlightSpeed());

        // Check flight then trail, allowing time for other plugins to load data
        new BukkitRunnable() {
            @Override public void run() {
                pl.getFlightManager().check(p);
                if (p.isFlying()) {
                    pl.getTrailManager().trailCheck(p);
                }
            }
        }.runTaskLater(pl, 10);
    }

    // Because commands might affect permissions/fly
    @EventHandler private void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();

        new BukkitRunnable() {
            public void run() {
                pl.getFlightManager().check(p);

                if (p.isFlying()) {
                    if (!pl.getTrailManager().getParticleTasks().containsKey(p)) {
                        new BukkitRunnable() {
                            @Override public void run() {
                                pl.getTrailManager().trailCheck(p);
                            }
                        }.runTask(pl);
                    }
                } else {
                    pl.getTrailManager().trailRemove(p);
                }
            }
        }.runTask(pl);
    }

    // Fall damage prevention
    @EventHandler private void onFallDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player && e.getCause() == DamageCause.FALL
                && pl.getFlightManager().getNoFallDmg().remove(e.getEntity())) {
            e.setCancelled(true);
        }
    }

    // On-the-fly permission management
    @EventHandler private void onWorldLoad(WorldLoadEvent e) {
        World world = e.getWorld();
        String worldName = world.getName();

        pl.registerDefaultPerms(worldName);
        for (String regionName : pl.getCheckManager().getWorldGuardHook().getRegionNames(world)) {
            pl.registerDefaultPerms(worldName + "." + regionName);
        }
    }
}