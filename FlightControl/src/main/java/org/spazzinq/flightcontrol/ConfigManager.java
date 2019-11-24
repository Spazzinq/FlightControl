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

package org.spazzinq.flightcontrol;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.api.objects.Sound;
import org.spazzinq.flightcontrol.objects.Category;
import org.spazzinq.flightcontrol.objects.CommentConf;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ConfigManager {
    private FlightControl pl;
    private PluginManager pm;

    @Getter
    private CommentConf config;
    private File f;

    @Getter @Setter
    private boolean autoEnable, autoUpdate, support,
                    worldBL, regionBL, combatChecked,
                    ownTown, townyWar, ownLand, cancelFall, vanishBypass, trail,
                    byActionBar, everyEnable, useFacEnemyRange;
    @Getter @Setter private double facEnemyRange;
    @Getter @Setter private float flightSpeed;
    @Getter private String dFlight, eFlight, cFlight, nFlight, disableTrail, enableTrail;
    @Getter private String noPermission;
    @Getter @Setter private Sound eSound, dSound, cSound, nSound;
    @Getter private HashSet<String> worlds;
    @Getter private HashMap<String, List<String>> regions;
    @Getter private HashMap<String, Category> categories;

    ConfigManager(FlightControl pl) {
        this.pl = pl;
        pm = pl.getServer().getPluginManager();
        f = new File(pl.getDataFolder(), "config.yml");

        reloadConfig();
        updateConfig();
    }

    void reloadConfig() {
        pl.saveDefaultConfig();
        try {
            config = new CommentConf(f, pl.getResource("config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // booleans
        autoUpdate = config.getBoolean("auto_update");
        autoEnable = config.getBoolean("settings.auto_enable");
        worldBL = config.isList("worlds.disable");
        regionBL = config.isConfigurationSection("regions.disable");
        combatChecked = config.getBoolean("settings.disable_flight_in_combat");
        ownTown = config.getBoolean("towny.enable_own_town");
        townyWar = config.getBoolean("towny.disable_during_war");
        ownLand = config.getBoolean("lands.enable_own_land");
        cancelFall = config.getBoolean("settings.prevent_fall_damage");
        vanishBypass = config.getBoolean("settings.vanish_bypass");
        byActionBar = config.getBoolean("messages.actionbar");

        // ints
        int range = config.getInt("settings.disable_enemy_range");
        if (useFacEnemyRange = (range != -1)) facEnemyRange = range;

        // floats
        flightSpeed = pl.calcActualSpeed((float) config.getDouble("settings.flight_speed"));

        // Messages
        dFlight = config.getString("messages.flight.disable");
        dFlight = config.getString("messages.flight.disable");
        eFlight = config.getString("messages.flight.enable");
        cFlight = config.getString("messages.flight.can_enable");
        nFlight = config.getString("messages.flight.cannot_enable");
        disableTrail = config.getString("messages.trail.disable");
        enableTrail = config.getString("messages.trail.enable");
        noPermission = config.getString("messages.permission_denied");

        // Load other stuff that have separate methods
        loadWorlds();
        loadSounds();
        loadTrail();

        // Reassign it anyways because it'll cause an NPE
        regions = new HashMap<>();
        if (pm.isPluginEnabled("WorldGuard")) {
            loadRegions();
        }
        // FIXME Fac loading
        //if (pm.isPluginEnabled("Factions"))
        loadCategories();

        // Region permission registering
        for (World w : Bukkit.getWorlds()) {
            String name = w.getName();
            pl.defaultPerms(name);
            for (String rg : pl.worldGuard.getRegions(w)) {
                pl.defaultPerms(name + "." + rg);
            }
        }
    }

    private void updateConfig() {
        boolean modified = false;
        // 3
        if (!config.isConfigurationSection("towny")) {
            config.addNode("trail", "towny:");
            config.addSubnodes("towny", Arrays.asList("disable_during_war: false", "enable_own_town: false"));
            modified = true;
        }
        if (!config.isBoolean("sounds.every_enable")) {
            config.addSubnode("sounds", "every_enable: false");
            modified = true;
        }
        // 3.1
        if (!(config.isInt("settings.flight_speed") || config.isDouble("settings.flight_speed"))) {
            config.addSubnode("settings.command", "flight_speed: 1.0");
            modified = true;
        }
        if (!config.isInt("settings.disable_enemy_range")) {
            config.addSubnode("settings.vanish_bypass", "disable_enemy_range: -1");
            modified = true;
        }
        if (!config.isBoolean("auto_update")) {
            config.addNode("settings", "auto_update: true");
            modified = true;
        }
        // 3.3
        if (!config.isBoolean("settings.auto_enable")) {
            config.addSubnode("settings",  "auto_enable: "
                    + (config.getBoolean("settings.command") ? "false" : "true"));
            modified = true;
        }
        if (config.isBoolean("settings.command")) {
            config.removeNode("settings.command");
            modified = true;
        }
        // 3.5
        if (!config.isConfigurationSection("lands")) {
            config.addNode("trail", "lands:");
            config.addSubnode("lands", "enable_own_land: false");
            modified = true;
        }

        if (modified) save();
    }

    // LOAD SECTION
    private void loadWorlds() {
        worlds = new HashSet<>();
        ConfigurationSection worldsCS = load(config, "worlds");
        if (worldsCS != null) {
            List<String> type = worldsCS.getStringList(worldBL ? "disable" : "enable");
            if (type != null) {
                for (String w : type) {
                    if (Bukkit.getWorld(w) != null) {
                        worlds.add(w);
                    }
                }
            }
        }
    }

    private void loadRegions() {
        ConfigurationSection regionsCS = load(config, "regions");
        if (regionsCS != null) addRegions(regionsCS.getConfigurationSection(regionBL ? "disable" : "enable"));
    }

    private void loadCategories() {
        categories = new HashMap<>();
        ConfigurationSection facs = load(config, "factions");
        if (facs != null) for (String cName : facs.getKeys(false)) {
            // Register permission defaults
            if (pm.getPermission("flightcontrol.factions." + cName) == null)
                pm.addPermission(new Permission("flightcontrol.factions." + cName, PermissionDefault.FALSE));
            ConfigurationSection categorySect = load(facs, cName);
            if (categorySect != null) {
                String type = categorySect.isList("disable") ? "disable" : (categorySect.isList("enable") ? "enable" : null);
                if (type != null)
                    categories.put(cName, createCategory(categorySect.getStringList(type), "disable".equals(type)));
                else
                    pl.getLogger().warning("Factions category \"" + cName + "\" is invalid! (missing \"enable\"/\"disable\")");
            }
        }
    }

    private void loadTrail() {
        trail = config.getBoolean("trail.enabled");

        if (trail) {
            pl.particles.setParticle(config.getString("trail.particle"));
            pl.particles.setAmount(config.getInt("trail.amount"));
            String offset = config.getString("trail.rgb");
            if (offset != null && (offset = offset.replaceAll("\\s+", "")).split(",").length == 3) {
                String[] xyz = offset.split(",");
                pl.particles.setRBG(xyz[0].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[0]) : 0,
                        xyz[1].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[1]) : 0,
                        xyz[2].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[2]) : 0);
            }
        }
    }

    private void loadSounds() {
        everyEnable = config.getBoolean("sounds.every_enable");
        eSound = getSound("sounds.enable");
        dSound = getSound("sounds.disable");
        cSound = getSound("sounds.can_enable");
        nSound = getSound("sounds.cannot_enable");
    }

    // LOAD HELPER METHODS
    private ConfigurationSection load(ConfigurationSection c, String type) {
        if (c.isConfigurationSection(type)) {
            ConfigurationSection typeS = c.getConfigurationSection(type);
            Set<String> typeKeys = new HashSet<>();
            for (String key : typeS.getKeys(true)) {
                String[] keyParts = key.split("\\.");
                // Get last part of key
                if (key.contains(".")) key = keyParts[keyParts.length - 1];
                typeKeys.add(key);
            }
            if (!typeKeys.isEmpty() && (typeKeys.contains("enable") || typeKeys.contains("disable"))) return typeS;
        }
        return null;
    }

    void loadWorld(World world) {
        String wName = world.getName();

        pl.defaultPerms(wName);
        for (String rg : pl.worldGuard.getRegions(world)) {
            pl.defaultPerms(wName + "." + rg);
        }

        ConfigurationSection worldsCS = load(pl.configManager.config,"worlds");
        if (worldsCS != null) {
            List<String> type = worldsCS.getStringList(pl.configManager.worldBL ? "disable" : "enable");
            if (type != null && type.contains(wName)) {
                pl.configManager.worlds.add(wName);
            }
        }

        ConfigurationSection regionsCS = load(pl.configManager.config,"regions");
        if (regionsCS != null) {
            ConfigurationSection dE = regionsCS.getConfigurationSection(pl.configManager.regionBL ? "disable" : "enable");
            if (dE.isList(wName)) {
                ArrayList<String> rgs = new ArrayList<>();
                for (String rg : dE.getStringList(wName)) {
                    if (pl.worldGuard.hasRegion(wName, rg)) {
                        rgs.add(rg);
                    }
                }
                pl.configManager.regions.put(wName, rgs);
            }
        }
    }

    private Sound getSound(String key) {
        if (config.isConfigurationSection(key)) {
            String s = config.getString(key + ".sound").toUpperCase().replaceAll("\\.", "_");
            if (Sound.is(s)) {
                return new Sound(s, (float) config.getDouble(key + ".volume"), (float) config.getDouble(key + ".pitch"));
            }
        }
        return null;
    }

    private void addRegions(ConfigurationSection c) {
        if (c != null) for (String w : c.getKeys(false)) {
            if (Bukkit.getWorld(w) != null && c.isList(w)) {
                ArrayList<String> rgs = new ArrayList<>();
                for (String rg : c.getStringList(w)) if (pl.worldGuard.hasRegion(w, rg)) rgs.add(rg);
                regions.put(w, rgs);
            }
        }
    }

    private Category createCategory(List<String> types, boolean blacklist) {
        if (types != null && !types.isEmpty() && (types.contains("OWN") || types.contains("ALLY") || types.contains("TRUCE") || types.contains("NEUTRAL") || types.contains("ENEMY") || types.contains("WARZONE") || types.contains("SAFEZONE") || types.contains("WILDERNESS"))) {
            return new Category(blacklist, types.contains("OWN"), types.contains("ALLY"), types.contains("TRUCE"), types.contains("NEUTRAL"), types.contains("ENEMY"), types.contains("WARZONE"), types.contains("SAFEZONE"), types.contains("WILDERNESS"));
        }
        return null;
    }

    // FILE CONFIG METHODS
    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }
    private void save() {
        try {
            config.save(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}