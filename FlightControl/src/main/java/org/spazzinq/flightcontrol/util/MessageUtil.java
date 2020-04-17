/*
 * Original code by ConnorLinfoot from
 * https://github.com/ConnorLinfoot/ActionBarAPI/blob/master/src/main/java/com/connorlinfoot/actionbarapi
 * /ActionBarAPI.java
 */

package org.spazzinq.flightcontrol.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public final class MessageUtil {
    private static String nms;
    private static boolean oldMethod;

    static {
        nms = Bukkit.getServer().getClass().getPackage().getName();
        nms = nms.substring(nms.lastIndexOf(".") + 1);

        // 1_7 may work with protocol hack
        if (nms.equalsIgnoreCase("v1_8_R1") || nms.startsWith("v1_7_")) {
            oldMethod = true;
        }
    }

    public static void msg(CommandSender s, String msg) {
        msg(s, msg, false);
    }

    public static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            boolean console = s instanceof ConsoleCommandSender;
            String finalMsg = msg;

            finalMsg = ChatColor.translateAlternateColorCodes('&', finalMsg);

            if (actionBar && s instanceof Player) {
                sendActionBar((Player) s, finalMsg);
            } else {
                s.sendMessage((console ? "[FlightControl] " : "")
                        + finalMsg);
            }
        }
    }

    public static String replaceVar(String msg, String value, String varName) {
        return msg.replaceAll("%" + varName + "%", value);
    }

    public static void sendActionBar(Player p, String msg) {
        if (p.isOnline()) {
            try {
                Object packet;
                Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutChat");

                Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nms + ".entity.CraftPlayer");
                Object craftPlayerHandle =
                        craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(p));
                Object playerConnection =
                        craftPlayerHandle.getClass().getDeclaredField("playerConnection").get(craftPlayerHandle);

                if (oldMethod) {
                    Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + nms + ".ChatSerializer");
                    Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nms +
                            ".IChatBaseComponent");
                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass,
                            byte.class}).newInstance(
                            iChatBaseComponentClass.cast(chatSerializerClass.getDeclaredMethod("a", String.class).invoke(
                                    chatSerializerClass, "{\"text\": \"" + msg + "\"}")), (byte) 2);
                } else {
                    Object chatComponentText =
                            Class.forName("net.minecraft.server." + nms + ".ChatComponentText").getConstructor(new Class<?>[]{String.class}).newInstance(msg);
                    Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nms +
                            ".IChatBaseComponent");
                    try {
                        Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nms +
                                ".ChatMessageType");
                        Object chatMessageType = null;
                        for (Object obj : chatMessageTypeClass.getEnumConstants()) {
                            if (obj.toString().equals("GAME_INFO")) {
                                chatMessageType = obj;
                            }
                        }
                        packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass,
                                chatMessageTypeClass}).newInstance(chatComponentText, chatMessageType);
                    } catch (ClassNotFoundException e) {
                        packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass,
                                byte.class}).newInstance(chatComponentText, (byte) 2);
                    }
                }

                playerConnection.getClass().getDeclaredMethod("sendPacket",
                        Class.forName("net.minecraft.server." + nms + ".Packet")).invoke(playerConnection, packet);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
