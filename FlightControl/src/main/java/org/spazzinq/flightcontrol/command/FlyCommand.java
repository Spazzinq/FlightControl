/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.manager.FlightManager;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;
import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class FlyCommand implements CommandExecutor {
    private final FlightControl pl;
    private final FlightManager flightManager;

    public FlyCommand() {
        pl = FlightControl.getInstance();
        flightManager = pl.getFlightManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (s instanceof Player) {
                if (PlayerUtil.hasPermission(s, FlyPermission.FLY_COMMAND)) {
                    Player p = (Player) s;

                    if (p.getAllowFlight()) {
                        flightManager.disableFlight(p, Cause.DISABLE_COMMAND, true);
                    } else {
                        flightManager.check(p, true);
                    }
                } else {
                    msg(s, pl.getLangManager().getPermDenied());
                }
            } else {
                pl.getLogger().info("Only players can use this command (the console can't fly, can it?)");
            }
        } else if (args.length == 1) {
            if (s instanceof ConsoleCommandSender || PlayerUtil.hasPermission(s, FlyPermission.ADMIN)) {
                Player p = Bukkit.getPlayer(args[0]);
                // Allow admins to disable flight
                if (p != null) {
                    String msg = p.getAllowFlight() ? pl.getLangManager().getFlyCommandDisable()
                            : pl.getLangManager().getFlyCommandEnable();

                    msgVar(s, msg, false, "player", p.getName());

                    if (p.getAllowFlight()) {
                        flightManager.disableFlight(p, Cause.DISABLE_COMMAND, true);
                    } else {
                        flightManager.check(p, true);
                    }
                } else {
                    msg(s, pl.getLangManager().getFlyCommandUsage());
                }
            } else {
                msg(s, pl.getLangManager().getPermDenied());
            }
        }
        return true;
    }
}
