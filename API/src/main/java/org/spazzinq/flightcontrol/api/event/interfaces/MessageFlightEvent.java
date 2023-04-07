/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.api.event.interfaces;

@SuppressWarnings("unused")
public interface MessageFlightEvent extends FlightEvent {
    /**
     * Returns the tentative message.
     *
     * @return the tentative message
     */
    String getMessage();

    /**
     * Sets the message to send.
     *
     * @param message the message to send
     */
    void setMessage(String message);

    /**
     * Returns true if the message will be sent at the action bar (the area above the hotbar).
     *
     * @return true if the message will be sent at the action bar, false otherwise
     */
    boolean isByActionbar();

    /**
     * Sets if the message will be sent at the action bar (the area above the hotbar).
     *
     * @param byActionbar a boolean to set if the message will be sent at the action bar
     */
    void setByActionbar(boolean byActionbar);
}
