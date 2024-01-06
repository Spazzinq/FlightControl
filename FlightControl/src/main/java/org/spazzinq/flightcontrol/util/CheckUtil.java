/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.util;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.Check;

import java.util.HashSet;

public final class CheckUtil {
    /**
     * Returns the first true check, or all true checks if debugging.
     *
     * @param checks the set of checks to check
     * @param p the player to check
     * @param debug if method should evaluate all regardless of performance
     * @return returns the first true check or all true checks if debugging
     */
    public static HashSet<Check> evaluate(HashSet<Check> checks, Player p, boolean debug) {
        HashSet<Check> trueChecks = new HashSet<>();

        // Did not put debug conditional at more narrow level
        // to prevent unnecessary rechecking (performance)
        if (debug) {
            for (Check check : checks) {
                if (check.check(p)) {
                    trueChecks.add(check);
                }
            }
        } else {
            for (Check check : checks) {
                if (check.check(p)) {
                    trueChecks.add(check);
                    break;
                }
            }
        }

        return trueChecks;
    }

    public static HashSet<Check> evaluate(HashSet<Check> checks, Player p) {
        return evaluate(checks, p, false);
    }
}
