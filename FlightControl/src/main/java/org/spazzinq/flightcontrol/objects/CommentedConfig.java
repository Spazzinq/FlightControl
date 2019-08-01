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

package org.spazzinq.flightcontrol.objects;

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

// Rewritten to allow comment saving & to ignore
// Bukkit "headers" in saveToString & loadFromString
public final class CommentedConfig extends YamlConfiguration {
    private HashMap<String, String> comments,
                                    addNodes = new HashMap<>();
    private HashMap<String, List<String>> addSubnodes = new HashMap<>();
    private final Yaml yaml;

    private CommentedConfig() {
        DumperOptions yamlOptions = new DumperOptions();
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer yamlRepresenter = new YamlRepresenter();
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    }
    public CommentedConfig(File configFile, InputStream configResource) throws IOException, InvalidConfigurationException {
        this();
        load(configFile);

        comments = loadComments(configResource);
        HashMap<String, String> oldComments = loadComments(new FileInputStream(configFile));

        // If comments from modified config do not match new ones, then save new version
        // (only matches ones from the MODIFIED config)
        for (Map.Entry<String, String> e : oldComments.entrySet()) {
            if (!e.getValue().equals(comments.get(e.getKey()))) save(configFile);
        }

        configResource.close();
    }

    public void addNode(String relativeKey, String node) { addNodes.put(relativeKey, node); }
    public void addSubnodes(String relativeKey, List<String> node) { addSubnodes.put(relativeKey, node); }
    public void addSubnode(String relativeKey, String node) { addSubnodes.put(relativeKey, Collections.singletonList(node)); }

    public void save(File f) throws IOException {
        if (f != null) {
            //noinspection UnstableApiUsage
            Files.createParentDirs(f);
            try (FileWriter writer = new FileWriter(f)) {
                writer.write(insertComments(insertSubnodes(insertNodes(new StringBuilder(saveToString())))).toString());
            }
        }
    }

    private String leadSpaces(String s) { return s.startsWith(" ") ? s.split("[^\\s]")[0] : ""; }

    // Insert BEFORE node
    // Comments should already have "#" at the beginning
    private StringBuilder insertBefore(StringBuilder tempConfig, HashMap<String, String> toInsert, boolean lineSeparator) {
        String[] lines = tempConfig.toString().split("\n");
        //              config index
        String node = "";
        int i = 0,
            depth = 0;

        for (String line : lines) {
            // Is it a node?
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localNode = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    // example node -> "lol.xd.420"
                    // same depth -> "lol.xd" | shallower by 1 -> "lol" | by 2 -> ""
                    // (add node on later)
                    int back = (depth - nDepth) / 2 + 1;
                    // Back the node down
                    for (int j = 0; j < back; j++) if (!node.isEmpty()) node = node.contains(".") ? node.substring(0, node.lastIndexOf(".")) : "";
                }
                // Update the node
                node = node.concat((node.isEmpty() ? "" : ".") + localNode);
                depth = nDepth;

                if (toInsert.containsKey(node)) {
                    // Add the spaces from the depth of the node
                    String insert = spaces + toInsert.get(node).replaceAll("\n", "\n" + spaces);
                    // Remove spaces at the end from above replaceAll
                    insert = insert.substring(0, insert.length() - depth) + (lineSeparator ? "\n" : "");
                    // Insert into newConf
                    tempConfig.insert(tempConfig.indexOf(line, i), insert);
                    // Set location to continue from
                    i += insert.length();
                }
            }
            i += line.length();
        }
        return tempConfig;
    }

    private StringBuilder insertComments(StringBuilder tempConfig) {
        insertBefore(tempConfig, comments, false);
        tempConfig.insert(0, comments.getOrDefault("header", ""));
        tempConfig.append(comments.getOrDefault("footer", ""));
        return tempConfig;
    }

    private StringBuilder insertNodes(StringBuilder tempConfig) {
        return insertBefore(tempConfig, addNodes, true);
    }

    // Insert AFTER node
    private StringBuilder insertSubnodes(StringBuilder tempConfig) {
        String[] lines = tempConfig.toString().split("\n");
        String node = "";
        int i = 0,
            depth = 0;

        for (String line : lines) {
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localNode = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++) if (!node.isEmpty()) node = node.contains(".") ? node.substring(0, node.lastIndexOf(".")) : "";
                }
                node = node.concat((node.isEmpty() ? "" : ".") + localNode);
                depth = nDepth;

                int insertLength = 0;
                if (addSubnodes.containsKey(node)) {
                    // Get all sections for node
                    for (String section : addSubnodes.get(node)) {
                        String k = "\n" + spaces
                                // If the node is a main node, indent
                                + (node.contains(".") ? "" : "  ") + section.replaceAll("\n", "\n" + spaces + (node.contains(".") ? "" : "  "));
                        k = k.substring(0, k.length() - depth + (node.contains(".") ? 2 : 0));
                        // Insert AFTER
                        tempConfig.insert(tempConfig.indexOf(line, i) + line.length(), k);
                        insertLength += k.length();
                    }
                }
                i += insertLength;
            }
            i += line.length();
        }
        return tempConfig;
    }

    // WARNING: This does NOT work for inlined comments (eg. test: #lol)
    private HashMap<String, String> loadComments(String config) {
        HashMap<String, String> comments = new HashMap<>();
        String[] lines = config.split("\n");
        // config = comment
        String node = "",
               c = "";
        int depth = 0;
        boolean headerDone = false;

        for (String line : lines) {
            // Substring of leadSpaces length also includes the comment symbol (#)
            if (line.contains("#")) c = c.concat(line.substring(leadSpaces(line).length()) + "\n");
            else if (line.isEmpty()) c = c.concat("\n");
            else if (line.contains(": ") || line.endsWith(":")) {
                String localNode = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line); int nDepth = spaces.length();

                if (depth >= nDepth) {
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++)
                        if (!node.isEmpty()) node = node.contains(".") ? node.substring(0, node.lastIndexOf(".")) : "";
                }
                node = node.concat((node.isEmpty() ? "" : ".") + localNode);
                depth = nDepth;

                if (!headerDone) {
                    if (countBlankLines(c) > 1) {
                        int index = c.lastIndexOf("\n\n") + 2;
                        comments.put("header", c.substring(0, index));
                        c = c.substring(index);
                    }
                    headerDone = true;
                }

                if (!c.isEmpty()) { comments.put(node, c); c = ""; }
            }
        }
        //                                                          Remove \n
        if (!c.isEmpty()) comments.put("footer", c.substring(0, c.length() - 1));
        return comments;
    }

    private HashMap<String, String> loadComments(InputStream is) throws IOException { return loadComments(isToString(is)); }
    @Override public void load(File f) throws IOException, InvalidConfigurationException { load(new FileInputStream(f)); }
    @SuppressWarnings("deprecation") public void load(InputStream is) throws InvalidConfigurationException, IOException { loadFromString(isToString(is)); }

    private String isToString(InputStream is) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024]; int length;
        while ((length = is.read(buffer)) != -1) out.write(buffer, 0, length);

        return out.toString();
    }

    @Override public void loadFromString(String contents) throws InvalidConfigurationException {
        Validate.notNull(contents, "Contents cannot be null");

        Map<?, ?> input;
        try { input = (Map<?, ?>) yaml.load(contents); }
        catch (YAMLException e) { throw new InvalidConfigurationException(e); }
        catch (ClassCastException e) { throw new InvalidConfigurationException("Top level is not a Map."); }

        if (input != null) convertMapsToSections(input, this);
    }

    @Override public String saveToString() {
        String dump = yaml.dump(getValues(false));
        return dump.equals(BLANK_CONFIG) ? "" : dump;
    }

    private int countBlankLines(String data) {
        int count = 0,
            index = 0;
        while (data.indexOf("\n\n", index) != -1) {
            count++;
            //                                  Second new line
            index += data.indexOf("\n\n", index) + 2;
        }
        return count;
    }
}
