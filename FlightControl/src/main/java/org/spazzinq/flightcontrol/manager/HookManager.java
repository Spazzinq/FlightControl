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
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.hook.combat.*;
import org.spazzinq.flightcontrol.hook.enchantment.CrazyEnchantmentsHook;
import org.spazzinq.flightcontrol.hook.enchantment.EnchantsHookBase;
import org.spazzinq.flightcontrol.hook.placeholder.ClipPlaceholder;
import org.spazzinq.flightcontrol.hook.placeholder.MVdWPlaceholder;
import org.spazzinq.flightcontrol.hook.territory.*;
import org.spazzinq.flightcontrol.hook.vanish.EssentialsVanishHook;
import org.spazzinq.flightcontrol.hook.vanish.PremiumSuperVanishHook;
import org.spazzinq.flightcontrol.hook.vanish.VanishHookBase;
import org.spazzinq.flightcontrol.multiversion.FactionsHookBase;
import org.spazzinq.flightcontrol.multiversion.WorldGuardHookBase;
import org.spazzinq.flightcontrol.multiversion.current.MassiveFactionsHook;
import org.spazzinq.flightcontrol.multiversion.current.SavageFactionsHook;
import org.spazzinq.flightcontrol.multiversion.current.WorldGuardHook7;
import org.spazzinq.flightcontrol.multiversion.legacy.FactionsUUIDHook;
import org.spazzinq.flightcontrol.multiversion.legacy.WorldGuardHook6;

import java.util.ArrayList;
import java.util.HashSet;

public class HookManager {
    private final FlightControl pl;
    private final PluginManager pm;
    private final boolean is1_13;

    // Load early to prevent NPEs
    @Getter private WorldGuardHookBase worldGuardHook = new WorldGuardHookBase();
    @Getter private VanishHookBase vanishHook = new VanishHookBase();
    @Getter private CombatHookBase combatHook = new CombatHookBase();
    @Getter private EnchantsHookBase enchantmentsHook = new EnchantsHookBase();
    @Getter private FactionsHookBase factionsHook = new FactionsHookBase();
    @Getter private TerritoryHookBase plotHook = new TerritoryHookBase();
    @Getter private HashSet<TerritoryHookBase> territoryHooks = new HashSet<>();

    @Getter private String hookedMsg;
    private final ArrayList<String> hooked = new ArrayList<>();

    public HookManager(FlightControl pl, boolean is1_13) {
        this.pl = pl;
        this.is1_13 = is1_13;
        pm = pl.getServer().getPluginManager();
    }

    public void loadHooks() {
        loadFactionsHooks();
        loadCombatHooks();
        loadVanishHooks();
        loadPlaceholderHooks();
        loadTerritoryHooks();

        if (pluginLoading("WorldGuard")) {
            worldGuardHook = is1_13 ? new WorldGuardHook7() : new WorldGuardHook6();
        }
        if (pluginLoading("CrazyEnchantments") && pm.getPlugin("CrazyEnchantments").getDescription().getVersion().startsWith("1.8")) {
            enchantmentsHook = new CrazyEnchantmentsHook();
        }

        loadHookMsg();
        pl.getLogger().info(hookedMsg);
    }

    private void loadFactionsHooks() {
        if (pluginLoading("Factions")) {
            if (pm.isPluginEnabled("MassiveCore")) {
                factionsHook = new MassiveFactionsHook();
            } else if (pm.getPlugin("Factions").getDescription().getAuthors().contains("ProSavage")) {
                factionsHook = new SavageFactionsHook();
            } else {
                factionsHook = new FactionsUUIDHook();
            }
        }
    }

    private void loadCombatHooks() {
        if (pluginLoading("CombatLogX")) {
            String version = pm.getPlugin("CombatLogX").getDescription().getVersion();
            boolean versionTen = version != null && version.startsWith("10.");

            combatHook = versionTen ? new CombatLogX10Hook() : new CombatLogX9Hook();
        } else if (pluginLoading("CombatTagPlus")) {
            combatHook = new CombatTagPlusHook(((CombatTagPlus) pm.getPlugin("CombatTagPlus")).getTagManager());
        } else if (pluginLoading("AntiCombatLogging")) {
            combatHook = new AntiCombatLoggingHook();
        } else if (pluginLoading("CombatLogPro")) {
            combatHook = new CombatLogProHook(pm.getPlugin("CombatLogPro"));
        } else if (pluginLoading("DeluxeCombat")) {
            combatHook = new DeluxeCombatHook();
        }
    }

    private void loadVanishHooks() {
        if (pluginLoading("PremiumVanish") || pluginLoading("SuperVanish")) {
            vanishHook = new PremiumSuperVanishHook();
        } else if (pluginLoading("Essentials")) {
            vanishHook = new EssentialsVanishHook((Essentials) pm.getPlugin("Essentials"));
        }
    }

    private void loadPlaceholderHooks() {
        if (pluginLoading("PlaceholderAPI")) {
            new ClipPlaceholder(pl).register();
        }
        if (pluginLoading("MVdWPlaceholderAPI")) {
            new MVdWPlaceholder(pl);
        }
    }

    private void loadTerritoryHooks() {
        if (pluginLoading("PlotSquared")) {
            String version = pm.getPlugin("PlotSquared").getDescription().getVersion().split("\\.")[0];

            switch (version) {
                case "5":
                    territoryHooks.add(new PlotSquaredFiveHook());
                    break;
                case "4":
                    territoryHooks.add(new PlotSquaredFourHook());
                    break;
                default:
                    territoryHooks.add(new PlotSquaredThreeHook());
                    break;
            }

        }
        if (pluginLoading("Towny")) {
            territoryHooks.add(new TownyHook());
        }
        if (pluginLoading("Lands")) {
            territoryHooks.add(new LandsHook(pl));
        }
        if (pluginLoading("GriefPrevention")) {
            territoryHooks.add(new GriefPreventionHook());
        }
    }

    private void loadHookMsg() {
        // Prepare hooked msg
        StringBuilder hookMsg = new StringBuilder("Hooked with ");
        if (hooked.isEmpty()) {
            hookMsg.append("no plugins.");
        } else {
            for (int i = 0; i < hooked.size(); i++) {
                if (i != 0) {
                    hookMsg.append(", ");
                }
                if (i == hooked.size() - 1) {
                    hookMsg.append("and ");
                }
                hookMsg.append(hooked.get(i));
            }
            hookMsg.append(".");
        }

        hookedMsg = hookMsg.toString();
    }

    private boolean pluginLoading(String pluginName) {
        // Problematic as isPluginEnabled
        boolean enabled = pm.getPlugin(pluginName) != null;

        if (enabled) {
            hooked.add(pluginName);
        }

        return enabled;
    }
}