/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import dev.espi.protectionstones.PSRegion;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class ProtectionStonesTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return PSRegion.fromLocation(p.getLocation()).getMembers().contains(p.getUniqueId());
    }
}
