package org.Spazzinq.FlightControl;

import org.bukkit.entity.Player;

class Sound {
    private org.bukkit.Sound value;
    private float v, p;
    Sound(String name, float v, float p) { value = org.bukkit.Sound.valueOf(name); this.v = v; this.p = p; }

    static void play(Player p, Sound s) { if (s != null) p.playSound(p.getLocation(), s.value, s.v, s.p); }
    static boolean is(String s) { for (org.bukkit.Sound sound : org.bukkit.Sound.values()) if (sound.toString().equals(s)) return true; return false; }
}
