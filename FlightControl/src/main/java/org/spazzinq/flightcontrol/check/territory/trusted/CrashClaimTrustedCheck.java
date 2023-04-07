/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import net.crashcraft.crashclaim.CrashClaim;
import net.crashcraft.crashclaim.permissions.PermissionHelper;
import net.crashcraft.crashclaim.permissions.PermissionRoute;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class CrashClaimTrustedCheck extends TerritoryCheck {
    private PermissionHelper api;

    @Override public boolean check(Player p) {
        if (api == null) {
            api = CrashClaim.getPlugin().getApi().getPermissionHelper();
        }

        return api.hasPermission(p.getLocation(), PermissionRoute.BUILD);
    }
}
