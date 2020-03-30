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

public enum FlyPermission {
    ADMIN("flightcontrol.admin"),
    BYPASS("flightcontrol.bypass"),
    NEARBYPASS("flightcontrol.nearbypass"),

    FLY_ALL("flightcontrol.flyall"),

    CATEGORY_STUB("flightcontrol.category."),
    FLY_STUB("flightcontrol.fly."),
    NO_FLY_STUB("flightcontrol.nofly."),

    TOWNY_OWN("flightcontrol.towny.own"),
    TOWNY_OLD("flightcontrol.owntown"),

    // TODO Do I need to check the parent permission too?
    LANDS_OWN("flightcontrol.lands.own"),
    LANDS_TRUSTED("flightcontrol.lands.trusted"),
    LANDS_OLD("flightcontrol.ownland"),

    CLAIM_OWN("flightcontrol.claim.own"),
    CLAIM_TRUSTED("flightcontrol.claim.trusted"),

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
