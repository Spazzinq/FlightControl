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

package org.spazzinq.flightcontrol.hook.territory;

import com.github.intellectualsites.plotsquared.plot.flag.Flags;
import com.github.intellectualsites.plotsquared.plot.object.Location;
import com.github.intellectualsites.plotsquared.plot.object.Plot;
import org.bukkit.entity.Player;

public final class PlotSquaredHook extends TerritoryHookBase {
    @Override public boolean isOwnTerritory(Player p) {
        Plot plot = getPlot(p);

        return plot != null && plot.hasOwner() && plot.getOwners().contains(p.getUniqueId());
    }

    @Override public boolean isTrustedTerritory(Player p) {
        Plot plot = getPlot(p);

        return plot != null && plot.getTrusted().contains(p.getUniqueId());
    }

    @Override public boolean canFly(Player p) {
        Plot plot = getPlot(p);

        return plot != null && plot.getFlag(Flags.FLY, false);
    }

    @Override public boolean cannotFly(Player p) {
        Plot plot = getPlot(p);

        return plot != null && !plot.getFlag(Flags.FLY, true);
    }

    private Plot getPlot(Player p) {
        org.bukkit.Location l = p.getLocation();

        return Plot.getPlot(new Location(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));
    }
}
