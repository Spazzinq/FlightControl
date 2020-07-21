/*
 * Original code by ConnorLinfoot from
 * https://github.com/ConnorLinfoot/ActionBarAPI/blob/master/src/main/java/com/connorlinfoot/actionbarapi
 * /ActionBarAPI.java
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
            boolean console = s instanceof ConsoleCommandSender;
            String finalMsg = msg;

            finalMsg = ChatColor.translateAlternateColorCodes('&', finalMsg);

            if (actionBar && s instanceof Player) {
                ActionbarUtil.sendActionBar((Player) s, finalMsg);
            } else {
                s.sendMessage((console ? "[FlightControl] " : "")
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
