/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
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

package org.spazzinq.flightcontrol;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import static org.spazzinq.flightcontrol.FlightControl.msg;

final class TempFly implements CommandExecutor {
    private FlightControl pl;
    TempFly(FlightControl pl) { this.pl = pl; }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.flyother")) {
            if (args.length == 1) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    if (s != p) {
                        if (pl.manager.tempBypass.contains(p)) {
                            pl.manager.tempBypass.remove(p);
                            msg(s, "&e&lFlightControl &7» &eYou disabled " + p.getName() + "'s temporary bypass flight!");
                        } else {
                            pl.manager.tempBypass.add(p);
                            msg(s, "&e&lFlightControl &7» &e" + p.getName() + " now has temporary bypass flight until the next server restart!");
                        }
                    } else msg(s, "&e&lFlightControl &7» &eControlling yourself is redundant... why don't you " +
                            (pl.config.command ? "just do /fly?" : "let your flight automatically enable?"));
                } else msg(s, "&e&lFlightControl &7» &eInvalid player! Please provide a valid player to give temporary bypass flight!");
            } else msg(s, "&c&lFlightControl &7» &cPlease provide a player to give temporary bypass flight!");
        } else msg(s, pl.config.noPerm);
        return true;
    }
}
