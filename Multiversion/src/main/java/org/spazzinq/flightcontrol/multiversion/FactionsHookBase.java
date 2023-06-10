/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion;

import org.bukkit.entity.Player;

public class FactionsHookBase extends Hook {
    public boolean inWarzone(Player p) {
        return false;
    }

    public boolean inSafezone(Player p) {
        return false;
    }

    public boolean inWilderness(Player p) {
        return false;
    }

    public boolean inOwnTerritory(Player p) {
        return false;
    }

    public boolean inAllyTerritory(Player p) {
        return false;
    }

    public boolean inTruceTerritory(Player p) {
        return false;
    }

    public boolean inNeutralTerritory(Player p) {
        return false;
    }

    public boolean inEnemyTerritory(Player p) {
        return false;
    }

    public boolean isEnemy(Player p, Player otherP) {
        return false;
    }
}
