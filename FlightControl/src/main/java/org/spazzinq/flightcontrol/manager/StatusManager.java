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

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.Cause;
import org.spazzinq.flightcontrol.util.CheckUtil;

import static org.spazzinq.flightcontrol.util.PlayerUtil.hasPermissionFly;
import static org.spazzinq.flightcontrol.util.PlayerUtil.hasPermissionNoFly;

public class StatusManager {
    FlightControl pl;

    public StatusManager(FlightControl pl) {
        this.pl = pl;
    }

    // TODO Add simple debug then ALL eval debug in FC main class
    boolean checkEnable(Player p) {
        // Eval always CheckSet
        Check check = CheckUtil.checkAll(pl.getCheckManager().getAlwaysChecks().getEnabled(), p);
        Cause cause = null;

        // Eval category CheckSet
        if (check == null) {
            Category category = pl.getCategoryManager().getCategory(p);
            check = CheckUtil.checkAll(category.getChecks().getEnabled(), p);
        }

        // Eval permissions
        if (check == null) {
            String worldName = p.getWorld().getName();
            String regionName = pl.getHookManager().getWorldGuardHook().getRegionName(p.getLocation());

            if (hasPermissionFly(p, worldName)
                    || regionName != null && hasPermissionFly(p, worldName + "." + regionName)) {
                cause = Cause.PERMISSION;
            }
        }

        return check != null || cause != null;
    }

    boolean checkDisable(Player p) {
        // Eval always CheckSet
        Check check = CheckUtil.checkAll(pl.getCheckManager().getAlwaysChecks().getDisabled(), p);
        Cause cause = null;

        // Eval category CheckSet
        if (check == null) {
            Category category = pl.getCategoryManager().getCategory(p);
            check = CheckUtil.checkAll(category.getChecks().getDisabled(), p);
        }

        // Eval permissions
        if (check == null) {
            String worldName = p.getWorld().getName();
            String regionName = pl.getHookManager().getWorldGuardHook().getRegionName(p.getLocation());

            if (hasPermissionNoFly(p, worldName)
                    || regionName != null && hasPermissionNoFly(p, worldName + "." + regionName)) {
                cause = Cause.PERMISSION;
            }
        }

        return check != null || cause != null;
    }
}
