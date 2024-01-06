/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.own;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimManager;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class GriefDefenderOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Claim claim = getClaimAt(p);

        return claim != null && p.getUniqueId().equals(claim.getOwnerUniqueId());
    }

    private Claim getClaimAt(Player p) {
        ClaimManager manager = GriefDefender.getCore().getClaimManager(p.getWorld().getUID());

        return manager == null ? null : manager.getClaimAt(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
    }
}
