/*
 * This file is part of FlightControl, which is licensed under the MIT License
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

package org.spazzinq.flightcontrol.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;

public class TempFlyManager {
    private FlightControl pl;
    private PlayerManager playerManager;

    public TempFlyManager(FlightControl pl) {
        this.pl = pl;
        playerManager = pl.getPlayerManager();
    }

    public void reloadTempflyData() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            checkTempfly(p);
        }
    }

    public void checkTempfly(Player p) {
        FlightPlayer flightPlayer = playerManager.getFlightPlayer(p);
        Long expiration = flightPlayer.getTempFlyEnd();

        if (expiration != null) {
            scheduleExpiration(p, expiration);
        }
    }

    public void removeTempfly(Player p) {
        playerManager.getFlightPlayer(p).setTempFly(null);
        pl.getFlightManager().check(p);
    }

    private void scheduleExpiration(Player p, long expiration) {
        if (expiration > System.currentTimeMillis()) {
            playerManager.getFlightPlayer(p).setTempFly(expiration);

             new BukkitRunnable() {
                @Override public void run() {
                    removeTempfly(p);
                }
            }.runTaskLater(pl, (expiration - System.currentTimeMillis()) / 50);
        } else {
            removeTempfly(p);
        }
    }
}
