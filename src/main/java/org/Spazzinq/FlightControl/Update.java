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

package org.Spazzinq.FlightControl;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

class Update {
    static private String version, newVersion;
    static boolean dled;

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
                URL website = new URL("https://github.com/Spazzinq/FlightControl/releases/download/" + newVersion + "/FlightControl.jar");
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(new File( "plugins/FlightControl.jar"));
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE); fos.close();
                dled = true;
            } catch (Exception ignored) {}
        }
    }

    static String newVer() { return newVersion; }
}
