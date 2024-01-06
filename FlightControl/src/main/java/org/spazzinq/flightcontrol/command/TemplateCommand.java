/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.command;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.manager.LangManager;

import java.util.Map;

public abstract class TemplateCommand implements CommandExecutor, TabCompleter {
    protected final FlightControl pl;

    protected Map<String, String> subCommands;
    protected String defaultHelp;

    public TemplateCommand() {
        pl = FlightControl.getInstance();
    }

    protected void buildHelp() {
        StringBuilder buildDefaultHelp = new StringBuilder(LangManager.HELP_HEADER);

        for (Map.Entry<String, String> c : subCommands.entrySet()) {
            buildDefaultHelp.append("&a").append(c.getKey()).append(" &7» &f").append(c.getValue()).append("\n");
        }

        defaultHelp = buildDefaultHelp.toString();
    }
}
