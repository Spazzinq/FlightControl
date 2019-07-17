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

package org.spazzinq.flightcontrol;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.spazzinq.flightcontrol.FlightControl.msg;

final class CMD implements CommandExecutor, TabCompleter {
    private static FlightControl pl;
    private String help;
    CMD(FlightControl pl) {
        CMD.pl = pl;
        help = " \n&a&lFlightControl &f" + pl.getDescription().getVersion() + "\n" +
                "&aBy &fSpazzinq\n " +
                "\n&a/fc &7» &fHelp\n" +
                "&a/fc update &7» &fUpdate flightcontrol\n" +
                "&a/fc actionbar &7» &fSend notifications through action bar\n" +
                "&a/fc combat &7» &fToggle automatic combat disabling\n" +
                "&a/fc falldamage &7» &fToggle fall damage prevention\n" +
                "&a/fc trails &7» &fToggle trails for the server\n" +
                "&a/fc vanishbypass &7» &fToggle vanish bypass\n" +
                "&a/fc command &7» &fUse /fly instead of automatic flight\n" +
                "\n&a/tt &7» &fPersonal trail toggle";
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    pl.config.reloadConfig(); msg(s, "&a&lFlightControl &7» &aConfiguration successfully reloaded!"); }
                else if (args[0].equalsIgnoreCase("update")) Update.install(s);
                else if (args[0].equalsIgnoreCase("combat")) {
                    pl.config.set("settings.disable_flight_in_combat", pl.config.useCombat = !pl.config.useCombat);
                    toggleOption(s, pl.config.useCombat, "Combat Disabling");
                }
                else if (args[0].equalsIgnoreCase("falldamage")) {
                    pl.config.set("settings.prevent_fall_damage", pl.config.cancelFall = !pl.config.cancelFall);
                    toggleOption(s, pl.config.cancelFall, "Prevent Fall Damage");
                }
                else if (args[0].equalsIgnoreCase("trails")) {
                    pl.config.set("trail.enabled", pl.config.trail = !pl.config.trail);
                    toggleOption(s, pl.config.trail, "Trails");
                    if (pl.config.trail) {
                        for (Player p : Bukkit.getOnlinePlayers()) if (p.isFlying()) pl.trail.trailCheck(p);
                    } else pl.trail.disableEnabledTrails();
                }
                else if (args[0].equalsIgnoreCase("vanishbypass")) {
                    pl.config.set("settings.vanish_bypass", pl.config.vanishBypass = !pl.config.vanishBypass);
                    toggleOption(s, pl.config.vanishBypass, "Vanish Bypass");
                }
                else if (args[0].equalsIgnoreCase("actionbar")) {
                    pl.config.set("messages.actionbar", pl.config.actionBar = !pl.config.actionBar);
                    toggleOption(s, pl.config.actionBar, "Actionbar Notifications");
                }
                else if (args[0].equalsIgnoreCase("command")) { toggleCommand(s); }
                else if (args[0].equalsIgnoreCase("support")) {
                    toggleOption(s, pl.config.support = !pl.config.support, "Live Support");
                    Player spazzinq = pl.getServer().getPlayer(UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c"));
                    if (pl.config.support) {
                        msg(s, "&e&lFlightControl &eWarning &7» &fLive support enables Spazzinq to check debug information on why flight is disabled. " +
                                "You can disable support at any time by repeating the command, but by default the access only lasts until you restart flightcontrol/the server.");
                        if (spazzinq != null && spazzinq.isOnline()) msg(spazzinq, "&c&lFlightControl &7» &c" + s.getName() + " has requested support.");
                    }
                }
                else if (args[0].equalsIgnoreCase("debug")) {
                    if (s instanceof Player) pl.debug((Player) s);
                    else pl.getLogger().info("Only players can use this command (it's information based on the player's location)");
                }
                else msg(s, help);
            } else msg(s, help);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("debug") && s instanceof Player && ((Player) s).getUniqueId().equals(UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c"))) {
            if (pl.config.support) pl.debug((Player) s);
            else msg(s, "&c&lFlightControl &7» &cSorry bud, you don't have permission to view debug information :I");
        } else msg(s, pl.config.noPerm);

        return true;
    }
    @Override public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
        return Arrays.asList("update", "actionbar", "combat", "falldamage", "trails", "vanishbypass", "command");
    }

    private static void toggleOption(CommandSender s, Boolean o, String prefix) {
        msg(s, (prefix.equals("Trail") ? "&a&l" : "&a&lFlightControl &a") + prefix + " &7» "
                + (o ? "&aEnabled" : "&cDisabled"));
    }

    static void toggleCommand(CommandSender s) {
        pl.config.set("settings.command", pl.config.command = !pl.config.command);
        toggleOption(s, pl.config.command, "Command");
        pl.flyCommand();
    }
}
