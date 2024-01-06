/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.object.TempflyTask;
import org.spazzinq.flightcontrol.util.CommandUtil;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.*;

import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class TempflyCommand extends TemplateCommand {
    private final List<String> exampleDurations = Arrays.asList("30minutes", "1hour", "3hours", "6hours", "12hours", "1day");

    public TempflyCommand() {
        subCommands = new HashMap<>() {{
            put("check [player]", "Returns a player's current tempfly duration");
            put("disable [player]", "Disables a player's tempfly");
            put("set (duration) [player]", "Sets a player's current tempfly duration");
            put("add (duration) [player]", "Adds time to a player's current tempfly duration");
            put("remove (duration) [player]", "Removes time from a player's current tempfly duration");
        }};

        buildHelp();
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // TODO Refactor
        Set<Player> targetPlayers = sender instanceof ConsoleCommandSender ? Collections.emptySet() : Collections.singleton((Player) sender);
        TempflyTask type;
        long duration = 0;

        if (PlayerUtil.hasPermission(sender, FlyPermission.TEMP_FLY)) {
            // If not base command
            if (args.length > 0) {
                type = TempflyTask.getTaskType(args[0].toUpperCase());

                if (type != TempflyTask.HELP) {
                    // Optional remote player
                    // /tempfly set/add/remove (duration) [player]
                    int optionalPlayerIndex = 2; // Default index

                    if (type == TempflyTask.CHECK || type == TempflyTask.DISABLE) {
                        // /tempfly check/disable [player]
                        optionalPlayerIndex = 1;
                    }

                    if (PlayerUtil.hasPermission(sender, FlyPermission.TEMP_FLY_OTHERS) && args.length > optionalPlayerIndex) {
                        if (args[optionalPlayerIndex].equalsIgnoreCase("all")) {
                            targetPlayers = new HashSet<>(Bukkit.getOnlinePlayers());
                        } else {
                            Player p = Bukkit.getPlayer(args[optionalPlayerIndex]);

                            if (p != null) {
                                targetPlayers = Collections.singleton(p);
                            }
                        }

                        if (targetPlayers.isEmpty()) {
                            type = TempflyTask.HELP;
                        }
                    } else if (sender instanceof ConsoleCommandSender) {
                        type = TempflyTask.HELP;
                    }

                    // Duration
                    // /tempfly set/add/remove (duration) [player]
                    if (type == TempflyTask.SET || type == TempflyTask.ADD || type == TempflyTask.REMOVE) {
                        if (args.length > 1) {
                            String durationStr = args[1].toLowerCase();

                            if (durationStr.matches("\\d+([smhd]|seconds?|minutes?|hours?|days?)")) {
                                duration = MathUtil.calculateDuration(durationStr) * 1000; // Convert to ms
                            }
                        } else {
                            type = TempflyTask.HELP;
                        }
                    }
                }
            // If has perm but is not command, send command help
            } else {
                type = TempflyTask.HELP;
            }
        // If does not have admin perms
        } else {
            type = TempflyTask.CHECK;

            // Checking other player
            // /tempfly check [player]
            if (args.length > 1 && PlayerUtil.hasPermission(sender, FlyPermission.TEMP_FLY_CHECK)) {
                int playerIndex = 1;
                Player p = Bukkit.getPlayer(args[playerIndex]);

                if (p != null) {
                    targetPlayers = Collections.singleton(p);
                }
            }
        }

        runTempflyTask(sender, targetPlayers, type, duration, "silenttempfly".equalsIgnoreCase(label));

        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
        for (int i = 0; i < args.length; i++) {
            // Ignore player case
            if (i != 1) {
                args[i] = args[i].toLowerCase();
            }
        }

        // /tempfly check/disable [player]
        // /tempfly set/add/remove (duration) [player]
        if (PlayerUtil.hasPermission(s, FlyPermission.TEMP_FLY)) {
            if (args.length > 0) {
                TempflyTask type = TempflyTask.getTaskType(args[0].toUpperCase());

                // /tempfly (check, add, remove, set, disable)
                if (args.length == 1) {
                    return CommandUtil.autoComplete(TempflyTask.types, args[0]);
                // /tempfly (check, add, remove, set, disable) (player)
                } else if (args.length == 2 && (type == TempflyTask.CHECK || type == TempflyTask.DISABLE)) {
                    // Default auto-complete for player
                    return null;
                } else if (type == TempflyTask.SET || type == TempflyTask.ADD || type == TempflyTask.REMOVE) {
                    // /tempfly set/add/remove (duration)
                    if (args.length == 2) {
                        return CommandUtil.autoComplete(exampleDurations, args[1]);
                    // /tempfly set/add/remove (duration) [player]
                    } else if (args.length == 3) {
                        // Default auto-complete for player
                        return null;
                    }
                }
            }
        }

        return Collections.emptyList();
    }

    private void runTempflyTask(CommandSender sender, Set<Player> targetPlayers, TempflyTask type, long duration, boolean silent) {
        for (Player targetPlayer : targetPlayers) {
            FlightPlayer flightPlayer = pl.getPlayerManager().getFlightPlayer(targetPlayer);
            boolean hasTimeLeft = type == TempflyTask.DISABLE && flightPlayer != null && flightPlayer.getTempflyTimer().hasTimeLeft();

            if (TempflyTask.modifiesDuration(type) && flightPlayer != null) {
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
                        msg = defaultHelp;
                        break;
                }

                msgVar(sender, msg, false, new HashMap<>() {{
                    if (targetPlayer != null) {
                        put("player", targetPlayer.getName());
                    }
                    if (flightPlayer != null) {
                        put("duration", PlayerUtil.durationToWords(flightPlayer));
                    }
                }});
            }
        }
    }
}
