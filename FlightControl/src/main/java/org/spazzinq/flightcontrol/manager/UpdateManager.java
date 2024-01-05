/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.Version;
import org.spazzinq.flightcontrol.object.VersionType;
import org.spazzinq.flightcontrol.util.FileUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class UpdateManager {
    private final FlightControl pl;

    @Getter @Setter private Version newVersion;
    @Getter @Setter private Version version;
    private boolean downloaded;

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

    public void checkForUpdate() {
        if (pl.getConfManager().isAutoUpdate()) {
            installUpdate(Bukkit.getConsoleSender(), false);
        } else {
            // Delay sending to be at bottom of console
            if (updateExists(true)) {
                new BukkitRunnable() {
                    @Override public void run() {
                       notifyUpdate(Bukkit.getConsoleSender());
                    }
                }.runTaskLaterAsynchronously(pl, 70);
            }
        }
    }

    public void installUpdate(CommandSender sender, boolean silentCheck) {
        if (!downloaded) {
            if (updateExists(true)) {
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
                }
            } else if (!silentCheck) {
                msg(sender, "&a&lFlightControl &7» &aNo updates found.");
            }
        } else {
            msg(sender, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate has already been " +
                    "downloaded. Restart (or reload) the server to install the update.");
        }
    }

    public boolean updateExists(boolean forceCheck) {
        checkSpigotVersion(forceCheck);

        return newVersion != null && newVersion.isNewer(version);
    }

    private void downloadPlugin() {
        try (FileOutputStream fos = new FileOutputStream("plugins/FlightControl.jar")) {
            URLConnection gitHub = new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion +
                    "/FlightControl.jar").openConnection();
            gitHub.setConnectTimeout(3000);
            gitHub.setReadTimeout(3000);

            ReadableByteChannel channel = Channels.newChannel(gitHub.getInputStream());
            fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);

            downloaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkSpigotVersion(boolean forceCheck) {
        // If it's been more than 5 minutes since last check
        if (forceCheck || System.currentTimeMillis() > lastCheckTimestamp + 5 * 60 * 1000) {
            try {
                URLConnection spigotConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=55168").openConnection();
                spigotConnection.setConnectTimeout(3000);
                spigotConnection.setReadTimeout(3000);

                newVersion = new Version(FileUtil.streamToString(spigotConnection.getInputStream()));
            } catch (IOException ignored) {}

            lastCheckTimestamp = System.currentTimeMillis();
        }
    }

    public void notifyUpdate(CommandSender sender) {
        if (updateExists(false) && notified.add(sender.getName())) {
            msg(sender, "&e&lFlightControl &7» &eWoot woot! Version &f" + getNewVersion() + "&e is now available! " +
                    "Update with \"/fc update\" and check out the new features: &fhttps://www.spigotmc" +
                    ".org/resources/55168/");
        }
    }
}
