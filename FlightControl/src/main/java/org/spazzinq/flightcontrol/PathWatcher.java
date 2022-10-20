/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2022 Spazzinq
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
