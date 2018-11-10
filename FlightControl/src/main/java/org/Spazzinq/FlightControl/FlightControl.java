package org.Spazzinq.FlightControl;

import org.Spazzinq.FlightControl.Multiversion.Regions;
import org.Spazzinq.FlightControl.Multiversion.v13.Regions13;
import org.Spazzinq.FlightControl.Multiversion.v8.Regions8;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class FlightControl extends org.bukkit.plugin.java.JavaPlugin {
    private Config c = new Config(this);
    Regions regions = Config.is13 ? new Regions13() : new Regions8();
    private boolean configWarning = true;

	public void onEnable() {
	    c.loadConfig(); new Listener(this);
        new Update(getDescription().getVersion());
        if (Update.exists()) Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> getLogger().info("Version " + Update.newVer() + " of FlightControl is available. " +
                "Perform /fc update to update and visit https://www.spigotmc.org/resources/flightcontrol.55168/ to view the changes."), 40);
    }
	public void onDisable() { c.disable(); }

	void msg(CommandSender s, String msg) {
	    if (s instanceof ConsoleCommandSender) msg = msg.replaceAll("FlightControl &7» ", "[FlightControl] ").replaceAll("»", "-");
	    if (msg != null && !msg.isEmpty()) s.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
	}

	public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("flightcontrol")) {
			if (s instanceof ConsoleCommandSender || s.hasPermission("flightcontrol.admin")) {
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						c.loadConfig();
						msg(s, "&a&lFlightControl &7» &aConfiguration successfully reloaded!");
					} else if (args[0].equalsIgnoreCase("combat")) {
						Config.useCombat = !Config.useCombat;
						toggleOption(s, Config.useCombat, "Automatic Combat Disabling");
					} else if (args[0].equalsIgnoreCase("falldamage")) {
						Config.cancelFall = !Config.cancelFall;
						toggleOption(s, Config.cancelFall, "Fall Damage Prevention");
					} else if (args[0].equalsIgnoreCase("vanishbypass")) {
						Config.vanishBypass = !Config.vanishBypass;
						toggleOption(s, Config.vanishBypass, "Vanish Bypass");
					} else if (args[0].equalsIgnoreCase("trails")) {
						Config.flightTrail = !Config.flightTrail;
						toggleOption(s, Config.flightTrail, "Trails");
					} else if (args[0].equalsIgnoreCase("update")) if (Update.exists()) {
					    boolean plugman = getServer().getPluginManager().isPluginEnabled("PlugMan");
					    if (!Update.dled) { Update.dl(); if (plugman) {
                            msg(s, "&a&lFlightControl &7» &aAutomatic installation in progress (you may need to reset the plugin config)...");
					        getServer().dispatchCommand(Bukkit.getConsoleSender(), "plugman reload FlightControl");
					    } else msg(s, "&a&lFlightControl &7» &aUpdate downloaded. Restart (or reload) the server to apply the update (you may need to reset the plugin config)."); }
					    else msg(s, "&a&lFlightControl &7» &aThe update to version " + Update.newVer() + " has already been downloaded. Please restart (or reload) the server to apply the update.");
                    } else msg(s, "&a&lFlightControl &7» &aNo updates found.");
					else if (args[0].equalsIgnoreCase("clean")) { saveConfig(); msg(s, "&a&lFlightControl &7» &a"); }
					else sendHelp(s);
				} else sendHelp(s);
				return true;
			} else msg(s, Config.permDenied);
		} else if (s instanceof Player && cmd.getName().equalsIgnoreCase("toggletrail")) {
			String uuid = ((Player) s).getUniqueId().toString();
			boolean o = Config.trailPrefs.contains(uuid);
			if (o) { Config.trailPrefs.remove(uuid); } else { Config.trailPrefs.add(uuid); }
			toggleOption(s, o, "Trail");
			return true;
		}
		return false;
	}

	private void toggleOption(CommandSender s, Boolean o, String prefix) {
		msg(s, (prefix.equals("Trail") ? "&a&l" : "&a&lFlightControl &a") + prefix + " &7» "
				+ (o ? "&aEnabled" : "&cDisabled"));
		if (!prefix.equals("Trail") && configWarning) {
		    msg(s, "&e&lFlightControl &eWarning &7» &fIn order to prevent the removal of documentation, the option has not been changed in the config. " +
                    "(Psst! You can quickly change it in the config then reload the plugin using /fc reload.)");
            configWarning = false;
		}
	}

	private void sendHelp(CommandSender s) {
		msg(s, "\n&a&lFlightControl &f" + getDescription().getVersion() + "\n" +
                "&aBy &fSpazzinq\n " +
                "\n&a/fc &7» &fFlightControl Help\n" +
                "&a/fc clean &7» &fRemove documentation in config (advanced users)\n" +
                "&a/fc update &7» &fUpdate FlightControl\n" +
                "&a/fc trails &7» &fToggle trails for the server\n" +
                "&a/fc combat &7» &fToggle automatic combat disabling\n" +
                "&a/fc falldamage &7» &fToggle fall damage prevention\n" +
                "&a/fc vanishbypass &7» &fToggle vanish bypass\n " +
                "\n&a/tt &7» &fPersonal trail toggle");
	}
}
