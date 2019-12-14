/*
 * This file is part of FlightControl, which is licensed under the MIT License
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

package org.spazzinq.flightcontrol.hook;

import com.earth2me.essentials.Essentials;
import lombok.Getter;
import net.minelink.ctplus.CombatTagPlus;
import org.bukkit.plugin.PluginManager;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.hook.combat.*;
import org.spazzinq.flightcontrol.hook.lands.BaseLands;
import org.spazzinq.flightcontrol.hook.lands.Lands;
import org.spazzinq.flightcontrol.hook.plot.NewSquared;
import org.spazzinq.flightcontrol.hook.plot.OldSquared;
import org.spazzinq.flightcontrol.hook.plot.Plot;
import org.spazzinq.flightcontrol.hook.towny.BaseTowny;
import org.spazzinq.flightcontrol.hook.towny.Towny;
import org.spazzinq.flightcontrol.hook.vanish.EssentialsVanish;
import org.spazzinq.flightcontrol.hook.vanish.PremiumSuperVanish;
import org.spazzinq.flightcontrol.hook.vanish.Vanish;
import org.spazzinq.flightcontrol.multiversion.Factions;
import org.spazzinq.flightcontrol.multiversion.WorldGuard;
import org.spazzinq.flightcontrol.multiversion.current.Massive;
import org.spazzinq.flightcontrol.multiversion.current.Savage;
import org.spazzinq.flightcontrol.multiversion.current.WorldGuard7;
import org.spazzinq.flightcontrol.multiversion.old.UUID;
import org.spazzinq.flightcontrol.multiversion.old.WorldGuard6;

import java.util.ArrayList;
import java.util.List;

public class HookManager {
    private FlightControl flightControl;
    private PluginManager pm;
    private boolean is1_13;

    // Load early to prevent NPEs
    @Getter private WorldGuard worldGuard = new WorldGuard();
    @Getter private Vanish vanish = new Vanish();
    @Getter private BaseTowny towny = new BaseTowny();
    // TODO Config & check implementation
    @Getter private BaseLands lands = new BaseLands();
    @Getter private Combat combat = new Combat();
    @Getter private Factions factions = new Factions();
    @Getter private Plot plot = new Plot();

    private List<String> hooked = new ArrayList<>();

    public HookManager(FlightControl flightControl, boolean is1_13) {
        this.flightControl = flightControl;
        this.is1_13 = is1_13;
        pm = flightControl.getServer().getPluginManager();
    }

    public void load() {
        loadFactions();
        loadCombat();
        loadVanish();

        if (plEnabled("PlotSquared")) {
            plot = is1_13 ? new NewSquared() : new OldSquared();
        }
        if (plEnabled("WorldGuard")) {
            worldGuard = is1_13 ? new WorldGuard7() : new WorldGuard6();
        }
        if (plEnabled("Towny")) {
            towny = new Towny();
        }
        if (plEnabled("Lands")) {
            lands = new Lands(flightControl);
        }

        // Prepare hooked msg
        StringBuilder hookedMsg = new StringBuilder("Hooked with ");
        if (hooked.isEmpty()) {
            hookedMsg.append("no plugins.");
        } else {
            for (String hook : hooked) {
                hookedMsg.append(hook).append(", ");
            }
            hookedMsg.delete(hookedMsg.length() - 2, hookedMsg.length());
            hookedMsg.insert(hookedMsg.lastIndexOf(",") + 1, " and");
            hookedMsg.append(".");
        }

        // Hook msg
        flightControl.getLogger().info(hookedMsg.toString());
    }

    private void loadFactions() {
        if (plEnabled("Factions")) {
            if (pm.isPluginEnabled("MassiveCore")) {
                factions = new Massive();
            } else if (pm.getPlugin("Factions").getDescription().getAuthors().contains("ProSavage")) {
                factions = new Savage();
            } else {
                factions = new UUID();
            }
        }
    }

    private void loadCombat() {
        if (plEnabled("CombatLogX")) {
            combat = new LogX();
        }
        else if (plEnabled("CombatTagPlus")) {
            combat = new TagPlus(((CombatTagPlus) pm.getPlugin("CombatTagPlus")).getTagManager());
        }
        else if (plEnabled("AntiCombatLogging")) {
            combat = new AntiLogging();
        }
        else if (plEnabled("CombatLogPro")) {
            combat = new LogPro(pm.getPlugin("CombatLogPro"));
        }
        else if (plEnabled("DeluxeCombat")) {
            combat = new Deluxe();
        }
    }

    private void loadVanish() {
        if (plEnabled("PremiumVanish") || plEnabled("SuperVanish")) {
            vanish = new PremiumSuperVanish();
        }
        else if (plEnabled("Essentials")) {
            vanish = new EssentialsVanish((Essentials) pm.getPlugin("Essentials"));
        }
    }

    private boolean plEnabled(String pluginName) {
        boolean enabled = pm.isPluginEnabled(pluginName);
        if (enabled) hooked.add(pluginName);
        return enabled;
    }
}
