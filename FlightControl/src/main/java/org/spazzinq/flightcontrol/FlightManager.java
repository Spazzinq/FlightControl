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

package org.spazzinq.flightcontrol;

import lombok.Getter;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.api.events.FlightCanEnableEvent;
import org.spazzinq.flightcontrol.api.events.FlightCannotEnableEvent;
import org.spazzinq.flightcontrol.api.events.FlightDisableEvent;
import org.spazzinq.flightcontrol.api.events.FlightEnableEvent;
import org.spazzinq.flightcontrol.api.objects.Sound;
import org.spazzinq.flightcontrol.objects.Evaluation;

import java.util.ArrayList;

public final class FlightManager {
    private FlightControl pl;
    // Msg when command enabled
    @Getter
    ArrayList<Player> alreadyCanMsgList = new ArrayList<>(),
                      tempBypassList = new ArrayList<>(),
                      disabledByPlayerList = new ArrayList<>();
    ArrayList<Entity> cancelFallList = new ArrayList<>();

    FlightManager(FlightControl pl) { this.pl = pl; }

    // MANAGE FLIGHT
    void check(Player p) { check(p, p.getLocation(), false); }
    void check(Player p, Location l) { check(p, l, false); }
    public void check(Player p, Location l, boolean usingCMD) {
        if (!p.hasPermission("flightcontrol.bypass") && p.getGameMode() != GameMode.SPECTATOR && !(pl.configManager.vanishBypass && pl.vanish.vanished(p)) && !tempBypassList.contains(p)) {
            Evaluation eval = pl.eval(p, l);
            boolean enable = eval.enable(),
                    disable = eval.disable();

            if (p.getAllowFlight()) {
                if (disable || !enable) disableFlight(p, false);
            }
            else if (enable && !disable) {
                if (usingCMD || (pl.configManager.autoEnable && !disabledByPlayerList.contains(p))) {
                    enableFlight(p, usingCMD);
                } else canEnable(p);
            }
            else if (usingCMD || alreadyCanMsgList.contains(p)) {
                cannotEnable(p);
            }
        } else if (!p.getAllowFlight()) {
            if (usingCMD || (pl.configManager.autoEnable && !disabledByPlayerList.contains(p))) {
                enableFlight(p, usingCMD);
            } else canEnable(p);
        }
    }

    private void canEnable(Player p) {
        if (!alreadyCanMsgList.contains(p)) {
            alreadyCanMsgList.add(p);
            FlightCanEnableEvent e = new FlightCanEnableEvent(p, p.getLocation(), pl.configManager.cFlight, pl.configManager.cSound, pl.configManager.byActionBar);

            pl.getApiManager().callEvent(e);
            if (!e.isCancelled()) {
                Sound.play(p, e.getSound());
                FlightControl.msg(p, e.getMessage(), e.isByActionbar());
            }
        }

    }
    private void cannotEnable(Player p) {
        FlightCannotEnableEvent e = new FlightCannotEnableEvent(p, p.getLocation(), pl.configManager.nFlight, pl.configManager.nSound, pl.configManager.byActionBar);

        pl.getApiManager().callEvent(e);
        if (!e.isCancelled()) {
            alreadyCanMsgList.remove(p);
            Sound.play(p, pl.configManager.nSound);
            FlightControl.msg(p, e.getMessage(), e.isByActionbar());
        }
    }

    private void enableFlight(Player p, boolean isCommand) {
        FlightEnableEvent e = new FlightEnableEvent(p, p.getLocation(), pl.configManager.eFlight, pl.configManager.eSound, pl.configManager.byActionBar, isCommand);

        pl.getApiManager().callEvent(e);
        if (!e.isCancelled()) {
            if (isCommand) disabledByPlayerList.remove(p);
            p.setAllowFlight(true);
            if (!pl.configManager.everyEnable) Sound.play(p, pl.configManager.eSound);
            FlightControl.msg(p, e.getMessage(), e.isByActionbar());
        }
    }
    public void disableFlight(Player p, boolean isCommand) {
        FlightDisableEvent e = new FlightDisableEvent(p, p.getLocation(), pl.configManager.dFlight, pl.configManager.dSound, pl.configManager.byActionBar, isCommand);

        pl.getApiManager().callEvent(e);
        if (!e.isCancelled()) {
            if (isCommand) {
                disabledByPlayerList.add(p);
                alreadyCanMsgList.add(p);
            } else alreadyCanMsgList.remove(p);

            if (pl.configManager.fallCancelled && p.isFlying()) {
                cancelFallList.add(p);
                new BukkitRunnable() { public void run() { cancelFallList.remove(p); } }.runTaskLater(pl, 300);
            }
            p.setAllowFlight(false);
            p.setFlying(false);
            pl.trailManager.trailRemove(p);
            Sound.play(p, pl.configManager.dSound);
            FlightControl.msg(p, e.getMessage(), e.isByActionbar());
        }
    }
}
