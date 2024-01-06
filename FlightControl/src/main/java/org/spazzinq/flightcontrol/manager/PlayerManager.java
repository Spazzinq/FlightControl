/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.FlightPlayer;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class PlayerManager {
    private final FlightControl pl;

    @Getter private final File storageFolder;
    private final HashMap<UUID, FlightPlayer> playerCache = new HashMap<>();

    public PlayerManager() {
        pl = FlightControl.getInstance();
        storageFolder = new File(pl.getDataFolder(), "data");
    }

    public FlightPlayer getFlightPlayer(@Nullable Player p) {
        if (p == null) {
            return null;
        }

        // Cached loading
        if (!playerCache.containsKey(p.getUniqueId())) {
            File dataFile = new File(storageFolder, p.getUniqueId() + ".yml");

            YamlConfiguration dataConf = YamlConfiguration.loadConfiguration(dataFile);

            float speed = dataConf.isDouble("flight_speed")
                    ? (float) dataConf.getDouble("flight_speed")
                    : pl.getConfManager().getDefaultRawFlightSpeed();

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

            p.setFlySpeed(flightPlayer.getRawFlightSpeed());
            if (!flightPlayer.isTrailWanted()) {
                pl.getTrailManager().disableTrail(p);
            }
        }
    }

    public void savePlayerData() {
        for (FlightPlayer flightPlayer : playerCache.values()) {
            flightPlayer.saveData();
        }
    }
}
