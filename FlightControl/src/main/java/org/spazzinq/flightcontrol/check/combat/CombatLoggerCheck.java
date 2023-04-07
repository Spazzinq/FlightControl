/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.combat;

import com.keurig.combatlogger.api.CombatLoggerAPI;
import org.bukkit.entity.Player;

public final class CombatLoggerCheck extends CombatCheck {
    @Override public boolean check(Player p) {
        return CombatLoggerAPI.isTagged(p);
    }
}
