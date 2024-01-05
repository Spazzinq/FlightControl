/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion;

public class Hook {
    public final boolean isHooked() {
        return !getClass().getName().contains("Generic");
    }
}
