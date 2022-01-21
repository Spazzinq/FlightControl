/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2021 Spazzinq
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

package org.spazzinq.flightcontrol.check.always;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.List;

public class NearbyEnemyCheck extends Check {
    private final FlightControl pl;

    public NearbyEnemyCheck(FlightControl pl) {
        this.pl = pl;
    }

    @Override public boolean check(Player p) {
        if (!PlayerUtil.hasPermission(p, FlyPermission.NEARBYPASS)) {
            Location l = p.getLocation();
            List<Player> worldPlayers = p.getWorld().getPlayers();

            worldPlayers.remove(p);

            for (Player otherP : worldPlayers) {
                if (!PlayerUtil.hasPermission(otherP, FlyPermission.NEARBYPASS)
                        && pl.getHookManager().getFactionsHook().isEnemy(p, otherP)
                        && l.distanceSquared(otherP.getLocation()) <= pl.getConfManager().getNearbyRangeSquared()) {

                    if (otherP.isFlying()) {
                        pl.getFlightManager().check(otherP);
                    }

                    return true;
                }
            }
        }
        return false;
    }

    @Override public Cause getCause() {
        return Cause.NEARBY;
    }
}