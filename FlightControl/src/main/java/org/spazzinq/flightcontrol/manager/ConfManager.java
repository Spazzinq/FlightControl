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
import lombok.Setter;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.api.object.Sound;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.util.MathUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfManager {
    private final FlightControl pl;
    private boolean ignoreReload;

    private final File confFile;
    @Getter private CommentConf conf;

    @Getter @Setter private boolean autoEnable;
    @Getter @Setter private boolean autoUpdate;
    @Getter @Setter private boolean inGameSupport;

    @Getter @Setter private Sound enableSound;
    @Getter @Setter private Sound disableSound;
    @Getter @Setter private Sound canEnableSound;
    @Getter @Setter private Sound cannotEnableSound;
    @Getter @Setter private float defaultFlightSpeed;
    @Getter @Setter private boolean combatChecked;
    @Getter @Setter private boolean cancelFall;
    @Getter @Setter private boolean vanishBypass;
    @Getter @Setter private boolean trail;
    @Getter @Setter private boolean everyEnable;
    @Getter @Setter private boolean everyDisable;
    @Getter @Setter private boolean tempflyAlwaysDecrease;

    @Getter @Setter private boolean nearbyCheck;
    @Getter @Setter private boolean isNearbyCheckEnemies;
    @Getter @Setter private double nearbyRangeSquared;

    @Getter @Setter private String aeEnchantName;

    public ConfManager() {
        pl = FlightControl.getInstance();
        confFile = new File(pl.getDataFolder(), "config.yml");
    }

    public boolean loadConf() {
        boolean reloaded = false;

        if (!ignoreReload) {
            ignoreReload = true;
            conf = new CommentConf(confFile, pl.getResource("config.yml"));

            if (conf.isBoolean("auto_update")) {
                migrateFromVersion3();
            }

            updateConf();

            // booleans
            autoUpdate = conf.getBoolean("settings.auto_update");
            autoEnable = conf.getBoolean("settings.auto_enable_flight");
            combatChecked = conf.getBoolean("settings.disable_flight_in_combat");
            cancelFall = conf.getBoolean("settings.prevent_fall_damage");
            vanishBypass = conf.getBoolean("settings.vanish_bypass");
            isNearbyCheckEnemies = conf.getBoolean("nearby_disable.factions_enemy");
            tempflyAlwaysDecrease = conf.getBoolean("tempfly.always_decrease");

            // ints
            int range = conf.getInt("nearby_disable.range");

            if (nearbyCheck = (range != -1)) {
                nearbyRangeSquared = range * range;
            }

            // floats
            defaultFlightSpeed = MathUtil.calcConvertedSpeed((float) conf.getDouble("settings.flight_speed"));

            // Strings
            aeEnchantName = conf.getString("settings.ae_enchant_name");

            // Load other stuff that have separate methods
            loadSounds();
            loadTrail();

            // Prevent reloading for the next 500ms
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ignoreReload = false;
                }
            }, 500);

            reloaded = true;
        }
        return reloaded;
    }

    // TODO Continue to add!
    public void updateConf() {
        boolean modified = false;

        // 4.2.5 - relocate lands, towny; add griefprevention
        if (conf.isConfigurationSection("towny") || conf.isConfigurationSection("lands")) {
            pl.getLogger().info("Territories have migrated to the categories.yml!");
            conf.deleteNode("towny");
            conf.deleteNode("lands");

            modified = true;
        }

        // 4.2.5 - change function of factions_enemy_range
        if (conf.isConfigurationSection("factions")) {
            pl.getLogger().info("Migrated the factions disable enemy range section of the configuration!");

            conf.addNode("nearby_disable:", "trail");
            conf.addIndentedSubnodes(new HashSet<>(Arrays.asList(
                    "range: " + conf.getInt("factions.disable_enemy_range"),
                    "factions_enemy: " + (conf.getInt("factions.disable_enemy_range") != -1))), "nearby_disable");

            conf.deleteNode("factions");

            modified = true;
        }

        // 4.5.0 - remove "territory"
        if (conf.isConfigurationSection("territory")) {
            pl.getLogger().info("Territories have migrated to the categories.yml!");
            conf.deleteNode("territory");

            modified = true;
        }

        // 4.5.8 - add AdvancedEnchantments custom enchant support
        if (!conf.isString("settings.ae_enchant_name")){
            pl.getLogger().info("Added AdvancedEnchantments custom enchant name setting!");
            conf.addSubnodes(Collections.singleton("ae_enchant_name: \"Flight\""), "settings.vanish_bypass");

            modified = true;
        }

        // 4.6.0 - add tempfly setting
        if (!conf.isConfigurationSection("tempfly")) {
            pl.getLogger().info("Added \"tempfly\" section to config!");
            conf.addNode("tempfly:","nearby_disable");
            conf.addIndentedSubnodes(Collections.singleton("always_decrease: true"), "tempfly");

            modified = true;
        }

        if (modified) {
            conf.save();
        }
    }

    private void loadTrail() {
        trail = conf.getBoolean("trail.enabled");

        if (trail) {
            pl.getParticle().setParticle(conf.getString("trail.particle"));
            pl.getParticle().setAmount(conf.getInt("trail.amount"));
            String offset = conf.getString("trail.rgb");
            if (offset != null && (offset = offset.replaceAll("\\s+", "")).split(",").length == 3) {
                String[] xyz = offset.split(",");
                pl.getParticle().setRBG(xyz[0].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[0]) : 0,
                        xyz[1].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[1]) : 0,
                        xyz[2].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[2]) : 0);
            }
        }
    }

    private void loadSounds() {
        everyEnable = conf.getBoolean("sounds.every_enable");
        everyDisable = conf.getBoolean("sounds.every_disable");
        enableSound = getSound("sounds.enable");
        disableSound = getSound("sounds.disable");
        canEnableSound = getSound("sounds.can_enable");
        cannotEnableSound = getSound("sounds.cannot_enable");
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
        ignoreReload = true;
        conf.set(path, value);
        conf.save();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ignoreReload = false;
            }
        }, 500);
    }

    private void migrateFromVersion3() {
        try {
            //noinspection UnstableApiUsage
            Files.move(confFile, new File(pl.getDataFolder(), "config_old.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        pl.saveDefaultConfig();
    }
}