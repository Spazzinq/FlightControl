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
    @Getter private CommentConf data;
    private Player player;

    @Getter private float actualFlightSpeed;
    @Setter private boolean trail;
    @Getter private Long tempFlyEnd;
    // Until next server restart
    @Setter private boolean infiniteTempfly;

    public FlightPlayer(CommentConf data, Player player, float actualFlightSpeed, boolean trail, Long tempFlyEnd) {
        this.data = data;
        this.player = player;
        this.actualFlightSpeed = actualFlightSpeed;
        this.trail = trail;
        this.tempFlyEnd = tempFlyEnd != null && tempFlyEnd > System.currentTimeMillis() ? tempFlyEnd : null;
    }

    public boolean hasTrail() {
        return trail;
    }

    public boolean toggleTrail() {
        trail = !trail;

        data.set("trail", trail);
        data.save();

        return trail;
    }

    public void setTempFly(Long tempFlyEnd) {
        this.tempFlyEnd = tempFlyEnd;
        infiniteTempfly = false;

        // Prevent NPE for data migration
        if (data != null) {
            data.set("temp_fly", tempFlyEnd);
            data.save();
        }
    }

    public boolean hasTempFly() {
        return (tempFlyEnd != null && tempFlyEnd > System.currentTimeMillis()) || infiniteTempfly;
    }

    public void setActualFlightSpeed(float actualFlightSpeed) {
        this.actualFlightSpeed = actualFlightSpeed;

        data.set("flight_speed", actualFlightSpeed);
        data.save();

        player.setFlySpeed(actualFlightSpeed);
    }
}
