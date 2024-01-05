/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.own;

import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class ProtectionStonesOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return PSRegion.fromLocation(p.getLocation()).getOwners().contains(p.getUniqueId());
    }
}
