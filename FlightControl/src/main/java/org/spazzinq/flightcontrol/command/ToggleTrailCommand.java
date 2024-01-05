/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class ToggleTrailCommand implements CommandExecutor {
    private final FlightControl pl;

    public ToggleTrailCommand() {
        pl = FlightControl.getInstance();
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof Player) {
            Player p = (Player) s;

            if (pl.getPlayerManager().getFlightPlayer(p).toggleTrail()) {
                // No need to check for trail enable because of command listener
                msg(s, pl.getLangManager().getPersonalTrailEnable(), pl.getLangManager().useActionBar());
            } else {
                pl.getTrailManager().disableTrail(p);
                msg(s, pl.getLangManager().getPersonalTrailDisable(), pl.getLangManager().useActionBar());
            }
        } else {
            pl.getLogger().info("Only players can use this command (the console isn't a player!)");
        }
        return true;
    }
}
