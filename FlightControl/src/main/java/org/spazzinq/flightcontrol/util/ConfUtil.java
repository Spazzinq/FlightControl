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

package org.spazzinq.flightcontrol.util;

import org.spazzinq.flightcontrol.object.ConfTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import static org.spazzinq.flightcontrol.object.ConfTask.*;

public final class ConfUtil {
    @SuppressWarnings("unchecked")
    public static void runTask(StringBuilder config, Object list, ConfTask task) {
        // Split into lines
        String[] lines = config.toString().split(System.lineSeparator());
        int previousIndentLength = 0;
        // Current actual node
        String node = "";

        // Position through config
        int pos = 0;
        int separatorLength = System.lineSeparator().length();
        // Current comment if SAVE_COMMENTS
        StringBuilder currentComment = task == SAVE_COMMENTS ? new StringBuilder() : null;
        // Where to start if DELETE_NODES
        int deleteStart = -1;
        // Node to delete if DELETE_NODES
        String deletingNode = null;

        for (String line : lines) {
            // Remove leading spaces
            String trimmedLine = line.trim();
            // Simple node (no hierarchy)
            String simpleNode;
            // Current indent
            String newIndent = leadingSpaces(line);
            int newIndentLength = newIndent.length();
            boolean endOfConfig = pos + line.length() == config.length();

            if ((trimmedLine.startsWith("#") || trimmedLine.isEmpty()) && task == SAVE_COMMENTS) {
                // Saves current line in comment
                currentComment.append(line).append(System.lineSeparator());

                if (endOfConfig) {
                    ((HashMap<String, Set<String>>) list).put("footer_ghost_node", Collections.singleton(currentComment.toString()));
                }
            } else if (trimmedLine.contains(":")) {
                // Substring off : to get simpleNode
                simpleNode = trimmedLine.substring(0, trimmedLine.indexOf(":"));

                // If newIndentLength decreases from previousIndentLength, then
                // substring off end (. + oldSimpleNode) until new node matches indent pattern
                if (previousIndentLength >= newIndentLength) {
                    int doubleIndentsBack = (previousIndentLength - newIndentLength) / 2;

                    for (int i = 0; i <= doubleIndentsBack; i++) {
                        node = node.contains(".") ? node.substring(0, node.lastIndexOf(".")) : "";
                    }
                }
                // Add the simpleNode on
                node += (node.isEmpty() ? "" : ".") + simpleNode;
                // Set previousIndentLength for next iteration
                previousIndentLength = newIndentLength;

                if (task == SAVE_COMMENTS) {
                    // Once the node changes, then add the comment to HashMap and reinitialize
                    if (currentComment.length() != 0) {
                        ((HashMap<String, Set<String>>) list).put(node, Collections.singleton(currentComment.toString()));
                        currentComment = new StringBuilder();
                    }
                }

                // Any WRITE task
                if (task.toString().contains("WRITE")) {
                    // Get as HashMap String and Set for simplicity (even though comments can be String and String)
                    if (((HashMap<String, Set<String>>) list).containsKey(node)) {
                        Set<String> insertingList = ((HashMap<String, Set<String>>) list).get(node);

                        for (String insert : insertingList) {
                            switch (task) {
                                case WRITE_INDENTED_SUBNODES:
                                    // Add new indent!
                                    insert = "  " + insert;
                                    // fall through
                                case WRITE_SUBNODES:
                                    // Add original indent!
                                    insert = newIndent + insert;
                                    // fall through
                                case WRITE_NODES:
                                    // New line!
                                    insert = System.lineSeparator() + insert;
                                    // fall through
                                default:
                                    break;
                            }
                            // Couldn't really add this in the flow of the case-switch, so it's here
                            if (task == WRITE_NODES) {
                                // Add new line for space since it's a new node
                                insert += System.lineSeparator();
                            }

                            // Insert before unless WRITE_INDENTED_SUBNODES or WRITE_SUBNODES (if those, then insert after)
                            config.insert(pos + (task == WRITE_INDENTED_SUBNODES || task == WRITE_SUBNODES ? line.length() : 0), insert);
                            // Account for insertion in pos
                            pos += insert.length();
                        }
                    }
                }

                if (task == DELETE_NODES) {
                    // '' && (ensure the node isn't a part of a later node before deleting || '')
                    if (deletingNode != null && (!node.matches("(\\S+\\.)*" + deletingNode + "(\\.\\S+)*") || endOfConfig)) {
                        int deleteEnd = pos + (endOfConfig ? line.length() : 0);

                        deletingNode = null;
                        // Account for deleted section in pos
                        pos -= deleteEnd - deleteStart;

                        config.delete(deleteStart, deleteEnd);
                    }
                    if (((Set<String>) list).contains(node)) {
                        // Set deletingNode and delete start (on next iteration, the code will first check if deletingNode exists)
                        deletingNode = node;
                        deleteStart = pos;
                    }
                }
            }
            // Include split character (System.lineSeparator())
            pos += line.length() + separatorLength;
        }
        if (task == WRITE_COMMENTS) {
            config.append(System.lineSeparator())
                  .append(((HashMap<String, Set<String>>) list).get("footer_ghost_node").iterator().next());
        }
    }

    private static String leadingSpaces(String string) {
        // Remove all non-whitespace that may be followed by whitespace
        return string.replaceAll("(\\S+)(\\s+)?", "");
    }
}
