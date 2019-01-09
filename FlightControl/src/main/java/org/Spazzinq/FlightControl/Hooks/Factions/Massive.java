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

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.Spazzinq.FlightControl.Category;
import org.Spazzinq.FlightControl.Config;
import org.bukkit.entity.Player;

import java.util.List;

public class Massive extends Factions {
    @Override
    public boolean rel(Player p) {
        for (String category : Config.categories.keySet()) {
            if (p.hasPermission("flightcontrol.factions." + category)) {
                Category c = Config.categories.get(category);
                MPlayer mp = MPlayer.get(p);
                Faction f = BoardColl.get().getFactionAt(PS.valueOf(p.getLocation()));
                FactionColl fColl = FactionColl.get();
                boolean own = false, ally = false, truce = false, neutral = false, enemy = false,
                        warzone = c.warzone && f == fColl.getWarzone(), safezone = c.safezone && f == fColl.getSafezone(), wilderness = c.wilderness && f.isNone();
                if (mp.hasFaction()) {
                    Rel r = f.getRelationWish(mp.getFaction());
                    if (c.own) own = mp.isInOwnTerritory();
                    if (c.ally) ally = r == Rel.ALLY;
                    if (c.truce) truce = r == Rel.TRUCE;
                    if (c.neutral) neutral = !f.isNone() && f != fColl.getWarzone() && f != fColl.getSafezone() && !mp.isInOwnTerritory() && r == Rel.NEUTRAL;
                    if (c.enemy) enemy = r == Rel.ENEMY;
                }
                return own || ally || truce || neutral || enemy || warzone || safezone || wilderness;
            }
        }
        return false;
    }
}
