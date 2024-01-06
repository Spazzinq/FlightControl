/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.own;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class WorldGuardOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return FlightControl.getInstance().getHookManager().getWorldGuardHook().isOwner(p);
    }
}
