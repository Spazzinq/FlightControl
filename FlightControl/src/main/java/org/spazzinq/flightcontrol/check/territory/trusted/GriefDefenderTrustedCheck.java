/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import com.griefdefender.api.claim.TrustTypes;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class GriefDefenderTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Claim claim = getClaimAt(p);

        return claim != null && claim.isUserTrusted(p.getUniqueId(), TrustTypes.ACCESSOR);
    }

    private Claim getClaimAt(Player p) {
        ClaimManager manager = GriefDefender.getCore().getClaimManager(p.getWorld().getUID());

        return manager == null ? null : manager.getClaimAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
    }
}
