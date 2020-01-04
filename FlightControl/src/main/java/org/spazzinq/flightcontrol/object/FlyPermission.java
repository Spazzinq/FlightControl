package org.spazzinq.flightcontrol.object;

public enum FlyPermission {
    ADMIN("flightcontrol.admin"),
    BYPASS("flightcontrol.bypass"),

    FLY_ALL("flightcontrol.flyall"),

    CATEGORY_STUB("flightcontrol.category."),
    FLY_STUB("flightcontrol.fly."),
    NO_FLY_STUB("flightcontrol.nofly."),

    TOWNY_OWN("flightcontrol.towny.own"),
    TOWNY_OLD("flightcontrol.owntown"),

    // TODO Do I need to check the parent permission too?
    // TODO Implement added
    LANDS_OWN("flightcontrol.lands.own"),
    LANDS_TRUSTED("flightcontrol.lands.trusted"),
    LANDS_OLD("flightcontrol.ownland"),

    FLY_SPEED("flightcontrol.flyspeed"),
    FLY_SPEED_OTHERS("flightcontrol.flypseed.others"),

    TEMP_FLY("flightcontrol.tempfly"),
    TEMP_FLY_OTHERS("flightcontrol.tempfly.others");

    private String stringPermission;

    FlyPermission(String stringPermission) {
        this.stringPermission = stringPermission;
    }

    @Override public String toString() {
        return stringPermission;
    }
}
