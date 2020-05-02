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

package org.spazzinq.flightcontrol.manager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.Evaluation;
import org.spazzinq.flightcontrol.util.MessageUtil;

import java.util.HashSet;

import static org.spazzinq.flightcontrol.object.FlyPermission.*;
import static org.spazzinq.flightcontrol.util.PlayerUtil.*;

public class StatusManager {
    private final FlightControl pl;

    private static final String[] debugEnableTitles = {
            "World",
            "Region",
            "Factions",
            "CrazyEnchants",

            "PlotSquared",
            "Towny: Own",
            "Lands: Own",
            "Lands: Trusted",
            "GriefPrevention: Own",
            "GriefPrevention: Trusted",

            "Permission: ALL",
            "Permission: World",
            "Permission: Region",
            "Tempfly"
    };

    private static final String[] debugDisableTitles = {
            "World",
            "Region",
            "Factions",
            "Combat",

            "PlotSquared",

            "Nearby",
            "Permission: World",
            "Permission: Region"
    };

    public StatusManager(FlightControl pl) {
        this.pl = pl;
    }

    public Evaluation evalFlight(Player p) {
        return evalFlight(p, false, null);
    }

    public Evaluation evalFlight(Player p, boolean debug, CommandSender debugRecipient) {
        Location l = p.getLocation();
        World world = l.getWorld();
        String worldName = world.getName(),
                regionName = pl.getHookManager().getWorldGuardHook().getRegionName(l);
        Region region = new Region(world, regionName);
        Category category = pl.getCategoryManager().getCategory(p);
        FactionRelation relation = pl.getFactionsManager().getRelationToLocation(p);

        if (regionName != null) { // Register new worlds/regions dynamically
            pl.registerDefaultPerms(worldName);
            pl.registerDefaultPerms(worldName + "." + regionName);
        }

        StringBuilder debugMsg = debug ? new StringBuilder("&e&lEnable\n&f") : null;

        boolean enable = oneTrue(debugMsg,
                /* Category checks */
                category.enabledContains(world),
                category.enabledContains(region),
                category.enabledContains(relation),

                /* Hook checks */
                // CrazyEnchants has Wings
                pl.getHookManager().getEnchantmentsHook().canFly(p),
                // PlotSquared has flight flag
                pl.getHookManager().getPlotHook().canFly(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()),
                // Towny Own
                (pl.getConfManager().isTownyOwn() || hasPermission(p, TOWNY_OWN))
                        && pl.getHookManager().getTownyHook().townyOwn(p)
                        && !(pl.getConfManager().isTownyWarDisable() && pl.getHookManager().getTownyHook().wartime()),
                // Lands Own
                (pl.getConfManager().isLandsOwnEnable() || hasPermission(p, LANDS_OWN))
                        && pl.getHookManager().getLandsHook().landsIsOwn(p),
                // Lands Trusted
                (pl.getConfManager().isLandsOwnEnable() && pl.getConfManager().isLandsIncludeTrusted() || hasPermission(p, LANDS_TRUSTED) || landsOwnerHasTrustedPerm(l))
                        && pl.getHookManager().getLandsHook().landsIsTrusted(p),
                // GriefPrevention Own
                (pl.getConfManager().isGpClaimOwnEnable() || hasPermission(p, CLAIM_OWN))
                        && pl.getHookManager().getGriefPreventionHook().claimIsOwn(l, p),
                // GriefPrevention Trusted
                ((pl.getConfManager().isGpClaimOwnEnable() && pl.getConfManager().isGpClaimIncludeTrusted()) || hasPermission(p, CLAIM_TRUSTED))
                        && pl.getHookManager().getGriefPreventionHook().claimIsTrusted(l, p),

                /* Permission checks */
                hasPermission(p, FLY_ALL),
                hasPermissionFly(p, worldName),
                regionName != null && hasPermissionFly(p, worldName + "." + regionName),

                /* Tempfly check*/
                pl.getPlayerManager().getFlightPlayer(p).hasTempFly()
        );

        if (debug) {
            debugMsg.append(" \n&e&lDisable\n&f");
        }

        boolean disable = oneTrue(debugMsg,
                /* Category checks */
                category.disabledContains(world),
                category.disabledContains(region),
                category.disabledContains(relation),

                /* Hook checks */
                // In combat
                pl.getHookManager().getCombatHook().tagged(p),
                // PlotSquared has deny flight flag
                pl.getHookManager().getPlotHook().cannotFly(worldName, l.getBlockX(), l.getBlockY(), l.getBlockZ()),
                // Nearby check
                nearbyCheck(p, l),

                /* Permission checks */
                hasPermissionNoFly(p, worldName),
                regionName != null && hasPermissionNoFly(p, worldName + "." + regionName)
        );

        if (debug) {
            MessageUtil.msg(debugRecipient,
                    debugMsg.toString().replaceAll("true", "&atrue"));
        }

        return new Evaluation(disable, enable);
    }

    // TODO Finish optimization with caching
    private boolean nearbyCheck(Player p, Location l) {
        if (!hasPermission(p, NEARBYPASS)) {
            World world = l.getWorld();
            boolean disable = false;

            // Prevent comparing 2 different worlds
            if (pl.getConfManager().isNearbyCheck() && p.getWorld().equals(l.getWorld())) {
                HashSet<Player> worldPlayers = new HashSet<>();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (world.equals(onlinePlayer.getWorld())) {
                        worldPlayers.add(onlinePlayer);
                    }
                }
                worldPlayers.remove(p);

                for (Player otherP : worldPlayers) {
                    if ((!pl.getConfManager().isNearbyCheckEnemies() || pl.getHookManager().getFactionsHook().isEnemy(p, otherP))
                            && l.distanceSquared(otherP.getLocation()) <= pl.getConfManager().getNearbyRangeSquared()) {
                        if (otherP.isFlying()) {
                            pl.getFlightManager().check(otherP);
                        }
                        disable = true;
                    }
                }
            }
            return disable;
        }
        return false;
    }

    private boolean landsOwnerHasTrustedPerm(Location l) {
        if (pl.getHookManager().getLandsHook().isHooked()) {
            Player landsOwner = Bukkit.getPlayer(pl.getHookManager().getLandsHook().getOwnerUUID(l));

            if (landsOwner != null) {
                return hasPermission(landsOwner, LANDS_TRUSTED);
            }
        }

        return false;
    }

    private boolean oneTrue(StringBuilder debugMsg, boolean... booleans) {
        // If debug
        if (debugMsg != null) {
            String[] debugTitles = booleans.length == debugEnableTitles.length ? debugEnableTitles : debugDisableTitles;

            for (int i = 0; i < booleans.length; i++) {
                debugMsg.append("&7 | &f").append(debugTitles[i]).append(" &7» ").append(booleans[i]);
            }
            // Return value does not matter as it is debug
        } else {
            for (boolean bool : booleans) {
                if (bool) {
                    return true;
                }
            }
        }

        return false;
    }
}
