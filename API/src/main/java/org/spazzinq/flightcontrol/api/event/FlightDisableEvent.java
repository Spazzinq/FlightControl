/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
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

