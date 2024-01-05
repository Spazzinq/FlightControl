/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory;

import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.check.Check;

public abstract class TerritoryCheck extends Check {
    @Override public Cause getCause() {
        return Cause.TERRITORY;
    }
}
