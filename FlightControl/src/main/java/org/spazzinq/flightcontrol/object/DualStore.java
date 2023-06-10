/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
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

    public void addEnabled(HashSet<T> checks) {
        enabled.addAll(checks);
    }

    @SafeVarargs public final void addDisabled(T... type) {
        disabled.addAll(Arrays.asList(type));
    }

    public void addDisabled(HashSet<T> checks) {
        disabled.addAll(checks);
    }

    public HashSet<T> get(boolean enabledOrDisabled) {
        return enabledOrDisabled ? enabled : disabled;
    }

    public boolean isEnabledEmpty() {
        return enabled.isEmpty();
    }

    public boolean isDisabledEmpty() {
        return disabled.isEmpty();
    }

    @Override public String toString() {
        return (enabled + "; " + disabled)
                // Remove unnecessary data from World toString
                .replaceAll("CraftWorld\\{name=", "").replaceAll("}", "");
    }
}
