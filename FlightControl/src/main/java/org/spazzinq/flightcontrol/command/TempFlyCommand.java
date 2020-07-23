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

package org.spazzinq.flightcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.HashMap;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;
import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public final class TempFlyCommand implements CommandExecutor {
    private final FlightControl pl;

    public TempFlyCommand(FlightControl pl) {
        this.pl = pl;
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        boolean console = s instanceof ConsoleCommandSender;

        if (args.length == 1) {
            if (PlayerUtil.hasPermission(s, FlyPermission.TEMP_FLY) || console) {
                Player argPlayer = Bukkit.getPlayer(args[0]);

                if (argPlayer != null) {
                    FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(argPlayer);

                    if (flightPlayer.getTempflyTimer().getTimeLeft() == null) {
                        msgVar(s, pl.getLangManager().getTempFlyDisabled(), false, "player", argPlayer.getName());
                    } else {
                        msgVar(s, pl.getLangManager().getTempFlyCheck(), false, new HashMap<String, String>() {{
                            put("player", argPlayer.getName());
                            put("duration", PlayerUtil.longTempflyPlaceholder(flightPlayer));
                        }});
                    }
                } else {
                    if (console) {
                        pl.getLogger().warning("Invalid player! Use \"/tempfly (player) disable\" to disable a player's temporary" +
                                " flight; otherwise, use \"/tempfly (length) (player)\" to enable flight.");
                    } else {
                        setTempFly(s, (Player) s, args[0], false);
                    }
                }
            } else {
                msg(s, pl.getLangManager().getPermDenied());
            }
        } else if (args.length == 2) {
            if (PlayerUtil.hasPermission(s, FlyPermission.TEMP_FLY_OTHERS) || console) {
                Player argPlayer = Bukkit.getPlayer(args[1]);

                if (argPlayer == null) {
                    msg(s, pl.getLangManager().getTempFlyUsage());
                } else {
                    setTempFly(s, argPlayer, args[0], "silenttempfly".equals(label.toLowerCase()));
                }
            } else {
                msg(s, pl.getLangManager().getPermDenied());
            }
        } else if (PlayerUtil.hasPermission(s, FlyPermission.TEMP_FLY)
                || PlayerUtil.hasPermission(s, FlyPermission.TEMP_FLY_OTHERS)
                || console) {
            msg(s, pl.getLangManager().getTempFlyUsage());
        } else {
            msg(s, pl.getLangManager().getPermDenied());
        }
        return true;
    }

    private void setTempFly(CommandSender s, Player p, String length, boolean silent) {
        FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(p);

        if (length.equalsIgnoreCase("disable")
                || length.equalsIgnoreCase("stop")
                || length.equalsIgnoreCase("end")
                || length.equalsIgnoreCase("off")) {
            flightPlayer.getTempflyTimer().reset();
            msgVar(s, pl.getLangManager().getTempFlyDisable(), false, "player", p.getName());
        } else if (length.matches("\\d+([smhd]|seconds?|minutes?|hours?|days?)")) {
            char unit = findUnit(length);
            int unitIndex = length.indexOf(unit);
            // Just in case it's a really
            long time = Long.parseLong(length.substring(0, unitIndex == -1 ? length.length() : unitIndex));
            boolean notOne = time != 1;
            StringBuilder lengthFormatted = new StringBuilder(time + " ");
            // In milliseconds
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
            if (notOne) {
                lengthFormatted.append("s");
            }

            if (!silent) {
                String msg = flightPlayer.getTempflyTimer().hasTimeLeft() ? pl.getLangManager().getTempFlyAdd()
                        : pl.getLangManager().getTempFlyEnable();

                msgVar(s, msg, false, new HashMap<String, String>() {{
                    put("player", p.getName());
                    put("duration", lengthFormatted.toString());
                }});
            }

            // Add on if the player already has tempfly
            flightPlayer.setTempFlyLength(time, flightPlayer.getTempflyTimer().hasTimeLeft());
        } else {
            msg(s, pl.getLangManager().getTempFlyUsage());
        }
    }

    private char findUnit(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.substring(i, i + 1).matches("[smhd]")) {
                return input.charAt(i);
            }
        }
        return 's';
    }
}
