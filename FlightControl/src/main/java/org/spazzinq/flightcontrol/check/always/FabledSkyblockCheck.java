/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import com.craftaro.skyblock.SkyBlock;
import com.craftaro.skyblock.api.SkyBlockAPI;
import com.craftaro.skyblock.api.island.Island;
import com.craftaro.skyblock.api.island.IslandManager;
import com.craftaro.skyblock.api.island.IslandUpgrade;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class FabledSkyblockCheck extends Check {
    @Override public boolean check(Player p) {
        Island island = getManager().getIslandPlayerAt(p);

        return island != null &&
                (island.hasUpgrade(IslandUpgrade.FLY)
                || p.hasPermission("fabledskyblock.fly.*")
                || (p.hasPermission("fabledskyblock.fly") && p.getUniqueId().equals(island.getOwnerUUID())));
    }

    private IslandManager getManager() { return SkyBlockAPI.getIslandManager(); }


    @Override public Cause getCause() {
        return Cause.FABLED_SKYBLOCK_FLY;
    }
}
