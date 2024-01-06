/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.api.event.interfaces;

@SuppressWarnings("unused")
public interface CommandFlightEvent {
    /**
     * Returns true if the event was triggered manually by player executing a command
     *
     * @return true if the event was triggered manually by player executing a command
     */
    boolean isByCommand();
}
