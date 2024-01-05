/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion;

import org.bukkit.Location;

public interface Particle {
    void spawn(Location l);

    void setParticle(String s);

    void setAmount(int amount);

    void setRBG(int r, int g, int b);
}
