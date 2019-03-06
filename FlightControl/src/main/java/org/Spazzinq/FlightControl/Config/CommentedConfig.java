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

package org.Spazzinq.FlightControl.Config;

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
import java.util.HashMap;
import java.util.Map;

public class CommentedConfig extends YamlConfiguration {
    private HashMap<String, String> comments;

    void save(File f, boolean comments) throws IOException {
        if (f != null) {
            //noinspection UnstableApiUsage
            Files.createParentDirs(f);
            try (FileWriter writer = new FileWriter(f)) { writer.write(comments ? insertComments(saveToString()) : saveToString()); }
        }
    }

    private String leadSpaces(String s) { return s.startsWith(" ") ? s.split("[^\\s]")[0] : ""; }

    private String insertComments(String config) {
        String[] lines = config.split("\n");
        StringBuilder newConf = new StringBuilder(config);
        String key = ""; int i = 0, depth = 0;

        for (String line : lines) {
            // Is a key
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localKey = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    // lol.xd.420
                    // depth - nDepth == 0 -> "lol.xd"
                    // shallower by 1 -> "lol"
                    //              2 -> ""
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++) if (!key.isEmpty()) key = key.contains(".") ? key.substring(0, key.lastIndexOf(".")) : "";
                }
                key = key.concat((key.isEmpty() ? "" : ".") + localKey);
                depth = nDepth;
                // Add comment(s)
                if (comments.containsKey(key)) {
                    String c = spaces + comments.get(key).replaceAll("\n", "\n" + spaces);
                    c = c.substring(0, c.length() - depth);
                    newConf.insert(newConf.indexOf(line, i), c);
                    i = newConf.indexOf("\n", i + c.length()) + 1;
                }
            }
        }
        return newConf.toString() + (comments.getOrDefault("footer", ""));
    }

    private void loadComments(String config) {
        comments = new HashMap<>();
        String[] lines = config.split("\n");
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
    void loadComments(File f) throws IOException {
        InputStream is = new FileInputStream(f);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; int length;
        while ((length = is.read(buffer)) != -1) output.write(buffer, 0, length);
        is.close();

        loadComments(output.toString());
    }

    private final DumperOptions yamlOptions = new DumperOptions();
    private final Representer yamlRepresenter = new YamlRepresenter();
    private final Yaml yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);

    @Override public String saveToString() {
        yamlOptions.setIndent(2);
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

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
    @Override public void load(File f) throws IOException, InvalidConfigurationException { load(new FileInputStream(f)); }

    @SuppressWarnings("deprecation")
    @Override public void load(InputStream is) throws InvalidConfigurationException, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; int length;
        while ((length = is.read(buffer)) != -1) output.write(buffer, 0, length);
        is.close();

        loadFromString(output.toString());
    }
}
