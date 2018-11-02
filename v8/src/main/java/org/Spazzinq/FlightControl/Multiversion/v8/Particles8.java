package org.Spazzinq.FlightControl.Multiversion.v8;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Particles8 implements org.Spazzinq.FlightControl.Multiversion.Particles {
    public void play(World w, Player p, Location to, Location from) {
        p.spigot().playEffect(to, Effect.CLOUD, 0, 0, 0F, 0F, 0F, 0F, 4, 0);
        Location l = to.clone().subtract(from);
        p.getWorld().spigot().playEffect(from.clone().subtract(l).subtract(l), Effect.CLOUD, 0, 0, 0F, 0F, 0F, 0F, 4, 160);
    }
}
