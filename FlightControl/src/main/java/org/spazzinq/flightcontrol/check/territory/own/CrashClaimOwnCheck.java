/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.own;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.api.CrashClaimAPI;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class CrashClaimOwnCheck extends TerritoryCheck {
    private CrashClaimAPI api;

    @Override public boolean check(Player p) {
        if (api == null) {
            api = CrashClaim.getPlugin().getApi();
        }

        return p.getUniqueId().equals(api.getClaim(p.getLocation()).getOwner());
    }
}
