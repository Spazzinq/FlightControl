package org.spazzinq.flightcontrol.manager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;

import java.io.IOException;
import java.nio.file.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.*;

public class FileManager extends BukkitRunnable {
    private FlightControl pl;
    private WatchService watcher;

    private static final String CATEGORIES = "categories.yml",
                                CONFIG = "config.yml";
    boolean ignoreCategories, ignoreConfig;

    public FileManager(FlightControl pl, Path dataPath) throws IOException {
        this.pl = pl;
        watcher = FileSystems.getDefault().newWatchService();

        // Only watch modifications and creations
        dataPath.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
    }

    private void logChanges(String filename) {
        pl.getLogger().info("Detected changes in " + filename + "! Loading changes...");
    }

    @Override
    public void run() {
        WatchKey key = watcher.poll();

        if (key != null) {
            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // Can still occur even though listening for ENTRY_CREATE and ENTRY_MODIFY
                if (kind == OVERFLOW) {
                    continue;
                }

                // context = path
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                String fileString = ev.context().toString();
                boolean changed = false;

                if (CATEGORIES.equals(fileString) && !ignoreCategories) {
                    ignoreCategories = true;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ignoreCategories = false;
                        }
                    }, 100);

                    logChanges(CATEGORIES);
                    pl.getCategoryManager().reloadCategories();
                    changed = true;
                } else if (CONFIG.equals(fileString) && !ignoreConfig) {
                    ignoreConfig = true;
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            ignoreConfig = false;
                        }
                    }, 100);

                    if (pl.getConfigManager().reloadConfig()) {
                        logChanges(CONFIG);
                        // If flight_speed is updated!
                        pl.getPlayerManager().reloadPlayerData();
                    }
                    changed = true;
                }

                if (changed) {
                    pl.checkPlayers();
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                cancel();
            }
        }

    }
}
