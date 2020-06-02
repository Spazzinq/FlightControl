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

import lombok.Getter;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.check.Check;
import org.spazzinq.flightcontrol.check.always.*;
import org.spazzinq.flightcontrol.check.bypasstrail.BypassPermissionCheck;
import org.spazzinq.flightcontrol.check.bypasstrail.InvisibilityPotionCheck;
import org.spazzinq.flightcontrol.check.bypasstrail.SpectatorModeCheck;
import org.spazzinq.flightcontrol.check.bypasstrail.vanish.EssentialsVanishCheck;
import org.spazzinq.flightcontrol.check.bypasstrail.vanish.PremiumSuperVanishCheck;
import org.spazzinq.flightcontrol.check.combat.*;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;
import org.spazzinq.flightcontrol.check.territory.own.*;
import org.spazzinq.flightcontrol.check.territory.trusted.*;
import org.spazzinq.flightcontrol.object.DualStore;

import java.util.HashSet;
import java.util.TreeMap;

public class CheckManager {
    private final FlightControl pl;
    private final PluginManager pm;

    @Getter private final DualStore<Check> alwaysChecks = new DualStore<>();
    @Getter private final HashSet<Check> bypassChecks = new HashSet<>();
    @Getter private final HashSet<Check> trailChecks = new HashSet<>();

    @Getter private final TreeMap<String, TerritoryCheck> ownTerritoryChecks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    @Getter private final TreeMap<String, TerritoryCheck> trustedTerritoryChecks = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    @Getter private String checksMsg;

    public CheckManager(FlightControl pl) {
        this.pl = pl;
        pm = pl.getServer().getPluginManager();
    }

    public void loadChecks() {
        alwaysChecks.getEnabled().clear();
        alwaysChecks.getDisabled().clear();
        bypassChecks.clear();
        ownTerritoryChecks.clear();
        trustedTerritoryChecks.clear();

        loadBypassAndTrailChecks();
        loadAlwaysChecks();
        loadTerritoryChecks();

        printLoadedChecks();
    }

    private void loadBypassAndTrailChecks() {
        /* ENABLE */
        // Bypass perm Check
        bypassChecks.add(new BypassPermissionCheck());
        // Spectator Check
        SpectatorModeCheck spectatorModeCheck = new SpectatorModeCheck();
        bypassChecks.add(spectatorModeCheck);
        trailChecks.add(spectatorModeCheck);
        // Vanish Checks
        HashSet<Check> vanishChecks = new HashSet<>();

        if (pluginLoading("PremiumVanish") || pluginLoading("SuperVanish")) {
            vanishChecks.add(new PremiumSuperVanishCheck());
        } else if (pluginLoading("Essentials")) {
            vanishChecks.add(new EssentialsVanishCheck());
        }

        if (!vanishChecks.isEmpty() && pl.getConfManager().isVanishBypass()) {
            bypassChecks.addAll(vanishChecks);
            trailChecks.addAll(vanishChecks);
        }
        // Invisibility potion Check
        trailChecks.add(new InvisibilityPotionCheck());
        /* DISABLE EMPTY */
    }

    private void loadAlwaysChecks() {
        /* ENABLE */
        // Flyall perm Check, Tempfly Check
        alwaysChecks.addEnabled(
                new FlyAllCheck(),
                new TempFlyCheck(pl.getPlayerManager())
        );
        // CrazyEnchants Check
        if (pluginLoading("CrazyEnchantments") && pm.getPlugin("CrazyEnchantments").getDescription().getVersion().startsWith("1.8")) {
            alwaysChecks.addEnabled(new CrazyEnchantmentsCheck());
        }

        /* DISABLE */
        // Combat Checks
        loadCombatChecks();
        // Nearby Checks
        if (pl.getConfManager().isNearbyCheck()) {
            alwaysChecks.addDisabled(pl.getConfManager().isNearbyCheckEnemies() ? new NearbyEnemyCheck(pl) : new NearbyCheck(pl));
        }
    }

    private void loadTerritoryChecks() {
        if (pluginLoading("PlotSquared")) {
            String version = pm.getPlugin("PlotSquared").getDescription().getVersion().split("\\.")[0];

            switch (version) {
                case "5":
                    ownTerritoryChecks.put("PlotSquared", new PlotSquared5OwnCheck());
                    trustedTerritoryChecks.put("PlotSquared", new PlotSquared5TrustedCheck());
                    break;
                case "4":
                    ownTerritoryChecks.put("PlotSquared", new PlotSquared4OwnCheck());
                    trustedTerritoryChecks.put("PlotSquared", new PlotSquared4TrustedCheck());
                    break;
                default:
                    ownTerritoryChecks.put("PlotSquared", new PlotSquared3OwnCheck());
                    trustedTerritoryChecks.put("PlotSquared", new PlotSquared3TrustedCheck());
                    break;
            }

        }
        if (pluginLoading("Towny")) {
            // trusted == own
            ownTerritoryChecks.put("Towny", new TownyCheck());
            trustedTerritoryChecks.put("Towny", new TownyCheck());
        }
        if (pluginLoading("Lands")) {
            ownTerritoryChecks.put("Lands", new LandsOwnCheck(pl));
            trustedTerritoryChecks.put("Lands", new LandsTrustedCheck(pl));
        }
        if (pluginLoading("GriefPrevention")) {
            ownTerritoryChecks.put("GriefPrevention", new GriefPreventionOwnCheck());
            trustedTerritoryChecks.put("GriefPrevention", new GriefPreventionTrustedCheck());
        }
        if (pluginLoading("RedProtect")) {
            ownTerritoryChecks.put("RedProtect", new RedProtectOwnCheck());
            trustedTerritoryChecks.put("RedProtect", new RedProtectTrustedCheck());
        }
        if (pluginLoading("BentoBox")) {
            ownTerritoryChecks.put("BentoBox", new BentoBoxOwnCheck());
            trustedTerritoryChecks.put("BentoBox", new BentoBoxTrustedCheck());
        }
    }

    private void loadCombatChecks() {
        HashSet<Check> combatChecks = new HashSet<>();

        if (pluginLoading("CombatLogX")) {
            String version = pm.getPlugin("CombatLogX").getDescription().getVersion();
            boolean versionTen = version != null && version.startsWith("10.");

            combatChecks.add(versionTen ? new CombatLogX10Check() : new CombatLogX9Check());
        } else if (pluginLoading("CombatTagPlus")) {
            combatChecks.add(new CombatTagPlusCheck());
        } else if (pluginLoading("AntiCombatLogging")) {
            combatChecks.add(new AntiCombatLoggingCheck());
        } else if (pluginLoading("CombatLogPro")) {
            combatChecks.add(new CombatLogProCheck());
        } else if (pluginLoading("DeluxeCombat")) {
            combatChecks.add(new DeluxeCombatCheck());
        } else if (pluginLoading("PvPManager")) {
            combatChecks.add(new PvPManagerCheck());
        }

        if (!combatChecks.isEmpty() && pl.getConfManager().isCombatChecked()) {
            alwaysChecks.addDisabled(combatChecks);
        }
    }

    private void printLoadedChecks() {
        HashSet<Check> checks = new HashSet<>(bypassChecks);
        checks.addAll(alwaysChecks.getEnabled());
        checks.addAll(alwaysChecks.getDisabled());
        checks.addAll(ownTerritoryChecks.values());
        checks.addAll(trustedTerritoryChecks.values());

        StringBuilder loadedChecksMsg = new StringBuilder("Loaded the following checks: ");

        for (Check check : checks) {
            loadedChecksMsg.append(check).append(", ");
        }
        loadedChecksMsg.delete(loadedChecksMsg.length() - 2, loadedChecksMsg.length());
        loadedChecksMsg.append(".");

        pl.getLogger().info(checksMsg = loadedChecksMsg.toString());
    }

    private boolean pluginLoading(String pluginName) {
        // Don't use .isPluginEnabled()--plugin might not yet be ready
        return pm.getPlugin(pluginName) != null;
    }
}