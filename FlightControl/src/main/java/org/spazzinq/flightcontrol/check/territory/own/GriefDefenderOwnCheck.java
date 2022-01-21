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

package org.spazzinq.flightcontrol.check.territory.own;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class GriefDefenderOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return p.getUniqueId().equals(getClaimAt(p).getOwnerUniqueId());
    }

    private Claim getClaimAt(Player p) {
        return GriefDefender.getCore().getClaimManager(p.getWorld().getUID()).getClaimAt(
                Vector3i.from(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
    }
}
