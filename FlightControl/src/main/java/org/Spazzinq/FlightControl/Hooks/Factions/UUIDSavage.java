/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
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

package org.Spazzinq.FlightControl.Hooks.Factions;

import com.massivecraft.factions.*;
import com.massivecraft.factions.struct.Relation;
import org.Spazzinq.FlightControl.Object.Category;
import org.bukkit.entity.Player;

public final class UUIDSavage extends Factions {
    @Override
    public boolean rel(Player p, Category c) {
        if (c != null) {
            Faction f = Board.getInstance().getFactionAt(new FLocation(p.getLocation()));
            FPlayer fP = FPlayers.getInstance().getByPlayer(p);
            boolean own = false, ally = false, truce = false, neutral = false, enemy = false,
                    warzone = c.warzone && f.isWarZone(), safezone = c.safezone && f.isSafeZone(), wilderness = c.wilderness && f.isWilderness();
            if (fP.hasFaction()) {
                if (c.own) own = fP.isInOwnTerritory();
                if (c.ally) ally = fP.isInAllyTerritory();
                if (c.truce) truce = fP.getRelationToLocation() == Relation.TRUCE;
                if (c.neutral) neutral = fP.isInNeutralTerritory();
                if (c.enemy) enemy = fP.isInEnemyTerritory();
            }
            return own || ally || truce || neutral || enemy || warzone || safezone || wilderness;
        }
        return false;
    }
}
