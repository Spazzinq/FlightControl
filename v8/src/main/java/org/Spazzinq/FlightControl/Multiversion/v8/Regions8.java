package org.Spazzinq.FlightControl.Multiversion.v8;

import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.Spazzinq.FlightControl.Multiversion.Regions;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Iterator;

public class Regions8 implements Regions {
    public String region(Location l) {
        Iterator<ProtectedRegion> iter = WGBukkit.getRegionManager(l.getWorld()).getApplicableRegions(l).iterator();
        if (iter.hasNext()) return iter.next().getId(); return "";
    }

    public boolean hasRegion(String world, String region) { return WGBukkit.getRegionManager(Bukkit.getWorld(world)).hasRegion(region); }
}
