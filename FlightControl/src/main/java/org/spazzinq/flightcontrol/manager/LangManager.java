package org.spazzinq.flightcontrol.manager;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.CommentConf;
import org.spazzinq.flightcontrol.util.ActionbarUtil;

import java.io.File;

public class LangManager {
    private FlightControl pl;
    private CommentConf lang;
    private File langFile;

    // Player messages
    @Getter private String disableFlight;
    @Getter private String enableFlight;
    @Getter private String canEnableFlight;
    @Getter private String cannotEnableFlight;
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
    @Getter private String tempFlyUsage;

    public LangManager(FlightControl pl) {
        this.pl = pl;
        langFile = new File(pl.getDataFolder(), "lang.yml");
    }

    public void reloadLang() {
        lang = new CommentConf(langFile, pl.getResource("lang.yml"));

        // Migrates messages!
        if (pl.getConfManager().getConf().isConfigurationSection("messages")) {
            migrateFromVersion4();
        }

        disableFlight = lang.getString("player.flight.disabled");
        enableFlight = lang.getString("player.flight.enabled");
        canEnableFlight = lang.getString("player.flight.can_enable");
        cannotEnableFlight = lang.getString("player.flight.cannot_enable");
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
        tempFlyUsage = lang.getString("admin.tempfly.usage");
    }

    public static void msg(CommandSender s, String msg) {
        msg(s, msg, false);
    }
    public static void msg(CommandSender s, String msg, boolean actionBar) {
        if (msg != null && !msg.isEmpty()) {
            boolean console = s instanceof ConsoleCommandSender;
            String finalMsg = msg;

            finalMsg = ChatColor.translateAlternateColorCodes('&', finalMsg);

            if (actionBar && s instanceof Player) {
                ActionbarUtil.send((Player) s, finalMsg);
            } else {
                s.sendMessage((console ? "[FlightControl] " : "")
                        + finalMsg);
            }
        }
    }

    public static String replaceVar(String msg, String value, String varName) {
        return msg.replaceAll("%" + varName + "%", value);
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

        pl.getLogger().info("Successfully migrated the messages from config.yml to lang.yml!");
    }
}
