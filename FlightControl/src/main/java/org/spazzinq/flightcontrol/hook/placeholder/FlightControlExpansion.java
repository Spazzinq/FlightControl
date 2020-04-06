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

package org.spazzinq.flightcontrol.hook.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;

public class FlightControlExpansion extends PlaceholderExpansion {
    private FlightControl pl;

    public FlightControlExpansion(FlightControl pl) {
        this.pl = pl;
    }

    @Override public String getIdentifier() {
        return "flightcontrol";
    }

    @Override public String getVersion(){
        return pl.getDescription().getVersion();
    }

    @Override public String onPlaceholderRequest(Player player, String identifier){
        if(player == null){
            return "";
        }

        // %flightcontrol_<identifier>%
        if (identifier.equals("tempfly_time")) {

        }

        // If invalid placeholder (f.e. %someplugin_placeholder3%)
        return null;
    }

    @Override public boolean canRegister(){
        return true;
    }

    @Override public String getAuthor(){
        return pl.getDescription().getAuthors().toString();
    }

    @Override public boolean persist() {
        return true;
    }
}
