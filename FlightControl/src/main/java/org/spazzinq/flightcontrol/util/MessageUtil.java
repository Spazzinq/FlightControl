package org.spazzinq.flightcontrol.util;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {
    public static void msg(CommandSender s, String msg) {
        msg(s, msg, false);
    }

    public static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            boolean console = s instanceof ConsoleCommandSender;
            String finalMsg = msg;

            finalMsg = ChatColor.translateAlternateColorCodes('&', finalMsg);

            if (actionBar && s instanceof Player) {
                ActionBarUtil.sendBar((Player) s, finalMsg);
            } else {
                s.sendMessage((console ? "[FlightControl] " : "")
                        + finalMsg);
            }
        }
    }

    public static String replaceVar(String msg, String value, String varName) {
        return msg.replaceAll("%" + varName + "%", value);
    }
}
