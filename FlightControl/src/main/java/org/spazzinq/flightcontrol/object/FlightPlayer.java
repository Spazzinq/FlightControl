/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
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
import lombok.SneakyThrows;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;

import java.io.File;

public class FlightPlayer {
    private final File dataFile;
    @Getter private final YamlConfiguration data;
    private final Player player;

    @Getter private final Timer tempflyTimer;
    @Getter private float actualFlightSpeed;
    @Setter private boolean trail;

    public FlightPlayer(File dataFile, YamlConfiguration data, Player player, float actualFlightSpeed, boolean trail, long tempFlyLength) {
        this.dataFile = dataFile;
        this.data = data;
        this.player = player;
        // Don't store speed in data conf if not personal for player
        this.actualFlightSpeed = actualFlightSpeed;
        this.trail = trail;
        this.tempflyTimer = new Timer(tempFlyLength) {
            @SneakyThrows @Override public void onFinish() {
                FlightControl.getInstance().getFlightManager().check(player);
                data.set("tempfly", null);
                data.save(dataFile);
            }
        };

        // Auto-save
        new BukkitRunnable() {
            @SneakyThrows @Override public void run() {
                data.set("tempfly", tempflyTimer.getTimeLeft() == 0 ? null : tempflyTimer.getTimeLeft());
                data.save(dataFile);
            }
            // 6000 ticks = 20 ticks * 300 = 5 minutes
        }.runTaskTimerAsynchronously(FlightControl.getInstance(), 6000, 6000);
    }

    public boolean trailWanted() {
        return trail;
    }

    @SneakyThrows public boolean toggleTrail() {
        trail = !trail;

        data.set("trail", trail);

        return trail;
    }

    @SneakyThrows public void setTempFlyLength(long tempFlyLength, boolean addTime) {
        if (addTime) {
            tempflyTimer.addTimeLeft(tempFlyLength);
        } else {
            tempflyTimer.setTotalTime(tempFlyLength);
        }

        // Prevent NPE for data migration
        if (data != null) {
            data.set("tempfly", tempflyTimer.getTimeLeft());
            data.save(dataFile);
        }
    }

    @SneakyThrows public void setActualFlightSpeed(float actualFlightSpeed) {
        this.actualFlightSpeed = actualFlightSpeed;

        data.set("flight_speed", actualFlightSpeed);

        player.setFlySpeed(actualFlightSpeed);
    }
}
