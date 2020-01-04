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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.Evaluation;

import java.util.List;

import static org.spazzinq.flightcontrol.manager.PermissionManager.*;
import static org.spazzinq.flightcontrol.object.FlyPermission.*;

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

            landsOwnerHasTrusted = hasPermission(landsOwner, LANDS_TRUSTED);
        }

        if (regionName != null) {
            pl.defaultPerms(worldName + "." + regionName); // Register new regions dynamically
        }

        boolean hasWorlds = category.getWorlds() != null,
                hasRegions = category.getRegions() != null,
                hasFactions = category.getFactions() != null;

        boolean enableCategoryCheck =
                // World check
                hasWorlds && category.getWorlds().getEnabled().contains(world)
                // Region check
                || hasRegions && category.getRegions().getEnabled().contains(region)
                // Factions check
                || hasFactions && pl.getHookManager().getFactionsHook().rel(p, category.getFactions().getEnabled());
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
                        && pl.getHookManager().getLandsHook().landsOwn(p)
                || ((pl.getConfManager().isLandsOwnEnable() && pl.getConfManager().isLandsTrusted()) || hasPermission(p, LANDS_TRUSTED) || landsOwnerHasTrusted)
                        && pl.getHookManager().getLandsHook().landsTrusted(p);
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
                hasWorlds && category.getWorlds().getDisabled().contains(world)
                // Region check
                || hasRegions && category.getRegions().getDisabled().contains(region)
                // Factions check
                || hasFactions && pl.getHookManager().getFactionsHook().rel(p, category.getFactions().getDisabled());
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

    // TODO Optimize?
    private boolean enemyCheck(Player p, Location l) {
        boolean disable = false;

        // Prevent comparing 2 different worlds
        if (pl.getConfManager().isUseFacEnemyRange() && p.getWorld().equals(l.getWorld())) {
            List<Player> worldPlayers = l.getWorld().getPlayers();
            worldPlayers.remove(p);
            List<Entity> nearbyEntities = p.getNearbyEntities(pl.getConfManager().getFacEnemyRange(), pl.getConfManager().getFacEnemyRange(), pl.getConfManager().getFacEnemyRange());

            if (nearbyEntities.size() <= worldPlayers.size()) {
                for (Entity e : nearbyEntities)
                    if (e instanceof Player) {
                        Player otherP = (Player) e;
                        // Distance is calculated a second time to match the shape of the other distance calculation
                        // (this would be a cube while the other would be a sphere otherwise)
                        if (pl.getHookManager().getFactionsHook().isEnemy(p, otherP) && l.distance(otherP.getLocation()) <= pl.getConfManager().getFacEnemyRange()) {
                            if (otherP.isFlying()) pl.getFlightManager().check(otherP);
                            disable = true;
                        }
                    }
            } else {
                for (Player otherP : worldPlayers)
                    if (pl.getHookManager().getFactionsHook().isEnemy(p, otherP) && l.distance(otherP.getLocation()) <= pl.getConfManager().getFacEnemyRange()) {
                        if (otherP.isFlying()) pl.getFlightManager().check(otherP);
                        disable = true;
                    }
            }
        }
        return disable;
    }
}
