/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import com.craftaro.skyblock.core.third_party.org.jooq.Update;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.UpdateStatus;
import org.spazzinq.flightcontrol.object.Version;
import org.spazzinq.flightcontrol.object.VersionType;
import org.spazzinq.flightcontrol.util.FileUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class UpdateManager {
    private static final String PLUGIN_PATH = "plugins/FlightControl.jar";
    private static UpdateStatus updateStatus;
    private final FlightControl pl;

    @Getter @Setter private Version newVersion;
    @Getter @Setter private Version version;

    private long lastCheckTimestamp;
    private final HashSet<String> notified = new HashSet<>();

    public UpdateManager() {
        pl = FlightControl.getInstance();
        version = new Version(pl.getDescription().getVersion());

        if (version.getVersionType() == VersionType.BETA) {
            pl.getLogger().warning(
                    " \n  _       _       _       _       _       _\n" +
                    " ( )     ( )     ( )     ( )     ( )     ( )\n" +
                    "  X       X       X       X       X       X\n" +
                    "-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,\n" +
                    "      X       X       X       X       X       X\n" +
                    "     (_)     (_)     (_)     (_)     (_)     (_)\n" +
                    " \nFlightControl version " + version + "-BETA is unstable\nand should not be run on a " +
                    "production server.\n \n" +
                    "  _       _       _       _       _       _\n" +
                    " ( )     ( )     ( )     ( )     ( )     ( )\n" +
                    "  X       X       X       X       X       X\n" +
                    "-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,-' `-. ,\n" +
                    "      X       X       X       X       X       X\n" +
                    "     (_)     (_)     (_)     (_)     (_)     (_)\n");
        }
    }

    public void checkForUpdate(CommandSender sender) {
        if (pl.getConfManager().isAutoUpdate()) {
            if (updateStatus == UpdateStatus.VERIFIED) {
                msg(sender, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate has already been " +
                        "downloaded but cannot install right now. Restart (or reload) the server to install the update.");
            } else if (updateStatus == UpdateStatus.DOWNLOADED) {
                msg(sender, "&c&lFlightControl &7» &cFATAL ERROR while attempting to download version &f" + getNewVersion() + "!");
            } else {
                checkUpdate(sender, silentCheck);
            }
        } else {
            // Delay sending to be at bottom of console
            if (checkUpdate(true)) {
                new BukkitRunnable() {
                    @Override public void run() {
                       notifyUpdate(Bukkit.getConsoleSender());
                    }
                }.runTaskLaterAsynchronously(pl, 70);
            }
        }
    }

    public void checkUpdate(CommandSender sender, boolean silentCheck) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                URLConnection spigotConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=55168").openConnection();
                spigotConnection.setConnectTimeout(3000);
                spigotConnection.setReadTimeout(3000);

                return FileUtil.streamToString(spigotConnection.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        future.thenAccept(response -> {
            newVersion = new Version(response);
            lastCheckTimestamp = System.currentTimeMillis();

            if (newVersion != null && newVersion.isNewer(version)) {
                if (newVersion.getMajorVersion() > version.getMajorVersion()) {
                    msg(sender, "&e&lFlightControl &7» &eThere is an update available, but the changes in the " +
                            "latest update that may not be compatible with your server, so you must manually update this plugin.");
                } else {
                    downloadPlugin();

                    if (Bukkit.getPluginManager().isPluginEnabled("Plugman")) {
                        msg(sender, "&a&lFlightControl &7» &aAutomatic installation finished (the configs have automatically " +
                                "updated too)! Welcome to FlightControl &f" + getNewVersion() + "&a!");
                        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman reload flightcontrol");
                    } else {
                        msg(sender, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate downloaded. Restart " +
                                "(or reload) the server to apply the update.");
                    }
                } else if (!silentCheck) {
                    msg(sender, "&a&lFlightControl &7» &aNo updates found.");
                }
            }
        });
    }

    @SneakyThrows private void downloadPlugin() {
        if (!currentlyDownloading && !verified) {
            currentlyDownloading = true;
            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                try (FileOutputStream fileOutputStream = new FileOutputStream(PLUGIN_PATH)) {
                    URLConnection gitHubJar = new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion +
                            "/FlightControl.jar").openConnection();
                    gitHubJar.setConnectTimeout(3000);
                    gitHubJar.setReadTimeout(3000);

                    ReadableByteChannel readableByteChannel = Channels.newChannel(gitHubJar.getInputStream());
                    fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

                    return gitHubJar.getHeaderField("content-md5");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            });

            future.thenAccept(knownHash -> {
                currentlyDownloading = false;
                downloaded = true;
                try {
                    byte[] bytes = Files.readAllBytes(Paths.get(PLUGIN_PATH));
                    String downloadHash = Base64.getEncoder().encodeToString(MessageDigest.getInstance("MD5").digest(bytes));

                    verified = knownHash.equals(downloadHash);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public void notifyUpdate(CommandSender sender) {
        if (checkUpdate(false) && notified.add(sender.getName())) {
            msg(sender, "&e&lFlightControl &7» &eWoot woot! Version &f" + getNewVersion() + "&e is now available! " +
                    "Update with \"/fc update\" and check out the new features: &fhttps://www.spigotmc" +
                    ".org/resources/55168/");
        }
    }
}
