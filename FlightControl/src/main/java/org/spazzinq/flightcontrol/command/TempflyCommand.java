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
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.object.TempflyTaskType;
import org.spazzinq.flightcontrol.util.CommandUtil;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.*;

import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class TempflyCommand extends TemplateCommand {
    private final List<String> exampleDurations = Arrays.asList("30minutes", "1hour", "3hours", "6hours", "12hours", "1day");

    public TempflyCommand() {
        subCommands = new TreeMap<String, String>() {{
            put("check [player]", "Returns a player's current tempfly duration");
            put("disable [player]", "Disables a player's tempfly");
            put("set (duration) [player]", "Sets a player's current tempfly duration");
            put("add (duration) [player]", "Adds time to a player's current tempfly duration");
            put("remove (duration) [player]", "Removes time from a player's current tempfly duration");
        }};
    }


    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player targetPlayer = sender instanceof ConsoleCommandSender ? null : (Player) sender;
        TempflyTaskType type;
        long duration = 0;

        if (args.length > 0) {
            type = TempflyTaskType.getTaskType(args[0].toUpperCase());

            if (type != TempflyTaskType.HELP) {
                // Optional player input
                // /tempfly set/add/remove (duration) [player]
                int optionalPlayerIndex = 2;

                if (type == TempflyTaskType.CHECK || type == TempflyTaskType.DISABLE) {
                    // /tempfly check/disable [player]
                    optionalPlayerIndex = 1;
                }

                if (args.length > optionalPlayerIndex) {
                    targetPlayer = Bukkit.getPlayer(args[optionalPlayerIndex]);

                    if (targetPlayer == null) {
                        type = TempflyTaskType.HELP;
                    }
                } else if (sender instanceof ConsoleCommandSender) {
                    type = TempflyTaskType.HELP;
                }

                // Duration
                if (args.length > 1 && (type == TempflyTaskType.SET || type == TempflyTaskType.ADD || type == TempflyTaskType.REMOVE)) {
                    String durationStr = args[2].toLowerCase();

                    if (durationStr.matches("\\d+([smhd]|seconds?|minutes?|hours?|days?)")) {
                        duration = MathUtil.calculateDuration(durationStr);
                    }
                }
            }
        } else if (sender.isOp()) {
            type = TempflyTaskType.HELP;
        } else {
            type = TempflyTaskType.CHECK;
        }

        runTempflyTask(sender, targetPlayer, type, duration, "silenttempfly".equals(label.toLowerCase()));

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

    private void runTempflyTask(CommandSender sender, Player targetPlayer, TempflyTaskType type, long duration, boolean silent) {
        FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(targetPlayer);
        boolean hasTimeLeft = type == TempflyTaskType.DISABLE && flightPlayer != null && flightPlayer.getTempflyTimer().hasTimeLeft();

        if (type != TempflyTaskType.CHECK && type != TempflyTaskType.HELP && flightPlayer != null) {
            flightPlayer.modifyTempflyDuration(type, duration);
        }

        if (!silent) {
            String msg;

            switch (type) {
                case CHECK:
                    msg = pl.getLangManager().getTempFlyCheck();
                    break;
                case SET: case ADD: case REMOVE:
                    msg = pl.getLangManager().getTempFlySet();
                    break;
                case DISABLE:
                    msg = hasTimeLeft
                            ? pl.getLangManager().getTempFlyDisable() : pl.getLangManager().getTempFlyDisabled();
                    break;
                case HELP: default:
                    msg = pl.getLangManager().getTempFlyUsage();
                    break;
            }

            msgVar(sender, msg, false, new HashMap<String, String>() {{
                if (targetPlayer != null) {
                    put("player", targetPlayer.getName());
                }
                if (flightPlayer != null) {
                    put("duration", PlayerUtil.longTempflyPlaceholder(flightPlayer));
                }
            }});
        }
    }
}
