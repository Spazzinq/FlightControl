/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class RegionPermissionCheck extends Check {
    private final boolean enabled;

    public RegionPermissionCheck(boolean enabled) {
        this.enabled = enabled;
    }

    @Override public boolean check(Player p) {
        String worldName = p.getWorld().getName();
        String regionName = FlightControl.getInstance().getHookManager().getWorldGuardHook().getRegionName(p.getLocation());

        return regionName != null && PlayerUtil.hasPermissionFly(enabled, p, worldName + "." + regionName);
    }

    @Override public Cause getCause() {
        return Cause.PERMISSION_REGION;
    }
}
