/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.category;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Region;
import org.spazzinq.flightcontrol.multiversion.WorldGuardGenericHook;

import java.util.HashSet;

public class CategoryRegionCheck extends CategoryCheck {
    private final WorldGuardGenericHook worldGuard;
    private final HashSet<Region> regions;

    public CategoryRegionCheck(WorldGuardGenericHook worldGuard, HashSet<Region> regions) {
        this.worldGuard = worldGuard;
        this.regions = regions;
    }

    @Override public boolean check(Player p) {
        String currentRegion = worldGuard.getRegionName(p.getLocation());

        for (Region region : regions) {
            // Avoid creating a new object for performance
            if (p.getWorld() == region.getWorld() && currentRegion.equals(region.getRegionName())) {
                return true;
            }
        }

        return false;
    }
}
