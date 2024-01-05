/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
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

    @Override
    public int compareTo(Category o) {
        return o.priority - priority;
    }
}
