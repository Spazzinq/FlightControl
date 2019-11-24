/*
 * This file is part of FlightControl, which is licensed under the MIT License
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
import org.spazzinq.flightcontrol.utils.FileUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Actually saves the comments left in the config and updates them dynamically.
 */
public class CommentConf extends YamlConfiguration {
    private final Yaml yaml;

    private HashMap<String, String> defaultComments,
                                    newNodes = new HashMap<>();
    private HashMap<String, List<String>> newSubnodes = new HashMap<>();
    private Set<String> oldNodes = new HashSet<>();

    private CommentConf() {
        // From saveToString in YamlConfiguration
        DumperOptions yamlOptions = new DumperOptions();
        yamlOptions.setIndent(options().indent());
        yamlOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        Representer yamlRepresenter = new YamlRepresenter();
        yamlRepresenter.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        yaml = new Yaml(new YamlConstructor(), yamlRepresenter, yamlOptions);
    }

    /**
     *
     * @param file the location of the current modified config
     * @param defaultConf the InputStream to the new config
     * @throws IOException
     * @throws InvalidConfigurationException
     */
    public CommentConf(File file, InputStream defaultConf) throws IOException, InvalidConfigurationException {
        this();

        // TODO                         v check if readFile works, class documentation
        String currentConf = FileUtil.readFile(file.toPath());

        defaultComments = FileUtil.parseComments(FileUtil.isToString(defaultConf));
        HashMap<String, String> currentComments = FileUtil.parseComments(currentConf);

        loadFromString(currentConf);

        // If comments from modified config do not match new ones, then save new version
        // Only matches ones from the MODIFIED config
        currentComments.forEach((node, value) -> {
            if (defaultComments.get(node).equals(value)) {
                try {
                    save(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        defaultConf.close();
    }

    // TODO System to decide if node/subnode
    public void addNode(String relativeKey, String node) {
        newNodes.put(relativeKey, node);
    }
    public void addSubnode(String relativeKey, String node) {
        newSubnodes.put(relativeKey, Collections.singletonList(node));
    }
    public void addSubnodes(String relativeKey, List<String> node) {
        newSubnodes.put(relativeKey, node);
    }
    public void removeNode(String node) {
        oldNodes.add(node);
    }

    /**
     * Saves the config to a file.
     * @param file the destination file
     * @throws IOException if the plugin cannot create the parent directories or write to the file
     */
    public void save(File file) throws IOException {
        if (file != null) {
            //noinspection UnstableApiUsage
            Files.createParentDirs(file);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(saveToString());
            }
        }
    }

    /**
     * Loads the config from a String.
     * @param data the String from which to load the config
     * @throws InvalidConfigurationException if the config parsed with the String is invalid
     */
    @Override public void loadFromString(String data) throws InvalidConfigurationException {
        Validate.notNull(data, "Contents cannot be null");

        Map<?, ?> input;
        try {
            input = (Map<?, ?>) yaml.load(data);
        }
        catch (YAMLException e) {
            throw new InvalidConfigurationException(e);
        }
        catch (ClassCastException e) {
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
        StringBuilder configBuilder = new StringBuilder(yaml.dump(getValues(false)));

        FileUtil.removeNodes(configBuilder, oldNodes);
        FileUtil.insertNodes(configBuilder, newNodes);
        FileUtil.insertSubnodes(configBuilder, newSubnodes);
        FileUtil.insertComments(configBuilder, defaultComments);

        String config = configBuilder.toString();
        return config.equals(BLANK_CONFIG) ? "" : config;
    }
}
