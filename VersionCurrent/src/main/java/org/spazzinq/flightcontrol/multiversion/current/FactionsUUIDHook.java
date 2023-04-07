/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion.current;

import com.massivecraft.factions.*;
import com.massivecraft.factions.perms.Relation;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;

public final class FactionsUUIDHook extends FactionsHookBase {
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

    /*
    WARNING: Some versions of Factions don't have Relation.isEnemy().
    "perms" package added in 1.6.9.5-U0.5.0 (versions before 1.6.9.5-U0.4.3 will not work).
    */
    @Override public boolean isEnemy(Player p, Player otherP) {
        return getFPlayer(p).getRelationTo(getFPlayer(otherP)) == Relation.ENEMY;
    }
}