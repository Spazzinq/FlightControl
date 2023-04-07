/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.api.event.interfaces;

import org.bukkit.Location;

@SuppressWarnings("unused")
public interface LocationFlightEvent extends FlightEvent {
    /**
     * Returns the event's Location.
     *
     * @return the event's location
     */
    Location getLocation();
}
