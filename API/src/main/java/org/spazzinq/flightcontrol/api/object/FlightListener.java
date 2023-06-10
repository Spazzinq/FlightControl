/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.api.object;

import org.bukkit.plugin.Plugin;
import org.spazzinq.flightcontrol.api.APIManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class FlightListener {
    private final List<HandlerMethod> handlers;
    private Plugin pl;

    private FlightListener() {
        handlers = new ArrayList<>();
        Method[] declaredMethods = getClass().getDeclaredMethods();

        for (Method method : declaredMethods) {
            if (method.isAnnotationPresent(FlightEventHandler.class)) {
                if (method.getParameterCount() == 1) {
                    Class<?> firstParameter = method.getParameterTypes()[0];
                    if (APIManager.getInstance().getEvents().contains(firstParameter)) {
                        handlers.add(new HandlerMethod(this, method));
                    } else {
                        new InstantiationException("Method " + method.getName() + " does not contain a specific Event" +
                                " parameter").printStackTrace();
                    }
                } else {
                    new InstantiationException("Method " + method.getName() + " contains more than one parameter").printStackTrace();
                }
            }
        }
    }

    /**
     * Initializes a new FlightListener.
     *
     * @param plugin the listener's plugin owner
     */
    public FlightListener(Plugin plugin) {
        this();
        this.pl = plugin;
    }

    /**
     * Returns the listener's plugin owner.
     *
     * @return the listener's plugin owner
     */
    public Plugin getPlugin() {
        return pl;
    }

    /**
     * Returns the list of HandlerMethods.
     *
     * @return The FlightListener's HandlerMethods
     */
    public List<HandlerMethod> getHandlers() {
        return handlers;
    }
}
