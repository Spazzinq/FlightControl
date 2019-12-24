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

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.object.DualStore;
import org.spazzinq.flightcontrol.object.Region;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class CategoryManager {
    private FlightControl pl;
    private PluginManager pm;

    @Getter private CommentConf conf;
    @Getter private File categoryFile;
    private HashSet<Category> categories = new HashSet<>();
    @Getter private Category global;

    public CategoryManager(FlightControl pl) {
        this.pl = pl;
        pm = pl.getServer().getPluginManager();
        categoryFile = new File(pl.getDataFolder(), "categories.yml");

        try {
            conf = new CommentConf(categoryFile, pl.getResource("categories.yml"));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void reloadCategories() {
        global = null;
        categories.clear();

        if (conf != null) {
            global = loadCategory("global", conf.getConfigurationSection("global"));

            ConfigurationSection categoriesSection = conf.getConfigurationSection("categories");
            for (String categoryName : categoriesSection.getKeys(false)) {
                Category category = loadCategory(categoryName, categoriesSection.getConfigurationSection(categoryName));
                categories.add(category);
            }
        }
    }

    private Category loadCategory(String name, ConfigurationSection category) {
        // Prevent permission auto-granting from "*" permission
        if (pm.getPermission("flightcontrol.category." + name) == null)
            pm.addPermission(new Permission("flightcontrol.category." + name, PermissionDefault.FALSE));
        // TODO Remove - legacy permission
        if (pm.getPermission("flightcontrol.factions." + name) == null)
            pm.addPermission(new Permission("flightcontrol.factions." + name, PermissionDefault.FALSE));

        DualStore<World> worlds = loadWorlds(name, category.getConfigurationSection("worlds"));
        DualStore<Region> regions = loadRegions(name, category.getConfigurationSection("regions"));
        DualStore<FactionRelation> factions = loadFactions(name, category.getConfigurationSection("factions"));

        int priority = name.equals("global") ? -1 : category.getInt("priority");

        return new Category(name, worlds, regions, factions, priority);
    }

    private DualStore<World> loadWorlds(String categoryName, ConfigurationSection worldsSection) {
        if (worldsSection == null) {
            return null;
        }
        DualStore<World> worlds = new DualStore<>();

        for (String worldName : worldsSection.getKeys(false)) {
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                nonexistent(categoryName, "worlds", "world", worldName);
            } else {
                // If true, then enabled
                //    false, then disabled
                if (worldsSection.getBoolean(worldName)) {
                    worlds.addEnabled(world);
                } else {
                    worlds.addDisabled(world);
                }
            }
        }
        return worlds;
    }

    private DualStore<Region> loadRegions(String categoryName, ConfigurationSection regionsSection) {
        if (regionsSection == null) {
            return null;
        }
        DualStore<Region> regions = new DualStore<>();

        // RegionID is formatted like WORLDNAME+REGIONNAME
        for (String regionID : regionsSection.getKeys(false)) {
            String[] regionData = regionID.split("\\+");
            String worldName = regionData[0],
                   regionName = regionData[1];
            World world = Bukkit.getWorld(regionData[0]);
            if (world == null) {
                nonexistent(categoryName, "regions", "world", worldName);
            } else if (!pl.getHookManager().getWorldGuard().hasRegion(worldName, regionName)) {
                nonexistent(categoryName, "regions", "region", regionName);
            } else {
                Region region = new Region(world, regionName);
                // If true, then enabled
                //    false, then disabled
                if (regionsSection.getBoolean(regionID)) {
                    regions.addEnabled(region);
                } else {
                    regions.addDisabled(region);
                }
            }
        }
        return regions;
    }

    private DualStore<FactionRelation> loadFactions(String categoryName, ConfigurationSection factionsSection) {
        if (factionsSection == null) {
            return null;
        }
        DualStore<FactionRelation> factions = new DualStore<>();

        for (String relationName : factionsSection.getStringList("enable")) {
            FactionRelation relation = FactionRelation.getRelation(relationName);
            if (relation == null) {
                nonexistent(categoryName, "factions", "faction relation", relationName);
            } else {
                factions.addEnabled(relation);
            }
        }
        for (String relationName : factionsSection.getStringList("disable")) {
            FactionRelation relation = FactionRelation.getRelation(relationName);
            if (relation == null) {
                nonexistent(categoryName, "factions", "faction relation", relationName);
            } else {
                factions.addDisabled(relation);
            }
        }
        return factions;
    }

    private void nonexistent(String category, String section, String type, String error) {
        // Ignore examples
        if (!"WORLDNAME".contains(error)) {
            pl.getLogger().warning("Category \"" + category + "\" in section \"" + section + "\" contains non-existent " + type + " \"" + error + "\"");
        }
    }

    // TODO Cached category grabbing
    public Category getCategory(Player p) {
        List<Category> categories = new ArrayList<>();

        for (Category c : getCategories()) {
            if (p.hasPermission("flightcontrol.category." + c.getName())) {
                categories.add(c);
            }
        }
        // Locate the highest priority category
        Collections.sort(categories);
        return categories.isEmpty() ? pl.getCategoryManager().getGlobal()
                : categories.get(0);
    }

    public HashSet<Category> getCategories() {
        return categories;
    }
}
