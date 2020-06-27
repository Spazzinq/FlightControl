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

package org.spazzinq.flightcontrol.placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class MVdWPlaceholder {
    public MVdWPlaceholder(FlightControl pl) {
        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_flying", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(e.getPlayer().isFlying());
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_short", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return PlayerUtil.shortPlaceholder(pl.getPlayerManager().getFlightPlayer(e.getPlayer()));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_long", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return PlayerUtil.longPlaceholder(pl.getPlayerManager().getFlightPlayer(e.getPlayer()));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_s", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.seconds(PlayerUtil.formatLength(pl.getPlayerManager().getFlightPlayer(e.getPlayer()).getTempFlyLength())));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_m", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.minutes(PlayerUtil.formatLength(pl.getPlayerManager().getFlightPlayer(e.getPlayer()).getTempFlyLength())));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_h", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.hours(PlayerUtil.formatLength(pl.getPlayerManager().getFlightPlayer(e.getPlayer()).getTempFlyLength())));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_d", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.days(PlayerUtil.formatLength(pl.getPlayerManager().getFlightPlayer(e.getPlayer()).getTempFlyLength())));
        });
    }
}
