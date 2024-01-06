/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.bypasstrail;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class InvisibilityPotionCheck extends Check {
    @Override public boolean check(Player p) {
        return p != null && p.hasPotionEffect(PotionEffectType.INVISIBILITY);
    }

    @Override public Cause getCause() {
        return Cause.INVISIBILITY_POTION;
    }
}
