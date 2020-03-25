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
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.object.FlightPlayer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerManager {
    private final FlightControl pl;

    @Getter private final File folder;
    private final HashMap<UUID, FlightPlayer> playerCache = new HashMap<>();

    public PlayerManager(FlightControl pl) {
        this.pl = pl;
        folder = new File(pl.getDataFolder(), "data");

        if (new File(folder, "disabled_trail.yml").exists()) {
            migrateFromVersion3();
        }
    }

    public FlightPlayer getFlightPlayer(Player p) {
        // Cached loading
        if (!playerCache.containsKey(p.getUniqueId())) {
            File data = new File(folder, p.getUniqueId() + ".yml");

            CommentConf dataConf = new CommentConf(data, pl.getResource("default_data.yml"));

            float speed = dataConf.isDouble("flight_speed")
                    ? (float) dataConf.getDouble("flight_speed")
                    : pl.getConfManager().getDefaultFlightSpeed();
            Long tempFlyLength = dataConf.isLong("temp_fly") ? dataConf.getLong("temp_fly") : null;

            FlightPlayer flightPlayer = new FlightPlayer(dataConf, p, speed, dataConf.getBoolean("trail"), tempFlyLength);

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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void migrateFromVersion3() {
        File disabledTrails = new File(folder, "disabled_trail.yml");
        YamlConfiguration disabledConf = YamlConfiguration.loadConfiguration(disabledTrails);

        HashMap<String, FlightPlayer> migrateStorage = new HashMap<>();

        for (String uuid : disabledConf.getStringList("disabled_trail")) {
            migrateStorage.put(uuid, new FlightPlayer(null, null, -1, false, null));
        }
        disabledTrails.delete();

        File tempfly = new File(folder, "tempfly.yml");
        YamlConfiguration tempflyConf = YamlConfiguration.loadConfiguration(tempfly);

        for (String uuid : tempflyConf.getKeys(false)) {
            FlightPlayer flightPlayer = migrateStorage.getOrDefault(uuid, new FlightPlayer(null, null,  -1, true, null));
            flightPlayer.setTempFly(tempflyConf.getLong(uuid));
        }
        tempfly.delete();

        for (Map.Entry<String, FlightPlayer> migration : migrateStorage.entrySet()) {
            String uuid = migration.getKey();
            FlightPlayer tempData = migration.getValue();
            File data = new File(folder, uuid + ".yml");
            CommentConf dataConf = new CommentConf(data, pl.getResource("default_data.yml"));

            dataConf.set("trail", tempData.trailWanted());

            Long tempFlyLength = tempData.getTempFlyEnd();
            dataConf.set("temp_fly", tempFlyLength);

            dataConf.save();
        }
    }
}
