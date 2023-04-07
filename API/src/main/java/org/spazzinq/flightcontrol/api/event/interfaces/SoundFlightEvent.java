/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.api.event.interfaces;


import org.spazzinq.flightcontrol.api.object.Sound;

@SuppressWarnings("unused")
public interface SoundFlightEvent {
    /**
     * Returns the Sound that will be played on the event's execution.
     *
     * @return the Sound that will be played on the event's execution
     */
    Sound getSound();

    void setSound(Sound sound);
}
