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
import org.spazzinq.flightcontrol.manager.PlayerManager;
import org.spazzinq.flightcontrol.util.MathUtil;

import static org.spazzinq.flightcontrol.FlightControl.msg;

public class FlySpeedCommand implements CommandExecutor {
    private FlightControl pl;
    private PlayerManager playerManager;

    public FlySpeedCommand(FlightControl pl) {
        this.pl = pl;
        playerManager = pl.getPlayerManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        boolean console = s instanceof ConsoleCommandSender;

        if (args.length == 1) {
            if (s.hasPermission("flightcontrol.flyspeed") || console) {
                if (console) {
                    pl.getLogger().warning("You aren't a player! Try /flyspeed (speed) (player) instead!");
                } else {
                    setSpeed(s, (Player) s, args[0]);
                }
            } else {
                msg(s, pl.getConfigManager().getNoPermission());
            }
        } else if (args.length == 2) {
            if (s.hasPermission("flightcontrol.flyspeed.others") || console) {
                Player argPlayer = Bukkit.getPlayer(args[1]);

                if (argPlayer == null) {
                    msg(s, "&c&lFlightControl &7» &cInvalid player! Use /flyspeed (speed) (player)!");
                } else {
                    setSpeed(s, argPlayer, args[0]);
                }
            } else {
                msg(s, pl.getConfigManager().getNoPermission());
            }
        } else {
            if (s.hasPermission("flightcontrol.flyspeed") || s.hasPermission("flightcontrol.flyspeed.others") || console) {
                msg(s, "&c&lFlightControl &7» &cUse /flyspeed (speed) [player]!");
            } else {
                msg(s, pl.getConfigManager().getNoPermission());
            }
        }
        return true;
    }

    private void setSpeed(CommandSender s, Player p, String wrongSpeedStr) {
        if (wrongSpeedStr.matches("\\d+|(\\d+)?.\\d+")) {
            float wrongSpeed = Float.parseFloat(wrongSpeedStr),
                  speed = MathUtil.calcActualSpeed(wrongSpeed);

            playerManager.getFlightPlayer(p).setActualFlightSpeed(speed);
            msg(p, "&a&lFlightControl &7» &aSet your flight speed to &f" + wrongSpeed + "&a!");
        } else {
            msg(s, "&c&lFlightControl &7» &cInvalid number! Acceptable formats: 1, 1.1, .9");
        }
    }
}
