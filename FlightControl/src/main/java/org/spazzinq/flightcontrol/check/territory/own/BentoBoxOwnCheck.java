/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.own;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class BentoBoxOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Optional<Island> island = getIsland(p);

        return island.isPresent() && island.get().getOwner() == p.getUniqueId();
    }

    private Optional<Island> getIsland(Player p) {
        return BentoBox.getInstance().getIslandsManager().getIslandAt(p.getLocation());
    }
}
