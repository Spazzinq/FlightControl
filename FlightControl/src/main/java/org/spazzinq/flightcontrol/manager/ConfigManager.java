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
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.objects.Sound;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.util.MathUtil;

import java.io.File;
import java.util.*;

public final class ConfigManager {
    private FlightControl pl;

    private File confFile;
    @Getter private CommentConf conf;

    @Getter @Setter
    private boolean autoEnable, autoUpdate, support,
                    combatChecked, ownTown, townyWar, ownLand,
                    cancelFall, vanishBypass, trail,
                    byActionBar, everyEnable, everyDisable,
                    useFacEnemyRange;
    @Getter @Setter private double facEnemyRange;
    @Getter @Setter private float defaultFlightSpeed;
    @Getter private String dFlight, eFlight, cFlight, nFlight, disableTrail, enableTrail;
    @Getter private String noPermission;
    @Getter @Setter private Sound eSound, dSound, cSound, nSound;

    public ConfigManager(FlightControl pl) {
        this.pl = pl;
        confFile = new File(pl.getDataFolder(), "config.yml");
    }

    public void reloadConfig() {
        try {
            conf = new CommentConf(confFile, pl.getResource("config.yml"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (conf.isConfigurationSection("auto_update")) {
            migrateFromVersion3();
        }

        // booleans
        autoUpdate = conf.getBoolean("settings.auto_update");
        autoEnable = conf.getBoolean("settings.auto_enable_flight");
        combatChecked = conf.getBoolean("settings.disable_flight_in_combat");
        cancelFall = conf.getBoolean("settings.prevent_fall_damage");
        vanishBypass = conf.getBoolean("settings.vanish_bypass");

        byActionBar = conf.getBoolean("messages.actionbar");

        ownTown = conf.getBoolean("towny.enable_own_town");
        townyWar = conf.getBoolean("towny.negate_during_war");
        ownLand = conf.getBoolean("lands.enable_own_land");

        // ints
        int range = conf.getInt("factions.disable_enemy_range");
        if (useFacEnemyRange = (range != -1)) facEnemyRange = range;

        // floats
        defaultFlightSpeed = MathUtil.calcActualSpeed((float) conf.getDouble("settings.flight_speed"));

        // Messages
        dFlight = conf.getString("messages.flight.disable");
        dFlight = conf.getString("messages.flight.disable");
        eFlight = conf.getString("messages.flight.enable");
        cFlight = conf.getString("messages.flight.can_enable");
        nFlight = conf.getString("messages.flight.cannot_enable");
        disableTrail = conf.getString("messages.trail.disable");
        enableTrail = conf.getString("messages.trail.enable");
        noPermission = conf.getString("messages.permission_denied");

        // Load other stuff that have separate methods
        loadSounds();
        loadTrail();
    }

    // TODO Finish
    public void updateConfig() {
        boolean modified = false;
        // 4
//        if (!config.isConfigurationSection("towny")) {
//            config.removeNode();
//            modified = true;
//        }

        if (modified) conf.save();
    }

    private void loadTrail() {
        trail = conf.getBoolean("trail.enabled");

        if (trail) {
            pl.getParticles().setParticle(conf.getString("trail.particle"));
            pl.getParticles().setAmount(conf.getInt("trail.amount"));
            String offset = conf.getString("trail.rgb");
            if (offset != null && (offset = offset.replaceAll("\\s+", "")).split(",").length == 3) {
                String[] xyz = offset.split(",");
                pl.getParticles().setRBG(xyz[0].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[0]) : 0,
                        xyz[1].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[1]) : 0,
                        xyz[2].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[2]) : 0);
            }
        }
    }

    private void loadSounds() {
        everyEnable = conf.getBoolean("sounds.every_enable");
        everyDisable = conf.getBoolean("sounds.every_disable");
        eSound = getSound("sounds.enable");
        dSound = getSound("sounds.disable");
        cSound = getSound("sounds.can_enable");
        nSound = getSound("sounds.cannot_enable");
    }

    private Sound getSound(String key) {
        if (conf.isConfigurationSection(key)) {
            String s = conf.getString(key + ".sound").toUpperCase().replaceAll("\\.", "_");
            if (Sound.is(s)) {
                return new Sound(s, (float) conf.getDouble(key + ".volume"), (float) conf.getDouble(key + ".pitch"));
            }
        }
        return null;
    }

    // FILE CONFIG METHODS
    public void set(String path, Object value) {
        conf.set(path, value);
        conf.save();
    }

    private void migrateFromVersion3() {
        conf.addSubnode("auto_update: " + conf.getBoolean("auto_update"), "settings");
        conf.removeNode("auto_update");

        conf.addSubnode("auto_enable_flight: " + conf.getBoolean("settings.auto_enable"), "settings");
        conf.removeNode("settings.auto_enable");

        conf.addSubnode("disable_enemy_range: " + conf.getInt("settings.disable_enemy_range"), "factions");
        // Clear other subnodes off of factions
        conf.removeNode("factions");
        conf.addNode("factions:", "towny");
        conf.removeNode("settings.disable_enemy_range");

        conf.addSubnode("negate_during_war: " + conf.getBoolean("towny.disable_during_war"), "towny");
        conf.removeNode("towny.disable_during_war");

        conf.addSubnode("every_disable: false", "sounds.every_enable");

        CommentConf categoryConf = pl.getCategoryManager().getConf();

        // WORLDS
        ConfigurationSection worlds = conf.getConfigurationSection("worlds");
        if (worlds.isList("enable") || worlds.isList("disable")) {
            boolean enable = worlds.isList("enable");
            List<String> worldList = worlds.getStringList(enable ? "enable" : "disable");

            for (String worldName : worldList) {
                if (!"exampleworld".equals(worldName)) {
                    categoryConf.addIndentedSubnode(worldName + ": " + enable, "global.worlds");
                }
            }
        }
        categoryConf.removeNode("global.worlds.WORLDNAME");
        categoryConf.removeNode("global.worlds.WORLDNAME2");
        conf.removeNode("worlds");

        // REGIONS
        ConfigurationSection regions = conf.getConfigurationSection("regions");
        if (regions.isConfigurationSection("enable") || regions.isConfigurationSection("disable")) {
            boolean enable = regions.isConfigurationSection("enable");
            String type = enable ? "enable" : "disable";
            Set<String> keys = regions.getConfigurationSection(type).getKeys(false);

            for (String worldName : keys) {
                if (!("exampleworld".equals(worldName) || "exampleworld2".equals(worldName))) {
                    List<String> list = regions.getStringList(type + "." + worldName);
                    for (String region : list) {
                        categoryConf.addIndentedSubnode(worldName + "+" + region + ": " + enable, "global.regions");
                    }

                }
            }
        }
        conf.removeNode("regions");

        // FACTIONS
        ConfigurationSection factions = conf.getConfigurationSection("factions");

        HashMap<String, Boolean> oldFactionCategories = new HashMap<>();
        for (String oldFactionCategory : factions.getKeys(false)) {
            List<String> categoryInfo = new ArrayList<>(Arrays.asList("priority: 0", "worlds: {}", "regions: {}", "factions:"));
            categoryConf.addIndentedSubnodes(categoryInfo, "categories." + oldFactionCategory);

            boolean enable = factions.getConfigurationSection(oldFactionCategory).isList("enable");

            categoryConf.addIndentedSubnodes(Arrays.asList("enable", "disable"), "categories." + oldFactionCategory + ".factions");
            oldFactionCategories.put(oldFactionCategory, enable);
        }
        categoryConf.removeNode("categories.example");
        categoryConf.removeNode("categories.example2");

        categoryConf.save();

        for (Map.Entry<String, Boolean> oldCategory : oldFactionCategories.entrySet()) {
            String name = oldCategory.getKey();
            boolean enable = oldCategory.getValue();
            categoryConf.set("categories." + name + ".factions." + enable, conf.getStringList("factions." + name + "." + enable));
        }

        conf.save();
        categoryConf.save();
    }
}