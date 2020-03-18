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

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.multiversion.FactionsHook;

import java.util.Set;

public final class MassiveFactionsHook extends FactionsHook {
    @Override public boolean rel(Player p, Set<FactionRelation> relations) {
        if (!relations.isEmpty()) {
            MPlayer mp = MPlayer.get(p);
            Faction f = BoardColl.get().getFactionAt(PS.valueOf(p.getLocation()));
            FactionColl fColl = FactionColl.get();
            boolean own = false;
            boolean ally = false;
            boolean truce = false;
            boolean neutral = false;
            boolean enemy = false;
            boolean warzone = relations.contains(FactionRelation.WARZONE) && f == fColl.getWarzone();
            boolean safezone = relations.contains(FactionRelation.SAFEZONE) && f == fColl.getSafezone();
            boolean wilderness = relations.contains(FactionRelation.WILDERNESS) && f.isNone();

            if (mp.hasFaction()) {
                Rel r = f.getRelationWish(mp.getFaction());
                if (relations.contains(FactionRelation.OWN)) own = mp.isInOwnTerritory();
                if (relations.contains(FactionRelation.ALLY)) ally = r == Rel.ALLY;
                if (relations.contains(FactionRelation.TRUCE)) truce = r == Rel.TRUCE;
                if (relations.contains(FactionRelation.NEUTRAL)) neutral = !f.isNone() && f != fColl.getWarzone() && f != fColl.getSafezone() && !mp.isInOwnTerritory() && r == Rel.NEUTRAL;
                if (relations.contains(FactionRelation.ENEMY)) enemy = r == Rel.ENEMY;
            }
            return own || ally || truce || neutral || enemy || warzone || safezone || wilderness;
        }
        return false;
    }

    @Override public boolean isEnemy(Player p, Player otherP) {
        return MPlayer.get(p).getRelationTo(MPlayer.get(otherP)) == Rel.ENEMY;
    }

    @Override public boolean isHooked() { return true; }
}
