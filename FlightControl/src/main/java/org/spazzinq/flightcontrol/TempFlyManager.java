/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
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

package org.spazzinq.flightcontrol;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class TempFlyManager {
    private FlightControl pl;

    private File tempflyFile;
    private FileConfiguration tempflyData;

    @Getter private Set<UUID> scheduledExpirations = new HashSet<>();

    TempFlyManager(FlightControl pl) {
        this.pl = pl;

        tempflyFile = new File(pl.getStorageFolder(), "tempfly.yml");
        reloadTempflyData();
    }

    void reloadTempflyData() {
        if (!tempflyFile.exists()) {
            try { //noinspection ResultOfMethodCallIgnored
                tempflyFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tempflyData = YamlConfiguration.loadConfiguration(tempflyFile);

        boolean modified = false;

        for (String strUUID : tempflyData.getKeys(false)) {
            long expiration = tempflyData.getLong(strUUID);

            if (expiration > System.currentTimeMillis()) {
                UUID uuid = UUID.fromString(strUUID);

                if (!scheduledExpirations.contains(uuid)) {
                    Player p = Bukkit.getPlayer(uuid);

                    if (p != null && p.isOnline()) {
                        scheduleExpiration(p, expiration);
                    }
                }
            } else {
                tempflyData.set(strUUID, null);
            }
        }
        if (modified) saveTempfly();
    }

    public void setTempfly(Player p, long expiration) {
        tempflyData.set(p.getUniqueId().toString(), expiration);
        scheduleExpiration(p, expiration);
        saveTempfly();
    }

    void getAndSetTempfly(Player p) {
        if (tempflyData.contains(p.getUniqueId().toString()) && !scheduledExpirations.contains(p.getUniqueId())) {
            scheduleExpiration(p, tempflyData.getLong(p.getUniqueId().toString()));
        }
    }

    public void removeTempfly(Player p) {
        tempflyData.set(p.getUniqueId().toString(), null);
        scheduledExpirations.remove(p.getUniqueId());
        pl.getFlightManager().getTempBypassList().remove(p);
        pl.getFlightManager().check(p);
        saveTempfly();
    }

    private void scheduleExpiration(Player p, long expiration) {
        if (expiration > System.currentTimeMillis()) {
            scheduledExpirations.add(p.getUniqueId());
            pl.getFlightManager().getTempBypassList().add(p);

            new BukkitRunnable() {
                @Override public void run() {
                    removeTempfly(p);
                }
            }.runTaskLater(pl, (expiration - System.currentTimeMillis()) / 50);
        }
    }

    private void saveTempfly() {
        try {
            tempflyData.save(tempflyFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
