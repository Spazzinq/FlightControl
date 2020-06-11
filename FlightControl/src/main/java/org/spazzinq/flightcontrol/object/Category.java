/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.spazzinq.flightcontrol.object;

import lombok.Getter;
import org.bukkit.World;
import org.spazzinq.flightcontrol.api.object.Region;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;

public class Category implements Comparable<Category> {
    @Getter private final String name;
    @Getter private final int priority;

    @Getter private final DualStore<Check> checks;

    // Just storage for other plugins to use
    @Getter private final DualStore<World> worlds;
    @Getter private final DualStore<Region> regions;
    @Getter private final DualStore<FactionRelation> factions;
    @Getter private final DualStore<Check> ownTerritories;
    @Getter private final DualStore<Check> trustedTerritories;

    public Category(String name, DualStore<Check> checks, DualStore<World> worlds, DualStore<Region> regions,
                    DualStore<FactionRelation> factions, DualStore<Check> ownTerritories, DualStore<Check> trustedTerritories, int priority) {
        this.name = name;
        this.checks = checks;
        this.worlds = worlds;
        this.regions = regions;
        this.factions = factions;
        this.ownTerritories = ownTerritories;
        this.trustedTerritories = trustedTerritories;
        this.priority = priority;
    }

//    public boolean enabledContains(World world) {
//        return worlds.getEnabled().contains(world);
//    }
//
//    public boolean enabledContains(Region region) {
//        return regions.getEnabled().contains(region);
//    }
//
//    public boolean enabledContains(FactionRelation relation) {
//        return factions.getEnabled().contains(relation);
//    }
//
//    public boolean disabledContains(World world) {
//        return worlds.getDisabled().contains(world);
//    }
//
//    public boolean disabledContains(Region region) {
//        return regions.getDisabled().contains(region);
//    }
//
//    public boolean disabledContains(FactionRelation relation) {
//        return factions.getDisabled().contains(relation);
//    }

    @Override
    public int compareTo(Category o) {
        return o.priority - priority;
    }
}
