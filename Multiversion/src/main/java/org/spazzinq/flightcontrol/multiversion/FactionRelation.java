/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.multiversion;

public enum FactionRelation {
    DEFAULT, OWN, ALLY, TRUCE, NEUTRAL, ENEMY, WARZONE, SAFEZONE, WILDERNESS;

    public static FactionRelation getRelation(String relationName) {
        try {
            return valueOf(relationName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
