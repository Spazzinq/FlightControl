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

package org.spazzinq.flightcontrol.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;

public final class TempFlyCommand implements CommandExecutor {
    private FlightControl pl;
    public TempFlyCommand(FlightControl pl) { this.pl = pl; }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.flyother")) {
            if (args.length >= 1) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    // TODO Messages for self-tempfly (enabling it on yourself)
                    if (pl.getFlightManager().getTempBypassList().contains(p)) {
                        pl.getFlightManager().getTempBypassList().remove(p);
                        FlightControl.msg(s, "&e&lFlightControl &7» &eYou disabled " + p.getName() + "'s temporary bypass flight!");
                    } else {
                        // TODO timed temp-fly
                        //                            if (args.length == 2) {
                        //                                args[1] = args[1].toLowerCase();
                        //                                if (args[1].matches("\\d+[smhd]")) {
                        //                                    char unit = args[1].charAt(args[1].length() - 1);
                        //                                    // Just in case it's a really
                        //                                    long time = Long.parseLong(args[1].substring(0, args[1].length() - 1)) * 1000;
                        //
                        //                                    switch (unit) {
                        //                                        case 'm': time *= 60; break;
                        //                                        case 'h': time *= 3600; break;
                        //                                        case 'd': time *= 86400; break;
                        //                                        default: break;
                        //                                    }
                        //
                        //                                    System.currentTimeMillis() + time
                        //                                }
                        //                            }
                        pl.getFlightManager().getTempBypassList().add(p);
                        FlightControl.msg(s, "&e&lFlightControl &7» &e" + p.getName() + " now has temporary bypass flight until the next server restart!");
                    }
                } else FlightControl.msg(s, "&e&lFlightControl &7» &eInvalid player! Please provide a valid player to give temporary bypass flight!");
            } else FlightControl.msg(s, "&c&lFlightControl &7» &cPlease provide a player to give temporary bypass flight!");
        } else FlightControl.msg(s, pl.getConfigManager().getNoPermission());
        return true;
    }
}
