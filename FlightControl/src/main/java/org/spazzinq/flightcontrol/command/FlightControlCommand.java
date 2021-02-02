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

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.always.AdvancedEnchantmentsCheck;
import org.spazzinq.flightcontrol.manager.ConfManager;
import org.spazzinq.flightcontrol.manager.LangManager;
import org.spazzinq.flightcontrol.object.FlyPermission;
import org.spazzinq.flightcontrol.util.CommandUtil;
import org.spazzinq.flightcontrol.util.MathUtil;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.util.*;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;
import static org.spazzinq.flightcontrol.util.MessageUtil.msgVar;

public class FlightControlCommand extends TemplateCommand {
    private final ConfManager config;

    public FlightControlCommand() {
        config = pl.getConfManager();

        subCommands = new TreeMap<String, String>() {{
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

        buildHelp();
        defaultHelp += "\n \n&a/tt &7» &fPersonal trail toggle";
    }

    private String loadHelp(String[] args) {
        if (args.length > 0 && !args[0].isEmpty()) {
            StringBuilder help = new StringBuilder(LangManager.HELP_HEADER.replaceAll("\\.\\.\\.", args[0] + "..."));

            for (Map.Entry<String, String> commandEntry : subCommands.entrySet()) {
                // If command starts with query
                if (commandEntry.getKey().startsWith(args[0])) {
                    // Add to displayed list
                    help.append("&a").append(commandEntry.getKey()).append(" &7» &f").append(commandEntry.getValue()).append("\n");
                }
            }

            return help.toString();
        } else {
            return defaultHelp;
        }
    }

    // TODO Refactor somehow
    @Override public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }

        if (s instanceof ConsoleCommandSender || PlayerUtil.hasPermission(s, FlyPermission.ADMIN)) {
            if (args.length > 0) {
                List<String> autoComplete = CommandUtil.autoComplete(subCommands.keySet(), args[0], false);

                switch (autoComplete.isEmpty()
                        ? args[0] : (autoComplete.size() == 1 ? autoComplete.get(0) : "")) {
                    case "reload":
                        pl.load();
                        msg(s, pl.getLangManager().getPluginReloaded());
                        break;
                    case "update":
                        new BukkitRunnable() {
                            @Override public void run() {
                                pl.getUpdateManager().installUpdate(s, false);
                            }
                        }.runTaskAsynchronously(pl);
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
                        } else {
                            pl.getTrailManager().removeEnabledTrails();
                        }
                        break;
                    case "vanishbypass":
                        config.setVanishBypass(!config.isVanishBypass());
                        config.set("settings.vanish_bypass", config.isVanishBypass());
                        msgToggle(s, config.isVanishBypass(), "Vanish Bypass");
                        break;
                    case "actionbar":
                        LangManager langManager = pl.getLangManager();

                        langManager.setUseActionBar(!langManager.useActionBar());
                        langManager.set("player.actionbar", langManager.useActionBar());
                        langManager.getLang().save();

                        msgToggle(s, langManager.useActionBar(), "Actionbar Notifications");
                        break;
                    case "autoenable":
                        config.setAutoEnable(!config.isAutoEnable());
                        config.set("settings.auto_enable_flight", config.isAutoEnable());
                        msgToggle(s, config.isAutoEnable(), "Flight Auto-Enable");
                        break;
                    case "autoupdate":
                        config.setAutoUpdate(!config.isAutoUpdate());
                        config.set("settings.auto_update", config.isAutoUpdate());
                        msgToggle(s, config.isAutoUpdate(), "Auto-Update");
                        break;
                    case "speed":
                    case "flightspeed":
                        if (args.length == 2) {
                            if (args[1].matches("\\.\\d+|\\d+(\\.\\d+)?")) {
                                float speed = Float.parseFloat(args[1]);
                                float actualSpeed = MathUtil.calcConvertedSpeed(speed);
                                if (speed > -1 && speed < 11) {
                                    if (config.getDefaultFlightSpeed() != actualSpeed) {
                                        config.set("settings.flight_speed", speed);
                                        config.setDefaultFlightSpeed(actualSpeed);

                                        pl.getPlayerManager().loadPlayerData();
                                        msgVar(s, pl.getLangManager().getGlobalFlightSpeedSet(), false, "speed", String.valueOf(speed));
                                    } else {
                                        msgVar(s, pl.getLangManager().getGlobalFlightSpeedSame(), false, "speed", String.valueOf(speed));
                                    }
                                } else {
                                    msg(s, pl.getLangManager().getGlobalFlightSpeedUsage());
                                }
                            } else {
                                msg(s, pl.getLangManager().getGlobalFlightSpeedUsage());
                            }
                        } else if (args.length == 1) {
                            msg(s, pl.getLangManager().getGlobalFlightSpeedUsage());
                        } else {
                            msg(s, pl.getLangManager().getGlobalFlightSpeedUsage());
                        }
                        break;
                    case "enemyrange":
                        if (args.length == 2) {
                            if (args[1].matches("\\d+|-1")) {
                                int range = Integer.parseInt(args[1]);
                                int rangeSquared = range * range;
                                if (range > -2) {
                                    if (config.getNearbyRangeSquared() != rangeSquared) {
                                        config.set("factions.disable_enemy_range", range);
                                        config.setNearbyCheck(range != -1);
                                        config.setNearbyRangeSquared(rangeSquared);

                                        msgVar(s, pl.getLangManager().getEnemyRangeSet(), false, "range", String.valueOf(range));
                                    } else {
                                        msgVar(s, pl.getLangManager().getEnemyRangeSame(), false, "range", String.valueOf(range));
                                    }
                                } else {
                                    msg(s, pl.getLangManager().getEnemyRangeUsage());
                                }
                            } else {
                                msg(s, pl.getLangManager().getEnemyRangeUsage());
                            }
                        } else if (args.length == 1) {
                            msg(s, pl.getLangManager().getEnemyRangeUsage());
                        } else {
                            msg(s, pl.getLangManager().getEnemyRangeUsage());
                        }
                        break;
                    case "support":
                        config.setInGameSupport(!config.isInGameSupport());
                        msgToggle(s, config.isInGameSupport(), "Live Support");
                        Player dev = pl.getServer().getPlayer(FlightControl.spazzinqUUID);

                        if (config.isInGameSupport()) {
                            msg(s, "&e&lFlightControl &7» &fLive support enables Spazzinq to check debug information " +
                                    "on why flight is disabled. " + "You can disable support at any time by repeating" +
                                    " the command, but by default the access only lasts until you restart " +
                                    "FlightControl/the server.");
                            if (dev != null && dev.isOnline()) {
                                msg(dev, "&c&lFlightControl &7» &f" + s.getName() + "&c has requested support.");
                            }
                        }
                        break;
                    case "debug":
                        if (args.length == 2) {
                            Player argsPlayer = Bukkit.getPlayer(args[1]);

                            if (argsPlayer != null) {
                                pl.getStatusManager().debug(s, argsPlayer);
                            }
                        } else if (s instanceof Player) {
                            pl.getStatusManager().debug(s, (Player) s);
                        } else {
                            pl.getLogger().info("Only players can use this command (it's information based on the " +
                                    "player's location)");
                        }
                        break;
                    case "advancedenchantments":
                        msg(s, "Enchants: " + new AdvancedEnchantmentsCheck(pl).getBootEnchants((Player) s).keySet()
                                + "\nConfName: " + pl.getConfManager().getAeEnchantName()
                                + "\nEquivalent: " + new AdvancedEnchantmentsCheck(pl).check((Player) s));
                        break;
                    default:
                        msg(s, loadHelp(args));
                        break;
                }
            } else {
                msg(s, loadHelp(args));
            }
        } else if (args.length == 1 && args[0].equals("debug") && s instanceof Player && ((Player) s).getUniqueId().equals(UUID.fromString("043f10b6-3d13-4340-a9eb-49cbc560f48c"))) {
            if (config.isInGameSupport()) {
                pl.getStatusManager().debug(s, (Player) s);
            } else {
                msg(s, "&c&lFlightControl &7» &cSorry bud, you don't have permission to view debug information :I");
            }
        } else {
            msg(s, pl.getLangManager().getPermDenied());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String label, String[] args) {
        for (int i = 0; i < args.length; i++) {
            args[i] = args[i].toLowerCase();
        }

        if (args.length == 1) {
            return CommandUtil.autoComplete(subCommands.keySet(), args[0], true);
        } else if (args.length == 2) {
            if (args[0].equals("speed")) {
                return Collections.singletonList("1");
            } else if (args[0].equals("enemyrange")) {
                return Arrays.asList("10", "-1");
            }
        }
        return Collections.emptyList();
    }

    private void msgToggle(CommandSender s, boolean toggle, String subPrefix) {
        msg(s, pl.getLangManager().getPrefix() + subPrefix + " &7» "
                + (toggle ? "&aEnabled" : "&cDisabled"));
    }
}
