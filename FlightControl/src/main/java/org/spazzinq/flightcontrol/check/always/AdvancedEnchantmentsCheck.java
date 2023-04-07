/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.always;

import net.advancedplugins.ae.api.AEAPI;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

import java.util.HashMap;

public class AdvancedEnchantmentsCheck extends Check {
    private final FlightControl pl;

    public AdvancedEnchantmentsCheck(FlightControl pl) {
        this.pl = pl;
    }

    @Override public boolean check(Player p) {
        return p.getInventory().getBoots() != null && getBootEnchants(p).containsKey(pl.getConfManager().getAeEnchantName());
    }

    public HashMap<String, Integer> getBootEnchants(Player p) {
        return AEAPI.getEnchantmentsOnItem(p.getInventory().getBoots());
    }

    @Override public Cause getCause() {
        return Cause.ENCHANT;
    }
}
