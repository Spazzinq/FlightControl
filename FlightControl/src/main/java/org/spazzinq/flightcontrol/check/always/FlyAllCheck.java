/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class FlyAllCheck extends Check {
    @Override public boolean check(Player p) {
        return PlayerUtil.hasPermission(p, FlyPermission.FLY_ALL);
    }

    @Override public Cause getCause() {
        return Cause.FLY_ALL;
    }
}
