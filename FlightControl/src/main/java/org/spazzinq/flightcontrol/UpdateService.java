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

public class UpdateService extends BukkitRunnable {
    @Override
    public void run() {
        if (pl.getConfManager().isAutoUpdate()) {
            installUpdate(false);
        } else {
            // Delay sending to be at bottom of console
            if (doesUpdateExist(true)) {
                new Thread(() -> {
                    try {
                        wait(3500);
                        notifyUpdate();
                    } catch (Exception ignored) {
                    }
                });
            }
        }
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

    private void checkSpigotVersion() {
        try {
            URLConnection spigotConnection = new URL("https://api.spigotmc.org/legacy/update.php?resource=55168").openConnection();
            spigotConnection.setConnectTimeout(3000);
            spigotConnection.setReadTimeout(3000);

            newVersion = new Version(FileUtil.streamToString(spigotConnection.getInputStream()));
        } catch (IOException ignored) {
        }
    }
}
