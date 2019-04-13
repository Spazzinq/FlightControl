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

package org.Spazzinq.FlightControl.Multiversion.v8;

import org.bukkit.Effect;
import org.bukkit.Location;

public class Particles8 implements org.Spazzinq.FlightControl.Multiversion.Particles {
    private Effect e = Effect.CLOUD;
    private float r = 0, g = 0, b = 0, speed = 0F;
    private int amount = 4, data = 0;

    public void play(Location l) {
        //                    playEffect(to, e, 0, data, r, g, b, speed, amount, 0);
        l.getWorld().spigot().playEffect(l, e, 0, data, r, g, b, speed, amount, 160);
    }
    public void setParticle(String s) {
        try { Effect.valueOf(s); } catch (Exception e) { return; }
        if (Effect.valueOf(s).getType() == Effect.Type.PARTICLE) e = Effect.valueOf(s);
        if (e == Effect.COLOURED_DUST) { speed = 1; data = 1; } else { speed = 0; data = 0; }
    }
    public void setAmount(int amount) { this.amount = amount; }
    public void setRBG(int r, int g, int b) { if ((r == 0 && g == 0 && b == 0)) { this.r=0;this.g=0;this.b=0; } else { this.r = (r / 255F) - 1; this.g = g / 255F; this.b = b / 255F; } }
}
