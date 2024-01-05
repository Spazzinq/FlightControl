/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.util;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtil extends YamlConfiguration {
    public static String streamToString(InputStream source) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;

        try {
            while ((length = source.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result.toString();
    }

    public static StringBuilder streamToBuilder(InputStream source) {
        return new StringBuilder(streamToString(source));
    }

    public static StringBuilder readFile(Path path) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new StringBuilder(new String(encoded, StandardCharsets.UTF_8));
    }

    public static void copyFile(InputStream source, File destination) throws IOException {
        OutputStream output = new FileOutputStream(destination);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = source.read(buffer)) > 0) {
            output.write(buffer, 0, length);
        }
        output.close();
    }

}
