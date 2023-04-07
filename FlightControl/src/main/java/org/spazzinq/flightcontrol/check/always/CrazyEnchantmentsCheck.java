/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.enums.CEnchantments;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public final class CrazyEnchantmentsCheck extends Check {
    @Override public boolean check(Player p) {
        return CrazyEnchantments.getInstance().hasEnchantment(p.getEquipment().getBoots(), CEnchantments.WINGS);
    }

    @Override public Cause getCause() {
        return Cause.ENCHANT;
    }
}
