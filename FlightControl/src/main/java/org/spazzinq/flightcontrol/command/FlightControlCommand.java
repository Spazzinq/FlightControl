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
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.manager.ConfigManager;
import org.spazzinq.flightcontrol.util.MathUtil;

import java.util.*;

import static org.spazzinq.flightcontrol.FlightControl.msg;

public final class FlightControlCommand implements CommandExecutor, TabCompleter {
    private Map<String, String> commands = new TreeMap<String, String>() {{
        put("actionbar", "Send notifications through the actionbar");
        put("autoenable", "Toggle automatic flight enabling");
        put("autoupdate", "Toggle automatic updates");
        put("combat", "Toggle combat disabling");
        put("enemyrange", "Change the factions enemy disable range");
        put("falldamage", "Toggle fall damage prevention");
        put("speed", "Change the global flight speed");
        put("reload", "Reload FlightControl's configuration");
        put("trails", "Toggle trails for the server");
        put("update", "Update FlightControl");
        put("vanishbypass", "Toggle vanish bypass");
    }};

    private FlightControl pl;
    private ConfigManager config;
    private String defaultHelp;
    private String buildHelp;

    public FlightControlCommand(FlightControl pl) {
        this.pl = pl;
        config = pl.getConfigManager();

        buildHelp = "&a&lFlightControl &f" + pl.getDescription().getVersion()
                + "\n&aBy &fSpazzinq\n \n"
                + "&a&lQUERY&a /fc &7» &f...\n \n";

        StringBuilder buildDefaultHelp = new StringBuilder(buildHelp);

        for (Map.Entry<String, String> c : commands.entrySet()) {
            buildDefaultHelp.append("&a").append(c.getKey()).append(" &7» &f").append(c.getValue()).append("\n");
        }
        buildDefaultHelp.append(" \n&a/tt &7» &fPersonal trail toggle");
        buildDefaultHelp.append("\n&a/tempfly (player) [duration] &7» &fActivate temporary flight");
        defaultHelp = " \n" + buildDefaultHelp.toString();
    }

    private String loadHelp(String[] args) {
        if (args.length > 0 && !args[0].isEmpty()) {
            StringBuilder help = new StringBuilder(buildHelp.replaceAll("\\.\\.\\.", args[0] + "..."));

            for (Map.Entry<String, String> c : commands.entrySet()) {
                if (c.getKey().startsWith(args[0])) {
                    help.append("&a").append(c.getKey()).append(" &7» &f").append(c.getValue()).append("\n");
                }
            }

            if (help.length() == buildHelp.length() + args[0].length()) return defaultHelp;

            return help.toString();
        } else return defaultHelp;
    }

    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }

        if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.admin")) {
            if (args.length > 0) {
                List<String> autoComplete = autoComplete(args[0]);

                switch (autoComplete.isEmpty() ? args[0] : (autoComplete.size() == 1 ? autoComplete.get(0) : "")) {
                    case "reload":
                        pl.reloadManagers();
                        msg(s, "&a&lFlightControl &7» &aConfiguration successfully reloaded!");
                        break;
                    case "update":
                        pl.getUpdater().install(s, false);
                        break;
                    case "combat":
                        config.setCombatChecked(!config.isCombatChecked());
                        config.set("settings.disable_flight_in_combat", config.isCombatChecked());
                        msgToggle(s, config.isCombatChecked(), "Combat Disabling");
                        break;
                    case "falldamage":
                        config.setCancelFall(!config.isCancelFall());
                        config.set("settings.prevent_fall_damage", config.isCancelFall());
                        msgToggle(s, config.isCancelFall(), "Prevent Fall Damage");
                        break;
                    case "trails":
                        config.setTrail(!config.isTrail());
                        config.set("trail.enabled", config.isTrail());
                        msgToggle(s, config.isTrail(), "Trails");
                        if (config.isTrail()) {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                if (p.isFlying()) {
                                    pl.getTrailManager().trailCheck(p);
                                }
                            }
                        }
                        else pl.getTrailManager().disableEnabledTrails();
                        break;
                    case "vanishbypass":
                        config.setVanishBypass(!config.isVanishBypass());
                        config.set("settings.vanish_bypass", config.isVanishBypass());
                        msgToggle(s, config.isVanishBypass(), "Vanish Bypass");
                        break;
                    case "actionbar":
                        config.setByActionBar(!config.isByActionBar());
                        config.set("messages.actionbar", config.isByActionBar());
                        msgToggle(s, config.isByActionBar(), "Actionbar Notifications");
                        break;
                    case "autoenable":
                        config.setAutoEnable(!config.isAutoEnable());
                        config.set("settings.auto_enable", config.isAutoEnable());
                        msgToggle(s, config.isAutoEnable(), "Flight Auto-Enable");
                        break;
                    case "autoupdate":
                        config.setAutoUpdate(!config.isAutoUpdate());
                        config.set("auto_update", config.isAutoUpdate());
                        msgToggle(s, config.isAutoUpdate(), "Auto-Update");
                        break;
                    case "speed": case "flightspeed":
                        if (args.length == 2) {
                            if (args[1].matches("\\.\\d+|\\d+(\\.\\d+)?")) {
                                float speed = Float.parseFloat(args[1]);
                                float actualSpeed = MathUtil.calcActualSpeed(speed);
                                if (speed > -1 && speed < 11) {
                                    if (config.getDefaultFlightSpeed() != actualSpeed) {
                                        config.set("settings.flight_speed", speed);
                                        config.setDefaultFlightSpeed(actualSpeed);

                                        pl.getPlayerManager().reloadPlayerData();
                                        msg(s, "&e&lFlightControl &7» &eSet the global flight speed to &f" + speed + "&e!");
                                    } else msg(s, "&e&lFlightControl &7» &eGreat news! The global flight speed is already &f" + speed + "&e!");
                                } else msg(s, "&e&lFlightControl &7» &ePlease provide a number between &f0 and 10 &e(inclusive)! The default speed is &f1&e!");
                            } else msg(s, "&e&lFlightControl &7» &ePlease provide a &fvalid decimal&e! Example: /fc speed 1");
                        } else if (args.length == 1) msg(s, "&e&lFlightControl &7» &ePlease provide a speed! Example: /fc speed 1");
                        else msg(s, "&e&lFlightControl &7» &eIncorrect usage! Usage: /fc speed (0-10)");
                        break;
                    case "enemyrange":
                        if (args.length == 2) {
                            if (args[1].matches("\\d+|-1")) {
                                int range = Integer.parseInt(args[1]);
                                if (range > -2) {
                                    if (config.getFacEnemyRange() != range) {
                                        config.set("settings.disable_enemy_range", range);
                                        config.setUseFacEnemyRange(range != -1);
                                        config.setFacEnemyRange(range);

                                        msg(s, "&e&lFlightControl &7» &eSet the disable enemy range to &f" + range + "&e!");
                                    } else msg(s, "&e&lFlightControl &7» &eGreat news! The disable enemy range is already &f" + range + "&e!");
                                } else msg(s, "&e&lFlightControl &7» &ePlease provide a number greater than &f0 (inclusive)! Enter &f-1 &eto disable the feature!");
                            } else msg(s, "&e&lFlightControl &7» &ePlease provide a &fvalid integer&e! Example: /fc enemyrange 10");
                        } else if (args.length == 1) msg(s, "&e&lFlightControl &7» &ePlease provide a range! Example: /fc enemyrange 10");
                        else msg(s, "&e&lFlightControl &7» &eIncorrect usage! Usage: /fc enemyrange (range)");
                        break;
                    case "support":
                        config.setSupport(!config.isSupport());
                        msgToggle(s, config.isSupport(), "Live Support");
                        Player spazzinq = pl.getServer().getPlayer(UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c"));
                        if (config.isSupport()) {
                            msg(s, "&e&lFlightControl &eWarning &7» &fLive support enables Spazzinq to check debug information on why flight is disabled. " + "You can disable support at any time by repeating the command, but by default the access only lasts until you restart flightcontrol/the server.");
                            if (spazzinq != null && spazzinq.isOnline()) {
                                msg(spazzinq, "&c&lFlightControl &7» &c" + s.getName() + " has requested support.");
                            }
                        }
                        break;
                    case "debug":
                        if (s instanceof Player) pl.debug((Player) s);
                        else pl.getLogger().info("Only players can use this command (it's information based on the player's location)");
                        break;
                    default:
                        msg(s, loadHelp(args));
                        break;
                }
            } else msg(s, loadHelp(args));
        } else if (args.length == 1 && args[0].equals("debug") && s instanceof Player && ((Player) s).getUniqueId().equals(UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c"))) {
            if (config.isSupport()) pl.debug((Player) s);
            else msg(s, "&c&lFlightControl &7» &cSorry bud, you don't have permission to view debug information :I");
        } else msg(s, config.getNoPermission());

        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }

        if (args.length == 1) {
            List<String> autoComplete = autoComplete(args[0]);
            return autoComplete.isEmpty() ? new ArrayList<>(commands.keySet()) : autoComplete;
        }
        else if (args.length == 2) {
            if (args[0].equals("speed")) {
                return Collections.singletonList("1");
            } else if (args[0].equals("enemyrange")) {
                return Arrays.asList("10", "-1");
            }
        }
        return Collections.emptyList();
    }

    private  void msgToggle(CommandSender s, boolean toggle, String subPrefix) {
        msg(s, ("Trail".equals(subPrefix) ? "&a&l" : "&a&lFlightControl &a") + subPrefix + " &7» "
                + (toggle ? "&aEnabled" : "&cDisabled"));
    }

    private List<String> autoComplete(String query) {
        List<String> matches = new ArrayList<>();

        for (String cmd : commands.keySet()) {
            if (cmd.startsWith(query)) {
                matches.add(cmd);
            }
        }
        return matches;
    }
}
