/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.api.object;

public enum Cause {
    // Bypass and trail Checks
    BYPASS_PERMISSION, INVISIBILITY_POTION, SPECTATOR_MODE, VANISH,
    // Always enable Checks
    ENCHANT, FLY_ALL, PERMISSION_REGION, PERMISSION_WORLD, SABER_FLY, FABLED_SKYBLOCK_FLY,
    // Always disable Checks
    COMBAT, NEARBY, HEIGHT_LIMIT,
    // Category specific Checks
    CATEGORY, TERRITORY,

    // Player-induced
    DISABLE_COMMAND
}
