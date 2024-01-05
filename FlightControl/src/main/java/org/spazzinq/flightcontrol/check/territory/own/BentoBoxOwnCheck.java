/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.own;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;
import world.bentobox.bentobox.BentoBox;

public class BentoBoxOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return BentoBox.getInstance().getIslands().isOwner(p.getWorld(), p.getUniqueId());
    }
}
