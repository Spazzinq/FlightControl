/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class WorldPermissionCheck extends Check {
    private final boolean enabled;
    public WorldPermissionCheck(boolean enabled) {
        this.enabled = enabled;
    }

    @Override public boolean check(Player p) {
        return PlayerUtil.hasPermissionFly(enabled, p, p.getWorld().getName());
    }

    @Override public Cause getCause() {
        return Cause.PERMISSION_WORLD;
    }
}
