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

package org.spazzinq.flightcontrol.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

public class ActionbarUtil {
    private static String nmsPackage;

    static {
        nmsPackage = Bukkit.getServer().getClass().getPackage().getName();
        nmsPackage = nmsPackage.substring(nmsPackage.lastIndexOf(".") + 1);
    }

    public static void sendActionBar(Player p, String msg) {
        try {
            Class<?> packetPlayOutChatClass = getNMSClass("PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = getNMSClass("IChatBaseComponent");
            Class<?> chatComponentTextClass = getNMSClass("ChatComponentText");

            Object packetPlayOutChat;
            Object chatComponentText = chatComponentTextClass.getConstructor(new Class[]{String.class}).newInstance(msg);

            Class<?> chatMessageTypeClass = getNMSClass("ChatMessageType");

            if (chatMessageTypeClass == null) {
                packetPlayOutChat = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass,
                        byte.class}).newInstance(chatComponentText, (byte) 2);
            } else {
                Object chatMessageType = chatMessageTypeClass.getField("GAME_INFO").get(null);

                try {
                    packetPlayOutChat = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass,
                            chatMessageTypeClass}).newInstance(chatComponentText, chatMessageType);
                } catch (NoSuchMethodException e) {
                    packetPlayOutChat = packetPlayOutChatClass.getConstructor(
                            new Class[] {iChatBaseComponentClass, chatMessageTypeClass, UUID.class}).newInstance(chatComponentText, chatMessageType, p.getUniqueId());
                }
            }

            Object getHandle = p.getClass().getMethod("getHandle", new Class[0]).invoke(p);
            Object playerConnection = getHandle.getClass().getField("playerConnection").get(getHandle);

            playerConnection.getClass().getMethod("sendPacket", new Class[]{getNMSClass("Packet")}).invoke(playerConnection, packetPlayOutChat);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | NoSuchFieldException e) {
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
