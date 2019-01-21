/*
 * Original code by ConnorLinfoot from
 * https://github.com/ConnorLinfoot/ActionBarAPI/blob/master/src/main/java/com/connorlinfoot/actionbarapi/ActionBarAPI.java
*/

package org.Spazzinq.FlightControl;

import org.bukkit.entity.Player;

class Actionbar {
//    private static FlightControl pl;
    private static String nms;
//    private static ArrayList<Player> sending = new ArrayList<>();
    private static boolean useOldMethods = false;

    Actionbar(FlightControl pl) {
//        Actionbar.pl = pl;
        nms = pl.getServer().getClass().getPackage().getName();
        nms = nms.substring(nms.lastIndexOf(".") + 1);
        // 1_7 may work with protocol hack
        if (nms.equalsIgnoreCase("v1_8_R1") || nms.startsWith("v1_7_")) useOldMethods = true;
    }

    static void send(Player p, String msg) {
        if (p.isOnline()) {
            try {
                Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + nms + ".entity.CraftPlayer");
                Object packet;
                Class<?> packetPlayOutChatClass = Class.forName("net.minecraft.server." + nms + ".PacketPlayOutChat");
                if (useOldMethods) {
                    Class<?> chatSerializerClass = Class.forName("net.minecraft.server." + nms + ".ChatSerializer");
                    Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nms + ".IChatBaseComponent");
                    packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class}).newInstance(
                            iChatBaseComponentClass.cast(chatSerializerClass.getDeclaredMethod("a", String.class).invoke(
                                    chatSerializerClass, "{\"text\": \"" + msg + "\"}")), (byte) 2);
                } else {
                    Object chatComponentText = Class.forName("net.minecraft.server." + nms + ".ChatComponentText")
                            .getConstructor(new Class<?>[]{String.class}).newInstance(msg);
                    Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nms + ".IChatBaseComponent");
                    try {
                        Class<?> chatMessageTypeClass = Class.forName("net.minecraft.server." + nms + ".ChatMessageType");
                        Object chatMessageType = null;
                        for (Object obj : chatMessageTypeClass.getEnumConstants()) if (obj.toString().equals("GAME_INFO")) chatMessageType = obj;
                        packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, chatMessageTypeClass})
                                .newInstance(chatComponentText, chatMessageType);
                    } catch (ClassNotFoundException e) { packet = packetPlayOutChatClass.getConstructor(new Class<?>[]{iChatBaseComponentClass, byte.class})
                            .newInstance(chatComponentText, (byte) 2); }
                }
                Object craftPlayerHandle = craftPlayerClass.getDeclaredMethod("getHandle").invoke(craftPlayerClass.cast(p));
                Object playerConnection = craftPlayerHandle.getClass().getDeclaredField("playerConnection").get(craftPlayerHandle);
                playerConnection.getClass().getDeclaredMethod("sendPacket", Class.forName("net.minecraft.server." + nms + ".Packet")).invoke(playerConnection, packet);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }
//    static void send(Player p, String msg, double duration) {
//        sending.add(p);
//        send(p, msg);
//        // Sends empty msg at the end of the duration. Allows messages shorter than 3 seconds, ensures precision.
//        if (duration >= 0) new BukkitRunnable() { public void run() { if (!sending.contains(p)) send(p, ""); sending.remove(p); } }.runTaskLater(pl, (long) duration + 1);
//        // Re-sends the messages every 3 seconds so it doesn't go away from the p's screen.
//        while (duration > 40) { duration -= 40; new BukkitRunnable() { public void run() {  if (!sending.contains(p)) send(p, msg); } }.runTaskLater(pl, (long) duration); }
//    }
}
