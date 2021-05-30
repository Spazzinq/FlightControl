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
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.util.CheckUtil;
import org.spazzinq.flightcontrol.util.MessageUtil;

import java.util.HashSet;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class StatusManager {
    private final FlightControl pl;

    public StatusManager() {
        pl = FlightControl.getInstance();
    }

    /**
     * Checks if the player's flight should be enabled.
     * @param targetPlayer the target of the check
     * @return Checks that indicate flight should enable
     */
    public HashSet<Check> checkEnable(Player targetPlayer) {
        return checkEnable(targetPlayer, null);
    }

    /**
     * Checks if the player's flight should be enabled
     * and sends a debug message to the sender.
     * @param targetPlayer the target of the debug check
     * @param sender the recipient of the debug info
     * @return Checks that indicate flight should enable
     */
    public HashSet<Check> checkEnable(Player targetPlayer, CommandSender sender) {
        return check(true, targetPlayer, sender);
    }

    /**
     * Checks if the player's flight should be disabled.
     * @param targetPlayer the target of the check
     * @return Checks that indicate flight should disable
     */
    public HashSet<Check> checkDisable(Player targetPlayer) {
        return checkDisable(targetPlayer, null);
    }

    /**
     * Checks if the player's flight should be disabled
     * and sends a debug message to the sender.
     * @param targetPlayer the target of the debug check
     * @param sender the recipient of the debug info
     * @return Checks that indicate flight should disable
     */
    public HashSet<Check> checkDisable(Player targetPlayer, CommandSender sender) {
        return check(false, targetPlayer, sender);
    }

    private HashSet<Check> check(boolean enable, Player targetPlayer, CommandSender sender) {
        boolean debug = sender != null;
        HashSet<Check> allChecks = new HashSet<>();
        // Always & Category Checks
        allChecks.addAll(pl.getCheckManager().getAlwaysChecks().get(enable));
        allChecks.addAll(pl.getCategoryManager().getCategory(targetPlayer).getChecks().get(enable));

        // Register perms before evaluating permission-based ones
        pl.getPermissionManager().registerLocationalFlyPerms(targetPlayer);

        // Evaluate all checks
        HashSet<Check> trueChecks = CheckUtil.evaluate(allChecks, targetPlayer, debug);

        if (debug) {
            HashSet<Check> falseChecks = new HashSet<>(allChecks);
            falseChecks.removeAll(trueChecks);

            MessageUtil.msg(sender, "&e&l" + (enable ? "Enable" : "Override") + "\n&aTrue&f: " + trueChecks + "\n&cFalse&f: " + falseChecks);
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
                ("\n&eBypass &7» &f" + CheckUtil.evaluate(pl.getCheckManager().getBypassChecks(), targetPlayer, true)));

        if (pl.getCheckManager().getIgnoreCheck().check(targetPlayer)) {
            MessageUtil.msg(sender, "&e&lCHECKS IGNORED.");
        } else {
            checkEnable(targetPlayer, sender);
            checkDisable(targetPlayer, sender);
        }

    }
}
