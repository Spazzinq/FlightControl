/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class HeightLimitCheck extends Check {
    @Override public boolean check(Player p) {
        return p.getLocation().getBlockY() > FlightControl.getInstance().getConfManager().getHeightLimit();
    }

    @Override public Cause getCause() {
        return Cause.HEIGHT_LIMIT;
    }
}
