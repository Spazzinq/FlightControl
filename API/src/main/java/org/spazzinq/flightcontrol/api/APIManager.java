/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 *
 * Copyright (c) 2022 Spazzinq
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

package org.spazzinq.flightcontrol.api;

import org.spazzinq.flightcontrol.api.event.FlightCanEnableEvent;
import org.spazzinq.flightcontrol.api.event.FlightCannotEnableEvent;
import org.spazzinq.flightcontrol.api.event.FlightDisableEvent;
import org.spazzinq.flightcontrol.api.event.FlightEnableEvent;
import org.spazzinq.flightcontrol.api.event.interfaces.FlightEvent;
import org.spazzinq.flightcontrol.api.object.FlightListener;
import org.spazzinq.flightcontrol.api.object.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("unused")
public class APIManager {
    private static APIManager instance;

    private final Map<Class<?>, List<HandlerMethod>> handlers = new HashMap<>();
    private final List<FlightListener> listeners = new ArrayList<>();

    private APIManager() {
        Set<Class<?>> events = new HashSet<>(Arrays.asList(FlightCanEnableEvent.class, FlightCannotEnableEvent.class,
                FlightDisableEvent.class, FlightEnableEvent.class));

        for (Class<?> event : events) {
            handlers.put(event, new ArrayList<>());
        }
    }

    /**
     * Adds a listener to the APIManager.
     *
     * @param listener the FlightListener to add
     */
    public void addListener(FlightListener listener) {
        if (!listener.getHandlers().isEmpty()) {
            listeners.add(listener);

            for (HandlerMethod m : listener.getHandlers()) {
                sortInsert(m);
            }
        }
    }

    /**
     * Removes a listener from the APIManager and unregisters its HandlerMethods.
     *
     * @param listener the FlightListener to unregister
     */
    public void removeListener(FlightListener listener) {
        listeners.remove(listener);

        for (HandlerMethod m : listener.getHandlers()) {
            handlers.get(m.getEventClass()).remove(m);
        }
    }

    /**
     * Returns if the APIManager contains the listener.
     *
     * @param listener the FlightListener to search for
     * @return true if the APIManager contains the listener, false otherwise
     */
    public boolean containsListener(FlightListener listener) {
        return listeners.contains(listener);
    }

    /**
     * Returns the list of all registered listeners.
     *
     * @return The list of all registered listeners
     */
    public List<FlightListener> getListeners() {
        return listeners;
    }

    /**
     * Calls an event.
     *
     * @param event the FlightEvent to call
     */
    public void callEvent(FlightEvent event) {
        try {
            callMethods(event);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Calls the HandlerMethods associated with the event type.
     *
     * @param event the FlightEvent instance
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    private void callMethods(FlightEvent event) throws InvocationTargetException, IllegalAccessException {
        // Prevent CME by cloning List
        List<HandlerMethod> handlersCopy = new ArrayList<>(handlers.get(event.getClass()));

        for (HandlerMethod m : handlersCopy) {
            if (m.getPlugin().isEnabled()) {
                m.getMethod().invoke(m.getListener(), event);
            } else if (containsListener(m.getListener())) {
                removeListener(m.getListener());
            }
        }
    }

    /**
     * Registers the event if it is not already registered.
     *
     * @param event the FlightEvent to add
     */
    public void registerEvent(FlightEvent event) {
        if (!handlers.containsKey(event.getClass())) {
            handlers.put(event.getClass(), new ArrayList<>());
        }
    }

    /**
     * Unregisters an event.
     *
     * @param event the FlightEvent to remove
     */
    public void unregisterEvent(FlightEvent event) {
        handlers.remove(event.getClass());
    }

    /**
     * Returns true if the APIManager contains the event.
     *
     * @param event the FlightEvent to search for
     * @return true if the APIManager contains the event
     */
    public boolean containsEvent(FlightEvent event) {
        return handlers.containsKey(event.getClass());
    }

    /**
     * Returns all registered events.
     *
     * @return All registered events
     */
    public Set<Class<?>> getEvents() {
        return handlers.keySet();
    }

    // Insert method in List at the correct priority
    private void sortInsert(HandlerMethod insert) {
        List<HandlerMethod> methods = handlers.get(insert.getEventClass());

        int i = 0;

        for (HandlerMethod m : methods) {
            if (insert.compareTo(m) < 1) {
                break;
            }
            i++;
        }

        if (i != methods.size()) {
            methods.add(i, insert);
        } else {
            methods.add(insert);
        }

        //        StringBuilder sb = new StringBuilder();
        //        for (HandlerMethod m : methods) {
        //            sb.append(m.getPriority() + "(" + m.getMethod().getName() + "), ");
        //        }
        //        Bukkit.getLogger().severe(sb.toString());
    }

    /**
     * Returns the APIManager instance.
     *
     * @return The APIManager instance
     */
    public static APIManager getInstance() {
        if (instance == null) {
            instance = new APIManager();
        }
        return instance;
    }
}