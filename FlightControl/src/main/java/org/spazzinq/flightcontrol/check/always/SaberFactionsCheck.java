/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import com.massivecraft.factions.FPlayers;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class SaberFactionsCheck extends Check {
    @Override public boolean check(Player p) {
        return FPlayers.getInstance().getByPlayer(p).canFlyAtLocation();
    }
    @Override public Cause getCause() {
        return Cause.SABER_FLY;
    }
}
