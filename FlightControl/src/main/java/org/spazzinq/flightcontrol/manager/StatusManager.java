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

package org.spazzinq.flightcontrol.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.Evaluation;
import org.spazzinq.flightcontrol.util.PermissionUtil;

import java.util.HashSet;
import java.util.Set;

import static org.spazzinq.flightcontrol.object.FlyPermission.*;
import static org.spazzinq.flightcontrol.util.PermissionUtil.*;

// TODO Rewrite system for entire class... This is ugly...
public class StatusManager {
    private final FlightControl pl;

    public StatusManager(FlightControl pl) {
        this.pl = pl;
    }

    Evaluation evalFlight(Player p, Location l) {
        World world = l.getWorld();
        String worldName = world.getName(),
               regionName = pl.getHookManager().getWorldGuardHook().getRegionName(l);
        Region region = new Region(world, regionName);
        Category category = pl.getCategoryManager().getCategory(p);

        // TODO Better system
        boolean landsOwnerHasTrusted = false;

        if (pl.getHookManager().getLandsHook().isHooked()) {
            Player landsOwner = Bukkit.getPlayer(pl.getHookManager().getLandsHook().getOwnerUUID(l));

            if (landsOwner != null) {
                landsOwnerHasTrusted = hasPermission(landsOwner, LANDS_TRUSTED);
            }
        }

        if (regionName != null) {
            pl.defaultPerms(worldName + "." + regionName); // Register new regions dynamically
        }

        boolean enableCategoryCheck =
                // World check
                category.getWorlds().enabledContains(world)
                // Region check
                || category.getRegions().enabledContains(region)
                // Factions check
                || pl.getHookManager().getFactionsHook().rel(p, category.getFactions().getEnabled());
        boolean enableHookCheck =
                // CrazyEnchantments check
                pl.getHookManager().getEnchantmentsHook().canFly(p)
                // Plot check
                || pl.getHookManager().getPlotHook().canFly(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ())
                // Towny check
                || (pl.getConfManager().isTownyOwn() || hasPermission(p, TOWNY_OWN))
                        && pl.getHookManager().getTownyHook().townyOwn(p)
                        && !(pl.getConfManager().isTownyWarDisable() && pl.getHookManager().getTownyHook().wartime())
                // Lands check
                || (pl.getConfManager().isLandsOwnEnable() || hasPermission(p, LANDS_OWN))
                        && pl.getHookManager().getLandsHook().landsIsOwn(p)
                || ((pl.getConfManager().isLandsOwnEnable() && pl.getConfManager().isLandsTrusted()) || hasPermission(p, LANDS_TRUSTED) || landsOwnerHasTrusted)
                        && pl.getHookManager().getLandsHook().landsIsTrusted(p)
                // GriefPrevention check
                || (pl.getConfManager().isGpClaimOwn() || hasPermission(p, CLAIM_OWN)) && pl.getHookManager().getGriefPreventionHook().isHooked();
        boolean enablePermissionCheck =
                // Global perm check
                hasPermission(p, FLY_ALL)
                // World perm check
                || hasPermissionFly(p, worldName)
                // Region perm check
                || regionName != null && hasPermissionFly(p, worldName + "." + regionName);
        boolean tempFly = pl.getPlayerManager().getFlightPlayer(p).hasTempFly();

        boolean disableCategoryCheck =
                // World check
                category.getWorlds().disabledContains(world)
                // Region check
                || category.getRegions().disabledContains(region)
                // Factions check
                || pl.getHookManager().getFactionsHook().rel(p, category.getFactions().getDisabled());
        boolean disableHookCheck =
                pl.getHookManager().getCombatHook().tagged(p)
                || pl.getHookManager().getPlotHook().cannotFly(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ());
        boolean disablePermissionCheck =
                // World perm check
                hasPermissionNoFly(p, worldName)
                // Region perm check
                || regionName != null && hasPermissionNoFly(p, worldName + "." + regionName);

        return new Evaluation(disableCategoryCheck || disableHookCheck || disablePermissionCheck || enemyCheck(p, l),
                              enableCategoryCheck || enableHookCheck || enablePermissionCheck || tempFly);
    }

    // TODO Finish optimization with caching
    private boolean enemyCheck(Player p, Location l) {
        if (!PermissionUtil.hasPermission(p, NEARBYPASS)) {
            World world = l.getWorld();
            boolean disable = false;

            // Prevent comparing 2 different worlds
            if (pl.getConfManager().isUseFacEnemyRange() && p.getWorld().equals(l.getWorld())) {
                Set<Player> worldPlayers = new HashSet<>();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (world.equals(onlinePlayer.getWorld())) {
                        worldPlayers.add(onlinePlayer);
                    }
                }
                worldPlayers.remove(p);

                for (Player otherP : worldPlayers) {
                    if (pl.getHookManager().getFactionsHook().isEnemy(p, otherP) && l.distanceSquared(otherP.getLocation()) <= pl.getConfManager().getFacEnemyRangeSquared()) {
                        if (otherP.isFlying()) pl.getFlightManager().check(otherP);
                        disable = true;
                    }
                }
            }
            return disable;
        }
        return false;
    }
}
