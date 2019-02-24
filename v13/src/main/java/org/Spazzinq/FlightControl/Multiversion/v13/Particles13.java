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
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Particles13 implements org.Spazzinq.FlightControl.Multiversion.Particles {
    private Particle particle = Particle.CLOUD;
    private Particle.DustOptions o;
    private int amount = 4, extra = 0;

    public void play(World w, Player p, Location to, Location from) {
        //                       loc   amnt    x      y      z
        p.spawnParticle(particle, to, 1, 0, 0, 0, extra, o);
        w.spawnParticle(particle, from.clone().subtract(to.clone().subtract(from)), 1, 0, 0, 0, extra, o);
    }
    public void setParticle(String s) {
        try { particle = Particle.valueOf(s); } catch (Exception ignored) { }
        if (particle == Particle.REDSTONE) extra = 1; else extra = 0;
    }
    public void setAmount(int amount) { this.amount = amount; }
    public void setRBG(int r, int g, int b) { if (particle == Particle.REDSTONE) o = new Particle.DustOptions(Color.fromRGB(r,g,b), amount); else o = null; }
}
