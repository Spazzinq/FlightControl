/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public final class PlotSquared3TrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        org.bukkit.Location l = p.getLocation();
        Plot plot = Plot.getPlot(new Location(l.getWorld().getName(), l.getBlockX(), l.getBlockY(), l.getBlockZ()));

        return plot != null && plot.getTrusted().contains(p.getUniqueId());
    }
}
