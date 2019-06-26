/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
 *
 * Copyright (cFile) 2019 Spazzinq
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

package org.Spazzinq.FlightControl;

import org.Spazzinq.FlightControl.Objects.Category;
import org.Spazzinq.FlightControl.Objects.CommentedConfig;
import org.Spazzinq.FlightControl.Objects.Sound;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.Spazzinq.FlightControl.FlightControl.defaultPerms;

final class Config {
    private FlightControl pl;
	static CommentedConfig cFile;
    private static PluginManager pm;

    private File f;
    private File dTrailF;
	private static FileConfiguration dTrailC;

	static boolean autoUpdate, command, support, worldBL, regionBL,
            useCombat, ownTown, townyWar, cancelFall,
            vanishBypass, trail, actionBar, everyEnable,
            useFacEnemyRange;
	static double facEnemyRange;
	static float flightSpeed;
    static String dFlight, eFlight, cFlight, nFlight, dTrail, eTrail, noPerm;
	static Sound eSound, dSound, cSound, nSound;
    static HashSet<String> worlds, trailPrefs;
    static HashMap<String, List<String>> regions;
    static HashMap<String, Category> categories;

	Config(FlightControl pl) {
	    this.pl = pl;
        pm = pl.getServer().getPluginManager();
        dTrailF = new File(pl.getDataFolder(), "disabled_trail.yml");
        f = new File(pl.getDataFolder(), "config.yml");

        reloadConfig();
        updateConfig();
    }

	void reloadConfig() {
	    pl.saveDefaultConfig();
        try { cFile = new CommentedConfig(f, pl.getResource("config.yml")); } catch (Exception e) { e.printStackTrace(); }

        // booleans
        autoUpdate = cFile.getBoolean("auto_update");
        command = cFile.getBoolean("settings.command");
        worldBL = cFile.isList("worlds.disable");
        regionBL = cFile.isConfigurationSection("regions.disable");
        useCombat = cFile.getBoolean("settings.disable_flight_in_combat");
        ownTown = cFile.getBoolean("towny.enable_own_town");
        townyWar = cFile.getBoolean("towny.disable_during_war");
        cancelFall = cFile.getBoolean("settings.prevent_fall_damage");
        vanishBypass = cFile.getBoolean("settings.vanish_bypass");
        actionBar = cFile.getBoolean("messages.actionbar");
        // ints
        int range = cFile.getInt("settings.disable_enemy_range");
        if (useFacEnemyRange  = range != -1) facEnemyRange = range;
        // Messages
        dFlight = cFile.getString("messages.flight.disable");
        dFlight = cFile.getString("messages.flight.disable");
        eFlight = cFile.getString("messages.flight.enable");
        cFlight = cFile.getString("messages.flight.can_enable");
        nFlight = cFile.getString("messages.flight.cannot_enable");
        dTrail = cFile.getString("messages.trail.disable");
        eTrail = cFile.getString("messages.trail.enable");
        noPerm = cFile.getString("messages.permission_denied");
        // Load other stuff that have separate methods
        loadWorlds(); loadSounds(); loadTrail(); loadTrailPrefs(); loadFlightSpeed();
        regions = new HashMap<>(); if (pm.isPluginEnabled("WorldGuard")) loadRegions();
        if (pm.isPluginEnabled("Factions")) loadCategories();
        // Region permission registering
        for (World w : Bukkit.getWorlds()) { String name = w.getName(); defaultPerms(name); for (String rg : pl.regions.regions(w)) defaultPerms(name + "." + rg); }
        // Set for players already online
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.isFlying()) FlightManager.trailCheck(p);
            p.setFlySpeed(flightSpeed);
        }
    }

    private void updateConfig() {
	    boolean modified = false;
	    // 3
	    if (!cFile.isConfigurationSection("towny"))  {
	        cFile.addNode("trail", "towny:");
	        cFile.addSubnodes("towny", Arrays.asList("disable_during_war: false", "enable_own_town: false"));
            modified = true;
        }
	    if (!cFile.isBoolean("sounds.every_enable")) { cFile.addSubnode("sounds","every_enable: false"); modified = true; }
	    // 3.1
        if (!(cFile.isInt("settings.flight_speed") || cFile.isDouble("settings.flight_speed"))) { cFile.addSubnode("settings.command", "flight_speed: 1.0"); modified = true; }
	    if (!cFile.isInt("settings.disable_enemy_range")) { cFile.addSubnode("settings.vanish_bypass", "disable_enemy_range: -1"); modified = true; }
        if (modified) save();
    }

    // LOAD METHODS
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

    private void loadWorlds() {
        worlds = new HashSet<>();
	    ConfigurationSection worldsCS = load(cFile,"worlds");
	    if (worldsCS != null) {
            List<String> type = worldsCS.getStringList(worldBL ? "disable" : "enable");
            if (type != null) for (String w : type) if (Bukkit.getWorld(w) != null) worlds.add(w);
        }
    }

    private void loadRegions() {
        ConfigurationSection regionsCS = load(cFile,"regions");
        if (regionsCS != null) addRegions(regionsCS.getConfigurationSection(regionBL ? "disable" : "enable"));
    }

	private void loadCategories() {
        categories = new HashMap<>();
	    ConfigurationSection facs = load(cFile,"factions");
	    if (facs != null) for (String cName : facs.getKeys(false)) {
	        // Register permission defaults
            if (pm.getPermission("flightcontrol.factions." + cName) == null) pm.addPermission(new Permission("flightcontrol.factions." + cName, PermissionDefault.FALSE));
            ConfigurationSection categorySect = load(facs, cName);
            if (categorySect != null) {
                String type = categorySect.isList("disable") ? "disable" : (categorySect.isList("enable") ? "enable" : null);
                if (type != null) categories.put(cName, createCategory(categorySect.getStringList(type), type.equals("disable")));
                else pl.getLogger().warning("Factions category \"" + cName + "\" is invalid! (missing \"enable\"/\"disable\")");
            }
	    }
	}

    private void loadTrail() {
        trail = cFile.getBoolean("trail.enabled");
        if (trail) {
            pl.particles.setParticle(cFile.getString("trail.particle"));
            pl.particles.setAmount(cFile.getInt("trail.amount"));
            String offset = cFile.getString("trail.rgb");
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
		if (!dTrailF.exists()) { try { //noinspection ResultOfMethodCallIgnored
            dTrailF.createNewFile(); } catch (IOException e) { e.printStackTrace(); } }
		dTrailC = YamlConfiguration.loadConfiguration(dTrailF);

		if (dTrailC.isList("disabled_trail")) {
			if (dTrailC.getStringList("disabled_trail") != null && !dTrailC.getStringList("disabled_trail").isEmpty()) {
				for (String uuid : dTrailC.getStringList("disabled_trail")) {
					try { if (pl.getServer().getPlayer(UUID.fromString(uuid)) != null || pl.getServer().getOfflinePlayer(UUID.fromString(uuid)) != null) trailPrefs.add(uuid); } catch (IllegalArgumentException ignored) { }
				}
			}
		} else dTrailC.createSection("disabled_trail");
	}

    private void loadSounds() {
        everyEnable = cFile.getBoolean("sounds.every_enable");
        if (cFile.isConfigurationSection("sounds.enable")) eSound = getSound("sounds.enable");
        if (cFile.isConfigurationSection("sounds.disable")) dSound = getSound("sounds.disable");
        if (cFile.isConfigurationSection("sounds.can_enable")) cSound = getSound("sounds.can_enable");
        if (cFile.isConfigurationSection("sounds.cannot_enable")) nSound = getSound("sounds.cannot_enable");
    }

    private void loadFlightSpeed() {
        float wrongSpeed = (float) cFile.getDouble("settings.flight_speed");
        if (wrongSpeed > 10f) wrongSpeed = 10f; else if (wrongSpeed < 0.0001f) wrongSpeed = 0.0001f;

        float defaultSpeed = 0.1f, maxSpeed = 1f;

        if (wrongSpeed < 1f) flightSpeed = defaultSpeed * wrongSpeed;
        else {
            float ratio = ((wrongSpeed - 1) / 9) * (maxSpeed - defaultSpeed);
            flightSpeed = ratio + defaultSpeed;
        }
    }

    // LOAD HELPER METHODS
    private Sound getSound(String key) {
        String s = cFile.getString(key + ".sound").toUpperCase().replaceAll("\\.", "_");
        if (Sound.is(s)) return new Sound(s, (float) cFile.getDouble(key + ".volume"), (float) cFile.getDouble(key + ".pitch"));
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
		if (types != null && !types.isEmpty() && (types.contains("OWN") || types.contains("ALLY") || types.contains("TRUCE")
                || types.contains("NEUTRAL") || types.contains("ENEMY") || types.contains("WARZONE")
                || types.contains("SAFEZONE") || types.contains("WILDERNESS"))) {
            return new Category(blacklist, types.contains("OWN"), types.contains("ALLY"), types.contains("TRUCE"), types.contains("NEUTRAL"), types.contains("ENEMY"), types.contains("WARZONE"),types.contains("SAFEZONE"), types.contains("WILDERNESS"));
		}
		return null;
	}

	// FILE CONFIG METHODS
    void saveTrails() { // Saves personal trail preferences
	    if (dTrailC != null && dTrailF != null) {
            dTrailC.set("disabled_trail", !trailPrefs.isEmpty() ? trailPrefs : null);
            try { dTrailC.save(dTrailF); } catch (IOException e) { e.printStackTrace(); }
        }
    }

    void set(String path, Object value) { cFile.set(path, value); save(); }
    private void save() { try { cFile.save(f); } catch (IOException e) { e.printStackTrace(); } }
}