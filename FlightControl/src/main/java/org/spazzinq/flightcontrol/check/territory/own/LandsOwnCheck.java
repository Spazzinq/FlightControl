/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.own;

import me.angeschossen.lands.api.integration.LandsIntegration;
import me.angeschossen.lands.api.land.Land;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public final class LandsOwnCheck extends TerritoryCheck {
    private final LandsIntegration landsIntegration;

    public LandsOwnCheck() {
        landsIntegration = new LandsIntegration(FlightControl.getInstance());
    }

    @Override public boolean check(Player p) {
        Land land = landsIntegration.getLand(p.getLocation());

        // Debug
        //        if (land == null || !p.getUniqueId().equals(land.getOwnerUID())) {
        //            if (land == null) {
        //                p.sendMessage("The LandChunk is null!");
        //            } else {
        //                p.sendMessage(p.getUniqueId() + " " + land.getOwnerUID() + " " + (p.getUniqueId().equals
        //                (land.getOwnerUID())));
        //            }
        //        }

        return land != null && p.getUniqueId().equals(land.getOwnerUID());
    }
}
