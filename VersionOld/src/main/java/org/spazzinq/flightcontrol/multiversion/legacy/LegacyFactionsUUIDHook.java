/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion.legacy;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Relation;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;

/**
 * Implements the checks for SavageFactions and legacy versions of FactionsUUID versions 1.6.9.5-U0.4.3 and before).
 */
public final class LegacyFactionsUUIDHook extends FactionsHookBase {
    @Override public boolean inWarzone(Player p) {
        return getFactionAtLocation(p).isWarZone();
    }

    @Override public boolean inSafezone(Player p) {
        return getFactionAtLocation(p).isSafeZone();
    }

    @Override public boolean inWilderness(Player p) {
        return getFactionAtLocation(p).isWilderness();
    }

    @Override public boolean inOwnTerritory(Player p) {
        return getFPlayer(p).isInOwnTerritory();
    }

    @Override public boolean inAllyTerritory(Player p) {
        return getFPlayer(p).isInAllyTerritory();
    }

    @Override public boolean inTruceTerritory(Player p) {
        return getFPlayer(p).getRelationToLocation() == Relation.TRUCE;
    }

    @Override public boolean inNeutralTerritory(Player p) {
        return getFPlayer(p).isInNeutralTerritory();
    }

    @Override public boolean inEnemyTerritory(Player p) {
        return getFPlayer(p).isInEnemyTerritory();
    }

    private Faction getFactionAtLocation(Player p) {
        return Board.getInstance().getFactionAt(new FLocation(p.getLocation()));
    }

    private FPlayer getFPlayer(Player p) {
        return FPlayers.getInstance().getByPlayer(p);
    }

    // WARNING: Some versions of Factions don't have Relation.isEnemy()
    @Override public boolean isEnemy(Player p, Player otherP) {
        return getFPlayer(p).getRelationTo(getFPlayer(otherP)) == Relation.ENEMY;
    }
}
