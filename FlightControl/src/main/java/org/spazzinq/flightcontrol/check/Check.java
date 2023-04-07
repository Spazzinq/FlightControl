/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.object.Cause;

public abstract class Check {
    public abstract boolean check(Player p);

    public abstract Cause getCause();

    @Override public String toString() {
        return getClass().getSimpleName();
    }
}
