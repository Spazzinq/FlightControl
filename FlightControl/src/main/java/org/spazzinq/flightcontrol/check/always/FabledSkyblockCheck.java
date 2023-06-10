/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.island.IslandManager;
import com.songoda.skyblock.upgrade.Upgrade;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class FabledSkyblockCheck extends Check {
    @Override public boolean check(Player p) {
        Island island = getManager().getIslandPlayerAt(p);

        return island != null &&
                (island.hasUpgrade(Upgrade.Type.Fly)
                || p.hasPermission("fabledskyblock.fly.*")
                || (p.hasPermission("fabledskyblock.fly") && p.getUniqueId().equals(island.getOwnerUUID())));
    }

    private IslandManager getManager() { return SkyBlock.getInstance().getIslandManager(); }


    @Override public Cause getCause() {
        return Cause.FABLED_SKYBLOCK_FLY;
    }
}
