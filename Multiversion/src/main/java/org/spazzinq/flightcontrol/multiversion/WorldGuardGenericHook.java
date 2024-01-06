/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.multiversion;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class WorldGuardGenericHook extends Hook {
    public String getRegionName(Location l) {
        ProtectedRegion region = getRegion(l);

        return region == null ? "none" : region.getId();
    }

    public boolean isMember(Player p) {
        return false;
    }

    public boolean isOwner(Player p) {
        return false;
    }

    protected ProtectedRegion getRegion(Location l) {
        return null;
    }
}
