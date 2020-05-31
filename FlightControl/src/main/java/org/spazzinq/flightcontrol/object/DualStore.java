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

package org.spazzinq.flightcontrol.object;

import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;

public class DualStore<T> {
    @Getter private final HashSet<T> enabled;
    @Getter private final HashSet<T> disabled;

    public DualStore() {
        enabled = new HashSet<>();
        disabled = new HashSet<>();
    }

    @SafeVarargs public final void addEnabled(T... type) {
        enabled.addAll(Arrays.asList(type));
    }

    @SafeVarargs public final void addDisabled(T... type) {
        disabled.addAll(Arrays.asList(type));
    }

    public void addEnabled(HashSet<T> checks) {
        enabled.addAll(checks);
    }

    public void addDisabled(HashSet<T> checks) {
        disabled.addAll(checks);
    }


    @Override public String toString() {
        return (enabled + "; " + disabled)
                // Remove unnecessary data from World toString
                .replaceAll("CraftWorld\\{name=", "").replaceAll("}", "");
    }
}
