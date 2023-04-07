/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.placeholder;

import be.maximvdw.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

public class MVdWPlaceholder {
    private final FlightControl pl;

    // TODO Refactor somehow
    public MVdWPlaceholder(FlightControl pl) {
        this.pl = pl;

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

            return PlayerUtil.durationToSymbols(pl.getPlayerManager().getFlightPlayer(e.getPlayer()));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_long", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return PlayerUtil.durationToWords(pl.getPlayerManager().getFlightPlayer(e.getPlayer()));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_s", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.seconds(getTimeLeft(e.getPlayer())));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_m", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.minutes(getTimeLeft(e.getPlayer())));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_h", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.hours(PlayerUtil.formatLength(getTimeLeft(e.getPlayer()))));
        });

        PlaceholderAPI.registerPlaceholder(pl, "flightcontrol_tempfly_d", e -> {
            if (e.getPlayer() == null) {
                return "";
            }

            return String.valueOf(MathUtil.days(getTimeLeft(e.getPlayer())));
        });
    }

    private long getTimeLeft(Player p) {
        return pl.getPlayerManager().getFlightPlayer(p).getTempflyTimer().getTimeLeft();
    }
}
