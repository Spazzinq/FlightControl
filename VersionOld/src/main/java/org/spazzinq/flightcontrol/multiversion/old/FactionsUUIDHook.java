/*
 * This file is part of FlightControl, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.spazzinq.flightcontrol.multiversion.old;

import com.massivecraft.factions.*;
import com.massivecraft.factions.perms.Relation;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.multiversion.FactionsHook;

import java.util.Set;

public final class FactionsUUIDHook extends FactionsHook {
    @Override public boolean rel(Player p, Set<FactionRelation> relations) {
        if (!relations.isEmpty()) {
            Faction f = Board.getInstance().getFactionAt(new FLocation(p.getLocation()));
            FPlayer fP = getPlayer(p);
            boolean own = false;
            boolean ally = false;
            boolean truce = false;
            boolean neutral = false;
            boolean enemy = false;
            boolean warzone = relations.contains(FactionRelation.WARZONE) && f.isWarZone();
            boolean safezone = relations.contains(FactionRelation.SAFEZONE) && f.isSafeZone();
            boolean wilderness = relations.contains(FactionRelation.SAFEZONE) && f.isWilderness();

            if (fP.hasFaction()) {
                if (relations.contains(FactionRelation.OWN)) own = fP.isInOwnTerritory();
                if (relations.contains(FactionRelation.ALLY)) ally = fP.isInAllyTerritory();
                // WARNING: Some versions of Factions don't have Relation.isInTruceTerritory()
                if (relations.contains(FactionRelation.TRUCE)) truce = fP.getRelationToLocation() == Relation.TRUCE;
                if (relations.contains(FactionRelation.NEUTRAL)) neutral = fP.isInNeutralTerritory();
                if (relations.contains(FactionRelation.ENEMY)) enemy = fP.isInEnemyTerritory();
            }
            return own || ally || truce || neutral || enemy || warzone || safezone || wilderness;
        }
        return false;
    }

    private FPlayer getPlayer(Player p) {
        return FPlayers.getInstance().getByPlayer(p);
    }

    // WARNING: Some versions of Factions don't have Relation.isEnemy()
    @Override public boolean isEnemy(Player p, Player otherP) {
        return getPlayer(p).getRelationTo(getPlayer(otherP)) == Relation.ENEMY;
    }

    @Override public boolean isHooked() {
        return true;
    }
}