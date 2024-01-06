/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.bypasstrail.vanish;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class PremiumSuperVanishCheck extends VanishCheck {
    @Override public boolean check(Player p) {
        return p.getMetadata("vanished").stream().findFirst().filter(MetadataValue::asBoolean).isPresent();
    }
}
