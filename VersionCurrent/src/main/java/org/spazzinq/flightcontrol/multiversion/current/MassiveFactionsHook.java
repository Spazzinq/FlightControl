/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion.current;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;

public final class MassiveFactionsHook extends FactionsHookBase {
    @Override public boolean inWarzone(Player p) {
        return getFactionAtLocation(p) == FactionColl.get().getWarzone();
    }

    @Override public boolean inSafezone(Player p) {
        return getFactionAtLocation(p) == FactionColl.get().getSafezone();
    }

    @Override public boolean inWilderness(Player p) {
        return getFactionAtLocation(p).isNone();
    }

    @Override public boolean inOwnTerritory(Player p) {
        return MPlayer.get(p).isInOwnTerritory();
    }

    @Override public boolean inAllyTerritory(Player p) {
        return getRelToLocation(p) == Rel.ALLY;
    }

    @Override public boolean inTruceTerritory(Player p) {
        return getRelToLocation(p) == Rel.TRUCE;
    }

    @Override public boolean inNeutralTerritory(Player p) {
        return getRelToLocation(p) == Rel.NEUTRAL;
    }

    @Override public boolean inEnemyTerritory(Player p) {
        return MPlayer.get(p).isInEnemyTerritory();
    }

    @Override public boolean isEnemy(Player p, Player otherP) {
        MPlayer massivePlayer = MPlayer.get(p);
        MPlayer massiveOtherPlayer = MPlayer.get(otherP);

        return massivePlayer != null && massiveOtherPlayer != null
                && massivePlayer.getRelationTo(massiveOtherPlayer) == Rel.ENEMY;
    }

    private Faction getFactionAtLocation(Player p) {
        return BoardColl.get().getFactionAt(PS.valueOf(p.getLocation()));
    }

    private Rel getRelToLocation(Player p) {
        return getFactionAtLocation(p).getRelationWish(MPlayer.get(p).getFaction());
    }
}
