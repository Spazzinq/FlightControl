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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.manager.LangManager;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TemplateCommand implements CommandExecutor, TabCompleter {
    protected FlightControl pl;

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

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
