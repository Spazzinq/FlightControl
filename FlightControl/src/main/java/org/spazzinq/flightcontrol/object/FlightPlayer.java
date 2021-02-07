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
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;

import java.io.File;
import java.util.UUID;

public class FlightPlayer {
    private final File dataFile;
    @Getter private final YamlConfiguration data;
    private final UUID uuid;

    @Getter private final Timer tempflyTimer;
    @Getter private float actualFlightSpeed;
    @Getter @Setter private boolean trailWanted;

    public FlightPlayer(File dataFile, YamlConfiguration data, UUID uuid, float actualFlightSpeed, boolean trailWanted, long tempflyDuration) {
        this.dataFile = dataFile;
        this.data = data;
        this.uuid = uuid;
        // Don't store speed in data conf if not personal for player
        this.actualFlightSpeed = actualFlightSpeed;
        this.trailWanted = trailWanted;
        this.tempflyTimer = new Timer(tempflyDuration) {
            @SneakyThrows @Override public void onFinish() {
                FlightControl.getInstance().getFlightManager().check(getPlayer());
                data.set("tempfly", null);
                data.save(dataFile);
            }

            @Override public void onStart() {
                new BukkitRunnable() {
                    @Override public void run() {
                        if (getPlayer() != null) {
                            FlightControl.getInstance().getFlightManager().check(getPlayer());
                        }
                    }
                }.runTaskLater(FlightControl.getInstance(), getTimeLeft() / 50 + 4);
            }
        };

        // Auto-save
        new BukkitRunnable() {
            @SneakyThrows @Override public void run() {
                saveData();
            }
            // 6000 ticks = 20 ticks * 300 = 5 minutes
        }.runTaskTimerAsynchronously(FlightControl.getInstance(), 6000, 6000);
    }

    @SneakyThrows public boolean toggleTrail() {
        trailWanted = !trailWanted;

        data.set("trail", trailWanted);

        return trailWanted;
    }

    @SneakyThrows public void modifyTempflyDuration(TempflyTask type, long duration) {
        // TODO Fix subtraction calculation
        switch (type) {
            case ADD:
                tempflyTimer.addTimeLeft(duration);
                break;
            case REMOVE:
                tempflyTimer.addElapsedTime(duration);
                break;
            case SET:
                tempflyTimer.setTotalTime(duration);
                break;
            case DISABLE:
                tempflyTimer.reset();
                break;
            default:
                break;
        }

        // Start if always running/currently flying
        if (type != TempflyTask.REMOVE
                && (Timer.alwaysDecrease
                    || getPlayer().isFlying())) {
            tempflyTimer.start();
        }

        // Prevent NPE for data migration
        if (data != null) {
            data.set("tempfly", tempflyTimer.getTimeLeft());
            data.save(dataFile);
        }
    }

    @SneakyThrows public void setActualFlightSpeed(float actualFlightSpeed) {
        this.actualFlightSpeed = actualFlightSpeed;
        getPlayer().setFlySpeed(actualFlightSpeed);

        data.set("flight_speed", actualFlightSpeed);
    }

    @SneakyThrows public void saveData() {
        data.set("tempfly", tempflyTimer.getTimeLeft() == 0 ? null : tempflyTimer.getTimeLeft());
        data.save(dataFile);
    }

    private Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }
}
