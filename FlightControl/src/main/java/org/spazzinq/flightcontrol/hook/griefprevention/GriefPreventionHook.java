package org.spazzinq.flightcontrol.hook.griefprevention;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GriefPreventionHook extends GriefPreventionBase {
    @Override public boolean claimIsOwn(Location location, Player player) {
        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, true, null);

        return claim != null && player.getUniqueId().equals(claim.ownerID);
    }

    @Override public boolean isHooked() {
        return true;
    }
}
