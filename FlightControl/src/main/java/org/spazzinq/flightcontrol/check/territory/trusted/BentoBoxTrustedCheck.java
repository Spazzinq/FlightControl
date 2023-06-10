/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.database.objects.Island;

import java.util.Optional;

public class BentoBoxTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Optional<Island> island = BentoBox.getInstance().getIslands().getIslandAt(p.getLocation());

        return island.isPresent() && island.get().getMemberSet().contains(p.getUniqueId());
    }
}
