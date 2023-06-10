/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public final class LandsTrustedCheck extends TerritoryCheck {
    private final LandsIntegration landsIntegration;

    public LandsTrustedCheck() {
        landsIntegration = new LandsIntegration(FlightControl.getInstance());
    }

    @Override public boolean check(Player p) {
        Land land = landsIntegration.getLand(p.getLocation());

        return land != null && land.getTrustedPlayers().contains(p.getUniqueId());
    }
}
