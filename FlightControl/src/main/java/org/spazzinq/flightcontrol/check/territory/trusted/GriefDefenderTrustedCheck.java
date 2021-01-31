package org.spazzinq.flightcontrol.check.territory.trusted;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class GriefDefenderTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return getClaimAt(p).getUserTrusts().contains(p.getUniqueId());
    }

    private Claim getClaimAt(Player p) {
        return GriefDefender.getCore().getClaimManager(p.getWorld().getUID()).getClaimAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
    }
}
