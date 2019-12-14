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

package org.spazzinq.flightcontrol.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public final class ConfigUtil {
    public static void insertComments(StringBuilder config, HashMap<String, String> comments) {
        insertBefore(config, comments, false);
        config.insert(0, comments.getOrDefault("header", ""));
        config.append(comments.getOrDefault("footer", ""));
    }

    public static void insertNodes(StringBuilder config, HashMap<String, String> nodes) {
        insertBefore(config, nodes, true);
        nodes.clear();
    }

    // Insert AFTER node
    public static void insertSubnodes(StringBuilder config, HashMap<String, List<String>> subnodes, boolean indented) {
        String[] lines = config.toString().split("\n");
        String currentNode = "";
        int i = 0,
            depth = 0;

        for (String line : lines) {
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                String localNode = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line);
                int nDepth = spaces.length();

                if (depth >= nDepth) {
                    int back = (depth - nDepth) / 2 + 1;
                    for (int j = 0; j < back; j++) if (!currentNode.isEmpty()) currentNode = currentNode.contains(".") ? currentNode.substring(0, currentNode.lastIndexOf(".")) : "";
                }
                currentNode = currentNode.concat((currentNode.isEmpty() ? "" : ".") + localNode);
                depth = nDepth;

                int insertLength = 0;
                if (subnodes.containsKey(currentNode)) {
                    // Get all sections for node
                    for (String section : subnodes.get(currentNode)) {
                        String k = "\n" + spaces
                                // If the node is a main node or indented, indent
                                + (currentNode.contains(".") && !indented ? "" : "  ") + section.replaceAll("\n", "\n" + spaces + (currentNode.contains(".") ? "" : "  "));
                        k = k.substring(0, k.length() - depth + (currentNode.contains(".") ? 2 : 0));
                        // Insert AFTER
                        config.insert(config.indexOf(line, i) + line.length(), k);
                        insertLength += k.length();
                    }
                }
                i += insertLength;
            }
            // Include the newline that was split
            i += line.length() + 1;
        }
        subnodes.clear();
    }

    // Works for both nodes and subnodes
    public static void removeNodes(StringBuilder config, Set<String> oldNodes) {
        String[] lines = config.toString().split("\n");
        String node = "",
               target = "";
        Integer start = null;
        int end = 0,
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

                if (oldNodes.contains(node) && start == null) {
                    start = end;
                    target = node;
                }
            }
            // Allow it to take one of the empty lines
            end += line.length() + 1;

            //               If subnode is over                         If whole node is over
            if (((oldNodes.contains(node) && node.contains(".")) || !node.contains(target)) && start != null) {
                //                                                    Don't remove next whole node
                config.delete(start, end - (!node.contains(target) ? line.length() + 1 : 0));
                start = null;
            }
        }
        oldNodes.clear();
    }

    // WARNING: This does NOT work for inlined comments (eg. test: # This is an inlined comment)
    public static HashMap<String, String> parseComments(String config) {
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

                if (!c.isEmpty()) {
                    comments.put(node, c); c = "";
                }
            }
        }
        //                                                          Remove \n
        if (!c.isEmpty()) comments.put("footer", c.substring(0, c.length() - 1));
        return comments;
    }

    // Insert BEFORE node
    // Comments should already have "#" at the beginning
    private static void insertBefore(StringBuilder config, HashMap<String, String> nodes, boolean lineSeparator) {
        String[] lines = config.toString().split("\n");
        String node = "";
        int i = 0,
            depth = 0;

        for (String line : lines) {
            // Is it a node?
            if (!line.contains("#") && (line.contains(": ") || line.endsWith(":"))) {
                // TODO Should probably substring after finding char location not split
                String localNode = line.replaceAll("\\s+", "").split(":")[0];
                String spaces = leadSpaces(line);
                int nDepth = spaces.length();

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

                if (nodes.containsKey(node)) {
                    // Add the spaces from the depth of the node
                    String insert = spaces + nodes.get(node).replaceAll("\n", "\n" + spaces);
                    // Remove spaces at the end from above replaceAll
                    insert = insert.substring(0, insert.length() - depth) + (lineSeparator ? "\n" : "");
                    // Insert into config
                    config.insert(config.indexOf(line, i), insert);
                    // Set location to continue from
                    i += insert.length();
                }
            }
            // Include the newline that was split
            i += line.length() + 1;
        }
    }

    private static String leadSpaces(String s) {
        return s.startsWith(" ") ? s.split("[^\\s]")[0] : "";
    }

    private static int countBlankLines(String data) {
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
