/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.own;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public final class TownyTownCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        try {
            Resident resident = TownyUniverse.getInstance().getResident(p.getName());
            Town townAtLoc = TownyAPI.getInstance().getTown(p.getLocation());

            if (resident != null && resident.hasTown() && resident.getTown().equals(townAtLoc)) {
                return true;
            }
        } catch (NotRegisteredException ignored) {
            // We don't care about people not in towns
        }

        return false;
    }
}
