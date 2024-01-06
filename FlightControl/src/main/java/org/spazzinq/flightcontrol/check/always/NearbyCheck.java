/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
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

public class NearbyCheck extends Check {
    private final FlightControl pl;

    public NearbyCheck(FlightControl pl) {
        this.pl = pl;
    }

    @Override public boolean check(Player p) {
        if (!PlayerUtil.hasPermission(p, FlyPermission.NEARBYPASS)) {
            Location l = p.getLocation();
            List<Player> worldPlayers = p.getWorld().getPlayers();

            worldPlayers.remove(p);

            for (Player otherP : worldPlayers) {
                if (!PlayerUtil.hasPermission(otherP, FlyPermission.NEARBYPASS)
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