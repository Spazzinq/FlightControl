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

package org.Spazzinq.FlightControl.Multiversion.v13;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;

public class Particles13 implements org.Spazzinq.FlightControl.Multiversion.Particles {
    private Particle particle = Particle.CLOUD;
    private Particle.DustOptions o;
    private int amount = 4;
    private double extra, x, y, z;

    public void spawn(Location loc) {
        loc.getWorld().spawnParticle(particle, particle == Particle.CLOUD ? loc.clone().subtract(0, .3, 0) : loc, amount, x, y, z, extra, o, true);
    }
    public void setParticle(String s) {
        try { particle = Particle.valueOf(s); } catch (Exception ignored) { }
        switch (particle) {
            case REDSTONE: case SPELL_MOB: case SPELL_MOB_AMBIENT: case NOTE: extra = 1; break;
            default: extra = 0;
        }
    }
    public void setAmount(int amount) { this.amount = amount; }
    public void setRBG(int r, int g, int b) {
        x = 0; y = 0; z = 0; o = null;
        switch (particle) {
            case REDSTONE: o = new Particle.DustOptions(Color.fromRGB(r,g,b), amount); break;
            case SPELL_MOB: case SPELL_MOB_AMBIENT: { x = r/255d; y = g/255d; z = b/255d; break; }
            case NOTE: x = r/24.0; break;
        }
    }
}
