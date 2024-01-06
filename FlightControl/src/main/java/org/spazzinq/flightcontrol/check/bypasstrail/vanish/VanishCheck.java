/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.check.bypasstrail.vanish;

import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public abstract class VanishCheck extends Check {
    @Override public Cause getCause() {
        return Cause.VANISH;
    }
}
