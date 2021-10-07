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

package org.spazzinq.flightcontrol.object;

import com.google.common.io.Files;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConstructor;
import org.bukkit.configuration.file.YamlRepresenter;
import org.spazzinq.flightcontrol.util.FileUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.spazzinq.flightcontrol.object.ConfTask.*;
import static org.spazzinq.flightcontrol.util.ConfUtil.runTask;

/**
 * Saves the comments left in the config and updates them based on a resource InputStream.
 */
@SuppressWarnings("unused")
public class CommentConf extends YamlConfiguration {
    private final Yaml yaml;
    private File file;

    private final HashMap<String, Set<String>> defaultComments = new HashMap<>();
    private final HashMap<String, Set<String>> addNodes = new HashMap<>();
    private final HashMap<String, Set<String>> addSubnodes = new HashMap<>();
    private final HashMap<String, Set<String>> addIndentedSubnodes = new HashMap<>();

    private CommentConf() {
        // From saveToString in YamlConfiguration
        DumperOptions yamlOptions = new DumperOptions();
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        yamlOptions.setAllowUnicode(true);

        Representer yamlRepresenter = new YamlRepresenter();
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    }

    /**
     * Saves stream to file, compares comments, and loads configuration.
     *
     * @param file              the location of the current modified config
     * @param defaultConfStream the InputStream to the new config
     */
    public CommentConf(File file, InputStream defaultConfStream) {
        this(file, defaultConfStream, true);
    }

    /**
     * Compares comments, loads configuration, and possibly saves the stream to a file.
     *
     * @param file              the location of the current modified config
     * @param defaultConfStream the InputStream to the new config
     * @param saveFile          should the stream be saved to the file
     */
    public CommentConf(File file, InputStream defaultConfStream, boolean saveFile) {
        this();
        this.file = file;
        boolean fileExists = file.exists();

        if (saveFile && !fileExists) {
            try {
                //noinspection UnstableApiUsage
                Files.createParentDirs(file);
                FileUtil.copyFile(defaultConfStream, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StringBuilder defaultConf = FileUtil.streamToBuilder(defaultConfStream);
        StringBuilder currentConf = FileUtil.readFile(file.toPath());
        HashMap<String, Set<String>> currentComments = new HashMap<>();

        runTask(defaultConf, SAVE_COMMENTS, defaultComments);
        runTask(currentConf, SAVE_COMMENTS, currentComments);

        // Load the config for YAMLConfiguration methods
        try {
            loadFromString(currentConf.toString());
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (fileExists) {
            // If comments from modified config do not match new ones, then save new version
            // Only matches ones from the MODIFIED config
            for (Map.Entry<String, Set<String>> comment : currentComments.entrySet()) {
                if (!comment.getValue().equals(defaultComments.get(comment.getKey()))) {
                    save();
                    break;
                }
            }
        }
    }

    /**
     * Adds the node above the relative node.
     *
     * @param node         the node to add
     * @param relativeNode the already-existing node/subnode to reference when adding
     */
    public void addNode(String node, String relativeNode) {
        addNodes.put(relativeNode, Collections.singleton(node));
    }

    /**
     * Removes a node. Works with both master nodes and subnodes.
     *
     * @param node the node to remove
     */
    public void deleteNode(String node) {
        set(node, null);
        //        deleteNodes.add(node);
    }

    /**
     * Adds the subnodes below the relative node and indents them.
     *
     * @param subnodes        the subnodes to add
     * @param relativeSubnode the already-existing node/subnode to reference when adding
     */
    public void addIndentedSubnodes(Set<String> subnodes, String relativeSubnode) {
        addIndentedSubnodes.put(relativeSubnode, subnodes);
    }

    /**
     * Adds the subnodes below the relative node.
     *
     * @param subnodes     the subnodes to add
     * @param relativeNode the already-existing node/subnode to reference when adding
     */
    public void addSubnodes(Set<String> subnodes, String relativeNode) {
        addSubnodes.put(relativeNode, subnodes);
    }

    /**
     * Loads the config from a String.
     *
     * @param data the String from which to load the config
     * @throws InvalidConfigurationException if the config parsed with the String is invalid
     */
    @Override public void loadFromString(String data) throws InvalidConfigurationException {
        Validate.notNull(data, "Contents cannot be null");

        Map<?, ?> input;
        try {
            input = yaml.load(data);
        } catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigurationException("Top level is not a Map.");
        }

        if (input != null) {
            convertMapsToSections(input, this);
        }
    }

    /**
     * Saves the config to a String.
     */
    @Override public String saveToString() {
        String config = yaml.dump(getValues(false));
        return config.equals(BLANK_CONFIG) ? "" : finalizeConfig(config);
    }

    private String finalizeConfig(String config) {
        StringBuilder configBuilder = new StringBuilder(config);

        runTask(configBuilder, WRITE_NODES, addNodes);
        addNodes.clear();
        runTask(configBuilder, WRITE_SUBNODES, addSubnodes);
        addSubnodes.clear();
        runTask(configBuilder, WRITE_INDENTED_SUBNODES, addIndentedSubnodes);
        addIndentedSubnodes.clear();
        runTask(configBuilder, WRITE_COMMENTS, defaultComments);

        return configBuilder.toString();
    }

    /**
     * Saves the config to a file.
     *
     * @param file the destination file
     * @throws IOException if the plugin cannot create the parent directories or write to the file
     */
    public void save(File file) throws IOException {
        if (file != null) {
            //noinspection UnstableApiUsage
            Files.createParentDirs(file);

            try (BufferedWriter out = java.nio.file.Files.newBufferedWriter(file.toPath(), StandardCharsets.UTF_8)) {
                out.write(saveToString());
            }
        }
    }

    @Override
    public void save(String file) throws IOException {
        save(new File(file));
    }

    /**
     * Saves the config to the file that was set on initialization.
     */
    public void save() {
        try {
            save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
