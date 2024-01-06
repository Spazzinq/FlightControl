/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import static org.spazzinq.flightcontrol.util.MathUtil.*;

public class ClipPlaceholder extends PlaceholderExpansion {
    private final FlightControl pl;

    public ClipPlaceholder(FlightControl pl) {
        this.pl = pl;
    }

    @Override public @NotNull String getIdentifier() {
        return "flightcontrol";
    }

    @Override public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        Player player = offlinePlayer == null ? null : offlinePlayer.getPlayer();

        if (player == null) {
            return "";
        }

        // %flightcontrol_<identifier>%

        if ("flying".equals(identifier)) {
            return String.valueOf(player.isFlying());
        } else if (identifier.startsWith("flying_")) {
            Player target = Bukkit.getPlayer(identifier.substring("flying_".length()));

            return target == null ? "offline" : String.valueOf(target.isFlying());
        }

        FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(player);
        long time = PlayerUtil.formatLength(flightPlayer.getTempflyTimer().getTimeLeft());

        switch (identifier) {
            case "tempfly_short": return PlayerUtil.durationToSymbols(flightPlayer);
            case "tempfly_long": return PlayerUtil.durationToWords(flightPlayer);
            case "tempfly_s": return String.valueOf(seconds(time));
            case "tempfly_m": return String.valueOf(minutes(time));
            case "tempfly_h": return String.valueOf(hours(time));
            case "tempfly_d": return String.valueOf(days(time));
            default: break;
        }

        // If invalid placeholder
        return null;
    }

    @Override public boolean canRegister() {
        return true;
    }

    @Override public @NotNull String getAuthor() {
        return pl.getDescription().getAuthors().get(0);
    }

    @Override public @NotNull String getVersion() {
        return pl.getDescription().getVersion();
    }

    @Override public boolean persist() {
        return true;
    }
}
