/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.api.event.interfaces;

import org.bukkit.entity.Player;

public interface PlayerFlightEvent extends FlightEvent {
    /**
     * Returns the player that caused the event's calling.
     *
     * @return the player that caused the event's calling
     */
    @SuppressWarnings("unused") Player getPlayer();
}
