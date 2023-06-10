/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol;

import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Listens for changes in the configurations and reloads them if they are created or modified.
 */
class PathWatcher extends BukkitRunnable {
    private final FlightControl pl;
    private WatchService watcher;

    private static final String CATEGORIES = "categories.yml";
    private static final String CONFIG = "config.yml";
    private static final String LANG = "lang.yml";

    PathWatcher(FlightControl pl, Path dataPath) {
        this.pl = pl;

        try {
            watcher = FileSystems.getDefault().newWatchService();

            // Only watch modifications and creations
            dataPath.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        WatchKey key = watcher.poll();

        if (key != null) {
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Can still occur even though listening for ENTRY_CREATE and ENTRY_MODIFY
                if (kind == OVERFLOW) {
                    continue;
                }

                // context = path
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                String fileString = ev.context().toString();
                boolean playerStateChanged = false;
                boolean cmdChanged = false;

                switch (fileString) {
                    case CATEGORIES:
                        logChanges(CATEGORIES);
                        pl.getCategoryManager().loadCategories();
                        playerStateChanged = true;
                        break;
                    case CONFIG:
                        if (pl.getConfManager().load()) {
                            cmdChanged = true;
                            playerStateChanged = true;
                            logChanges(CONFIG);
                            // If flight_speed is updated!
                            pl.getPlayerManager().loadPlayerData();
                        }
                        break;
                    case LANG:
                        if (pl.getLangManager().load()) {
                            logChanges(LANG);
                        }
                        break;
                    default:
                        break;
                }
                if (playerStateChanged) {
                    pl.getCheckManager().loadChecks();

                    pl.getFlightManager().checkAllPlayers();
                    pl.getTrailManager().checkAllPlayers();
                    pl.getStickybarManager().checkAllPlayers();
                }
                if (cmdChanged) {
                    pl.registerCommands();
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                cancel();
            }
        }
    }

    private void logChanges(String filename) {
        pl.getLogger().info("Detected changes in " + filename + "! Loading changes...");
    }
}
