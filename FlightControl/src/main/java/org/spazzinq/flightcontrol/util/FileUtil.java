/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2021 Spazzinq
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
