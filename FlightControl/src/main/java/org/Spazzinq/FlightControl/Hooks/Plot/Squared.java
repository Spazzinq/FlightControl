package org.Spazzinq.FlightControl.Hooks.Plot;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.flag.Flags;
import org.bukkit.Location;

public class Squared extends Plot {
    private PlotAPI pAPI = new PlotAPI();
    @Override public boolean flight(Location l) { return (pAPI.getPlot(l) != null) ? pAPI.getPlot(l).getFlag(Flags.FLY, true) : true; }
}
