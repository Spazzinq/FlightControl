/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.always;

import com.badbones69.crazyenchantments.paper.CrazyEnchantments;
import com.badbones69.crazyenchantments.paper.api.enums.CEnchantments;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public final class CrazyEnchantmentsCheck extends Check {
    @Override public boolean check(Player p) {
        return CrazyEnchantments.getPlugin().getStarter().getCrazyManager().hasEnchantment(p.getEquipment().getBoots(), CEnchantments.WINGS);
    }

    @Override public Cause getCause() {
        return Cause.ENCHANT;
    }
}
