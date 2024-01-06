/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.own;

import com.plotsquared.core.location.Location;
import com.plotsquared.core.plot.Plot;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public final class PlotSquaredOwnCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        org.bukkit.Location l = p.getLocation();
        Plot plot = Location.at(l.getWorld().toString(), l.getBlockX(), l.getBlockY(), l.getBlockZ()).getPlot();

        return plot != null && plot.hasOwner() && plot.getOwners().contains(p.getUniqueId());
    }
}
