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

package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.objects.Region;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.object.DualStore;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class CategoryManager {
    private final FlightControl pl;
    private final PluginManager pm;

    @Getter private CommentConf conf;
    @Getter private final File categoryFile;
    private final ArrayList<Category> categories = new ArrayList<>();
    @Getter private Category global;

    public CategoryManager(FlightControl pl) {
        this.pl = pl;
        pm = pl.getServer().getPluginManager();
        categoryFile = new File(pl.getDataFolder(), "categories.yml");
    }

    public void reloadCategories() {
        conf = new CommentConf(categoryFile, pl.getResource("categories.yml"));

        global = null;
        categories.clear();

        global = loadCategory("global", conf.getConfigurationSection("global"));
        ConfigurationSection categoriesSection = conf.getConfigurationSection("categories");

        for (String categoryName : categoriesSection.getKeys(false)) {
            Category category = loadCategory(categoryName, categoriesSection.getConfigurationSection(categoryName));
            categories.add(category);
        }

        Collections.sort(categories);
    }

    private Category loadCategory(String name, ConfigurationSection category) {
        // Prevent permission auto-granting from "*" permission
        if (pm.getPermission("flightcontrol.category." + name) == null) {
            pm.addPermission(new Permission("flightcontrol.category." + name, PermissionDefault.FALSE));
        }

        DualStore<World> worlds = loadWorlds(name, category.getConfigurationSection("worlds"));
        DualStore<Region> regions = loadRegions(name, category.getConfigurationSection("regions"));
        DualStore<FactionRelation> factions = loadFactions(name, category.getConfigurationSection("factions"));

        int priority = "global".equals(name) ? -1 : category.getInt("priority");

        return new Category(name, worlds, regions, factions, priority);
    }

    private DualStore<World> loadWorlds(String categoryName, ConfigurationSection worldsSection) {
        DualStore<World> worlds = new DualStore<>();

        if (worldsSection != null) {
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
        }
        return worlds;
    }

    private DualStore<Region> loadRegions(String categoryName, ConfigurationSection regionsSection) {
        DualStore<Region> regions = new DualStore<>();

        if (regionsSection != null) {
            // RegionID is formatted like WORLDNAME+REGIONNAME
            for (String regionID : regionsSection.getKeys(false)) {
                String[] regionData = regionID.split("\\+");
                String worldName = regionData[0];
                Region region = new Region(Bukkit.getWorld(worldName), regionData[1]);

                if (region.getWorld() == null) {
                    nonexistent(categoryName, "regions", "world", worldName);
                } else if (!pl.getHookManager().getWorldGuardHook().hasRegion(region)) {
                    nonexistent(categoryName, "regions", "region", region.getRegionName());
                } else {
                    // If true, then enabled
                    //    false, then disabled
                    if (regionsSection.getBoolean(regionID)) {
                        regions.addEnabled(region);
                    } else {
                        regions.addDisabled(region);
                    }
                }
            }
        }
        return regions;
    }

    private DualStore<FactionRelation> loadFactions(String categoryName, ConfigurationSection factionsSection) {
        DualStore<FactionRelation> factions = new DualStore<>();

        if (factionsSection != null) {
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
        }
        return factions;
    }

    private void nonexistent(String category, String section, String type, String error) {
        // Ignore examples
        if (!error.contains("WORLDNAME")) {
            pl.getLogger().warning("Category \"" + category + "\" in section \"" + section + "\" contains " +
                    "non-existent " + type + " \"" + error + "\"");
        }
    }

    // TODO Cached category grabbing - use FlightPlayer?
    public Category getCategory(Player p) {
        for (Category category : getCategories()) {
            if (PlayerUtil.hasPermissionCategory(p, category)) {
                return category;
            }
        }

        return pl.getCategoryManager().getGlobal();
    }

    public ArrayList<Category> getCategories() {
        return categories;
    }
}
