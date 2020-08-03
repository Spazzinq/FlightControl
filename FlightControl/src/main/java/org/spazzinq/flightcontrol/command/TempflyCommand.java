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
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.object.TempflyTaskType;
import org.spazzinq.flightcontrol.util.CommandUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;
import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class TempflyCommand implements CommandExecutor, TabCompleter {
    private final FlightControl pl;

    private final List<String> exampleDurations = Arrays.asList("30minutes", "1hour", "3hours", "6hours", "12hours", "1day");

    public TempflyCommand(FlightControl pl) {
        this.pl = pl;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        boolean console = sender instanceof ConsoleCommandSender;
        Player targetPlayer = null;

        // /tempfly (check, disable)
        if (args.length < 2) {
            if (console) {
                msg(sender, pl.getLangManager().getTempFlyUsage());
            } else {
                targetPlayer = (Player) sender;
            }
        // /tempfly (check, add, remove, set, disable) (player) [duration]
        } else if (console || PlayerUtil.hasPermission(sender, FlyPermission.TEMP_FLY_OTHERS)) {
            targetPlayer = Bukkit.getPlayer(args[1]);
        }

        if (targetPlayer == null) {
            msg(sender, pl.getLangManager().getTempFlyUsage());
        } else {
            TempflyTaskType type = getTaskType(args[0].toUpperCase());

            long duration = 0;
            if (args.length == 3) {
                // Duration
                String durationStr = args[2].toLowerCase();

                if (durationStr.matches("\\d+([smhd]|seconds?|minutes?|hours?|days?)")) {
                    duration = calculateDuration(durationStr);
                }
            }

            runTempflyTask(sender, targetPlayer, type, duration, "silenttempfly".equals(label.toLowerCase()));
        }

        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
        for (int i = 0; i < args.length; i++) {
            // Ignore player case
            if (i != 1) {
                args[i] = args[i].toLowerCase();
            }
        }

        if (PlayerUtil.hasPermission(s, FlyPermission.TEMP_FLY_OTHERS)) {
            // /tempfly (check, add, remove, set, disable)
            if (args.length == 1) {
                return CommandUtil.autoComplete(TempflyTaskType.types, args[0]);
                // /tempfly (check, add, remove, set, disable) (player)
            } else if (args.length == 2) {
                // Default auto-complete for player
                return null;
                // /tempfly (check, add, remove, set, disable) (player) [duration]
            } else if (args.length == 3) {
                return CommandUtil.autoComplete(exampleDurations, args[2]);
            }
        }

        return Collections.emptyList();
    }

    private void runTempflyTask(CommandSender sender, Player targetPlayer, TempflyTaskType type) {
        runTempflyTask(sender, targetPlayer, type, 0, false);
    }

    private void runTempflyTask(CommandSender sender, Player targetPlayer, TempflyTaskType type, long duration, boolean silent) {
        FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(targetPlayer);
        boolean hasTimeLeft = type == TempflyTaskType.DISABLE && flightPlayer.getTempflyTimer().hasTimeLeft();

        if (type != TempflyTaskType.CHECK) {
            flightPlayer.modifyTempflyDuration(type, duration);
        }

        if (!silent) {
            String msg;

            switch (type) {
                case CHECK:
                    msg = pl.getLangManager().getTempFlyCheck();
                    break;
                case SET: case ADD: case REMOVE: default:
                    msg = pl.getLangManager().getTempFlySet();
                    break;
                case DISABLE:
                    msg = hasTimeLeft
                            ? pl.getLangManager().getTempFlyDisable() : pl.getLangManager().getTempFlyDisabled();
                    break;
            }

            msgVar(sender, msg, false, new HashMap<String, String>() {{
                put("player", targetPlayer.getName());
                put("duration", PlayerUtil.longTempflyPlaceholder(flightPlayer));
            }});
        }
    }

    private long calculateDuration(String durationStr) {
        char unit = findUnit(durationStr);
        int unitIndex = durationStr.indexOf(unit);
        // Just in case it's a really
        long duration = Long.parseLong(durationStr.substring(0, unitIndex == -1 ? durationStr.length() : unitIndex));
        // In milliseconds
        duration *= 1000;

        switch (unit) {
            case 'm':
                duration *= 60;
                break;
            case 'h':
                duration *= 3600;
                break;
            case 'd':
                duration *= 86400;
                break;
            default:
                break;
        }

        return duration;
    }

    private char findUnit(String input) {
        for (int i = 0; i < input.length(); i++) {
            if (input.substring(i, i + 1).matches("[smhd]")) {
                return input.charAt(i);
            }
        }
        return 's';
    }

    private TempflyTaskType getTaskType(String name) {
        TempflyTaskType type;

        try {
            type = TempflyTaskType.valueOf(name);
        } catch (IllegalArgumentException e) {
            type = TempflyTaskType.CHECK;
        }

        return type;
    }
}
