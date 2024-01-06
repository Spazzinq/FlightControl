/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.object;

import lombok.Getter;

@Getter
public enum UpdateStatus {
    UNKNOWN("&e&lFlightControl &7» &eFailed to check for updates. Check your internet connection?"),
    FETCHING_FROM_SPIGOT("&a&lFlightControl &7» &aChecking for updates..."),
    NEEDS_UPDATE("&e&lFlightControl &7» &eThere is an update available, but you either disabled automatic " +
            "updates or the update is major and requires manual installation. Remember to download and update the " +
            "plugin to receive new features and security updates!"),
    UP_TO_DATE("&a&lFlightControl &7» &aNo updates found. You're in the clear!"),
    DOWNLOADING("&a&lFlightControl &7» &aCurrently downloading an update..."),
    DOWNLOADED("&c&lFlightControl &7» &cCould not verify the newly downloaded update is not corrupted!"),
    VERIFIED("&a&lFlightControl &7» &aThe update has been downloaded but requires a server restart. Restart " +
            "(or reload) the server to install the update.");

    private final String message;

    UpdateStatus(String message) {
        this.message = message;
    }
}
