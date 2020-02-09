package org.spazzinq.flightcontrol.hook.griefprevention;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.hook.Hook;

public class GriefPreventionBase extends Hook {
    public boolean claimIsOwn(Location location, Player player) {
        return false;
    }
}
