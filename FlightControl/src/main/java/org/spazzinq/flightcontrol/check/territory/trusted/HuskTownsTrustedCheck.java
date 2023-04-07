/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import net.william278.husktowns.HuskTownsAPI;
import net.william278.husktowns.chunk.ClaimedChunk;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class HuskTownsTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        ClaimedChunk chunk = getChunk(p);

        return chunk != null && chunk.getPlotChunkMembers().contains(p.getUniqueId());
    }

    private ClaimedChunk getChunk(Player p) {
        return HuskTownsAPI.getInstance().getClaimedChunk(p.getLocation());
    }
}
