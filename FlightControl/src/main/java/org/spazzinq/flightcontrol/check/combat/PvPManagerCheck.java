/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.combat;

import me.NoChance.PvPManager.PvPlayer;
import org.bukkit.entity.Player;

public final class PvPManagerCheck extends CombatCheck {
    @Override public boolean check(Player p) {
        return PvPlayer.get(p).isInCombat();
    }
}
