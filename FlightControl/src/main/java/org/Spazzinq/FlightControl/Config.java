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

package org.Spazzinq.FlightControl;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Config {
	private FlightControl pl;
	private FileConfiguration c;
    private PluginManager pm;
    private static File dTrailF;
	private static FileConfiguration dTrailC;

	static boolean isSpigot, command, useCombat, cancelFall, vanishBypass, flightTrail, actionBar, support;
	static Sound eSound, dSound, cSound, nSound;
	static String dFlight, eFlight, cFlight, nFlight, dTrail, eTrail, permDenied;
//	static double abLength;
	static HashMap<String, List<String>> eRegions;
	static List<String> eWorlds, trailPrefs;
    public static HashMap<String, Category> categories = new HashMap<>();

	Config(FlightControl i) {
		pl = i;
		pm = pl.getServer().getPluginManager();
        isSpigot = pl.getServer().getVersion().contains("Spigot");
		dTrailF = new File(pl.getDataFolder(), "disabled_trail.yml");

		reloadConfig();
	}

	void reloadConfig() {
        pl.saveDefaultConfig();
        pl.reloadConfig();
        c = pl.getConfig();

        command = c.getBoolean("settings.command"); pl.flyCommand();
        useCombat = c.getBoolean("settings.disable_flight_in_combat");
        cancelFall = c.getBoolean("settings.prevent_fall_damage");
        vanishBypass = c.getBoolean("settings.vanish_bypass");
        flightTrail = c.getBoolean("settings.flight_trail");
        actionBar = c.getBoolean("messages.actionbar");
//        abLength = c.getDouble("messages.actionbar.duration") * 20;
        dFlight = c.getString("messages.flight.disable");
        eFlight = c.getString("messages.flight.enable");
        cFlight = c.getString("messages.flight.can_enable");
        nFlight = c.getString("messages.flight.cannot_enable");
        dTrail = c.getString("messages.trail.disable");
        eTrail = c.getString("messages.trail.enable");
        permDenied = c.getString("messages.permission_denied");
        eWorlds = new ArrayList<>(); trailPrefs = new ArrayList<>();
        eRegions = new HashMap<>(); categories = new HashMap<>();
        loadWorlds(); loadSounds(); loadTrailPrefs();
        if (pm.isPluginEnabled("WorldGuard")) loadRegions();
        if (pm.isPluginEnabled("Factions")) loadFacCategories();
        for (World w : Bukkit.getWorlds()) { String name = w.getName(); defaultPerms(name); for (String rg : pl.regions.regions(w)) defaultPerms(name + "." + rg); }
    }

	private void defaultPerms(String suffix) {
        if (pm.getPermission("flightcontrol.fly." + suffix) == null)
            pm.addPermission(new Permission("flightcontrol.fly." + suffix, PermissionDefault.FALSE));
        // No "nofly" worlds; disable permission register
        if (suffix.contains(".") && pm.getPermission("flightcontrol.nofly." + suffix) == null)
            pm.addPermission(new Permission("flightcontrol.nofly." + suffix, PermissionDefault.FALSE));
    }

    private ConfigurationSection load(ConfigurationSection c, String type) {
        if (c.isConfigurationSection(type)) {
            ConfigurationSection typeS = c.getConfigurationSection(type);
            Set<String> typeKeys = new HashSet<>();
            for (String key : typeS.getKeys(true)) {
                if (key.contains(".")) key = key.split("\\.")[1];
                typeKeys.add(key);
            }
            // "disable" may seem useless right now, but it's for future functionality
            if (!typeKeys.isEmpty() && (typeKeys.contains("enable") || typeKeys.contains("disable")
                    || type.equals("enable") || type.equals("disable"))) return typeS;
        }
        return null;
    }

    private void loadWorlds() {
	    ConfigurationSection worlds = load(c,"worlds");
	    if (worlds != null) {
	        eWorlds = addWorlds(worlds.getStringList("enable"));
        }
    }

    private void loadRegions() {
        ConfigurationSection regions = load(c,"regions");
        if (regions != null) eRegions = addRegions(load(regions, "enable"));
    }

	private void loadFacCategories() {
	    ConfigurationSection facs = load(c,"factions");
	    if (facs != null) for (String cName : facs.getKeys(false)) {
            if (pm.getPermission("flightcontrol.factions." + cName) == null) pm.addPermission(new Permission("flightcontrol.factions." + cName, PermissionDefault.FALSE));
            ConfigurationSection categorySect = load(facs, cName);
            if (categorySect != null) {
                Category category = createCategory(categorySect.getStringList("enable"));
                if (category != null) Config.categories.put(cName, category);
                else pl.getLogger().warning("Factions category \"" + cName + "\" is invalid/empty!");
            }
	    }
	}

    private void loadSounds() {
	    String e = c.getString("sounds.enable.sound").toUpperCase().replaceAll("\\.", "_"),
                d = c.getString("sounds.disable.sound").toUpperCase().replaceAll("\\.", "_"),
                cE = c.getString("sounds.can_enable.sound").toUpperCase().replaceAll("\\.", "_"),
                n = c.getString("sounds.cannot_enable.sound").toUpperCase().replaceAll("\\.", "_");
        if (Sound.is(e)) eSound = new Sound(e, (float) c.getDouble("sounds.enable.volume"), (float) c.getDouble("sounds.enable.pitch"));
        if (Sound.is(d)) dSound = new Sound(d, (float) c.getDouble("sounds.disable.volume"), (float) c.getDouble("sounds.disable.pitch"));
        if (Sound.is(cE)) cSound = new Sound(cE, (float) c.getDouble("sounds.can_enable.volume"), (float) c.getDouble("sounds.can_enable.pitch"));
        if (Sound.is(n)) nSound = new Sound(n, (float) c.getDouble("sounds.cannot_enable.volume"), (float) c.getDouble("sounds.cannot_enable.pitch"));
    }

	// Per-player trail preferences
	private void loadTrailPrefs() {
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

	private List<String> addWorlds(List<String> type) {
	    List<String> worlds = new ArrayList<>();
	    if (type != null) for (String w : type) if (Bukkit.getWorld(w) != null) worlds.add(w);
	    return worlds;
    }

	private HashMap<String, List<String>> addRegions(ConfigurationSection c) {
        HashMap<String, List<String>> regions = new HashMap<>();
        if (c != null) for (String w : c.getKeys(false)) {
            if (Bukkit.getWorld(w) != null) {
                ArrayList<String> rgs = new ArrayList<>();
                for (String rg : c.getStringList(w)) if (pl.regions.hasRegion(w, rg)) rgs.add(rg);
                regions.put(w, rgs);
            }
        }
        return regions;
    }

	private Category createCategory(List<String> types) {
		if (types != null && !types.isEmpty() && (types.contains("OWN") || types.contains("ALLY") || types.contains("TRUCE")
                || types.contains("NEUTRAL") || types.contains("ENEMY") || types.contains("WARZONE")
                || types.contains("SAFEZONE") || types.contains("WILDERNESS"))) {
            return new Category(types.contains("OWN"), types.contains("ALLY"), types.contains("TRUCE"), types.contains("NEUTRAL"), types.contains("ENEMY"), types.contains("WARZONE"),types.contains("SAFEZONE"), types.contains("WILDERNESS"));
		}
		return null;
	}

    // Saves personal trail preferences
	static void save() {
	    if (dTrailC != null) {
            dTrailC.set("disabled_trail", (trailPrefs != null && !trailPrefs.isEmpty()) ? trailPrefs : null);
            try { dTrailC.save(dTrailF); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}