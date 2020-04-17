/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
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
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;

public final class FactionsUUIDHook extends FactionsHookBase {
    @Override public boolean hasFaction(Player p) {
        return getFPlayer(p).hasFaction();
    }

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