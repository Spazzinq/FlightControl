/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.multiversion.current;

import net.prosavage.factionsx.core.Faction;
import net.prosavage.factionsx.manager.PlayerManager;
import net.prosavage.factionsx.persist.data.FactionsKt;
import net.prosavage.factionsx.util.Relation;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionsGenericHook;

public class FactionsXHook extends FactionsGenericHook {
    @Override public boolean inWarzone(Player p) {
        return getFactionAtLocation(p).isWarzone();
    }

    @Override public boolean inSafezone(Player p) {
        return getFactionAtLocation(p).isSafezone();
    }

    @Override public boolean inWilderness(Player p) {
        return getFactionAtLocation(p).isWilderness();
    }

    @Override public boolean inOwnTerritory(Player p) {
        return getFactionAtLocation(p) == getFaction(p);
    }

    @Override public boolean inAllyTerritory(Player p) {
        return getRelToLocation(p) == Relation.ALLY;
    }

    @Override public boolean inTruceTerritory(Player p) {
        return getRelToLocation(p) == Relation.TRUCE;
    }

    @Override public boolean inNeutralTerritory(Player p) {
        return getRelToLocation(p) == Relation.NEUTRAL;
    }

    @Override public boolean inEnemyTerritory(Player p) {
        return getRelToLocation(p) == Relation.ENEMY;
    }

    @Override public boolean isEnemy(Player p, Player otherP) {
        return getFaction(p).getRelationTo(getFaction(otherP)) == Relation.ENEMY;
    }

    private Faction getFactionAtLocation(Player p) {
        return FactionsKt.getFLocation(p.getLocation()).getFaction();
    }

    private Relation getRelToLocation(Player p) {
        return getFactionAtLocation(p).getRelationTo(getFaction(p));
    }

    private Faction getFaction(Player p) {
        return PlayerManager.INSTANCE.getFPlayer(p).getFaction();
    }
}
