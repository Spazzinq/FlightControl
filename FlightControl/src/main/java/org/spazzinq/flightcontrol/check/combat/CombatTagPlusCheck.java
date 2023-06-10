/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.combat;

import net.minelink.ctplus.CombatTagPlus;
import net.minelink.ctplus.TagManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CombatTagPlusCheck extends CombatCheck {
    private TagManager combatManager;

    @Override public boolean check(Player p) {
        if (combatManager == null) {
            combatManager = ((CombatTagPlus) Bukkit.getPluginManager().getPlugin("CombatTagPlus")).getTagManager();
        }

        return combatManager.isTagged(p.getUniqueId());
    }
}
