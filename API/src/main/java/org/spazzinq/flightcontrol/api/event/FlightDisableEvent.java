/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2022 Spazzinq
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.spazzinq.flightcontrol.api.event;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.api.event.interfaces.*;
import org.spazzinq.flightcontrol.api.object.Cause;
import org.spazzinq.flightcontrol.api.object.Sound;

public class FlightDisableEvent implements PlayerFlightEvent, LocationFlightEvent, MessageFlightEvent,
        SoundFlightEvent, CommandFlightEvent, Cancellable {
    @Getter private final Player player;
    @Getter private final Location location;
    @Getter private final Cause cause;
    @Getter @Setter private String message;
    @Getter @Setter private Sound sound;
    @Getter @Setter private boolean byActionbar;
    @Getter private final boolean byCommand;
    @Getter @Setter private boolean cancelled;

    public FlightDisableEvent(Player player, Location location, Cause cause, String message, Sound sound, boolean byActionbar,
                              boolean byCommand) {
        this.player = player;
        this.location = location;
        this.cause = cause;
        this.message = message;
        this.sound = sound;
        this.byActionbar = byActionbar;
        this.byCommand = byCommand;
    }
}

