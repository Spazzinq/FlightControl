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

package org.spazzinq.flightcontrol.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class ToggleTrailCommand implements CommandExecutor {
    private final FlightControl pl;

    public ToggleTrailCommand() {
        pl = FlightControl.getInstance();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof Player) {
            Player p = (Player) s;

            if (pl.getPlayerManager().getFlightPlayer(p).toggleTrail()) {
                // No need to check for trail enable because of command listener
                msg(s, pl.getLangManager().getPersonalTrailEnable(), pl.getLangManager().useActionBar());
            } else {
                pl.getTrailManager().disableTrail(p);
                msg(s, pl.getLangManager().getPersonalTrailDisable(), pl.getLangManager().useActionBar());
            }
        } else {
            pl.getLogger().info("Only players can use this command (the console isn't a player!)");
        }
        return true;
    }
}
