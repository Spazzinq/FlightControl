/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.FlightControl;

import java.util.HashSet;

public class PermissionManager {
    private final FlightControl pl;
    private final PluginManager pm;

    private final HashSet<String> flyPermissionSuffixCache = new HashSet<>();

    public PermissionManager() {
        pl = FlightControl.getInstance();
        pm = Bukkit.getPluginManager();
    }

    /**
     * Registers permissions relative to the player's location to prevent
     * operator status from automatically receiving unnecessary permissions.
     * @param p the player from which world and region data are checked
     */
    public void registerLocationalFlyPerms(Player p) {
        String worldName = p.getWorld().getName();
        String regionName = FlightControl.getInstance().getHookManager()
                .getWorldGuardHook().getRegionName(p.getLocation());

        // Register new world permissions dynamically
        pl.getPermissionManager().registerDefaultFlyPerms(worldName);
        if (regionName != null) { // Register new region permissions dynamically
            pl.getPermissionManager().registerDefaultFlyPerms(worldName + "." + regionName);
        }
    }

    /**
     * Registers the suffix permission to prevent operator status
     * from automatically receiving unnecessary permissions.
     *
     * @param suffix the suffix to be appended to the base permission
     */
    public void registerDefaultFlyPerms(String suffix) {
        if (!flyPermissionSuffixCache.contains(suffix)) {
            registerDefaultPerm("flightcontrol.fly." + suffix);
            registerDefaultPerm("flightcontrol.nofly." + suffix);

            flyPermissionSuffixCache.add(suffix);
        }
    }

    /**
     * Registers or re-registers the permission. See {@link #registerDefaultFlyPerms(String)} for more info.
     * @param permString the entire permission String
     */
    public void registerDefaultPerm(String permString) {
        Permission perm = pm.getPermission(permString);

        if (perm == null) {
            pm.addPermission(new Permission(permString, PermissionDefault.FALSE));
        } else if (perm.getDefault() != PermissionDefault.FALSE) {
            perm.setDefault(PermissionDefault.FALSE);
        }
    }
}
