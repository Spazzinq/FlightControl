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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.CommentConf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class LangManager {
    private final HashSet<String> languages = new HashSet<>(Arrays.asList("en", "fr", "zh"));
    private Locale locale;

    private final FlightControl pl;
    @Getter private CommentConf lang;
    private final File langFile;
    private boolean ignoreReload;

    // Bool
    @Setter private boolean useActionBar;

    // Player messages
    @Getter private String disableFlight;
    @Getter private String enableFlight;
    @Getter private String canEnableFlight;
    @Getter private String cannotEnableFlight;
    @Getter private String heightDenied;
    @Getter private String personalTrailDisable;
    @Getter private String personalTrailEnable;
    @Getter private String permDenied;

    // Admin messages
    @Getter private String prefix;
    @Getter private String pluginReloaded;
    // Config editing messages
    @Getter private String globalFlightSpeedSet;
    @Getter private String globalFlightSpeedSame;
    @Getter private String globalFlightSpeedUsage;
    @Getter private String enemyRangeSet;
    @Getter private String enemyRangeSame;
    @Getter private String enemyRangeUsage;
    // Command messages
    @Getter private String flyCommandEnable;
    @Getter private String flyCommandDisable;
    @Getter private String flyCommandUsage;
    @Getter private String flySpeedSet;
    @Getter private String flySpeedSame;
    @Getter private String flySpeedUsage;
    @Getter private String tempFlyEnable;
    @Getter private String tempFlyAdd;
    @Getter private String tempFlyDisable;
    @Getter private String tempFlyDisabled;
    @Getter private String tempFlyCheck;
    @Getter private String tempFlyUsage;

    public LangManager(FlightControl pl) {
        this.pl = pl;
        langFile = new File(pl.getDataFolder(), "lang.yml");
        locale = Locale.getDefault();
    }

    public boolean loadLang() {
        boolean reloaded = false;

        if (!ignoreReload) {
            ignoreReload = true;

            if (langFile.exists()) {
                YamlConfiguration tempLocaleConf = YamlConfiguration.loadConfiguration(langFile);

                if (tempLocaleConf.isString("locale")) {
                    String preferredLocale = tempLocaleConf.getString("locale");

                    if (languages.contains(preferredLocale)) {
                        locale = Locale.forLanguageTag(preferredLocale);

                        try {
                            //noinspection UnstableApiUsage
                            Files.move(langFile, new File(pl.getDataFolder(), "lang_old.yml"));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        pl.getLogger().info("Generated a new lang.yml!");
                    }
                }
            }

            InputStream langResource = pl.getResource("lang_" + locale.getLanguage() + ".yml");
            boolean langResourceExists = langResource != null;

            if (!langResourceExists) {
                pl.getLogger().warning("No custom lang file for " + locale.getDisplayLanguage() + " could be found! " +
                        "Defaulting to English...");
            }

            lang = new CommentConf(langFile, langResourceExists ? langResource : pl.getResource("lang_en.yml"));

            // Migrate config messages
            if (pl.getConfManager().getConf().isConfigurationSection("messages")) {
                migrateFromVersion4();
            }

            // boolean
            useActionBar = lang.getBoolean("player.actionbar");

            // Strings
            disableFlight = lang.getString("player.flight.disabled");
            enableFlight = lang.getString("player.flight.enabled");
            canEnableFlight = lang.getString("player.flight.can_enable");
            cannotEnableFlight = lang.getString("player.flight.cannot_enable");
            heightDenied = lang.getString("player.flight.height_denied");
            personalTrailDisable = lang.getString("player.trail.disabled");
            personalTrailEnable = lang.getString("player.trail.enabled");
            permDenied = lang.getString("player.permission_denied");

            prefix = lang.getString("admin.prefix");
            pluginReloaded = lang.getString("admin.reloaded");
            // Config set
            globalFlightSpeedSet = lang.getString("admin.global_flight_speed.set");
            globalFlightSpeedSame = lang.getString("admin.global_flight_speed.same");
            globalFlightSpeedUsage = lang.getString("admin.global_flight_speed.usage");
            enemyRangeSet = lang.getString("admin.enemy_range.set");
            enemyRangeSame = lang.getString("admin.enemy_range.same");
            enemyRangeUsage = lang.getString("admin.enemy_range.usage");
            // Commands
            flyCommandEnable = lang.getString("admin.fly.enable");
            flyCommandDisable = lang.getString("admin.fly.disable");
            flyCommandUsage = lang.getString("admin.fly_command.usage");
            flySpeedSet = lang.getString("admin.flyspeed.set");
            flySpeedSame = lang.getString("admin.flyspeed.same");
            flySpeedUsage = lang.getString("admin.flyspeed.usage");
            tempFlyEnable = lang.getString("admin.tempfly.enable");
            tempFlyAdd = lang.getString("admin.tempfly.add");
            tempFlyDisable = lang.getString("admin.tempfly.disable");
            tempFlyDisabled = lang.getString("admin.tempfly.disabled");
            tempFlyCheck = lang.getString("admin.tempfly.check");
            tempFlyUsage = lang.getString("admin.tempfly.usage");

            // Prevent reloading for the next 250ms
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

    public void updateLang() {
        boolean modified = false;

        if (!lang.isString("admin.tempfly.check")) {
            lang.addSubnodes(Collections.singleton("check: \"&e&lFlightControl &7» &f%player%&e has &f%duration%&e of" +
                    " flight remaining.\""), "admin.tempfly.disabled");

            modified = true;
        }

        //noinspection ConstantConditions
        if (modified) {
            lang.save();
        }
    }

    public void set(String path, Object value) {
        ignoreReload = true;
        lang.set(path, value);
        lang.save();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                ignoreReload = false;
            }
        }, 500);
    }

    private void migrateFromVersion4() {
        CommentConf conf = pl.getConfManager().getConf();
        ConfigurationSection msgs = conf.getConfigurationSection("messages");

        lang.set("player.actionbar", msgs.getBoolean("actionbar"));

        lang.set("player.flight.enabled", msgs.getString("flight.enable"));
        lang.set("player.flight.disabled", msgs.getString("flight.disable"));
        lang.set("player.flight.can_enable", msgs.getString("flight.can_enable"));
        lang.set("player.flight.cannot_enable", msgs.getString("flight.cannot_enable"));

        lang.set("player.trail.enabled", msgs.getString("trail.enable"));
        lang.set("player.trail.disabled", msgs.getString("trail.disable"));

        lang.set("player.permission_denied", msgs.getString("permission_denied"));

        lang.save();

        conf.deleteNode("messages");
        conf.save();

        pl.getLogger().info("Successfully migrated the messages from config.yml to lang.yml!");
    }

    public boolean useActionBar() {
        return useActionBar;
    }
}
