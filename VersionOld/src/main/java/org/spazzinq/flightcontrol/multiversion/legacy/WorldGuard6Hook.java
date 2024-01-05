/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion.legacy;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.WorldGuardGenericHook;

import java.util.Iterator;

public class WorldGuard6Hook extends WorldGuardGenericHook {
    public boolean isMember(Player p) {
        ProtectedRegion region = getRegion(p.getLocation());

        return region != null && region.hasMembersOrOwners() && region.isMember(WGBukkit.getPlugin().wrapPlayer(p));
    }

    public boolean isOwner(Player p) {
        ProtectedRegion region = getRegion(p.getLocation());

        return region != null && region.hasMembersOrOwners() && region.isOwner(WGBukkit.getPlugin().wrapPlayer(p));
    }

    @Override protected ProtectedRegion getRegion(Location l) {
        ProtectedRegion region = null;
        Iterator<ProtectedRegion> iter = WGBukkit.getRegionManager(l.getWorld())
                .getApplicableRegions(l).iterator();

        if (iter.hasNext()) {
            region = iter.next();
        }

        return region;
    }
}
