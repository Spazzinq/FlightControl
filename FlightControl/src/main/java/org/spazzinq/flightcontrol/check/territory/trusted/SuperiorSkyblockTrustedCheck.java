/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

public class SuperiorSkyblockTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        SuperiorPlayer player = SuperiorSkyblockAPI.getPlayer(p);
        Island island = SuperiorSkyblockAPI.getIslandAt(p.getLocation());
        
        return player != null && island != null && !island.isSpawn();
    }
}
