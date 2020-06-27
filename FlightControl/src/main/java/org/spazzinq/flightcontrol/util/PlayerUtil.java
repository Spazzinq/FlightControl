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

package org.spazzinq.flightcontrol.util;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.object.FlyPermission;

import static org.spazzinq.flightcontrol.util.MathUtil.timeArray;

public final class PlayerUtil {
    private static final String[] longUnits = {"day", "hour", "minute", "second"};
    private static final String[] shortUnits = {"d", "h", "m", "s"};

    public static boolean hasPermission(CommandSender p, FlyPermission flyPermission) {
        return p.hasPermission(flyPermission.toString());
    }

    public static boolean hasPermissionFly(Player p, String data) {
        return p.hasPermission(FlyPermission.FLY_STUB + data);
    }

    public static boolean hasPermissionNoFly(Player p, String data) {
        return p.hasPermission(FlyPermission.NO_FLY_STUB + data);
    }

    public static boolean hasPermissionCategory(Player p, Category category) {
        return p.hasPermission(FlyPermission.CATEGORY_STUB + category.getName());
    }

    public static boolean hasPermissionTempfly(Player p, Category category) {
        return p.hasPermission(FlyPermission.TEMP_FLY_STUB + category.getName());
    }

    public static String shortPlaceholder(FlightPlayer flightPlayer) {
        long length = formatLength(flightPlayer.getTempFlyLength());

        if (length > 0) {
            StringBuilder builder = new StringBuilder();
            int[] amounts = timeArray(length);

            for (int n = 0; n < amounts.length; n++) {
                if (amounts[n] != 0) {
                    builder.append(amounts[n]).append(shortUnits[n]);
                    if (n != amounts.length - 1) {
                        builder.append(" ");
                    }
                }
            }

            return builder.toString();
        } else {
            return "0s";
        }
    }

    public static String longPlaceholder(FlightPlayer flightPlayer) {
        long length = formatLength(flightPlayer.getTempFlyLength());

        if (length > 0) {
            StringBuilder builder = new StringBuilder();
            int[] amounts = timeArray(length);

            for (int n = 0; n < amounts.length; n++) {
                if (amounts[n] != 0) {
                    builder.append(amounts[n]).append(" ").append(longUnits[n]);
                    if (amounts[n] > 1) {
                        builder.append("s");
                    }
                    builder.append(", ");
                }
            }
            builder.delete(builder.length() - 2, builder.length());

            return builder.toString();
        } else {
            return "0 seconds";
        }
    }

    public static long formatLength(Long length) {
        return length == null ? 0 : (length - System.currentTimeMillis()) / 1000;
    }
}
