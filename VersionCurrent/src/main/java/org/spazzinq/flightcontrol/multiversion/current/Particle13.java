/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion.current;

import org.bukkit.Color;
import org.bukkit.Location;
import org.spazzinq.flightcontrol.multiversion.Particle;

public class Particle13 implements Particle {
    private org.bukkit.Particle particle = org.bukkit.Particle.CLOUD;
    private org.bukkit.Particle.DustOptions o;
    private int amount = 4;
    private double extra, x, y, z;

    public void spawn(Location loc) {
        if (loc.getWorld() != null) {
            loc.getWorld().spawnParticle(particle, particle == org.bukkit.Particle.CLOUD ? loc.clone().subtract(0, .3, 0) : loc,
                    amount, x, y, z, extra, o, true);
        }
    }

    public void setParticle(String s) {
        try {
            particle = org.bukkit.Particle.valueOf(s);
        } catch (Exception ignored) {
        }
        switch (particle) {
            case REDSTONE:
            case SPELL_MOB:
            case SPELL_MOB_AMBIENT:
            case NOTE:
                extra = 1;
                break;
            default:
                extra = 0;
                break;
        }
    }

    public void setAmount(int amount) { this.amount = amount; }

    public void setRBG(int r, int g, int b) {
        x = 0;
        y = 0;
        z = 0;
        o = null;
        switch (particle) {
            case REDSTONE:
                o = new org.bukkit.Particle.DustOptions(Color.fromRGB(r, g, b), amount);
                break;
            case SPELL_MOB:
            case SPELL_MOB_AMBIENT: {
                x = r / 255d;
                y = g / 255d;
                z = b / 255d;
                break;
            }
            case NOTE:
                x = r / 24.0;
                break;
            default:
                break;
        }
    }
}
