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

package org.spazzinq.flightcontrol.hooks.factions;

import com.massivecraft.factions.*;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.objects.Category;

public final class Savage extends Factions {
    @Override public boolean rel(Player p, Category c) {
        if (c != null) {
            Faction f = Board.getInstance().getFactionAt(new FLocation(p.getLocation()));
            FPlayer fP = getPlayer(p);
            boolean own = false,
                    ally = false,
                    truce = false,
                    neutral = false,
                    enemy = false,
                    warzone = c.warzone && f.isWarZone(),
                    safezone = c.safezone && f.isSafeZone(),
                    wilderness = c.wilderness && f.isWilderness();
            if (fP.hasFaction()) {
                if (c.own) own = fP.isInOwnTerritory();
                if (c.ally) ally = fP.isInAllyTerritory();
                // WARNING: Some versions of Factions don't have Relation.isInTruceTerritory()
                if (c.truce) truce = fP.getRelationToLocation().isTruce();
                if (c.neutral) neutral = fP.isInNeutralTerritory();
                if (c.enemy) enemy = fP.isInEnemyTerritory();
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
        return getPlayer(p).getRelationTo(getPlayer(otherP)).isEnemy();
    }

    @Override public boolean isHooked() { return true; }
}