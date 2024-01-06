/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.own;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class ResidenceOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        ClaimedResidence residence = Residence.getInstance().getResidenceManager().getByLoc(p.getLocation());

        return residence != null && residence.isOwner(p);
    }
}
