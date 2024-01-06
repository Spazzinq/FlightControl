/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.category;

import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashSet;

public class CategoryWorldCheck extends CategoryCheck {
    private final HashSet<World> worlds;

    public CategoryWorldCheck(HashSet<World> worlds) {
        this.worlds = worlds;
    }

    @Override public boolean check(Player p) {
        return worlds.contains(p.getWorld());
    }
}
