/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion.current;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.multiversion.WorldGuardHookBase;

import java.util.Iterator;

@SuppressWarnings("ALL")
public class WorldGuardHook7 extends WorldGuardHookBase {
    private WorldGuardPlatform platform;

    public boolean isMember(Player p) {
        ProtectedRegion region = getRegion(p.getLocation());

        return region != null && region.hasMembersOrOwners() && region.isMember(WorldGuardPlugin.inst().wrapPlayer(p));
    }

    public boolean isOwner(Player p) {
        ProtectedRegion region = getRegion(p.getLocation());

        return region != null && region.hasMembersOrOwners() && region.isOwner(WorldGuardPlugin.inst().wrapPlayer(p));
    }

    @Override protected ProtectedRegion getRegion(Location l) {
        ProtectedRegion region = null;
        Iterator<ProtectedRegion> iter = getRegionContainer().createQuery()
                .getApplicableRegions(BukkitAdapter.adapt(l)).iterator();

        if (iter.hasNext()) {
            region = iter.next();
        }

        return region;
    }

    private RegionContainer getRegionContainer() {
        if (platform == null) {
            try {
                platform = WorldGuard.getInstance().getPlatform();
            } catch (NullPointerException ignored) {}
        }

        return platform.getRegionContainer();
    }
}
