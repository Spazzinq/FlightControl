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
    private File dTrailF;
	private FileConfiguration dTrailC;

	static boolean is13, useCombat, cancelFall, vanishBypass, flightTrail, actionBar;
	static Sound eSound, dSound;
	static String dFlight, eFlight, permDenied;
	static HashMap<String, List<String>> eRegions, dRegions;
	static List<String> eWorlds, dWorlds, trailPrefs;
    private static HashMap<String, AbstractMap.SimpleEntry<List<String>, List<String>>> categories = new HashMap<>();

	Config(FlightControl i) {
		pl = i;
		pm = pl.getServer().getPluginManager();
        is13 = pl.getServer().getVersion().contains("1.13");
		dTrailF = new File(pl.getDataFolder(), "disabled_trail.yml");
	}

	void loadConfig() {
	    pl.saveDefaultConfig();
		pl.reloadConfig();
		c = pl.getConfig();
		useCombat = c.getBoolean("settings.disable_flight_in_combat");
		cancelFall = c.getBoolean("settings.disable_fall_damage");
		vanishBypass = c.getBoolean("settings.vanish_bypass");
		flightTrail = c.getBoolean("settings.flight_trail");
		actionBar = c.getBoolean("messages.actionbar");
		dFlight = c.getString("messages.disable_flight");
		eFlight = c.getString("messages.enable_flight");
		permDenied = c.getString("messages.permission_denied");
        eWorlds = dWorlds = trailPrefs = new ArrayList<>(); eRegions = dRegions = new HashMap<>(); categories = new HashMap<>();
        loadWorlds(); loadSounds(); loadTrailPrefs();
        if (pm.isPluginEnabled("WorldGuard")) loadRegions();
		if (pm.isPluginEnabled("Factions")) loadFacCategories();
		for (World w : Bukkit.getWorlds()) { String name = w.getName(); defaultPerms(name); for (String rg : pl.regions.regions(w)) defaultPerms(name + "." + rg); }
	}

    public static HashMap<String, AbstractMap.SimpleEntry<List<String>, List<String>>> getCategories() { return categories; }

	private void defaultPerms(String suffix) {
        if (pm.getPermission("flightcontrol.fly." + suffix) == null)
            pm.addPermission(new Permission("flightcontrol.fly." + suffix, PermissionDefault.FALSE));
        if (pm.getPermission("flightcontrol.nofly." + suffix) == null)
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
            if (!typeKeys.isEmpty() && (typeKeys.contains("enable_flight") || typeKeys.contains("disable_flight")
                    || type.equals("enable_flight") || type.equals("disable_flight"))) return typeS;
        }
        return null;
    }

    private void loadWorlds() {
	    ConfigurationSection worlds = load(c,"worlds");
	    if (worlds != null) {
	        eWorlds = addWorlds(worlds.getStringList("enable_flight"));
	        dWorlds = addWorlds(worlds.getStringList("disable_flight"));
        }
    }

    private void loadRegions() {
        ConfigurationSection regions = load(c,"regions");
        if (regions != null) {
            eRegions = addRegions(load(regions, "enable_flight"));
            dRegions = addRegions(load(regions, "disable_flight"));
        }
    }

	private void loadFacCategories() {
	    ConfigurationSection facs = load(c,"factions");
	    if (facs != null) for (String cName : facs.getKeys(false)) {
            if (pm.getPermission("flightcontrol.factions." + cName) == null) pm.addPermission(new Permission("flightcontrol.factions." + cName, PermissionDefault.FALSE));
            ConfigurationSection category = load(facs, cName);
            if (category != null) {
                Config.categories.put(cName, new AbstractMap.SimpleEntry<>(
                        typeValidator(category.getStringList("enable_flight")),
                        typeValidator(category.getStringList("disable_flight"))));
            }
	    }
	}

    private void loadSounds() {
	    String e = c.getString("sounds.enable_flight.sound").toUpperCase().replaceAll("\\.", "_"),
                d = c.getString("sounds.disable_flight.sound").toUpperCase().replaceAll("\\.", "_");
        if (Sound.is(e)) eSound = new Sound(e, (float) c.getDouble("sounds.enable_flight.volume"), (float) c.getDouble("sounds.enable_flight.pitch")); else eSound = null;
        if (Sound.is(d)) dSound = new Sound(d, (float) c.getDouble("sounds.disable_flight.volume"), (float) c.getDouble("sounds.disable_flight.pitch")); else dSound = null;
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
        if (c != null) {
            for (String w : c.getKeys(false)) {
                if (Bukkit.getWorld(w) != null) {
                    ArrayList<String> rgs = new ArrayList<>();
                    for (String rg : c.getStringList(w)) {
                        if (pl.regions.hasRegion(w, rg)) rgs.add(rg);
                    }
                    regions.put(w, rgs);
                }
            }
        }
        return regions;
    }

	private List<String> typeValidator(List<String> types) {
		if (types != null && !types.isEmpty()) {
			ArrayList<String> toRemove = new ArrayList<>();
			for (String type : types) {
				if (type == null || type.isEmpty()
						|| !(type.contains("OWN") || type.contains("ALLY") || type.contains("TRUCE")
								|| type.contains("NEUTRAL") || type.contains("ENEMY") || type.contains("WARZONE")
								|| type.contains("SAFEZONE") || type.contains("WILDERNESS"))) toRemove.add(type);
			}
			types.removeAll(toRemove);
		}
		return types;
	}

    // Saves personal trail preferences
	void disable() {
        dTrailC.set("disabled_trail", (trailPrefs != null && !trailPrefs.isEmpty()) ? trailPrefs : null);
        try { dTrailC.save(dTrailF); } catch (IOException e) { e.printStackTrace(); }
    }
}