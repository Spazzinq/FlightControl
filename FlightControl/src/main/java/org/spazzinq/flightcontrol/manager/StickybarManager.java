/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.HashMap;

import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class StickybarManager {
    private final FlightControl pl;

    @Getter private final HashMap<Player, BukkitTask> stickyTasks = new HashMap<>();

    public StickybarManager() {
        pl = FlightControl.getInstance();
    }

    /**
     * Checks whether the player's sticky actionbar should be enabled.
     * @param p the target player
     */
    public void stickybarCheck(Player p) {
        boolean conditions = pl.getLangManager().useActionBar() && pl.getLangManager().isActionBarSticky()
                && !stickyTasks.containsKey(p);

        if (conditions) {
            enableStickybar(p);
        } else {
            disableStickybar(p);
        }
    }

    /**
     * Enables the player sticky actionbar regardless of status.
     * @param p the target player
     */
    public void enableStickybar(Player p) {
        stickyTasks.put(p, new BukkitRunnable() {
            @Override public void run() {
                msgVar(p, pl.getLangManager().getTempflyActionbar(), true, "duration", PlayerUtil.durationToWords(pl.getPlayerManager().getFlightPlayer(p)));
            }
        }.runTaskTimerAsynchronously(pl, 20, 20));
    }

    /**
     * Disables the player sticky actionbar regardless of status.
     * @param p the target player
     */
    public void disableStickybar(Player p) {
        BukkitTask task = stickyTasks.remove(p);

        if (task != null) {
            task.cancel();
        }
    }

    /**
     * Disables all player sticky actionbars regardless of status.
     */
    public void disableAllStickybars() {
        for (BukkitTask tasks : stickyTasks.values()) {
            tasks.cancel();
        }
        stickyTasks.clear();
    }

    /**
     * Disables all player sticky actionbars then re-checks all online players.
     */
    public void checkAllPlayers() {
        disableAllStickybars();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isFlying()) {
                stickybarCheck(p);
            }
        }
    }
}
