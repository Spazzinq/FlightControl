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

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import static org.spazzinq.flightcontrol.util.MathUtil.*;

public class ClipPlaceholder extends PlaceholderExpansion {
    private final FlightControl pl;

    public ClipPlaceholder(FlightControl pl) {
        this.pl = pl;
    }

    @Override public String getIdentifier() {
        return "flightcontrol";
    }

    @Override public String onPlaceholderRequest(Player player, String identifier){
        if (player == null) {
            return "";
        }

        // %flightcontrol_<identifier>%

        if (identifier.equals("flying")) {
            return String.valueOf(player.isFlying());
        }

        FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(player);
        long time = PlayerUtil.formatLength(flightPlayer.getTempflyTimer().getTimeLeft());

        switch (identifier) {
            case "tempfly_short": return PlayerUtil.shortPlaceholder(flightPlayer);
            case "tempfly_long": return PlayerUtil.longPlaceholder(flightPlayer);
            case "tempfly_s": return String.valueOf(seconds(time));
            case "tempfly_m": return String.valueOf(minutes(time));
            case "tempfly_h": return String.valueOf(hours(time));
            case "tempfly_d": return String.valueOf(days(time));
        }

        // If invalid placeholder
        return null;
    }

    @Override public boolean canRegister(){
        return true;
    }

    @Override public String getAuthor(){
        return pl.getDescription().getAuthors().toString();
    }

    @Override public String getVersion(){
        return pl.getDescription().getVersion();
    }

    @Override public boolean persist() {
        return true;
    }
}
