/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.api.object;

import lombok.Getter;
import org.bukkit.World;

import java.util.Objects;

public class Region {
    @Getter private final World world;
    @Getter private final String regionName;

    public Region(World world, String regionName) {
        this.world = world;
        this.regionName = regionName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Region)) {
            return false;
        }
        Region region = (Region) o;
        return world.equals(region.world) &&
                regionName.equals(region.regionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, regionName);
    }

    @Override public String toString() {
        return world + "." + regionName;
    }
}
