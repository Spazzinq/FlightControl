/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
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
        return p != null && p.hasPermission(flyPermission.toString());
    }

    public static boolean hasPermissionFly(boolean flyOrNoFly, Player p, String data) {
        return p != null && p.hasPermission((flyOrNoFly ? FlyPermission.FLY_STUB : FlyPermission.NO_FLY_STUB) + data);
    }

    public static boolean hasPermissionCategory(Player p, Category category) {
        return p != null && p.hasPermission(FlyPermission.CATEGORY_STUB + category.getName());
    }

    public static boolean hasPermissionTempfly(Player p, Category category) {
        return p != null && p.hasPermission(FlyPermission.TEMP_FLY_STUB + category.getName());
    }

    /**
     * Converts given long into a short-hand String representation (e.g. "10s", "10d").
     * @param duration duration of time (in ms)
     * @return short-hand String representation of duration
     */
    public static String durationToSymbols(long duration) {
        if (duration > 0) {
            StringBuilder builder = new StringBuilder();
            int[] amounts = timeArray(duration);

            for (int n = 0; n < amounts.length; n++) {
                if (amounts[n] != 0) {
                    builder.append(amounts[n]).append(shortUnits[n]);
                    if (n != amounts.length - 1) {
                        builder.append(" ");
                    }
                }
            }

            return builder.toString();
        }

        return "0s";
    }

    /**
     * Converts given long into a String representation (e.g. "10 seconds", "10 days").
     * @param duration given duration of time (in ms)
     * @return String representation of duration
     */
    public static String durationToWords(long duration) {
        if (duration > 0) {
            StringBuilder builder = new StringBuilder();
            int[] amounts = timeArray(duration);

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
        }

        return "0 seconds";
    }

    public static String durationToWords(FlightPlayer flightPlayer) {
        return durationToWords(formatLength(flightPlayer.getTempflyTimer().getTimeLeft()));
    }

    public static String durationToSymbols(FlightPlayer flightPlayer) {
        return durationToSymbols(formatLength(flightPlayer.getTempflyTimer().getTimeLeft()));
    }

    public static long formatLength(Long length) {
        return length == null ? 0 : length / 1000;
    }
}
