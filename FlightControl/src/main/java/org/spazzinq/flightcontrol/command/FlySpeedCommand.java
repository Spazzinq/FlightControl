/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.manager.PlayerManager;
import org.spazzinq.flightcontrol.object.FlightPlayer;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.HashMap;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;
import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class FlySpeedCommand implements CommandExecutor {
    private final FlightControl pl;
    private final PlayerManager playerManager;

    public FlySpeedCommand() {
        pl = FlightControl.getInstance();
        playerManager = pl.getPlayerManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        boolean console = s instanceof ConsoleCommandSender;

        if (args.length == 1) {
            if (PlayerUtil.hasPermission(s, FlyPermission.FLY_SPEED) || console) {
                if (console) {
                    pl.getLogger().warning("You aren't a player! Try /flyspeed (speed) (player) instead!");
                } else {
                    setSpeed(s, (Player) s, args[0]);
                }
            } else {
                msg(s, pl.getLangManager().getPermDenied());
            }
        } else if (args.length == 2) {
            if (PlayerUtil.hasPermission(s, FlyPermission.FLY_SPEED_OTHERS) || console) {
                Player argPlayer = Bukkit.getPlayer(args[1]);

                if (argPlayer == null) {
                    msg(s, pl.getLangManager().getFlySpeedUsage());
                } else {
                    setSpeed(s, argPlayer, args[0]);
                }
            } else {
                msg(s, pl.getLangManager().getPermDenied());
            }
        } else {
            if (PlayerUtil.hasPermission(s, FlyPermission.FLY_SPEED) || PlayerUtil.hasPermission(s,
                    FlyPermission.FLY_SPEED_OTHERS) || console) {
                msg(s, pl.getLangManager().getFlySpeedUsage());
            } else {
                msg(s, pl.getLangManager().getPermDenied());
            }
        }
        return true;
    }

    private void setSpeed(CommandSender s, Player p, String wrongSpeedStr) {
        if (wrongSpeedStr.matches("\\d+|(\\d+)?.\\d+")) {
            float wrongSpeed = Math.min(Float.parseFloat(wrongSpeedStr), pl.getConfManager().getMaxFlightSpeed());
            float speed = MathUtil.calcConvertedSpeed(wrongSpeed, pl.getConfManager().getMaxFlightSpeed());
            FlightPlayer flightPlayer = playerManager.getFlightPlayer(p);

            if (flightPlayer.getActualFlightSpeed() == speed) {
                msgVar(s, pl.getLangManager().getFlySpeedSame(), false, new HashMap<>() {{
                    put("speed", String.valueOf(wrongSpeed));
                    put("player", p.getName());
                }});
            } else {
                playerManager.getFlightPlayer(p).setActualFlightSpeed(speed);

                msgVar(s, pl.getLangManager().getFlySpeedSet(), false, new HashMap<>() {{
                    put("speed", String.valueOf(wrongSpeed));
                    put("player", p.getName());
                }});
            }
        } else {
            msg(s, pl.getLangManager().getFlySpeedUsage());
        }
    }
}
