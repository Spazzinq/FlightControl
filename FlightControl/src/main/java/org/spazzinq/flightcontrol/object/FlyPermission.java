/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.object;

public enum FlyPermission {
    ADMIN("flightcontrol.admin"),
    BYPASS("flightcontrol.bypass"),
    NEARBYPASS("flightcontrol.nearbypass"),
    IGNORE("flightcontrol.ignore"),

    FLY_ALL("flightcontrol.flyall"),
    FLY_COMMAND("flightcontrol.flycommand"),

    CATEGORY_STUB("flightcontrol.category."),
    TEMP_FLY_STUB("flightcontrol.tempfly."),
    FLY_STUB("flightcontrol.fly."),
    NO_FLY_STUB("flightcontrol.nofly."),

    FLY_SPEED("flightcontrol.flyspeed"),
    FLY_SPEED_OTHERS("flightcontrol.flypseed.others"),

    TEMP_FLY("flightcontrol.tempfly"),
    TEMP_FLY_CHECK("flightcontrol.tempfly.check"),
    TEMP_FLY_OTHERS("flightcontrol.tempfly.others");

    private final String stringPermission;

    FlyPermission(String stringPermission) {
        this.stringPermission = stringPermission;
    }

    @Override public String toString() {
        return stringPermission;
    }
}
