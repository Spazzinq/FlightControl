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

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    private final FlightControl pl;

    @Getter private final File folder;
    private final HashMap<UUID, FlightPlayer> playerCache = new HashMap<>();

    public PlayerManager(FlightControl pl) {
        this.pl = pl;
        folder = new File(pl.getDataFolder(), "data");
    }

    public FlightPlayer getFlightPlayer(Player p) {
        // Cached loading
        if (!playerCache.containsKey(p.getUniqueId())) {
            File dataFile = new File(folder, p.getUniqueId() + ".yml");

            YamlConfiguration dataConf = YamlConfiguration.loadConfiguration(dataFile);

            float speed = dataConf.isDouble("flight_speed")
                    ? (float) dataConf.getDouble("flight_speed")
                    : pl.getConfManager().getDefaultFlightSpeed();

            long tempFlyLength;

            if (dataConf.isInt("tempfly")) {
                tempFlyLength = dataConf.getInt("tempfly");
                // Migrate from 4.3.11 - old key
            } else if (dataConf.isInt("temp_fly")) {
                tempFlyLength = System.currentTimeMillis() - dataConf.getInt("temp_fly");

                dataConf.set("temp_fly", null);
                dataConf.set("tempfly", tempFlyLength);
            } else {
                tempFlyLength = 0;
            }

            FlightPlayer flightPlayer = new FlightPlayer(dataFile, dataConf, p.getUniqueId(), speed, dataConf.getBoolean("trail", true),
                    tempFlyLength);

            playerCache.put(p.getUniqueId(), flightPlayer);
        }

        return playerCache.get(p.getUniqueId());
    }

    public void loadPlayerData() {
        playerCache.clear();

        for (Player p : Bukkit.getOnlinePlayers()) {
            FlightPlayer flightPlayer = getFlightPlayer(p);

            p.setFlySpeed(flightPlayer.getActualFlightSpeed());
            if (!flightPlayer.trailWanted()) {
                pl.getTrailManager().trailRemove(p);
            }
        }
    }

    public void savePlayerData() {
        for (FlightPlayer flightPlayer : playerCache.values()) {
            flightPlayer.saveData();
        }
    }
}
