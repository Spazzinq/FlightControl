package org.Spazzinq.FlightControl.Multiversion.v13;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Particles13 implements org.Spazzinq.FlightControl.Multiversion.Particles {
    public void play(World w, Player p, Location to, Location from) {
        p.spawnParticle(Particle.CLOUD, to, 4, 0, 0, 0, 0);
        Location l = to.clone().subtract(from);
        w.spawnParticle(Particle.CLOUD, from.clone().subtract(l).subtract(l), 0,0,0,0);
    }
}
