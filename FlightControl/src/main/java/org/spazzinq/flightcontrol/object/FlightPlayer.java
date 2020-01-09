/*
 * This file is part of FlightControl, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
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

package org.spazzinq.flightcontrol.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

public class FlightPlayer {
    @Getter private final CommentConf data;
    private final Player player;

    @Getter private float actualFlightSpeed;
    @Setter private boolean trail;
    @Getter private Long tempFlyEnd;

    public FlightPlayer(CommentConf data, Player player, float actualFlightSpeed, boolean trail, Long tempFlyEnd) {
        this.data = data;
        this.player = player;
        // Don't store speed in data conf if not personal for player
        this.actualFlightSpeed = actualFlightSpeed;
        this.trail = trail;
        setTempFly(tempFlyEnd);
    }

    public boolean trailWanted() {
        return trail;
    }

    public boolean toggleTrail() {
        trail = !trail;

        data.set("trail", trail);
        data.save();

        return trail;
    }

    public void setTempFly(Long tempFlyEnd) {
        Long finalTempFlyEnd = tempFlyEnd;

        if (finalTempFlyEnd != null && finalTempFlyEnd <= System.currentTimeMillis()) {
            finalTempFlyEnd = null;
        }
        this.tempFlyEnd = finalTempFlyEnd;

        // Prevent NPE for data migration
        if (data != null) {
            data.set("temp_fly", finalTempFlyEnd);
            data.save();
        }
    }

    public boolean hasTempFly() {
        if (tempFlyEnd != null && tempFlyEnd <= System.currentTimeMillis()) {
            setTempFly(null);
        }

        return tempFlyEnd != null;
    }

    public void setActualFlightSpeed(float actualFlightSpeed) {
        this.actualFlightSpeed = actualFlightSpeed;

        data.set("flight_speed", actualFlightSpeed);
        data.save();

        player.setFlySpeed(actualFlightSpeed);
    }
}
