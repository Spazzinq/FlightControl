/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2022 Spazzinq
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
