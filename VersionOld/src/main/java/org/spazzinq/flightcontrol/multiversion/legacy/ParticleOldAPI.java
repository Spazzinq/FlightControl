/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.multiversion.legacy;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.spazzinq.flightcontrol.multiversion.Particle;

public class ParticleOldAPI implements Particle {
    private Effect effect = Effect.CLOUD;
    private float r = 0, g = 0, b = 0, speed = 0F;
    private int amount = 4, data = 0;

    public void spawn(Location l) {
        //                    playEffect(to, effect, 0, data, r, g, b, speed, amount, 0);
        l.getWorld().spigot().playEffect(l, effect, 0, data, r, g, b, speed, amount, 160);
    }

    public void setParticle(String s) {
        try {
            Effect.valueOf(s);
        } catch (Exception e) {
            return;
        }
        if (Effect.valueOf(s).getType() == Effect.Type.PARTICLE) {
            effect = Effect.valueOf(s);
        }
        if (effect == Effect.COLOURED_DUST) {
            speed = 1;
            data = 1;
        } else {
            speed = 0;
            data = 0;
        }
    }

    public void setAmount(int amount) { this.amount = amount; }

    public void setRBG(int r, int g, int b) {
        if ((r == 0 && g == 0 && b == 0)) {
            this.r = 0;
            this.g = 0;
            this.b = 0;
        } else {
            this.r = (r / 255F) - 1;
            this.g = g / 255F;
            this.b = b / 255F;
        }
    }
}
