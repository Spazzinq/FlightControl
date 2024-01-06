/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class ActionbarUtil {
    private static boolean isV1_8;
    private static String nmsPackage;

    static {
        nmsPackage = Bukkit.getServer().getClass().getPackage().getName();
        nmsPackage = nmsPackage.substring(nmsPackage.lastIndexOf(".") + 1);

        if (Bukkit.getServer().getBukkitVersion().contains("1.8")) {
            isV1_8 = true;
        }
    }

    public static void sendActionbar(Player p, String msg) {
        if (isV1_8) {
            sendActionbarLegacy(p, msg);
        } else {
            sendActionbarNew(p, msg);
        }
    }

    public static void sendActionbarNew(Player p, String msg) {
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(msg));
    }

    @SuppressWarnings("ConstantConditions")
    public static void sendActionbarLegacy(Player p, String msg) {
        try {
            Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");

            Object chatComponentText = chatComponentTextClass.getConstructor(new Class[]{String.class}).newInstance(msg);
            Object packetPlayOutChat = packetPlayOutChatClass.getConstructor(
                    new Class[]{iChatBaseComponentClass, byte.class})
                    .newInstance(chatComponentText, (byte) 2);

            Object handle = p.getClass().getMethod("getHandle", new Class[0]).invoke(p);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);

            playerConnection.getClass()
                    .getMethod("sendPacket", new Class[]{getNMSClass("Packet")})
                    .invoke(playerConnection, packetPlayOutChat);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?> getNMSClass(String className) {
        try {
            return Class.forName("net.minecraft.server." + nmsPackage + "." + className);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
