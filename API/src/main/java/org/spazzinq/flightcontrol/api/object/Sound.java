/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2021 Spazzinq
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

package org.spazzinq.flightcontrol.api.object;

import lombok.Getter;
import org.bukkit.entity.Player;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class Sound {
    public static boolean playEveryEnable;
    public static boolean playEveryDisable;
    public static Sound enableSound;
    public static Sound disableSound;
    public static Sound canEnableSound;
    public static Sound cannotEnableSound;

    @Getter private final org.bukkit.Sound sound;
    @Getter private final float volume;
    @Getter private final float pitch;

    public Sound(org.bukkit.Sound sound) {
        this(sound, 1, 1);
    }

    public Sound(String name) {
        this(name, 1, 1);
    }

    public Sound(org.bukkit.Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public Sound(String name, float volume, float pitch) {
        this(org.bukkit.Sound.valueOf(name), volume, pitch);
    }

    public static void play(Player p, Sound s) {
        play(p, s, false);
    }

    public static void play(Player p, Sound s, boolean forcedEvent) {
        if (s != null) {
            p.playSound(p.getLocation(), s.sound, s.volume, s.pitch);
        }
    }

    public static void playEnable(Player p) {
        play(p, enableSound, false);
    }
    public static void playDisable(Player p) {
        play(p, disableSound, false);
    }
    public static void playCanEnable(Player p) {
        play(p, canEnableSound, false);
    }
    public static void playCannotEnable(Player p) {
        play(p, cannotEnableSound, false);
    }

    public static Sound valueOf(String str, double volume, double pitch) {
        try {
            String soundStr = str.toUpperCase().replaceAll("\\.", "_");
            org.bukkit.Sound.valueOf(str.toUpperCase().replaceAll("\\.", "_"));

            return new Sound(str, (float) volume, (float) pitch);
        } catch (Exception ignored) {}

        return null;
    }
}
