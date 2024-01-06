/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2022 Spazzinq
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

package org.spazzinq.flightcontrol.object;

import lombok.Getter;

@Getter
public enum UpdateStatus {
    UNKNOWN("&e&lFlightControl &7» &eFailed to check for updates. Unknown update status."),
    FETCHING_FROM_SPIGOT("&a&lFlightControl &7» &aChecking for updates..."),
    NEEDS_UPDATE("&e&lFlightControl &7» &eThere is an update available, but you either disabled automatic " +
            "updates or the update is major and requires manual installation. Remember to download and update the " +
            "plugin to receive new features and security updates!"),
    UP_TO_DATE("&a&lFlightControl &7» &aNo updates found. You're in the clear!"),
    DOWNLOADING("&a&lFlightControl &7» &aCurrently downloading an update..."),
    DOWNLOADED_BUT_NOT_VERIFIED("&c&lFlightControl &7» &cCould not verify the newly downloaded update is not corrupted!"),
    VERIFIED("&a&lFlightControl &7» &aThe update has already been downloaded but cannot install right now. Restart " +
            "(or reload) the server to install the update."),
    WILL_AUTO_UPDATE("&a&lFlightControl &7» &aAutomatic installation finished! Welcome to a new version of FlightControl.");

    private final String message;

    UpdateStatus(String message) {
        this.message = message;
    }
}
