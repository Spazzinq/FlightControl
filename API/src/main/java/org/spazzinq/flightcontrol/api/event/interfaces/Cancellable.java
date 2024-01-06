/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.api.event.interfaces;

@SuppressWarnings("unused")
public interface Cancellable {
    /**
     * Returns true if the event is cancelled.
     *
     * @return true if the event is cancelled
     */
    boolean isCancelled();

    /**
     * Sets if the event is cancelled. If the event is cancelled, it will not execute the HandlerMethods assigned to
     * itself.
     *
     * @param cancel a boolean to set if the event is cancelled
     */
    void setCancelled(boolean cancel);
}
