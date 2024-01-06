/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.bypasstrail.vanish;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class EssentialsVanishCheck extends VanishCheck {
    Essentials e;

    @Override public boolean check(Player p) {
        if (e == null) {
            e = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        }

        return e.getUser(p) != null && e.getUser(p).isVanished();
    }
}
