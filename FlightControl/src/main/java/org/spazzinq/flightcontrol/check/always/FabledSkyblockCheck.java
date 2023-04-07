/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import com.songoda.skyblock.SkyBlock;
import com.songoda.skyblock.island.Island;
import com.songoda.skyblock.upgrade.Upgrade;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public class FabledSkyblockCheck extends Check {
    @Override public boolean check(Player p) {
        return getIsland(p).hasUpgrade(Upgrade.Type.Fly)
                || p.hasPermission("fabledskyblock.fly.*")
                || (p.hasPermission("fabledskyblock.fly") && getPlayerIsland(p) != null && getPlayerIsland(p).equals(getIsland(p)));
    }

    private Island getIsland(Player p) {
        return SkyBlock.getInstance().getIslandManager().getIslandPlayerAt(p);
    }

    private Island getPlayerIsland(Player p) {
        return SkyBlock.getInstance().getIslandManager().getIslandByOwner(p);
    }

    @Override public Cause getCause() {
        return Cause.FABLED_SKYBLOCK_FLY;
    }
}
