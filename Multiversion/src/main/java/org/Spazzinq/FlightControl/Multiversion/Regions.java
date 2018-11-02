package org.Spazzinq.FlightControl.Multiversion;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Set;

public interface Regions { String region(Location l); Set<String> regions(World w); boolean hasRegion(String world, String region); }
