/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;


import java.util.Objects;

public final class PlotSquared6TrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        org.bukkit.Location l = p.getLocation();
        Plot plot = Location.at(Objects.requireNonNull(l.getWorld()).getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()).getPlot();

        return plot != null && plot.getTrusted().contains(p.getUniqueId());
    }
}
