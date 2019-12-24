/*
 * This file is part of FlightControl, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
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
import org.spazzinq.flightcontrol.multiversion.FactionRelation;

public class Category implements Comparable<Category> {
    @Getter private String name;
    @Getter private int priority;

    @Getter private DualStore<World> worlds;
    @Getter private DualStore<Region> regions;
    @Getter private DualStore<FactionRelation> factions;

    public Category(String name, DualStore<World> worlds, DualStore<Region> regions, DualStore<FactionRelation> factions, int priority) {
        this.name = name;
        this.worlds = worlds;
        this.regions = regions;
        this.factions = factions;
        this.priority = priority;
    }

    @Override
    public int compareTo(Category o) {
        return o.priority - priority;
    }
}
