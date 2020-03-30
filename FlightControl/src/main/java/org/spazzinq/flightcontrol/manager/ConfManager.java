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
import org.spazzinq.flightcontrol.api.objects.Sound;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.util.MathUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ConfManager {
    private final FlightControl pl;
    private boolean ignoreReload;

    private final File confFile;
    @Getter private CommentConf conf;

    @Getter @Setter private boolean autoEnable;
    @Getter @Setter private boolean autoUpdate;
    @Getter @Setter private boolean inGameSupport;

    @Getter @Setter private Sound eSound, dSound, cSound, nSound;
    @Getter @Setter private float defaultFlightSpeed;
    @Getter @Setter private boolean combatChecked;
    @Getter @Setter private boolean cancelFall;
    @Getter @Setter private boolean vanishBypass;
    @Getter @Setter private boolean trail;
    @Getter @Setter private boolean everyEnable;
    @Getter @Setter private boolean everyDisable;

    @Getter @Setter private boolean townyOwn;
    @Getter @Setter private boolean townyWarDisable;
    @Getter @Setter private boolean landsOwnEnable;
    @Getter @Setter private boolean landsIncludeTrusted;
    @Getter @Setter private boolean gpClaimOwnEnable;
    @Getter @Setter private boolean gpClaimIncludeTrusted;
    @Getter @Setter private boolean nearbyCheck;
    @Getter @Setter private boolean isNearbyCheckEnemies;
    @Getter @Setter private double nearbyRangeSquared;

    public ConfManager(FlightControl pl) {
        this.pl = pl;
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

            updateConfig();

            // booleans
            autoUpdate = conf.getBoolean("settings.auto_update");
            autoEnable = conf.getBoolean("settings.auto_enable_flight");
            combatChecked = conf.getBoolean("settings.disable_flight_in_combat");
            cancelFall = conf.getBoolean("settings.prevent_fall_damage");
            vanishBypass = conf.getBoolean("settings.vanish_bypass");

            townyOwn = conf.getBoolean("territory.towny.enable_own_town");
            townyWarDisable = conf.getBoolean("territory.towny.negate_during_war");
            landsOwnEnable = conf.getBoolean("territory.lands.enable_own_land");
            landsIncludeTrusted = conf.getBoolean("territory.lands.include_trusted");
            gpClaimOwnEnable = conf.getBoolean("territory.griefprevention.enable_own_claim");
            gpClaimIncludeTrusted = conf.getBoolean("territory.griefprevention.include_trusted");

            // ints
            int range = conf.getInt("nearby_disable.range");

            if (nearbyCheck = (range != -1)) {
                nearbyRangeSquared = range * range;
            }

            isNearbyCheckEnemies = conf.getBoolean("nearby_disable.factions_enemy");

            // floats
            defaultFlightSpeed = MathUtil.calcConvertedSpeed((float) conf.getDouble("settings.flight_speed"));

            // Load other stuff that have separate methods
            loadSounds();
            loadTrail();

            // Prevent reloading for the next 250ms
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    ignoreReload = false;
                }
            }, 250);

            reloaded = true;
        }
        return reloaded;
    }

    // TODO Continue to add!
    public void updateConfig() {
        boolean modified = false;

        // 4.1.0 - moved to lang.yml
        if (conf.isConfigurationSection("messages")) {
            pl.getLogger().info("Removed the messages section from config.yml!");
            conf.deleteNode("messages");
            modified = true;
        }

        // 4.2.5 - relocate lands, towny; add griefprevention
        if (conf.isConfigurationSection("towny") || conf.isConfigurationSection("lands")) {
            pl.getLogger().info("Migrated the towny and lands section of the configuration!");

            conf.addNode("territory:", "trail");
            conf.addSubnodes(new HashSet<>(Arrays.asList("towny:", "lands:", "griefprevention:")), "territory");
            conf.addIndentedSubnodes(new HashSet<>(Arrays.asList(
                    "enable_own_town: " + conf.getBoolean("towny.enable_own_town"),
                    "negate_during_war: " + conf.getBoolean("towny.negate_during_war"))), "territory.towny");
            conf.addIndentedSubnodes(new HashSet<>(Arrays.asList(
                    "enable_own_land: " + conf.getBoolean("lands.enable_own_land"),
                    "include_trusted: " + conf.getBoolean("lands.include_trusted", false))), "territory.lands");
            conf.addIndentedSubnodes(new HashSet<>(Collections.singletonList("enable_own_claim: false")), "territory.griefprevention");

            conf.deleteNode("towny");
            conf.deleteNode("lands");

            modified = true;
        }

        // 4.2.5 - change function of factions_enemy_range
        if (conf.isConfigurationSection("factions")) {
            pl.getLogger().info("Migrated the factions disable enemy range section of the configuration!");

            conf.addNode("nearby_disable:", "trail");
            conf.addSubnodes(new HashSet<>(Arrays.asList(
                    "range: " + conf.getInt("factions.disable_enemy_range"),
                    "factions_enemy: " + (conf.getInt("factions.disable_enemy_range") != -1))), "nearby_disable");

            conf.deleteNode("factions");

            modified = true;
        }

        // 4.3.8 - add trusted to GriefPrevention section
        if (conf.isBoolean("territory.griefprevention.include_trusted")) {
            pl.getLogger().info("Added \"include_trusted\" to the GriefPrevention section of the config!");

            conf.addSubnodes(new HashSet<>(Collections.singleton("include_trusted: false")), "territory.griefprevention.enable_own_claim");

            modified = true;
        }

        if (modified) {
            conf.save();
        }
    }

    private void loadTrail() {
        trail = conf.getBoolean("trail.enabled");

        if (trail) {
            pl.getParticleManager().setParticle(conf.getString("trail.particle"));
            pl.getParticleManager().setAmount(conf.getInt("trail.amount"));
            String offset = conf.getString("trail.rgb");
            if (offset != null && (offset = offset.replaceAll("\\s+", "")).split(",").length == 3) {
                String[] xyz = offset.split(",");
                pl.getParticleManager().setRBG(xyz[0].matches("-?\\d+(.(\\d+)?)?") ? Integer.parseInt(xyz[0]) : 0,
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