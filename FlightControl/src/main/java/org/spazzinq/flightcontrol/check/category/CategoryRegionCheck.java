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

package org.spazzinq.flightcontrol.check.category;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.multiversion.WorldGuardHookBase;
import org.spazzinq.flightcontrol.object.Category;

import java.util.HashSet;

public class CategoryRegionCheck extends CategoryCheck {
    private WorldGuardHookBase worldGuard;

    public CategoryRegionCheck(WorldGuardHookBase worldGuard, Category category, boolean enabledOrDisabled) {
        super(category, enabledOrDisabled);

        this.worldGuard = worldGuard;
    }

    @Override public boolean check(Player p) {
        World world = p.getWorld();
        String region = worldGuard.getRegionName(p.getLocation());
        HashSet<Region> regions = enabledOrDisabled ? category.getRegions().getEnabled() : category.getRegions().getDisabled();

        for (Region catRegion : regions) {
            // Avoid creating a new object for performance
            if (world == catRegion.getWorld() && region.equals(catRegion.getRegionName())) {
                return true;
            }
        }

        return false;
    }
}
