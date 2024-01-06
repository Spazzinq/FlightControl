/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public final class RedProtectTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Region rg = RedProtect.get().getAPI().getRegion(p.getLocation());

        return rg != null && rg.isMember(p);
    }
}
