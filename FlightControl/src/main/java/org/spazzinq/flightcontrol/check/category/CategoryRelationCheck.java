/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.category;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.manager.FactionsManager;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;

import java.util.HashSet;

public class CategoryRelationCheck extends CategoryCheck {
    private final FactionsManager factionsManager;
    private final HashSet<FactionRelation> relations;

    public CategoryRelationCheck(FactionsManager factionsManager, HashSet<FactionRelation> relations) {
        this.factionsManager = factionsManager;
        this.relations = relations;
    }

    @Override public boolean check(Player p) {
        FactionRelation relation = factionsManager.getRelationToLocation(p);

        return relations.contains(relation);
    }
}
