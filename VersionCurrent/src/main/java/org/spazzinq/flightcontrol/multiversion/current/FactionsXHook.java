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

package org.spazzinq.flightcontrol.multiversion.current;

import net.prosavage.factionsx.core.Faction;
import net.prosavage.factionsx.manager.PlayerManager;
import net.prosavage.factionsx.persist.data.FactionsKt;
import net.prosavage.factionsx.util.Relation;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;

public class FactionsXHook extends FactionsHookBase {
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
