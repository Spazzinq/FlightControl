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

package org.Spazzinq.FlightControl.Config;

import org.Spazzinq.FlightControl.Category;
import org.Spazzinq.FlightControl.FlightControl;
import org.Spazzinq.FlightControl.Hooks.Factions.Factions;
import org.Spazzinq.FlightControl.Sound;
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
    private static PluginManager pm;
    private static File dTrailF;
	private static FileConfiguration dTrailC;

	public static boolean isSpigot, command, support, worldBL, regionBL, fac,
            useCombat, cancelFall, vanishBypass, trail, actionBar;
	public static Sound eSound, dSound, cSound, nSound;
	public static String dFlight, eFlight, cFlight, nFlight, dTrail, eTrail, permDenied;
//	static double abLength;
    public static HashSet<String> worlds, trailPrefs;
    public static HashMap<String, List<String>> regions;
    public static HashMap<String, Category> categories;

	public Config(FlightControl i) {
        pl = i;
        pm = pl.getServer().getPluginManager();
        isSpigot = pl.getServer().getVersion().contains("Spigot");
        dTrailF = new File(pl.getDataFolder(), "disabled_trail.yml");
    }

	public void reloadConfig() {
        pl.saveDefaultConfig();
        pl.reloadConfig();
        c = pl.getConfig();

        command = c.getBoolean("settings.command");
        worldBL = c.isList("worlds.disable");
        regionBL = c.isConfigurationSection("regions.disable");
        fac = !Factions.class.equals(pl.fac.getClass());

        useCombat = c.getBoolean("settings.disable_flight_in_combat");
        cancelFall = c.getBoolean("settings.prevent_fall_damage");
        vanishBypass = c.getBoolean("settings.vanish_bypass");
        actionBar = c.getBoolean("messages.actionbar");
//        abLength = c.getDouble("messages.actionbar.duration") * 20;
        dFlight = c.getString("messages.flight.disable");
        dFlight = c.getString("messages.flight.disable");
        eFlight = c.getString("messages.flight.enable");
        cFlight = c.getString("messages.flight.can_enable");
        nFlight = c.getString("messages.flight.cannot_enable");
        dTrail = c.getString("messages.trail.disable");
        eTrail = c.getString("messages.trail.enable");
        permDenied = c.getString("messages.permission_denied");
        loadWorlds(); loadSounds(); loadTrail(); loadTrailPrefs();
        regions = new HashMap<>();
        if (pm.isPluginEnabled("WorldGuard")) loadRegions();
        if (pm.isPluginEnabled("Factions")) loadCategories();
        for (World w : Bukkit.getWorlds()) { String name = w.getName(); defaultPerms(name); for (String rg : pl.regions.regions(w)) defaultPerms(name + "." + rg); }
    }

    public static void defaultPerms(String suffix) {
        if (pm.getPermission("flightcontrol.fly." + suffix) == null)
            pm.addPermission(new Permission("flightcontrol.fly." + suffix, PermissionDefault.FALSE));
        if (pm.getPermission("flightcontrol.nofly." + suffix) == null)
            pm.addPermission(new Permission("flightcontrol.nofly." + suffix, PermissionDefault.FALSE));
    }

    public static ConfigurationSection load(ConfigurationSection c, String type) {
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
	    ConfigurationSection worldsCS = load(c,"worlds");
	    if (worldsCS != null) {
            List<String> type = worldsCS.getStringList(Config.worldBL ? "disable" : "enable");
            if (type != null) for (String w : type) if (Bukkit.getWorld(w) != null) worlds.add(w);
        }
    }

    private void loadRegions() {
        ConfigurationSection regionsCS = load(c,"regions");
        if (regionsCS != null) addRegions(regionsCS.getConfigurationSection(Config.regionBL ? "disable" : "enable"));
    }

	private void loadCategories() {
        categories = new HashMap<>();
	    ConfigurationSection facs = load(c,"factions");
	    if (facs != null) for (String cName : facs.getKeys(false)) {
            if (pm.getPermission("flightcontrol.factions." + cName) == null) pm.addPermission(new Permission("flightcontrol.factions." + cName, PermissionDefault.FALSE));
            ConfigurationSection categorySect = load(facs, cName);
            if (categorySect != null) {
                String type = categorySect.isList("disable") ? "disable" : (categorySect.isList("enable") ? "enable" : null);
                if (type != null) Config.categories.put(cName, createCategory(categorySect.getStringList(type), type.equals("disable")));
                else pl.getLogger().warning("Factions category \"" + cName + "\" is invalid! (missing \"enable\"/\"disable\")");
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

    private void loadTrail() {
        trail = c.getBoolean("trail.enabled");
        if (trail) {
            pl.particles.setParticle(c.getString("trail.particle"));
            pl.particles.setAmount(c.getInt("trail.amount"));
            String offset = c.getString("trail.rgb");
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

    // Saves personal trail preferences
	public void save() {
	    if (dTrailC != null) {
            dTrailC.set("disabled_trail", (trailPrefs != null && !trailPrefs.isEmpty()) ? trailPrefs : null);
            try { dTrailC.save(dTrailF); } catch (IOException e) { e.printStackTrace(); }
        }
    }
}