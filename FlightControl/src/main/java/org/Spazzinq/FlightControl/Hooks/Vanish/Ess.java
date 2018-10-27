package org.Spazzinq.FlightControl.Hooks.Vanish;

import com.earth2me.essentials.Essentials;
import org.bukkit.entity.Player;

public class Ess extends Vanish {
    private Essentials e;
    public Ess(Essentials e) { this.e = e; }
    @Override public boolean vanished(Player p) { return e.getUser(p).isVanished(); }
}
