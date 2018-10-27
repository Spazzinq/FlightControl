package org.Spazzinq.FlightControl.Multiversion.v13;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.Spazzinq.FlightControl.Multiversion.Regions;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Iterator;

public class Regions13 implements Regions {
    public String region(Location l) {
        Iterator<ProtectedRegion> iter = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery().getApplicableRegions(BukkitAdapter.adapt(l)).iterator();
        if (iter.hasNext()) return iter.next().getId(); return "";
    }
    public boolean hasRegion(String world, String region) { return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getWorld(world))).hasRegion(region); }
}
