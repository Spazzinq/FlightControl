/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.always;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class IgnoreCheck extends Check {
    @Override public boolean check(Player p) {
        return PlayerUtil.hasPermission(p, FlyPermission.IGNORE);
    }
    @Override public Cause getCause() {
        // Check never calls anything, so no need for Cause
        return null;
    }
}
