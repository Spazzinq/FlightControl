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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.APIManager;
import org.spazzinq.flightcontrol.api.event.FlightCanEnableEvent;
import org.spazzinq.flightcontrol.api.event.FlightCannotEnableEvent;
import org.spazzinq.flightcontrol.api.event.FlightDisableEvent;
import org.spazzinq.flightcontrol.api.event.FlightEnableEvent;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.api.object.Sound;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.util.CheckUtil;

import java.util.HashSet;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class FlightManager {
    private final FlightControl pl;

    @Getter private final HashSet<Player> alreadyCanMsg = new HashSet<>();
    @Getter private final HashSet<Player> disabledByPlayer = new HashSet<>();
    @Getter private final HashSet<Entity> noFallDmg = new HashSet<>();

    public FlightManager() {
        pl = FlightControl.getInstance();
    }

    public void check(Player p) {
        check(p, false);
    }

    public void check(Player p, boolean isCommand) {
        HashSet<Check> bypassChecks = CheckUtil.checkAll(pl.getCheckManager().getBypassChecks(), p);
        // If not bypassing checks
        if (bypassChecks.isEmpty()) {
            HashSet<Check> enableChecks = pl.getStatusManager().checkEnable(p);
            HashSet<Check> disableChecks = pl.getStatusManager().checkDisable(p);
            boolean enable = !enableChecks.isEmpty();
            boolean disable = !disableChecks.isEmpty();
            Cause enableCause = enable ? enableChecks.iterator().next().getCause() : null;
            Cause disableCause = disable ? disableChecks.iterator().next().getCause() : null;

            if (p.getAllowFlight()) {
                // If override or not enabled
                if (disable || !enable) {
                    disableFlight(p, disableCause,false);
                }
            // If all clear to enable
            } else if (enable && !disable) {
                // If directly enabled or auto-enable is enabled
                if (isCommand || (pl.getConfManager().isAutoEnable() && !disabledByPlayer.contains(p))) {
                    enableFlight(p, enableCause, isCommand);
                } else {
                    canEnable(p, enableCause);
                }
            // If denied
            } else if (isCommand || alreadyCanMsg.contains(p)) {
                cannotEnable(p, disableCause);
            }
        // If bypassing checks
        } else if (!p.getAllowFlight()) {
            Cause bypassCause = !bypassChecks.isEmpty() ? bypassChecks.iterator().next().getCause() : null;

            if (isCommand || (pl.getConfManager().isAutoEnable() && !disabledByPlayer.contains(p))) {
                enableFlight(p, bypassCause, isCommand);
            } else {
                canEnable(p, bypassCause);
            }
        }
    }

    private void canEnable(Player p, Cause cause) {
        if (!alreadyCanMsg.contains(p)) {
            alreadyCanMsg.add(p);
            FlightCanEnableEvent e = new FlightCanEnableEvent(p, p.getLocation(), cause,
                    pl.getLangManager().getCanEnableFlight(),
                    pl.getConfManager().getCanEnableSound(), pl.getLangManager().useActionBar());

            APIManager.getInstance().callEvent(e);

            if (!e.isCancelled()) {
                Sound.play(p, e.getSound());
                msg(p, e.getMessage(), e.isByActionbar());
            }
        }

    }

    private void cannotEnable(Player p, Cause cause) {
        FlightCannotEnableEvent e = new FlightCannotEnableEvent(p, p.getLocation(), cause,
                pl.getLangManager().getCannotEnableFlight(),
                pl.getConfManager().getCannotEnableSound(), pl.getLangManager().useActionBar());

        APIManager.getInstance().callEvent(e);

        if (!e.isCancelled()) {
            alreadyCanMsg.remove(p);
            Sound.play(p, pl.getConfManager().getCannotEnableSound());
            msg(p, e.getMessage(), e.isByActionbar());
        }
    }

    private void enableFlight(Player p, Cause cause, boolean isCommand) {
        FlightEnableEvent e = new FlightEnableEvent(p, p.getLocation(), cause, pl.getLangManager().getEnableFlight(),
                pl.getConfManager().getEnableSound(), pl.getLangManager().useActionBar(), isCommand);

        APIManager.getInstance().callEvent(e);

        if (!e.isCancelled()) {
            if (isCommand) {
                disabledByPlayer.remove(p);
            }
            p.setAllowFlight(true);
            if (!pl.getConfManager().isEveryEnable()) {
                Sound.play(p, pl.getConfManager().getEnableSound());
            }
            msg(p, e.getMessage(), e.isByActionbar());
        }
    }

    public void disableFlight(Player p, Cause cause, boolean isCommand) {
        FlightDisableEvent e = new FlightDisableEvent(p, p.getLocation(), cause, pl.getLangManager().getDisableFlight(),
                pl.getConfManager().getDisableSound(), pl.getLangManager().useActionBar(), isCommand);

        APIManager.getInstance().callEvent(e);

        if (!e.isCancelled()) {
            // Don't pause if always decreasing
            if (!pl.getConfManager().isTempflyAlwaysDecrease()) {
                pl.getPlayerManager().getFlightPlayer(p).getTempflyTimer().pause();
            }

            if (isCommand) {
                disabledByPlayer.add(p);
                alreadyCanMsg.add(p);
            } else {
                alreadyCanMsg.remove(p);
            }

            if (pl.getConfManager().isCancelFall() && p.isFlying()) {
                noFallDmg.add(p);
                new BukkitRunnable() {
                    public void run() { noFallDmg.remove(p); }
                }.runTaskLater(pl, 300);
            }
            p.setAllowFlight(false);
            p.setFlying(false);
            pl.getTrailManager().trailRemove(p);
            Sound.play(p, pl.getConfManager().getDisableSound());
            msg(p, e.getMessage(), e.isByActionbar());
        }
    }

    /**
     * Verifies flight access for all online players.
     */
    public void checkAllPlayers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            check(p);
        }
    }
}
