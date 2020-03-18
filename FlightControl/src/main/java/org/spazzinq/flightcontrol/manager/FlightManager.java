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
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.events.FlightCanEnableEvent;
import org.spazzinq.flightcontrol.api.events.FlightCannotEnableEvent;
import org.spazzinq.flightcontrol.api.events.FlightDisableEvent;
import org.spazzinq.flightcontrol.api.events.FlightEnableEvent;
import org.spazzinq.flightcontrol.api.objects.Sound;
import org.spazzinq.flightcontrol.object.Evaluation;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PermissionUtil;

import java.util.ArrayList;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public final class FlightManager {
    private final FlightControl pl;

    @Getter private final ArrayList<Player> alreadyCanMsg = new ArrayList<>();
    @Getter private final ArrayList<Player> disabledByPlayer = new ArrayList<>();
    @Getter private final ArrayList<Entity> noFallDmg = new ArrayList<>();

    public FlightManager(FlightControl pl) {
        this.pl = pl;
    }

    public void check(Player p) {
        check(p, p.getLocation(), false);
    }

    public void check(Player p, Location l) {
        check(p, l, false);
    }

    public void check(Player p, Location l, boolean usingCMD) {
        if (!PermissionUtil.hasPermission(p, FlyPermission.BYPASS)
                && p.getGameMode() != GameMode.SPECTATOR
                && !(pl.getConfManager().isVanishBypass() && pl.getHookManager().getVanishHook().vanished(p))) {
            Evaluation eval = pl.getStatusManager().evalFlight(p, l);
            boolean enable = eval.enabled(),
                    disable = eval.disabled();

            if (p.getAllowFlight()) {
                if (disable || !enable) {
                    disableFlight(p, false);
                }
            } else if (enable && !disable) {
                if (usingCMD || (pl.getConfManager().isAutoEnable() && !disabledByPlayer.contains(p))) {
                    enableFlight(p, usingCMD);
                } else {
                    canEnable(p);
                }
            } else if (usingCMD || alreadyCanMsg.contains(p)) {
                cannotEnable(p);
            }
        } else if (!p.getAllowFlight()) {
            if (usingCMD || (pl.getConfManager().isAutoEnable() && !disabledByPlayer.contains(p))) {
                enableFlight(p, usingCMD);
            } else {
                canEnable(p);
            }
        }
    }

    private void canEnable(Player p) {
        if (!alreadyCanMsg.contains(p)) {
            alreadyCanMsg.add(p);
            FlightCanEnableEvent e = new FlightCanEnableEvent(p, p.getLocation(), pl.getLangManager().getCanEnableFlight(),
                    pl.getConfManager().getCSound(), pl.getLangManager().useActionBar());

            pl.getApiManager().callEvent(e);
            if (!e.isCancelled()) {
                Sound.play(p, e.getSound());
                msg(p, e.getMessage(), e.isByActionbar());
            }
        }

    }

    private void cannotEnable(Player p) {
        FlightCannotEnableEvent e = new FlightCannotEnableEvent(p, p.getLocation(), pl.getLangManager().getCannotEnableFlight(),
                pl.getConfManager().getNSound(), pl.getLangManager().useActionBar());

        pl.getApiManager().callEvent(e);
        if (!e.isCancelled()) {
            alreadyCanMsg.remove(p);
            Sound.play(p, pl.getConfManager().getNSound());
            msg(p, e.getMessage(), e.isByActionbar());
        }
    }

    private void enableFlight(Player p, boolean isCommand) {
        FlightEnableEvent e = new FlightEnableEvent(p, p.getLocation(), pl.getLangManager().getEnableFlight(),
                pl.getConfManager().getESound(), pl.getLangManager().useActionBar(), isCommand);

        pl.getApiManager().callEvent(e);
        if (!e.isCancelled()) {
            if (isCommand) {
                disabledByPlayer.remove(p);
            }
            p.setAllowFlight(true);
            if (!pl.getConfManager().isEveryEnable()) {
                Sound.play(p, pl.getConfManager().getESound());
            }
            msg(p, e.getMessage(), e.isByActionbar());
        }
    }

    public void disableFlight(Player p, boolean isCommand) {
        FlightDisableEvent e = new FlightDisableEvent(p, p.getLocation(), pl.getLangManager().getDisableFlight(),
                pl.getConfManager().getDSound(), pl.getLangManager().useActionBar(), isCommand);

        pl.getApiManager().callEvent(e);
        if (!e.isCancelled()) {
            if (isCommand) {
                disabledByPlayer.add(p);
                alreadyCanMsg.add(p);
            } else {
                alreadyCanMsg.remove(p);
            }

            if (pl.getConfManager().isCancelFall() && p.isFlying()) {
                noFallDmg.add(p);
                new BukkitRunnable() { public void run() { noFallDmg.remove(p); } }.runTaskLater(pl, 300);
            }
            p.setAllowFlight(false);
            p.setFlying(false);
            pl.getTrailManager().trailRemove(p);
            Sound.play(p, pl.getConfManager().getDSound());
            msg(p, e.getMessage(), e.isByActionbar());
        }
    }
}
