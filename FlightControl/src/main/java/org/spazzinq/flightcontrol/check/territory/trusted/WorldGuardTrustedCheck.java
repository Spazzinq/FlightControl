/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class WorldGuardTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return FlightControl.getInstance().getHookManager().getWorldGuardHook().isMember(p);
    }
}
