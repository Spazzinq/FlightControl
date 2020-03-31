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

package org.spazzinq.flightcontrol.hook.plot;

import com.intellectualcrafters.plot.flag.Flags;
import com.intellectualcrafters.plot.object.Location;

public final class OldPlotSquaredHook extends PlotHook {
    @Override public boolean canFly(String world, int x, int y, int z) {
        com.intellectualcrafters.plot.object.Plot p =
                com.intellectualcrafters.plot.object.Plot.getPlot(new Location(world, x, y, z));
        return p != null && p.getFlag(Flags.FLY, false);
    }

    @Override public boolean cannotFly(String world, int x, int y, int z) {
        com.intellectualcrafters.plot.object.Plot p =
                com.intellectualcrafters.plot.object.Plot.getPlot(new Location(world, x, y, z));
        return p != null && !p.getFlag(Flags.FLY, true);
    }

    @Override public boolean isHooked() { return true; }
}
