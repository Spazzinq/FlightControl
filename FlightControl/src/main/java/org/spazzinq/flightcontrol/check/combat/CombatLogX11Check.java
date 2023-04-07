/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.combat;

import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.manager.ICombatManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CombatLogX11Check extends CombatCheck {
    private ICombatManager combatManager;

    @Override public boolean check(Player p) {
        if (combatManager == null) {
            combatManager = ((ICombatLogX
                    ) Bukkit.getPluginManager().getPlugin("CombatLogX")).getCombatManager();
        }

        return combatManager.isInCombat(p);
    }
}
