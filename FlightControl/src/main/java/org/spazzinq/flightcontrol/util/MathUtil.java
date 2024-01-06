/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.util;

public final class MathUtil {
    public static float calcConvertedSpeed(float rationalSpeed, float maxRawSpeed) {
        float multiplier = 0.1f;

        // Set min-max bounds
        if (rationalSpeed > 10f) {
            rationalSpeed = 10f;
        } else if (rationalSpeed < 0.0001f) {
            rationalSpeed = 0.0001f;
        }

        if (rationalSpeed < 1f) {
            // Scale down if less than 1
            return multiplier * rationalSpeed;
        } else {
            // Use ratio formula otherwise
            float ratio = ((rationalSpeed - 1) / 9) * (maxRawSpeed - multiplier);
            return ratio + multiplier;
        }
    }

    /**
     * Returns the normally written duration in seconds.
     * @param durationStr The input String
     * @return The duration in seconds
     */
    public static long calculateDuration(String durationStr) {
        // Remove whitespace
        durationStr = durationStr.replaceAll("\\s+", "");

        char unit = findUnit(durationStr);
        int unitIndex = durationStr.indexOf(unit);
        // Substring off unit
        long duration = Long.parseLong(durationStr.substring(0, unitIndex == -1 ? durationStr.length() : unitIndex));

        switch (unit) {
            case 'm' -> duration *= 60;
            case 'h' -> duration *= 3600;
            case 'd' -> duration *= 86400;
            default -> {}
        }
        return duration;
    }

    public static int days(long length) {
        return (int) (length / 86400);
    }

    public static short hours(long length) {
        return (short) (length % 86400 / 3600);
    }

    public static short minutes(long length) {
        return (short) (length % 3600 / 60);
    }

    public static short seconds(long length) {
        return (short) (length % 60);
    }

    public static int[] timeArray(long length) {
        return new int[] {days(length), hours(length), minutes(length), seconds(length)};
    }

    private static char findUnit(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.substring(i, i + 1).matches("[smhd]")) {
                return input.charAt(i);
            }
        }
        return 's';
    }
}
