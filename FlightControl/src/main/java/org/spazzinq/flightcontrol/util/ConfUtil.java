/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2020 Spazzinq
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

import org.jetbrains.annotations.NotNull;
import org.spazzinq.flightcontrol.object.ConfTask;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import static org.spazzinq.flightcontrol.object.ConfTask.*;

public final class ConfUtil {
    /*
     * //////////////////////////////////////////////////////////////////////////////
     * YOU MUST USE \n TO SEPARATE LINES BECAUSE IT WILL NOT BE RECOGNIZED OTHERWISE
     * \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
     * */
    private static final String NEW_LINE = "\n";

    @SuppressWarnings("unchecked")
    public static void runTask(StringBuilder config, ConfTask task, Object list) {
        // Position through config
        int pos = 0;
        // Indent length of the line before
        int previousIndentLength = 0;
        // Current actual node
        String node = "";

        // Split into lines
        String[] lines = config.toString().split(NEW_LINE);
        // Current comment if SAVE_COMMENTS
        StringBuilder currentComment = task == SAVE_COMMENTS ? new StringBuilder() : null;

        for (String line : lines) {
            // Remove leading spaces
            String trimmedLine = line.trim();
            // Current indent
            String newIndent = leadingSpaces(line);

            if ((trimmedLine.startsWith("#") || trimmedLine.isEmpty()) && task == SAVE_COMMENTS) {
                // Saves current line in comment
                currentComment.append(line).append(NEW_LINE);

                // If end of config
                if (config.length() == pos + line.length()) {
                    ((HashMap<String, Set<String>>) list).put("footer_ghost_node",
                            Collections.singleton(currentComment.toString()));
                }
            } else if (trimmedLine.contains(":")) {
                // Substring off : to get simpleNode
                String simpleNode = trimmedLine.substring(0, trimmedLine.indexOf(":"));

                // Update node
                node = updateNode(node, simpleNode, previousIndentLength, newIndent);
                // Set for next iteration
                previousIndentLength = newIndent.length();

                // Once the node changes, then add the comment to HashMap and reinitialize
                if (task == SAVE_COMMENTS && currentComment.length() != 0) {
                    ((HashMap<String, Set<String>>) list).put(node, Collections.singleton(currentComment.toString()));
                    currentComment = new StringBuilder();
                }

                // Any WRITE task
                // Get as HashMap String and Set for simplicity (even though comments can be String and String)
                if (task.toString().contains("WRITE") && ((HashMap<String, Set<String>>) list).containsKey(node)) {
                    Set<String> insertingList = ((HashMap<String, Set<String>>) list).get(node);

                    for (String insert : insertingList) {
                        switch (task) {
                            case WRITE_INDENTED_SUBNODES:
                                // Add new indent (double space)!
                                insert = "  " + insert;
                                // fall through
                            case WRITE_SUBNODES:
                                // Add original indent!
                                insert = newIndent + insert;
                                // fall through
                            case WRITE_NODES:
                                // New line!
                                insert = NEW_LINE + insert;
                                // fall through
                            default:
                        }
                        // Couldn't really add this in the flow of the case-switch, so it's here
                        if (task == WRITE_NODES) {
                            // Add new line for space since it's a new node
                            insert += NEW_LINE;
                        }

                        // Insert before unless WRITE_INDENTED_SUBNODES or WRITE_SUBNODES (if those, then insert after)
                        config.insert(pos + (task == WRITE_INDENTED_SUBNODES || task == WRITE_SUBNODES ?
                                line.length() : 0), insert);
                        // Account for insertion in pos
                        pos += insert.length();
                    }
                }
            }
            // Include split character (new line)
            pos += line.length() + NEW_LINE.length();
        }

        if (task == WRITE_COMMENTS) {
            Set<String> footerSet = ((HashMap<String, Set<String>>) list).get("footer_ghost_node");

            if (footerSet != null) {
                // Only should iterate once
                for (String footer : footerSet) {
                    config.append(footer, 0, footer.length() - NEW_LINE.length());
                }
            }
        }
    }

    @NotNull
    private static String updateNode(String node, String simpleNode, int previousIndentLength, String newIndent) {
        // If newIndentLength decreases from previousIndentLength, then
        // substring off end (. + oldSimpleNode) until new node matches indent pattern
        if (previousIndentLength >= newIndent.length()) {
            int doubleIndentsBack = (previousIndentLength - newIndent.length()) / 2;

            for (int i = 0; i <= doubleIndentsBack; i++) {
                node = node.contains(".") ? node.substring(0, node.lastIndexOf(".")) : "";
            }
        }

        // Add the simpleNode on
        node += (node.isEmpty() ? "" : ".") + simpleNode;

        return node;
    }

    private static String leadingSpaces(String string) {
        // Remove all non-whitespace that may be followed by whitespace
        return string.replaceAll("(\\S+)(\\s+)?", "");
    }
}
