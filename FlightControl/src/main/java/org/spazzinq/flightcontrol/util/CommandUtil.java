/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class CommandUtil {
    public static List<String> autoComplete(Set<String> data, String query, boolean autoFill) {
        List<String> matches = new ArrayList<>();

        for (String entry : data) {
            if (entry.startsWith(query)) {
                matches.add(entry);
            }
        }

        return autoFill && matches.isEmpty() ? new ArrayList<>(data) : matches;
    }

    public static List<String> autoComplete(List<String> data, String query) {
        List<String> matches = new ArrayList<>();

        for (String entry : data) {
            if (entry.startsWith(query)) {
                matches.add(entry);
            }
        }
        return matches.isEmpty() ? data : matches;
    }
}
