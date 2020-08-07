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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.Version;
import org.spazzinq.flightcontrol.object.VersionType;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.UUID;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public class UpdateManager {
    private final FlightControl pl;

    @Getter private Version newVersion;
    @Getter private final Version version;
    private boolean downloaded;

    private final HashSet<UUID> notified = new HashSet<>();

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
            installUpdate(Bukkit.getConsoleSender(), true);
        } else {
            // Delay sending to be at bottom of console
            new BukkitRunnable() {
                @Override public void run() {
                    if (updateExists()) {
                        pl.getLogger().info("Hooray! Version " + newVersion + " is available for update." +
                                " Perform \"/fc update\" to update and visit https://www.spigotmc" +
                                ".org/resources/55168/ to view the feature changes!" +
                                ".");
                    }
                }
            }.runTaskLaterAsynchronously(pl, 70);
        }
    }

    public void installUpdate(CommandSender s, boolean silentCheck) {
        new BukkitRunnable() {
            @Override public void run() {
                if (updateExists()) {
                    if (!downloaded) {
                        downloadPlugin();

                        if (Bukkit.getPluginManager().isPluginEnabled("Plugman")) {
                            msg(s, "&a&lFlightControl &7» &aAutomatic installation finished (the configs have automatically " +
                                    "updated too)! Welcome to FlightControl &f" + getNewVersion() + "&a!");
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman reload flightcontrol");
                        } else {
                            msg(s, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate downloaded. Restart " +
                                    "(or reload) the server to apply the update.");
                        }
                    } else {
                        msg(s, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate has already been " +
                                "downloaded. Restart (or reload) the server to apply the update.");
                    }
                } else if (!silentCheck) {
                    msg(s, "&a&lFlightControl &7» &aNo updates found.");
                }
            }
        }.runTaskAsynchronously(pl);
    }

    public boolean updateExists() {
        try {
            URL spigot = new URL("https://api.spigotmc.org/legacy/update.php?resource=55168");
            newVersion =
                    new Version(new BufferedReader(new InputStreamReader(spigot.openConnection().getInputStream())).readLine());
        } catch (Exception ignored) {
            return false;
        }

        return newVersion.isNewer(version);
    }

    private void downloadPlugin() {
        try (FileOutputStream fos = new FileOutputStream(new File("plugins/FlightControl.jar"))) {
            URL gitHub = new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion +
                    "/FlightControl.jar");
            ReadableByteChannel channel = Channels.newChannel(gitHub.openStream());

            fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            downloaded = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyUpdate(Player p) {
        if (updateExists() && !notified.contains(p.getUniqueId())) {
            notified.add(p.getUniqueId());
            msg(p, "&e&lFlightControl &7» &eWoot woot! Version &f" + getNewVersion() + "&e is now available! " +
                    "Update with \"/fc update\" and check out the new features: &fhttps://www.spigotmc" +
                    ".org/resources/55168/");
        }
    }
}
