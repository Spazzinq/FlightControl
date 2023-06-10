/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public final class MessageUtil {
    public static void msg(CommandSender s, String msg) {
        msg(s, msg, false);
    }

    public static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            String finalMsg = ChatColor.translateAlternateColorCodes('&', msg);

            if (actionBar && s instanceof Player) {
                ActionbarUtil.sendActionbar((Player) s, finalMsg);
            } else {
                s.sendMessage((s instanceof ConsoleCommandSender ? "[FlightControl] " : "")
                        + finalMsg);
            }
        }
    }

    public static void msgVar(CommandSender s, String msg, boolean actionBar, HashMap<String, String> toReplace) {
        String finalMsg = msg;

        for (Map.Entry<String, String> entry : toReplace.entrySet()) {
            finalMsg = finalMsg.replaceAll("%" + entry.getKey() + "%", entry.getValue());
        }

        msg(s, finalMsg, actionBar);
    }

    public static void msgVar(CommandSender s, String msg, boolean actionBar, String var, String value) {
        msg(s, msg.replace("%" + var + "%", value), actionBar);
    }
}
