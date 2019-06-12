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

package org.Spazzinq.FlightControl.Objects;

import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Rewritten to ignore Bukkit "headers" in saveToString & loadFromString
public class CommentedConfig extends YamlConfiguration {
    private HashMap<String, String> comments, addSection = new HashMap<>();
    private HashMap<String, List<String>> addSubsection = new HashMap<>();
    private final Yaml yaml;

    public CommentedConfig() {
        // TODO moved from save/load methods causes issues (maybe gc)?
        DumperOptions yamlOptions = new DumperOptions(); yamlOptions.setIndent(options().indent()); yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer yamlRepresenter = new YamlRepresenter(); yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    }
    public CommentedConfig(File f, InputStream is) throws IOException, InvalidConfigurationException { this(); load(f); loadComments(is); }

    public HashMap<String, String> comments() { return comments; }
    public void addSection(String relativeKey, String key) { addSection.put(relativeKey, key); }
    public void addSubsections(String relativeKey, List<String> key) { addSubsection.put(relativeKey, key); }
    public void addSubsection(String relativeKey, String key) { addSubsection.put(relativeKey, Collections.singletonList(key)); }

    public void save(File f) throws IOException {
        if (f != null) {
            //noinspection UnstableApiUsage
            Files.createParentDirs(f);
            try (FileWriter writer = new FileWriter(f)) { writer.write(insertComments(insertSubKeys(insertKeys(saveToString())))); }
        }
    }

    private String leadSpaces(String s) { return s.startsWith(" ") ? s.split("[^\\s]")[0] : ""; }
    private String strFromIS(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; int length;
        while ((length = is.read(buffer)) != -1) out.write(buffer, 0, length);
        is.close();

        return out.toString();
    }

    // BEFORE
    // Comments should already have "#" at the beginning
    private String insertComments(String config) {
        String[] lines = config.split("\n");
        StringBuilder newConf = new StringBuilder(config);
        //              config index
        String key = ""; int i = 0, depth = 0;

        for (String line : lines) {
            // Is a key?
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localKey = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    // example key -> "lol.xd.420"
                    // same depth -> "lol.xd" | shallower by 1 -> "lol" | by 2 -> ""
                    // (add key on later)
                    int back = (depth - nDepth) / 2 + 1;
                    // Back the key down
                    for (int j = 0; j < back; j++) if (!key.isEmpty()) key = key.contains(".") ? key.substring(0, key.lastIndexOf(".")) : "";
                }
                // Add the key up
                key = key.concat((key.isEmpty() ? "" : ".") + localKey);
                depth = nDepth;
                // Add comment(s)
                if (comments.containsKey(key)) {
                    String c = spaces + comments.get(key).replaceAll("\n", "\n" + spaces);
                    // Remove spaces at the end from above replaceAll
                    c = c.substring(0, c.length() - depth);
                    newConf.insert(newConf.indexOf(line, i), c);
                    // Set location to continue from
                    i += c.length();
                }
            }
            i += line.length();
        }
        return newConf.toString() + (comments.getOrDefault("footer", ""));
    }

    // BEFORE
    // TODO Fix temporary solution for main keys (Stop copy pasting code)
    private String insertKeys(String config) {
        String[] lines = config.split("\n");
        StringBuilder newConf = new StringBuilder(config);
        String key = ""; int i = 0, depth = 0;

        for (String line : lines) {
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localKey = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++) if (!key.isEmpty()) key = key.contains(".") ? key.substring(0, key.lastIndexOf(".")) : "";
                }
                key = key.concat((key.isEmpty() ? "" : ".") + localKey);
                depth = nDepth;

                if (addSection.containsKey(key)) {
                    String k = spaces + addSection.get(key).replaceAll("\n", "\n" + spaces);
                    k = k.substring(0, k.length() - depth) + "\n";
                    // Insert BEFORE
                    newConf.insert(newConf.indexOf(line, i), k);
                    i += k.length();
                }

            }
            i += line.length();
        }
        return newConf.toString();
    }

    // AFTER
    private String insertSubKeys(String config) {
        String[] lines = config.split("\n");
        StringBuilder newConf = new StringBuilder(config);
        String key = ""; int i = 0, depth = 0;

        for (String line : lines) {
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localKey = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++) if (!key.isEmpty()) key = key.contains(".") ? key.substring(0, key.lastIndexOf(".")) : "";
                }
                key = key.concat((key.isEmpty() ? "" : ".") + localKey);
                depth = nDepth;

                int length = 0;

                if (addSubsection.containsKey(key))
                    for (String section : addSubsection.get(key)) {
                        String k = spaces + "  " + section.replaceAll("\n", "\n" + spaces + "  ");
                        k = "\n" + k.substring(0, k.length() - depth);
                        // Insert AFTER
                        newConf.insert(newConf.indexOf(line, i) + line.length(), k);
                        length += k.length();
                    }
                i += length;
            }
            i += line.length();
        }
        return newConf.toString();
    }

    // TODO Does this work for comments on the same line as content? (lol: # example)
    private void loadComments(String config) {
        comments = new HashMap<>();
        String[] lines = config.split("\n");
        // c = comment
        String key = "", c = ""; int depth = 0;

        for (String line : lines) {
            if (line.contains("#")) c = c.concat(line.substring(leadSpaces(line).length()) + "\n");
            else if (line.isEmpty()) c = c.concat("\n");
            else if (line.contains(": ") || line.endsWith(":")) {
                String localKey = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++)
                        if (!key.isEmpty()) key = key.contains(".") ? key.substring(0, key.lastIndexOf(".")) : "";
                }
                key = key.concat((key.isEmpty() ? "" : ".") + localKey);
                depth = nDepth;

                if (!c.isEmpty()) { comments.put(key, c); c = ""; }
            }
        }
        if (!c.isEmpty()) comments.put("footer", c.substring(0, c.length() - 1));
    }
    public HashMap<String, String> loadComments(InputStream is) throws IOException { loadComments(strFromIS(is)); return comments; }
    @Override public void load(File f) throws IOException, InvalidConfigurationException { load(new FileInputStream(f)); }
    @SuppressWarnings("deprecation") public void load(InputStream is) throws InvalidConfigurationException, IOException { loadFromString(strFromIS(is)); }

    @Override public String saveToString() {
        String dump = yaml.dump(getValues(false));
        return dump.equals(BLANK_CONFIG) ? "" : dump;
    }
    @Override public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try { input = (Map<?, ?>) yaml.load(contents); }
        catch (YAMLException e) { throw new InvalidConfigurationException(e); }
        catch (ClassCastException e) { throw new InvalidConfigurationException("Top level is not a Map."); }

        if (input != null) convertMapsToSections(input, this);
    }
}
