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

import com.earth2me.essentials.Essentials;
import lombok.Getter;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.hook.combat.*;
import org.spazzinq.flightcontrol.hook.enchantment.CrazyEnchantmentsHook;
import org.spazzinq.flightcontrol.hook.enchantment.EnchantsHook;
import org.spazzinq.flightcontrol.hook.griefprevention.GriefPreventionBase;
import org.spazzinq.flightcontrol.hook.griefprevention.GriefPreventionHook;
import org.spazzinq.flightcontrol.hook.lands.LandsBase;
import org.spazzinq.flightcontrol.hook.lands.LandsHook;
import org.spazzinq.flightcontrol.hook.plot.NewPlotSquaredHook;
import org.spazzinq.flightcontrol.hook.plot.OldPlotSquaredHook;
import org.spazzinq.flightcontrol.hook.plot.PlotHook;
import org.spazzinq.flightcontrol.hook.towny.TownyBase;
import org.spazzinq.flightcontrol.hook.towny.TownyHook;
import org.spazzinq.flightcontrol.hook.vanish.EssentialsVanishHook;
import org.spazzinq.flightcontrol.hook.vanish.PremiumSuperVanishHook;
import org.spazzinq.flightcontrol.hook.vanish.VanishHook;
import org.spazzinq.flightcontrol.multiversion.FactionsHook;
import org.spazzinq.flightcontrol.multiversion.WorldGuardHook;
import org.spazzinq.flightcontrol.multiversion.current.MassiveFactionsHook;
import org.spazzinq.flightcontrol.multiversion.current.SavageFactionsHook;
import org.spazzinq.flightcontrol.multiversion.current.WorldGuardHook7;
import org.spazzinq.flightcontrol.multiversion.old.FactionsUUIDHook;
import org.spazzinq.flightcontrol.multiversion.old.WorldGuardHook6;

import java.util.HashSet;
import java.util.Set;

public class HookManager {
    private final FlightControl pl;
    private final PluginManager pm;
    private final boolean is1_13;

    // Load early to prevent NPEs
    @Getter private WorldGuardHook worldGuardHook = new WorldGuardHook();
    @Getter private VanishHook vanishHook = new VanishHook();
    @Getter private TownyBase townyHook = new TownyBase();
    @Getter private LandsBase landsHook = new LandsBase();
    @Getter private CombatHook combatHook = new CombatHook();
    @Getter private EnchantsHook enchantmentsHook = new EnchantsHook();
    @Getter private FactionsHook factionsHook = new FactionsHook();
    @Getter private PlotHook plotHook = new PlotHook();
    @Getter private GriefPreventionBase griefPreventionHook = new GriefPreventionBase();

    @Getter private String hookedMsg;
    private final Set<String> hooked = new HashSet<>();

    public HookManager(FlightControl pl, boolean is1_13) {
        this.pl = pl;
        this.is1_13 = is1_13;
        pm = pl.getServer().getPluginManager();
    }

    public void load() {
        boolean factionsEnabled = loadFactions();
        loadCombat();
        loadVanish();

        if (pluginEnabled("PlotSquared")) {
            plotHook = is1_13 ? new NewPlotSquaredHook() : new OldPlotSquaredHook();
        }
        if (pluginEnabled("WorldGuard")) {
            worldGuardHook = is1_13 ? new WorldGuardHook7() : new WorldGuardHook6();
        }
        if (pluginEnabled("Towny")) {
            townyHook = new TownyHook();
        }
        if (pluginEnabled("Lands")) {
            landsHook = new LandsHook(pl);
        }
        if (pluginEnabled("CrazyEnchantments")) {
            enchantmentsHook = new CrazyEnchantmentsHook();
        }
        if (pluginEnabled("GriefPrevention")) {
            griefPreventionHook = new GriefPreventionHook();
        }

        loadHookMsg();
        pl.getLogger().info(hookedMsg);

        if (!factionsEnabled) {
            pl.getLogger().warning("Factions not detected. FlightControl will attempt to hook again in a few seconds...");
            new BukkitRunnable() {
                @Override
                public void run() {
                    boolean factionsEnabled = loadFactions();
                    if (factionsEnabled) {
                        loadHookMsg();
                    }
                    pl.getLogger().info(factionsEnabled ? "Hooked with Factions!" : "Still did not detect Factions.");
                }
            }.runTaskLater(pl,  40);
        }
    }

    private boolean loadFactions() {
        boolean factionsEnabled = pluginEnabled("Factions");

        if (factionsEnabled) {
            if (pm.isPluginEnabled("MassiveCore")) {
                factionsHook = new MassiveFactionsHook();
            } else if (pm.getPlugin("Factions").getDescription().getAuthors().contains("ProSavage")) {
                factionsHook = new SavageFactionsHook();
            } else {
                factionsHook = new FactionsUUIDHook();
            }
        }
        return factionsEnabled;
    }

    private void loadCombat() {
        if (pluginEnabled("CombatLogX")) {
            String version = pm.getPlugin("CombatLogX").getDescription().getVersion();
            boolean versionTen = version != null && version.startsWith("10.");

            combatHook = versionTen ? new CombatLogX10Hook() : new CombatLogX9Hook();
        }
        else if (pluginEnabled("CombatTagPlus")) {
            combatHook = new CombatTagPlusHook(((CombatTagPlus) pm.getPlugin("CombatTagPlus")).getTagManager());
        }
        else if (pluginEnabled("AntiCombatLogging")) {
            combatHook = new AntiCombatLoggingHook();
        }
        else if (pluginEnabled("CombatLogPro")) {
            combatHook = new CombatLogProHook(pm.getPlugin("CombatLogPro"));
        }
        else if (pluginEnabled("DeluxeCombat")) {
            combatHook = new DeluxeCombatHook();
        }
    }

    private void loadVanish() {
        if (pluginEnabled("PremiumVanish") || pluginEnabled("SuperVanish")) {
            vanishHook = new PremiumSuperVanishHook();
        }
        else if (pluginEnabled("Essentials")) {
            vanishHook = new EssentialsVanishHook((Essentials) pm.getPlugin("Essentials"));
        }
    }

    private void loadHookMsg() {
        // Prepare hooked msg
        StringBuilder hookMsg = new StringBuilder("Hooked with ");
        if (hooked.isEmpty()) {
            hookMsg.append("no plugins.");
        } else {
            for (String hook : hooked) {
                hookMsg.append(hook).append(", ");
            }
            hookMsg.delete(hookMsg.length() - 2, hookMsg.length());
            hookMsg.insert(hookMsg.lastIndexOf(",") + 1, " and");
            hookMsg.append(".");
        }

        hookedMsg = hookMsg.toString();
    }

    private boolean pluginEnabled(String pluginName) {
        boolean enabled = pm.isPluginEnabled(pluginName);

        if (enabled) {
            hooked.add(pluginName);
        }
        return enabled;
    }
}
