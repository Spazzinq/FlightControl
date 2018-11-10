package org.Spazzinq.FlightControl;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

class Update {
    static private String version, newVersion;
    static boolean dled;

    Update(String version) { Update.version = version; }

    static boolean exists() { try {
        newVersion = new BufferedReader(new InputStreamReader(new URL("https://api.spigotmc.org/legacy/update.php?resource=55168").openConnection().getInputStream())).readLine();
    } catch (IOException e) { e.printStackTrace(); } return !version.equals(newVersion); }

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
