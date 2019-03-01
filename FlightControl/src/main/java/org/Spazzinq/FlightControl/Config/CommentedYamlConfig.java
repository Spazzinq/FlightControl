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
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

class CommentedYamlConfig extends YamlConfiguration {
    private HashMap<String, String> comments;

    CommentedYamlConfig() { comments = new HashMap<>(); }

    public void save(String file) { save(new File(file)); }

    public void save(File file) {
        if (file != null) {
            try {
                Files.createParentDirs(file);
                String data = insertComments(saveToString());
                FileWriter writer = new FileWriter(file);
                writer.write(data);
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void addComment(String path, String stringLines) {
        String[] commentLines = stringLines.split("\n");
        StringBuilder comment = new StringBuilder();
        String spaces = "";
        for (int i = 0; i < path.length(); i++) if (path.charAt(i) == '.') spaces = spaces.concat("  ");
        for (String line : commentLines) {
            if (!line.isEmpty()) line = spaces + line;
            if (comment.length() > 0) comment.append(System.getProperty("line.separator"));
            comment.append(line);
        }
        comments.put(path, comment.toString());
    }

    private String insertComments(String yaml) {
        if (!comments.isEmpty()) {
            String[] lines = yaml.split("[" + System.getProperty("line.separator") + "]");
            // This holds the current path the lines are at in the config
            StringBuilder newConfig = new StringBuilder(), currentPath = new StringBuilder();
            // Flags if the line is a node or unknown text
            boolean alreadyCommented = false, node;
            // The depth of the path. (number of words separated by periods - 1)
            int depth = 0;

            // This will cause the first line to be ignored.
            boolean firstLine = true;
            // Loop through the config lines
            for (final String line : lines) {
                if (firstLine) {
                    firstLine = false;
                    if (line.startsWith("#")) continue;
                }
                // If the line is a node (and not something like a list value)
                if (line.contains(": ") || line.endsWith(":")) {
                    // This is a new node so we need to mark it for commenting (if there are comments)
                    alreadyCommented = false;
                    node = true;
                    int index = line.endsWith(":") ? line.length() - 1 : line.indexOf(": ");
                    if (currentPath.toString().isEmpty()) currentPath = new StringBuilder(line.substring(0, index));
                    else {
                        // Whitespace preceding the node
                        int whiteSpace = 0;
                        for (int n = 0; n < line.length(); n++) {
                            if (line.charAt(n) == ' ') whiteSpace++;
                            else break;
                        }
                        // Find out if the current depth (whitespace * 2) is greater/lesser/equal to the previous depth
                        if (whiteSpace / 2 > depth) {
                            // Path is deeper.  Add a . and the node name
                            currentPath.append(".").append(line, whiteSpace, index);
                            depth++;
                        }
                        else {
                            boolean shallower = whiteSpace / 2 < depth;
                            if (shallower) {
                                // Path is shallower, calculate current depth from whitespace (whitespace / 2) and subtract that many levels from the currentPath
                                int newDepth = whiteSpace / 2;
                                for (int i = 0; i < depth - newDepth; i++)
                                    currentPath.replace(currentPath.lastIndexOf("."), currentPath.length(), "");
                                depth = newDepth;
                            }

                            // Grab the index of the final period
                            int lastIndex = currentPath.lastIndexOf(".");
                            if (lastIndex == -1) {
                                // if there isn't a final period, set the current path to nothing because we're at root
                                currentPath = new StringBuilder();
                            }
                            else {
                                // If there is a final period, replace everything after it with nothing
                                currentPath.replace(currentPath.lastIndexOf("."), currentPath.length(), "").append(".");
                            }
                            // Add the new node name to the path
                            currentPath.append(line, whiteSpace, index);
                        }
                    }
                }
                else node = false;
                StringBuilder newLine = new StringBuilder(line);
                if (node && !alreadyCommented) {
                    // If there's a comment for the current path, retrieve it and flag that path as already commented
                    String comment = comments.get(currentPath.toString());
                    if (comment != null && !comment.isEmpty()) {
                        // Add the comment to the beginning of the current line
                        newLine.insert(0, System.getProperty("line.separator")).insert(0, comment);
                        alreadyCommented = true;
                    }
                }
                newLine.append(System.getProperty("line.separator"));
                // Add the (modified) line to the total config String
                newConfig.append(newLine.toString());
            }
            return newConfig.toString();
        }
        return yaml;
    }
}
