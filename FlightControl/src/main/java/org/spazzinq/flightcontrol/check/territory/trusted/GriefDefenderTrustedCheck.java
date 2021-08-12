package org.spazzinq.flightcontrol.check.territory.trusted;

import com.flowpowered.math.vector.Vector3i;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class GriefDefenderTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return getClaimAt(p).isUserTrusted(p.getUniqueId(), TrustTypes.ACCESSOR);
    }

    private Claim getClaimAt(Player p) {
        return GriefDefender.getCore().getClaimManager(p.getWorld().getUID()).getClaimAt(
                Vector3i.from(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ()));
    }
}
