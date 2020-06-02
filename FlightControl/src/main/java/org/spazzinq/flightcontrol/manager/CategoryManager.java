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

import com.google.common.io.Files;
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
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.check.category.CategoryRegionCheck;
import org.spazzinq.flightcontrol.check.category.CategoryRelationCheck;
import org.spazzinq.flightcontrol.check.category.CategoryWorldCheck;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;
import org.spazzinq.flightcontrol.multiversion.FactionRelation;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.object.DualStore;
import org.spazzinq.flightcontrol.util.PlayerUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

public class CategoryManager {
    private final FlightControl pl;
    private final PluginManager pm;

    @Getter private CommentConf conf;
    @Getter private final File categoryFile;
    @Getter private final ArrayList<Category> categories = new ArrayList<>();
    @Getter private Category global;

    public CategoryManager(FlightControl pl) {
        this.pl = pl;
        pm = pl.getServer().getPluginManager();
        categoryFile = new File(pl.getDataFolder(), "categories.yml");
    }

    public void loadCategories() {
        conf = new CommentConf(categoryFile, pl.getResource("categories.yml"));

        global = null;
        categories.clear();

        // Remove the old global territory
        if (!conf.isConfigurationSection("global.territory")) {
            migrateFromVersion4();
        }

        global = loadCategory("global", conf.getConfigurationSection("global"));
        ConfigurationSection categoriesSection = conf.getConfigurationSection("categories");

        for (String categoryName : categoriesSection.getKeys(false)) {
            categories.add(
                    loadCategory(categoryName.toLowerCase(), categoriesSection.getConfigurationSection(categoryName))
            );
        }

        Collections.sort(categories);
    }

    private Category loadCategory(String name, ConfigurationSection category) {
        // Prevent permission auto-granting from "*" permission
        if (pm.getPermission("flightcontrol.category." + name) == null) {
            pm.addPermission(new Permission("flightcontrol.category." + name, PermissionDefault.FALSE));
        }

        DualStore<Check> checks = new DualStore<>();

        DualStore<World> worlds = loadWorlds(name, category.getConfigurationSection("worlds"), checks);
        DualStore<Region> regions = loadRegions(name, category.getConfigurationSection("regions"), checks);
        DualStore<FactionRelation> factions = loadFactions(name, category.getConfigurationSection("factions"), checks);

        DualStore<Check> ownTerritories = loadTerritoryTypes(name, category.getConfigurationSection("territory"), checks, "own");
        DualStore<Check> trustedTerritories = loadTerritoryTypes(name, category.getConfigurationSection("territory"), checks, "trusted");

        int priority = "global".equals(name) ? -1 : category.getInt("priority");

        return new Category(name, checks, worlds, regions, factions, ownTerritories, trustedTerritories, priority);
    }

    private DualStore<World> loadWorlds(String categoryName, ConfigurationSection worldsSection, DualStore<Check> checks) {
        DualStore<World> worlds = new DualStore<>();

        if (worldsSection != null) {
            List<String> enable = worldsSection.getStringList("enable");
            List<String> disable = worldsSection.getStringList("disable");

            if (enable != null) {
               for (String worldName : enable) {
                   World world = Bukkit.getWorld(worldName);

                   if (world != null) {
                       worlds.addEnabled(world);
                   } else {
                       nonexistent(categoryName, "worlds", "enabled world", worldName);
                   }
               }
            }
            if (disable != null) {
                for (String worldName : disable) {
                    World world = Bukkit.getWorld(worldName);

                    if (world != null) {
                        worlds.addDisabled(world);
                    } else {
                        nonexistent(categoryName, "worlds", "disabled world", worldName);
                    }
                }
            }

            if (!worlds.isEnabledEmpty()) {
                checks.addEnabled(new CategoryWorldCheck(worlds.getEnabled()));
            }
            if (!worlds.isDisabledEmpty()) {
                checks.addDisabled(new CategoryWorldCheck(worlds.getDisabled()));
            }
        }

        return worlds;
    }

    private DualStore<Region> loadRegions(String categoryName, ConfigurationSection regionsSection,
                                          DualStore<Check> checks) {
        DualStore<Region> regions = new DualStore<>();

        if (regionsSection != null) {
            ConfigurationSection enable = regionsSection.getConfigurationSection("enable");
            ConfigurationSection disable = regionsSection.getConfigurationSection("disable");

            if (enable != null) {
                for (String worldName : enable.getKeys(false)) {
                    World world = Bukkit.getWorld(worldName);

                    if (world != null) {
                        for (String regionName : enable.getStringList(worldName)) {
                            regions.addEnabled(new Region(world, regionName));
                        }
                    } else {
                        nonexistent(categoryName, "regions", "enabled world", worldName);
                    }
                }
            }
            if (disable != null) {
                for (String worldName : disable.getKeys(false)) {
                    if (Bukkit.getWorld(worldName) != null) {
                        for (String regionName : disable.getStringList(worldName)) {
                            regions.addDisabled(new Region(Bukkit.getWorld(worldName), regionName));
                        }
                    } else {
                        nonexistent(categoryName, "regions", "disabled world", worldName);
                    }
                }
            }

            if (!regions.isEnabledEmpty()) {
                checks.addEnabled(new CategoryRegionCheck(pl.getHookManager().getWorldGuardHook(), regions.getEnabled()));
            }
            if (!regions.isDisabledEmpty()) {
                checks.addDisabled(new CategoryRegionCheck(pl.getHookManager().getWorldGuardHook(), regions.getDisabled()));
            }
        }

        return regions;
    }

    private DualStore<FactionRelation> loadFactions(String categoryName, ConfigurationSection factionsSection,
                                                    DualStore<Check> checks) {
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

            if (!factions.isEnabledEmpty()) {
                checks.addEnabled(new CategoryRelationCheck(pl.getFactionsManager(), factions.getEnabled()));
            }
            if (!factions.isDisabledEmpty()) {
                checks.addDisabled(new CategoryRelationCheck(pl.getFactionsManager(), factions.getDisabled()));
            }
        }
        return factions;
    }

    private DualStore<Check> loadTerritoryTypes(String categoryName, ConfigurationSection territorySection, DualStore<Check> checks, String type) {
        DualStore<Check> territories = new DualStore<>();
        TreeMap<String, TerritoryCheck> territoryChecks = "own".equals(type) ? pl.getCheckManager().getOwnTerritoryChecks() : pl.getCheckManager().getTrustedTerritoryChecks();

        if (territorySection != null) {
            ConfigurationSection enable = territorySection.getConfigurationSection("enable");
            ConfigurationSection disable = territorySection.getConfigurationSection("disable");

            if (enable != null && enable.isList(type)) {
                for (String territory : enable.getStringList(type)) {
                    Check check = territoryChecks.get(territory);
                    if (check != null) {
                        territories.addEnabled(check);
                    } else {
                        nonexistent(categoryName, "territory", "enabled check", territory, "Is the plugin supported and installed on the server?");
                    }
                }
            }

            if (disable != null && disable.isList(type)) {
                for (String territory : disable.getStringList(type)) {
                    Check check = territoryChecks.get(territory);
                    if (check != null) {
                        territories.addDisabled(territoryChecks.get(territory));
                    } else {
                        nonexistent(categoryName, "territory", "disabled check", territory, "Is the plugin supported and installed on the server?");
                    }
                }
            }
        }

        checks.addEnabled(territories.getEnabled());
        checks.addDisabled(territories.getDisabled());

        return territories;
    }

    private void nonexistent(String category, String section, String type, String error) {
        nonexistent(category, section, type, error, "");
    }

    private void nonexistent(String category, String section, String type, String error, String extra) {
        // Ignore examples
        if (!error.contains("WORLDNAME")) {
            pl.getLogger().warning("Category \"" + category + "\" in section \"" + section + "\" contains " +
                    "non-existent " + type + " \"" + error + ".\" " + extra);
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

    private void migrateFromVersion4() {
        pl.getLogger().severe("The categories.yml was updated to a new format, and FlightControl could not migrate the data. Please reconfigure your categories.yml!");
        try {
            //noinspection UnstableApiUsage
            Files.move(categoryFile, new File(pl.getDataFolder(), "categories_old.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadCategories();
    }
}
