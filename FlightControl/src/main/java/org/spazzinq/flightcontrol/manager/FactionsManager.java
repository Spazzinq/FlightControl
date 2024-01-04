/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.multiversion.FactionsGenericHook;

public class FactionsManager {
    private final FlightControl pl;
    private FactionsGenericHook factions;

    public FactionsManager() {
        pl = FlightControl.getInstance();
    }

    public FactionRelation getRelationToLocation(Player p) {
        if (factions == null) {
            factions = pl.getHookManager().getFactionsHook();
        }

        FactionRelation relation = FactionRelation.DEFAULT;

        if (factions.inWarzone(p)) {
            relation = FactionRelation.WARZONE;
        } else if (factions.inSafezone(p)) {
            relation = FactionRelation.SAFEZONE;
        } else if (factions.inWilderness(p)) {
            relation = FactionRelation.WILDERNESS;
        } else if (factions.inOwnTerritory(p)) {
            relation = FactionRelation.OWN;
        } else if (factions.inAllyTerritory(p)) {
            relation = FactionRelation.ALLY;
        } else if (factions.inTruceTerritory(p)) {
            relation = FactionRelation.TRUCE;
        } else if (factions.inEnemyTerritory(p)) {
            relation = FactionRelation.ENEMY;
        } else if (factions.inNeutralTerritory(p)) {
            relation = FactionRelation.NEUTRAL;
        }

        return relation;
    }
}
