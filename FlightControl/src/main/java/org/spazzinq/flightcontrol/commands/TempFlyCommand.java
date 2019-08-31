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
import org.spazzinq.flightcontrol.FlightManager;
import org.spazzinq.flightcontrol.TempFlyManager;

import static org.spazzinq.flightcontrol.FlightControl.msg;

public final class TempFlyCommand implements CommandExecutor {
    private FlightControl pl;
    private FlightManager flightManager;
    private TempFlyManager tempflyManager;

    public TempFlyCommand(FlightControl pl) {
        this.pl = pl;
        flightManager = pl.getFlightManager();
        tempflyManager = pl.getTempflyManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.flyother")) {
            if (args.length >= 1) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p != null) {
                    boolean isSelf = p == s;
                    if (flightManager.getTempList().contains(p)) {
                        if (tempflyManager.getScheduledExpirations().contains(p.getUniqueId())) {
                            tempflyManager.removeTempfly(p);
                        } else {
                            flightManager.getTempList().remove(p);
                        }
                        msg(s, "&e&lFlightControl &7» &eYou disabled &f" + (isSelf ? "your own" : p.getName() + "&e's") + " &etemporary flight!");
                    } else {
                        boolean set = false;
                        StringBuilder length = null;
                        if (args.length == 2) {
                            if (args[1].matches("\\d+([smhd]|seconds?|minutes?|hours?|days?)")) {
                                char unit = findUnit(args[1]);
                                // Just in case it's a really
                                long time = Long.parseLong(args[1].substring(0, args[1].length() - 1));
                                boolean isNotOne = time != 1;
                                length = new StringBuilder(time + " ");
                                time *= 1000;

                                switch (unit) {
                                    case 'm':
                                        length.append("minute");
                                        time *= 60;
                                        break;
                                    case 'h':
                                        length.append("hour");
                                        time *= 3600;
                                        break;
                                    case 'd':
                                        length.append("day");
                                        time *= 86400;
                                        break;
                                    default:
                                        length.append("second");
                                        break;
                                }
                                if (isNotOne) length.append("s");

                                time += System.currentTimeMillis();

                                // Should always be above the current time, so no need to check
                                tempflyManager.setTempfly(p, time);
                                set = true;
                            } else msg(s, "&e&lFlightControl &7» &eInvalid format! Please use &f/tempfly (player) (length)&e! Follow the length with " +
                                    "a unit of time (NO SPACE!): &fs (seconds), m (minutes), h (hours), d (days)&e.");
                        } else {
                            flightManager.getTempList().add(p);
                            set = true;
                        }
                        if (set) msg(s, "&e&lFlightControl &7» &e" + (isSelf ? "You" : p.getName()) + " now " + (isSelf ? "have" : "has") + " temporary flight "
                                + (length == null ? "until the next server restart/restart of FlightControl" : "for " + length));
                    }
                } else msg(s, "&e&lFlightControl &7» &ePlease provide a valid player!");
            } else msg(s, "&c&lFlightControl &7» &cPlease provide a player to give temporary flight!");
        } else msg(s, pl.getConfigManager().getNoPermission());
        return true;
    }

    private Character findUnit(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.substring(i, i+1).matches("[smhd]")) return input.charAt(i);
        }
        return 's';
    }
}
