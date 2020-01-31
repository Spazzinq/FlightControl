package org.spazzinq.flightcontrol.object;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class FlightWorld {
    private static Set<FlightWorld> worlds = new HashSet<>();

    private World world;
    @Getter private Set<Player> players;

    public FlightWorld(World world, Collection<Player> players) {
        this.world = world;
        
        worlds.add(this);

        if (players != null) {
            this.players = new HashSet<>(players);
        }
    }

    public boolean addPlayer(Player p) {
        return players.add(p);
    }

    public boolean removePlayer(Player p) {
        return players.remove(p);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlightWorld)) {
            return false;
        }
        FlightWorld that = (FlightWorld) o;
        return world.equals(that.world);
    }

    @Override public int hashCode() {
        return Objects.hash(world);
    }
}
