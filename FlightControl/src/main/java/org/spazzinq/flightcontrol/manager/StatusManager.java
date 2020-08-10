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

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.check.always.RegionPermissionCheck;
import org.spazzinq.flightcontrol.check.always.WorldPermissionCheck;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.util.CheckUtil;
import org.spazzinq.flightcontrol.util.MessageUtil;

import java.util.HashSet;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;
import static org.spazzinq.flightcontrol.util.PlayerUtil.hasPermissionFly;

public class StatusManager {
    final FlightControl pl;

    public StatusManager() {
        pl = FlightControl.getInstance();
    }

    public HashSet<Check> checkEnable(Player p) {
        return checkEnable(p, null);
    }

    public HashSet<Check> checkDisable(Player p) {
        return checkDisable(p, null);
    }

    public HashSet<Check> checkEnable(Player p, CommandSender s) {
        return check(true, p, s);
    }

    public HashSet<Check> checkDisable(Player p, CommandSender s) {
        return check(false, p, s);
    }

    private HashSet<Check> check(boolean enabled, Player p, CommandSender s) {
        boolean debug = s != null;

        // Eval always CheckSet
        HashSet<Check> trueChecks = CheckUtil.checkAll(pl.getCheckManager().getAlwaysChecks().get(enabled), p, debug);

        // Eval category CheckSet
        if (trueChecks.isEmpty() || debug) {
            Category category = pl.getCategoryManager().getCategory(p);

            if (category != null) {
                HashSet<Check> categoryChecks = CheckUtil.checkAll(category.getChecks().get(enabled), p, debug);

                trueChecks.addAll(categoryChecks);
            }
        }

        // Eval permissions
        if (trueChecks.isEmpty() || debug) {
            String worldName = p.getWorld().getName();
            String regionName = pl.getHookManager().getWorldGuardHook().getRegionName(p.getLocation());

            // Register new world permissions dynamically
            pl.getPermissionManager().registerDefaultPerms(worldName);
            if (regionName != null) { // Register new region permissions dynamically
                pl.getPermissionManager().registerDefaultPerms(worldName + "." + regionName);
            }

            if (hasPermissionFly(enabled, p, worldName)) {
                trueChecks.add(WorldPermissionCheck.getInstance());
            }

            // Allow debug to still eval
            if ((trueChecks.isEmpty() || debug)
                    && regionName != null && hasPermissionFly(enabled, p, worldName + "." + regionName)) {
                trueChecks.add(RegionPermissionCheck.getInstance());
            }
        }

        if (debug) {
            Category category = pl.getCategoryManager().getCategory(p);
            HashSet<Check> falseChecks = new HashSet<>(pl.getCheckManager().getAlwaysChecks().get(enabled));
            falseChecks.addAll(category.getChecks().get(enabled));
            falseChecks.add(WorldPermissionCheck.getInstance());
            falseChecks.add(RegionPermissionCheck.getInstance());

            falseChecks.removeAll(trueChecks);

            MessageUtil.msg(s, "&e&l" + (enabled ? "Enable" : "Override") + "\n&aTrue&f: " + trueChecks + "\n&cFalse&f: " + falseChecks);
        }

        return trueChecks;
    }

    /**
     * Sends debug information about a player's flight status.
     *
     * @param sender the recipient of the debug message
     * @param targetPlayer the target of the debug check
     */
    public void debug(CommandSender sender, Player targetPlayer) {
        Location l = targetPlayer.getLocation();
        World world = l.getWorld();
        String regionName = pl.getHookManager().getWorldGuardHook().getRegionName(l);
        Category category = pl.getCategoryManager().getCategory(targetPlayer);

        // config options (settings) and permissions that act upon the same function are listed as
        // setting boolean (space) permission boolean
        msg(sender, "&a&lFlightControl &f" + pl.getDescription().getVersion() +
                "\n&eTarget &7» &f" + targetPlayer.getName() +
                "\n&eCategory &7» &f" + category.getName() +
                (pl.getHookManager().getWorldGuardHook().isHooked() ? "\n&eW.RG &7» &f" + world.getName() + "." + regionName : "") +
                (pl.getHookManager().getFactionsHook().isHooked() ? "\n&eFac &7» &f" + category.getFactions() : "") +
                "\n&eWRLDs &7» &f" + category.getWorlds() +
                (pl.getHookManager().getWorldGuardHook().isHooked() ? "\n&eRGs &7» &f" + category.getRegions() : "") +
                ("\n&eBypass &7» &f" + CheckUtil.checkAll(pl.getCheckManager().getBypassChecks(), targetPlayer, true)));

        checkEnable(targetPlayer, sender);
        checkDisable(targetPlayer, sender);
    }
}
