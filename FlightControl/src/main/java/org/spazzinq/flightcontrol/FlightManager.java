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

import org.spazzinq.flightcontrol.objects.Evaluation;
import org.spazzinq.flightcontrol.objects.Sound;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

final class FlightManager {
    private FlightControl pl;
    // Msg when command enabled
    ArrayList<Player> notif = new ArrayList<>();
    ArrayList<Entity> fall = new ArrayList<>();
    ArrayList<Player> tempBypass = new ArrayList<>();

    FlightManager(FlightControl pl) { this.pl = pl; }

    // MANAGE FLIGHT
    void check(Player p) { check(p, p.getLocation(), false); }
    void check(Player p, Location l) { check(p, l, false); }
    void check(Player p, Location l, boolean usingCMD) {
        if (!p.hasPermission("flightcontrol.bypass") && p.getGameMode() != GameMode.SPECTATOR && !(pl.config.vanishBypass && pl.vanish.vanished(p)) && !tempBypass.contains(p)) {
            Evaluation eval = pl.eval(p, l);
            boolean enable = eval.enable(),
                    disable = eval.disable();

            if (p.getAllowFlight()) { if (disable || !enable) disableFlight(p); }
            else if (enable && !disable) {
                if (usingCMD) enableFlight(p);
                else canEnable(p);
            } else if (usingCMD) cannotEnable(p);
        } else if (!p.getAllowFlight()) {
            if (usingCMD) enableFlight(p);
            else canEnable(p);
        }
    }

    private void canEnable(Player p) {
        if (!pl.config.command) enableFlight(p);
        else if (!notif.contains(p)) {
            notif.add(p); Sound.play(p, pl.config.cSound);
            FlightControl.msg(p, pl.config.cFlight, pl.config.actionBar);
        }
    }
    private void cannotEnable(Player p) { Sound.play(p, pl.config.nSound); FlightControl.msg(p, pl.config.nFlight, pl.config.actionBar); }

    private void enableFlight(Player p) {
        p.setAllowFlight(true);
        if (!pl.config.everyEnable) Sound.play(p, pl.config.eSound);
        FlightControl.msg(p, pl.config.eFlight, pl.config.actionBar);
    }
    void disableFlight(Player p) {
        if (pl.config.command) notif.remove(p);
        if (pl.config.cancelFall && p.isFlying()) {
            fall.add(p);
            new BukkitRunnable() { public void run() { fall.remove(p); } }.runTaskLater(pl, 300);
        }
        p.setAllowFlight(false);
        p.setFlying(false);
        pl.trail.trailRemove(p);
        Sound.play(p, pl.config.dSound);
        FlightControl.msg(p, pl.config.dFlight, pl.config.actionBar);
    }
}
