/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.own;

import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.chunk.ClaimedChunk;
import net.william278.husktowns.claim.TownClaim;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

import java.util.Optional;

public class HuskTownsOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Optional<TownClaim> claim = getClaim(p);

        return claim.isPresent() && claim.get().claim().isPlotManager(p.getUniqueId());
    }

    private Optional<TownClaim> getClaim(Player p) {
        return HuskTownsAPI.getInstance().getClaimAt(p.getLocation());
    }
}
