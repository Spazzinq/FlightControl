/*
 * This file is part of FlightControl, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
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
import org.spazzinq.flightcontrol.object.Version;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashSet;
import java.util.UUID;

import static org.spazzinq.flightcontrol.util.MessageUtil.msg;

public final class UpdateManager {
    @Getter private Version newVersion;
    @Getter private final Version version;
    private boolean downloaded;

    private final HashSet<UUID> notified = new HashSet<>();

    public UpdateManager(String versionStr) {
        version = new Version(versionStr);
    }

    public boolean updateExists() {
        try {
            URL spigot = new URL("https://api.spigotmc.org/legacy/update.php?resource=55168");
            newVersion = new Version(new BufferedReader(new InputStreamReader(spigot.openConnection().getInputStream())).readLine());
        } catch (Exception ignored) {
            return false;
        }

        return newVersion.isNewer(version);
    }

    private void downloadPlugin() {
        if (updateExists()) {
            try (FileOutputStream fos = new FileOutputStream(new File("plugins/FlightControl.jar"))) {
                URL gitHub = new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion + "/FlightControl.jar");
                ReadableByteChannel channel = Channels.newChannel(gitHub.openStream());

                fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
                downloaded = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void installUpdate(CommandSender s, boolean silentCheck) {
        if (updateExists()) {
            if (!downloaded) {
                downloadPlugin();
                if (Bukkit.getPluginManager().isPluginEnabled("Plugman")) {
                    msg(s, "&a&lFlightControl &7» &aAutomatic installation finished (the configs have automatically updated too)! Welcome to FlightControl " + getNewVersion() + "!");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman reload flightcontrol");
                } else
                    msg(s, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate downloaded. Restart (or reload) the server to apply the update.");
            } else
                msg(s, "&a&lFlightControl &7» &aVersion &f" + getNewVersion() + " &aupdate has already been downloaded. Restart (or reload) the server to apply the update.");
        } else if (!silentCheck) {
            msg(s, "&a&lFlightControl &7» &aNo updates found.");
        }
    }

    public void notifyUpdate(Player p) {
        if (updateExists() && !notified.contains(p.getUniqueId())) {
            notified.add(p.getUniqueId());
            msg(p, "&e&lFlightControl &7» &eWoot woot! Version &f" + getNewVersion() + "&e is now available! " +
                                       "Update with \"/fc update\" and check out the new features: &fhttps://www.spigotmc.org/resources/flightcontrol.55168/");
        }
    }
}
