/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2024 George Fang
 */

package org.spazzinq.flightcontrol.api.object;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.spazzinq.flightcontrol.api.APIManager;

import java.lang.reflect.Method;

public class HandlerMethod implements Comparable<HandlerMethod> {
    @Getter private final Class<?> eventClass;
    @Getter private final FlightListener listener;
    @Getter private final Method method;
    @Getter private final FlightEventHandler.Priority priority;

    public HandlerMethod(FlightListener listener, Method method) {
        method.setAccessible(true);
        this.listener = listener;
        this.method = method;

        if (method.isAnnotationPresent(FlightEventHandler.class)) {
            if (method.getParameterCount() == 1) {
                Class<?> firstParameter = method.getParameterTypes()[0];
                if (APIManager.getInstance().getEvents().contains(firstParameter)) {
                    eventClass = firstParameter;
                } else {
                    throw new IllegalArgumentException("Method does not contain a specific Event parameter");
                }
            } else {
                throw new IllegalArgumentException("Method does not have only one parameter");
            }

            priority = method.getAnnotation(FlightEventHandler.class).priority();
        } else {
            throw new IllegalArgumentException("EventHandler annotation is not present");
        }
    }

    /**
     * Returns the method's plugin owner.
     *
     * @return the method's plugin owner
     */
    public Plugin getPlugin() { return getListener().getPlugin(); }

    @Override public int compareTo(HandlerMethod o) {
        return priority.compareTo(o.priority);
    }
}
