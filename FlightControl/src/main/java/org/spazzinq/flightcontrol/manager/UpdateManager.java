/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;
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

    private static UpdateStatus updateStatus = UpdateStatus.UNKNOWN;
    private final FlightControl pl;

    @Getter @Setter private Version newVersion;
    @Getter @Setter private Version version;

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
        if (updateStatus == UpdateStatus.UNKNOWN || updateStatus == UpdateStatus.UP_TO_DATE) {
            fetchSpigotVersion(sender);
        } else {
            sendStatus(sender);
        }
    }

    public void fetchSpigotVersion(CommandSender sender) {
        updateStatus = UpdateStatus.FETCHING_FROM_SPIGOT;
        sendStatus(sender);

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                URLConnection spigotConnection =
                        new URL("https://api.spigotmc.org/legacy/update.php?resource=55168").openConnection();
                spigotConnection.setConnectTimeout(3000);
                spigotConnection.setReadTimeout(3000);

                return FileUtil.streamToString(spigotConnection.getInputStream());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        // Set status back to UNKNOWN if check fails
        future.exceptionally(exception -> {
            updateStatus = UpdateStatus.UNKNOWN;
            sendStatus(sender);

            return null;
        });

        future.thenAccept(response -> {
            newVersion = new Version(response);

            if (newVersion.isNewer(version)) {
                updateStatus = UpdateStatus.NEEDS_UPDATE;

                if (pl.getConfManager().isAutoUpdate() && newVersion.getMajorVersion() == version.getMajorVersion()) {
                    downloadFromGitHub(sender);
                }
            } else {
                updateStatus = UpdateStatus.UP_TO_DATE;
            }

            sendStatus(sender);
        });
    }

    @SneakyThrows private void downloadFromGitHub(CommandSender sender) {
        updateStatus = UpdateStatus.DOWNLOADING;

        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try (FileOutputStream fileOutputStream = new FileOutputStream(PLUGIN_PATH)) {
                URLConnection gitHubJar =
                        new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion +
                                "/FlightControl.jar").openConnection();
                gitHubJar.setConnectTimeout(3000);
                gitHubJar.setReadTimeout(3000);

                ReadableByteChannel readableByteChannel = Channels.newChannel(gitHubJar.getInputStream());
                fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

                return gitHubJar.getHeaderField("content-md5");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        });

        future.thenAccept(knownHash -> {
            updateStatus = UpdateStatus.DOWNLOADED;

            try {
                // Read the bytes from the file path, then compute a hash
                byte[] fileBytes = Files.readAllBytes(Paths.get(PLUGIN_PATH));
                byte[] computedHash = MessageDigest.getInstance("MD5").digest(fileBytes);
                String encodedHash = Base64.getEncoder().encodeToString(computedHash);

                // Compare it to the hash provided by GitHub
                if (knownHash.equals(encodedHash)) {
                    updateStatus = UpdateStatus.VERIFIED;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            sendStatus(sender);
        });
    }

    public void sendStatus(CommandSender sender) {
        sendStatus(sender, false);
    }

    public void sendStatus(CommandSender sender, boolean silenceIfAlreadyNotified) {
        if (!silenceIfAlreadyNotified || (updateStatus == UpdateStatus.NEEDS_UPDATE && notified.add(sender.getName()))) {
            msg(sender, updateStatus.getMessage());
        }
    }
}
