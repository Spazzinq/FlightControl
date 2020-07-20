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

import static org.spazzinq.flightcontrol.util.PlayerUtil.hasPermissionFly;
import static org.spazzinq.flightcontrol.util.PlayerUtil.hasPermissionNoFly;

public class StatusManager {
    FlightControl pl;

    public StatusManager(FlightControl pl) {
        this.pl = pl;
    }

    public HashSet<Check> checkEnable(Player p) {
        return checkEnable(p, null);
    }

    public HashSet<Check> checkDisable(Player p) {
        return checkDisable(p, null);
    }

    public HashSet<Check> checkEnable(Player p, CommandSender s) {
        boolean debug = s != null;

        // Eval always CheckSet
        HashSet<Check> trueChecks = CheckUtil.checkAll(pl.getCheckManager().getAlwaysChecks().getEnabled(), p, debug);

        // Eval category CheckSet
        if (trueChecks.isEmpty() || debug) {
            Category category = pl.getCategoryManager().getCategory(p);

            if (category != null) {
                HashSet<Check> enabledCatChecks = CheckUtil.checkAll(category.getChecks().getEnabled(), p, debug);

                trueChecks.addAll(enabledCatChecks);
            }
        }

        // Eval permissions
        if (trueChecks.isEmpty() || debug) {
            String worldName = p.getWorld().getName();
            String regionName = pl.getHookManager().getWorldGuardHook().getRegionName(p.getLocation());

            pl.registerDefaultPerms(worldName);
            if (regionName != null) { // Register new regions dynamically
                pl.registerDefaultPerms(worldName + "." + regionName);
            }

            if (hasPermissionFly(p, worldName)) {
                trueChecks.add(WorldPermissionCheck.getInstance());
            }

            // Allow debug to still eval
            if ((trueChecks.isEmpty() || debug)
                    && regionName != null && hasPermissionFly(p, worldName + "." + regionName)) {
                trueChecks.add(RegionPermissionCheck.getInstance());
            }
        }

        if (debug) {
            Category category = pl.getCategoryManager().getCategory(p);
            HashSet<Check> falseChecks = new HashSet<>();
            falseChecks.addAll(pl.getCheckManager().getAlwaysChecks().getEnabled());
            falseChecks.addAll(category.getChecks().getEnabled());
            falseChecks.add(WorldPermissionCheck.getInstance());
            falseChecks.add(RegionPermissionCheck.getInstance());

            falseChecks.removeAll(trueChecks);

            MessageUtil.msg(s, "&e&lEnable\n&aTrue&f: " + trueChecks + "\n&cFalse&f: " + falseChecks);
        }

        return trueChecks;
    }

    public HashSet<Check> checkDisable(Player p, CommandSender s) {
        boolean debug = s != null;

        // Eval always CheckSet
        HashSet<Check> trueChecks = CheckUtil.checkAll(pl.getCheckManager().getAlwaysChecks().getDisabled(), p, debug);

        // Eval category CheckSet
        if (trueChecks.isEmpty() || debug) {
            Category category = pl.getCategoryManager().getCategory(p);

            if (category != null) {
                HashSet<Check> disabledCatChecks = CheckUtil.checkAll(category.getChecks().getDisabled(), p, debug);

                trueChecks.addAll(disabledCatChecks);
            }
        }

        // Eval permissions
        if (trueChecks.isEmpty() || debug) {
            String worldName = p.getWorld().getName();
            String regionName = pl.getHookManager().getWorldGuardHook().getRegionName(p.getLocation());

            if (hasPermissionNoFly(p, worldName)) {
                trueChecks.add(WorldPermissionCheck.getInstance());
            }

            // Allow debug to still eval
            if ((trueChecks.isEmpty() || debug)
                    && regionName != null && hasPermissionNoFly(p, worldName + "." + regionName)) {
                trueChecks.add(RegionPermissionCheck.getInstance());
            }
        }

        if (debug) {
            Category category = pl.getCategoryManager().getCategory(p);
            HashSet<Check> falseChecks = new HashSet<>();
            falseChecks.addAll(pl.getCheckManager().getAlwaysChecks().getDisabled());
            falseChecks.addAll(category.getChecks().getDisabled());
            falseChecks.add(WorldPermissionCheck.getInstance());
            falseChecks.add(RegionPermissionCheck.getInstance());

            falseChecks.removeAll(trueChecks);

            MessageUtil.msg(s, "&e&lOverride\n&aTrue&f: " + trueChecks + "\n&cFalse&f: " + falseChecks);
        }

        return trueChecks;
    }
}
