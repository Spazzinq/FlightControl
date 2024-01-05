/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.object;

import java.util.ArrayList;
import java.util.Set;

public enum TempflyTask {
    HELP, CHECK, ADD, REMOVE, SET, DISABLE;

    public static final ArrayList<String> types = new ArrayList<>();
    private static final Set<TempflyTask> modifyDuration = Set.of(ADD, REMOVE, SET, DISABLE);

    static {
        for (TempflyTask type : values()) {
            String name = type.name().toLowerCase();

            types.add(name);
        }
    }

    public static TempflyTask getTaskType(String name) {
        TempflyTask type;

        try {
            type = TempflyTask.valueOf(name);
        } catch (IllegalArgumentException e) {
            type = TempflyTask.CHECK;
        }

        return type;
    }

    public static boolean modifiesDuration(TempflyTask task) {
        return modifyDuration.contains(task);
    }
}
