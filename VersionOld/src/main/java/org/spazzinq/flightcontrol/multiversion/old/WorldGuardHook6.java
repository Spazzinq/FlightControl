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

package org.spazzinq.flightcontrol.multiversion.old;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.multiversion.WorldGuardHook;

import java.util.Iterator;
import java.util.Set;

public class WorldGuardHook6 extends WorldGuardHook {
    public String getRegionName(Location l) {
        Iterator<ProtectedRegion> iter = WGBukkit.getRegionManager(l.getWorld()).getApplicableRegions(l).iterator();

        if (iter.hasNext()) {
            return iter.next().getId();
        }
        return null;
    }

    public Set<String> getRegionNames(World world) {
        return WGBukkit.getRegionManager(world).getRegions().keySet();
    }

    public boolean hasRegion(Region region) {
        return WGBukkit.getRegionManager(region.getWorld()).hasRegion(region.getRegionName());
    }
}
