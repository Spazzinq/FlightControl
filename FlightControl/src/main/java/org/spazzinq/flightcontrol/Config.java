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

package org.spazzinq.flightcontrol;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.objects.Category;
import org.spazzinq.flightcontrol.objects.CommentedConfig;
import org.spazzinq.flightcontrol.objects.Sound;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Config {
    private FlightControl pl;
    private CommentedConfig configData;
    private PluginManager pm;

    private File f;
    private File trailFile;
    private FileConfiguration trailConfig;

    public boolean command, autoEnable;
    boolean autoUpdate, support, worldBL, regionBL,
            useCombat, ownTown, townyWar, cancelFall,
            vanishBypass, trail, actionBar, everyEnable,
            useFacEnemyRange;
    double facEnemyRange;
    float flightSpeed;
    String dFlight, eFlight, cFlight, nFlight,
           dTrail, eTrail;
    public String noPerm;
    Sound eSound, dSound, cSound, nSound;
    HashSet<String> worlds, trailPrefs;
    HashMap<String, List<String>> regions;
    HashMap<String, Category> categories;

    Config(FlightControl pl) {
        this.pl = pl;
        pm = pl.getServer().getPluginManager();
        trailFile = new File(pl.getDataFolder(), "disabled_trail.yml");
        f = new File(pl.getDataFolder(), "config.yml");

        reloadConfig();
        updateConfig();
    }

    void reloadConfig() {
        pl.saveDefaultConfig();
        try {
            configData = new CommentedConfig(f, pl.getResource("config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // booleans
        autoUpdate = configData.getBoolean("auto_update");
        autoEnable = configData.getBoolean("settings.auto_enable");
        command = configData.getBoolean("settings.command");
        worldBL = configData.isList("worlds.disable");
        regionBL = configData.isConfigurationSection("regions.disable");
        useCombat = configData.getBoolean("settings.disable_flight_in_combat");
        ownTown = configData.getBoolean("towny.enable_own_town");
        townyWar = configData.getBoolean("towny.disable_during_war");
        cancelFall = configData.getBoolean("settings.prevent_fall_damage");
        vanishBypass = configData.getBoolean("settings.vanish_bypass");
        actionBar = configData.getBoolean("messages.actionbar");

        // ints
        int range = configData.getInt("settings.disable_enemy_range");
        if (useFacEnemyRange = (range != -1)) facEnemyRange = range;

        // Messages
        dFlight = configData.getString("messages.flight.disable");
        dFlight = configData.getString("messages.flight.disable");
        eFlight = configData.getString("messages.flight.enable");
        cFlight = configData.getString("messages.flight.can_enable");
        nFlight = configData.getString("messages.flight.cannot_enable");
        dTrail = configData.getString("messages.trail.disable");
        eTrail = configData.getString("messages.trail.enable");
        noPerm = configData.getString("messages.permission_denied");

        // Load other stuff that have separate methods
        loadWorlds();
        loadSounds();
        loadTrail();
        loadTrailPrefs();
        loadFlightSpeed();

        if (pm.isPluginEnabled("WorldGuard")) {
            regions = new HashMap<>();
            loadRegions();
        }
        if (pm.isPluginEnabled("Factions")) loadCategories();

        // Region permission registering
        for (World w : Bukkit.getWorlds()) {
            String name = w.getName();
            pl.defaultPerms(name);
            for (String rg : pl.regions.regions(w)) pl.defaultPerms(name + "." + rg);
        }

        // Set for players already online
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isFlying()) pl.trail.trailCheck(p);
            p.setFlySpeed(flightSpeed);
        }
    }

    private void updateConfig() {
        boolean modified = false;
        // 3
        if (!configData.isConfigurationSection("towny")) {
            configData.addNode("trail", "towny:");
            configData.addSubnodes("towny", Arrays.asList("disable_during_war: false", "enable_own_town: false"));
            modified = true;
        }
        if (!configData.isBoolean("sounds.every_enable")) {
            configData.addSubnode("sounds", "every_enable: false");
            modified = true;
        }
        // 3.1
        if (!(configData.isInt("settings.flight_speed") || configData.isDouble("settings.flight_speed"))) {
            configData.addSubnode("settings.command", "flight_speed: 1.0");
            modified = true;
        }
        if (!configData.isInt("settings.disable_enemy_range")) {
            configData.addSubnode("settings.vanish_bypass", "disable_enemy_range: -1");
            modified = true;
        }
        // 3.3
        if (!configData.isBoolean("settings.auto_enable")) {
            configData.addSubnode("settings",  "auto_enable: " + (command ? "false" : "true"));
            modified = true;
        }
        if (modified) save();
    }

    // LOAD SECTION
    private void loadWorlds() {
        worlds = new HashSet<>();
        ConfigurationSection worldsCS = load(configData, "worlds");
        if (worldsCS != null) {
            List<String> type = worldsCS.getStringList(worldBL ? "disable" : "enable");
            if (type != null) for (String w : type) if (Bukkit.getWorld(w) != null) worlds.add(w);
        }
    }

    private void loadRegions() {
        ConfigurationSection regionsCS = load(configData, "regions");
        if (regionsCS != null) addRegions(regionsCS.getConfigurationSection(regionBL ? "disable" : "enable"));
    }

    private void loadCategories() {
        categories = new HashMap<>();
        ConfigurationSection facs = load(configData, "factions");
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
        trail = configData.getBoolean("trail.enabled");
        if (trail) {
            pl.particles.setParticle(configData.getString("trail.particle"));
            pl.particles.setAmount(configData.getInt("trail.amount"));
            String offset = configData.getString("trail.rgb");
            if (offset != null && (offset = offset.replaceAll("\\s+", "")).split(",").length == 3) {
                String[] xyz = offset.split(",");
                pl.particles.setRBG(xyz[0].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[0]) : 0,
                        xyz[1].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[1]) : 0,
                        xyz[2].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[2]) : 0);
            }
        }
    }

    // Per-player trail preferences
    private void loadTrailPrefs() {
        trailPrefs = new HashSet<>();
        if (!trailFile.exists()) {
            try { //noinspection ResultOfMethodCallIgnored
                trailFile.createNewFile();
            } catch (IOException e) { e.printStackTrace(); }
        }
        trailConfig = YamlConfiguration.loadConfiguration(trailFile);

        if (trailConfig.isList("disabled_trail")) {
            if (trailConfig.getStringList("disabled_trail") != null && !trailConfig.getStringList("disabled_trail").isEmpty()) {
                for (String uuid : trailConfig.getStringList("disabled_trail")) {
                    try {
                        if (pl.getServer().getPlayer(UUID.fromString(uuid)) != null || pl.getServer().getOfflinePlayer(UUID.fromString(uuid)) != null)
                            trailPrefs.add(uuid);
                    } catch (IllegalArgumentException ignored) { }
                }
            }
        }
        else trailConfig.createSection("disabled_trail");
    }

    private void loadSounds() {
        everyEnable = configData.getBoolean("sounds.every_enable");
        eSound = getSound("sounds.enable");
        dSound = getSound("sounds.disable");
        cSound = getSound("sounds.can_enable");
        nSound = getSound("sounds.cannot_enable");
    }

    private void loadFlightSpeed() {
        float wrongSpeed = (float) configData.getDouble("settings.flight_speed"),
              defaultSpeed = 0.1f,
              maxSpeed = 1f;

        if (wrongSpeed > 10f) wrongSpeed = 10f;
        else if (wrongSpeed < 0.0001f) wrongSpeed = 0.0001f;

        if (wrongSpeed < 1f) flightSpeed = defaultSpeed * wrongSpeed;
        else {
            float ratio = ((wrongSpeed - 1) / 9) * (maxSpeed - defaultSpeed);
            flightSpeed = ratio + defaultSpeed;
        }
    }

    // LOAD HELPER METHODS
    static ConfigurationSection load(ConfigurationSection c, String type) {
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

    private Sound getSound(String key) {
        if (configData.isConfigurationSection(key)) {
            String s = configData.getString(key + ".sound").toUpperCase().replaceAll("\\.", "_");
            if (Sound.is(s)) {
                return new Sound(s, (float) configData.getDouble(key + ".volume"), (float) configData.getDouble(key + ".pitch"));
            }
        }
        return null;
    }

    private void addRegions(ConfigurationSection c) {
        if (c != null) for (String w : c.getKeys(false)) {
            if (Bukkit.getWorld(w) != null && c.isList(w)) {
                ArrayList<String> rgs = new ArrayList<>();
                for (String rg : c.getStringList(w)) if (pl.regions.hasRegion(w, rg)) rgs.add(rg);
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
    void saveTrails() { // Saves personal trail preferences
        if (trailConfig != null && trailFile != null) {
            trailConfig.set("disabled_trail", !trailPrefs.isEmpty() ? trailPrefs : null);
            try { trailConfig.save(trailFile); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    void set(String path, Object value) {
        configData.set(path, value);
        save();
    }
    private void save() { try { configData.save(f); } catch (IOException e) { e.printStackTrace(); } }
}