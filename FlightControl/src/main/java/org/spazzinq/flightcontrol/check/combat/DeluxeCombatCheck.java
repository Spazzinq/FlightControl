/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.combat;

import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.entity.Player;

public final class DeluxeCombatCheck extends CombatCheck {
    private DeluxeCombatAPI api;
    @Override public boolean check(Player p) {
        if (api == null) {
            api = new DeluxeCombatAPI();
        }
        // DeluxeCombat loads player data too late sometimes, which is why this try-catch is necessary
        try {
            return api.isInCombat(p);
        } catch (NullPointerException e) {
            return false;
        }
    }
}
