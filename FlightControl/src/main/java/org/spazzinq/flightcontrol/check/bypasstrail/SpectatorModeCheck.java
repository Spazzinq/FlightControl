/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.bypasstrail;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class SpectatorModeCheck extends Check {
    @Override public boolean check(Player p) {
        return p != null && p.getGameMode() == GameMode.SPECTATOR;
    }

    @Override public Cause getCause() {
        return Cause.SPECTATOR_MODE;
    }
}
