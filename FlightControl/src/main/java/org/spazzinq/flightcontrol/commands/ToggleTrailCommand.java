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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.TrailManager;

import java.util.UUID;

public class ToggleTrailCommand implements CommandExecutor {
    private FlightControl pl;
    private TrailManager manager;

    public ToggleTrailCommand(FlightControl pl) {
        this.pl = pl;
        manager = pl.getTrailManager();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof Player) {
            Player p = (Player) s;
            UUID uuid = p.getUniqueId();
            if (manager.getTrailPrefs().contains(uuid)) {
                manager.getTrailPrefs().remove(uuid);
                // No need to check for trail enable because of command listener
                FlightControl.msg(s, pl.getConfigManager().getEnableTrail(), pl.getConfigManager().isByActionBar());
            }
            else {
                manager.getTrailPrefs().add(uuid);
                manager.trailRemove(p);
                FlightControl.msg(s, pl.getConfigManager().getDisableTrail(), pl.getConfigManager().isByActionBar());
            }
        } else pl.getLogger().info("Only players can use this command (the console isn't a player!)");
        return true;
    }
}
