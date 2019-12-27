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
import org.spazzinq.flightcontrol.manager.FlightManager;

import static org.spazzinq.flightcontrol.manager.LangManager.msg;
import static org.spazzinq.flightcontrol.manager.LangManager.replaceVar;

public final class FlyCommand implements CommandExecutor {
    private FlightControl pl;
    private FlightManager flightManager;
    public FlyCommand(FlightControl pl) {
        this.pl = pl;
        flightManager = pl.getFlightManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (s instanceof Player) {
                Player p = (Player) s;

                if (p.getAllowFlight()) {
                    flightManager.disableFlight(p, true);
                } else {
                    flightManager.check(p, p.getLocation(), true);
                }
            } else pl.getLogger().info("Only players can use this command (the console can't fly, can it?)");
        } else if (args.length == 1) {
            if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.admin")) {
                Player p = Bukkit.getPlayer(args[0]);
                // Allow admins to disable flight
                if (p != null) {
                    msg(s, replaceVar(p.getAllowFlight() ? pl.getLangManager().getFlyCommandEnable()
                                                         : pl.getLangManager().getFlyCommandDisable(), p.getName(), "player"));

                    if (p.getAllowFlight()) {
                        flightManager.disableFlight(p, true);
                    } else {
                        flightManager.check(p, p.getLocation(), true);
                    }
                    // TODO this aint working?
                } else msg(s, pl.getLangManager().getFlyCommandUsage());
            } else msg(s, pl.getLangManager().getPermDenied());
        }
        return true;
    }
}
