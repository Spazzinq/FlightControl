/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2021 Spazzinq
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

package org.spazzinq.flightcontrol.manager;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;

public class FactionsManager {
    private final FlightControl pl;
    private FactionsHookBase factions;

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
