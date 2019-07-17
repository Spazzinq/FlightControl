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

package org.spazzinq.flightcontrol.objects;

public final class Category {
    public final boolean blacklist, own, ally, truce, neutral, enemy, warzone, safezone, wilderness;
    private String debug;

    public Category(boolean blacklist, boolean own, boolean ally, boolean truce, boolean neutral, boolean enemy, boolean warzone, boolean safezone, boolean wilderness) {
        this.blacklist = blacklist; this.own = own; this.ally = ally; this.truce = truce; this.neutral = neutral; this.enemy = enemy; this.warzone = warzone;
        this.safezone = safezone; this.wilderness = wilderness;

        debug = blacklist + " [" + (own ? "own," : "") + (ally ? "ally," : "") + (truce ? "truce," : "") + (neutral ? "neutral," : "")
                + (enemy ? "enemy," : "") + (warzone ? "warzone," : "") + (safezone ? "safezone," : "") + (wilderness ? "wilderness," : "");
        debug = debug.substring(0, debug.length() - 1) + "]";
    }

    @Override
    public String toString() {
        return debug;
    }
}
