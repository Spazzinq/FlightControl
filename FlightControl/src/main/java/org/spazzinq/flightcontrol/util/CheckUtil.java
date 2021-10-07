/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2021 Spazzinq
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
