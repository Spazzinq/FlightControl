/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.bypasstrail;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class BypassPermissionCheck extends Check {
    @Override public boolean check(Player p) {
        return PlayerUtil.hasPermission(p, FlyPermission.BYPASS);
    }

    @Override public Cause getCause() {
        return Cause.BYPASS_PERMISSION;
    }
}
