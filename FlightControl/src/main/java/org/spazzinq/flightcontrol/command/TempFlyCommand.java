/*
 * This file is part of FlightControl, which is licensed under the MIT License
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

package org.spazzinq.flightcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.manager.TempFlyManager;
import org.spazzinq.flightcontrol.object.FlightPlayer;

import static org.spazzinq.flightcontrol.FlightControl.msg;

public final class TempFlyCommand implements CommandExecutor {
    private FlightControl pl;
    private TempFlyManager tempflyManager;

    public TempFlyCommand(FlightControl pl) {
        this.pl = pl;
        tempflyManager = pl.getTempflyManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        boolean console = s instanceof ConsoleCommandSender;

        if (args.length == 1) {
            if (s.hasPermission("flightcontrol.tempfly") || console) {
                Player argPlayer = Bukkit.getPlayer(args[0]);

                if (argPlayer != null) {
                    FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(argPlayer);

                    if (flightPlayer.getTempFlyEnd() == null) {
                        msg(s, "&e&lFlightControl &7» &e" + argPlayer.getName() + "'s temporary flight is already disabled!");
                    } else {
                        flightPlayer.setTempFly(null);
                        msg(s, "&e&lFlightControl &7» &eDisabled " + argPlayer.getName() + "'s temporary flight!");
                    }
                } else {
                    if (console) {
                        pl.getLogger().warning("Invalid player! Use /tempfly (player) to disable a player's temporary flight; otherwise, use /tempfly (length) (player) to enable flight.");
                    } else {
                        setTempFly(s, (Player) s, args[0]);
                    }
                }


            } else {
                msg(s, pl.getConfigManager().getNoPermission());
            }
        } else if (args.length == 2) {
            if (s.hasPermission("flightcontrol.tempfly.others") || console) {
                Player argPlayer = Bukkit.getPlayer(args[1]);

                if (argPlayer == null) {
                    msg(s, "&c&lFlightControl &7» &cInvalid player! Use /tempfly (length) (player)!");
                } else {
                    setTempFly(s, argPlayer, args[0]);
                }
            } else {
                msg(s, pl.getConfigManager().getNoPermission());
            }
        } else {
            if (s.hasPermission("flightcontrol.tempfly") || s.hasPermission("flightcontrol.tempfly.others") || console) {
                msg(s, "&c&lFlightControl &7» &cUse /tempfly (length) [player]!");
            } else {
                msg(s, pl.getConfigManager().getNoPermission());
            }
        }
        return true;
    }

    private void setTempFly(CommandSender s, Player p, String length) {
        boolean self = p.equals(s);

        if (pl.getPlayerManager().getFlightPlayer(p).hasTempFly()) {
            tempflyManager.removeTempfly(p);
            msg(s, "&e&lFlightControl &7» &eYou disabled &f" + (self ? "your own" : p.getName() + "&e's") + " &etemporary flight!");
        } else {
            if (length.matches("\\d+([smhd]|seconds?|minutes?|hours?|days?)")) {
                char unit = findUnit(length);
                int unitIndex = length.indexOf(unit);
                // Just in case it's a really
                long time = Long.parseLong(length.substring(0, unitIndex == -1 ? length.length() : unitIndex));
                boolean notOne = time != 1;
                StringBuilder lengthFormatted = new StringBuilder(time + " ");
                time *= 1000;

                switch (unit) {
                    case 'm':
                        lengthFormatted.append("minute");
                        time *= 60;
                        break;
                    case 'h':
                        lengthFormatted.append("hour");
                        time *= 3600;
                        break;
                    case 'd':
                        lengthFormatted.append("day");
                        time *= 86400;
                        break;
                    default:
                        lengthFormatted.append("second");
                        break;
                }
                if (notOne) lengthFormatted.append("s");

                time += System.currentTimeMillis();

                pl.getPlayerManager().getFlightPlayer(p).setTempFly(time);
                // Should always be above the current time, so no need to check
                tempflyManager.checkTempfly(p);
                msg(s, "&e&lFlightControl &7» &e" + (self ? "You" : p.getName()) + " now " + (self ? "have" : "has") + " temporary flight for " + lengthFormatted);
            } else msg(s, "&c&lFlightControl &7» &cInvalid format! Please use &f/tempfly (player) (length)&e! Follow the length with " +
                    "a unit of time. Example lengths: &f1d, 4h, 5m, 30s, 2days, 1hour, 10minutes, 60seconds&e.");
        }
    }

    private char findUnit(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.substring(i, i+1).matches("[smhd]")) return input.charAt(i);
        }
        return 's';
    }
}
