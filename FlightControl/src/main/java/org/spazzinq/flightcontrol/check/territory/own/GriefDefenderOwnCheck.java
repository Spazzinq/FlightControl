package org.spazzinq.flightcontrol.check.territory.own;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class GriefDefenderOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return p.getUniqueId().equals(getClaimAt(p).getOwnerUniqueId());
    }

    private Claim getClaimAt(Player p) {
        return GriefDefender.getCore().getClaimManager(p.getWorld().getUID()).getClaimAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
    }
}
