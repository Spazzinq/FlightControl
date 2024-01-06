/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;
import world.bentobox.bentobox.BentoBox;

public class BentoBoxTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        return BentoBox.getInstance().getIslandsManager().locationIsOnIsland(p, p.getLocation());
    }
}
