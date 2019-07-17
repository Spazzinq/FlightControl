/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
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

package org.spazzinq.flightcontrol;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import static org.spazzinq.flightcontrol.FlightControl.msg;

final class Update {
    private static String version, newVersion;
    private static boolean dled;

    Update(String version) { Update.version = version; }

    static boolean exists() {
        try {
        newVersion = new BufferedReader(new InputStreamReader(new URL("https://api.spigotmc.org/legacy/update.php?resource=55168").openConnection().getInputStream())).readLine();
        } catch (Exception ignored) { return false; }
        return version.matches("\\d+(.\\d+)?") && newVersion.matches("\\d+(.\\d+)?") ? Double.parseDouble(newVersion) > Double.parseDouble(version) : !version.equals(newVersion);
    }

   static void dl() {
        if (exists()) {
            try {
                URL website = new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion + "/flightcontrol.jar");
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(new File( "plugins/flightcontrol.jar"));
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE); fos.close();
                dled = true;
            } catch (Exception ignored) {}
        }
    }

    static boolean dled() { return dled; }
    static String newVer() { return newVersion; }

    static void install(CommandSender s) {
        if (exists()) {
            if (!dled) {
                dl();
                if (Bukkit.getPluginManager().isPluginEnabled("Plugman")) {
                    msg(s, "&a&lFlightControl &7» &aAutomatic installation finished (the config has automatically updated too)! Welcome to flightcontrol " + newVer() + "!");
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman reload flightcontrol");
                } else msg(s, "&a&lFlightControl &7» &aVersion &f" + newVer() + " &aupdate downloaded. Restart (or reload) the server to apply the update.");
            } else msg(s, "&a&lFlightControl &7» &aVersion &f" + newVer() + " &aupdate has already been downloaded. Restart (or reload) the server to apply the update.");
        } else msg(s, "&a&lFlightControl &7» &aNo updates found.");
    }
}
