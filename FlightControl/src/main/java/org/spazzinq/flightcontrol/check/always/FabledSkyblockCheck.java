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
