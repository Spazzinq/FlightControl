/*
 * This file is part of FlightControl-parent, which is licensed under the MIT License
 *
 * Copyright (c) 2019 Spazzinq
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

import org.bukkit.plugin.Plugin;
import org.spazzinq.flightcontrol.api.events.FlightCanEnableEvent;
import org.spazzinq.flightcontrol.api.events.FlightCannotEnableEvent;
import org.spazzinq.flightcontrol.api.events.FlightDisableEvent;
import org.spazzinq.flightcontrol.api.events.FlightEnableEvent;
import org.spazzinq.flightcontrol.api.events.interfaces.FlightEvent;
import org.spazzinq.flightcontrol.api.objects.FlightListener;
import org.spazzinq.flightcontrol.api.objects.HandlerMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

@SuppressWarnings("ALL")
public class APIManager {
    private static APIManager instance;
    private Map<Class, List<HandlerMethod>> handlers = new HashMap<>();

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<FlightListener> listeners = new ArrayList<>();

    private APIManager() {
        Set<Class> events = new HashSet<>(Arrays.asList(FlightCanEnableEvent.class, FlightCannotEnableEvent.class, FlightDisableEvent.class, FlightEnableEvent.class));

        for (Class event : events) {
            handlers.put(event, new ArrayList<>());
        }
    }

    public void addListener(FlightListener listener, Plugin plugin) {
        if (!listener.getHandlers().isEmpty()) {
            listeners.add(listener.setPlugin(plugin));

            for (HandlerMethod m : listener.getHandlers()) {
                sortInsert(m);
            }
        }
    }
    public void removeListener(FlightListener listener) {
        listeners.remove(listener);

        for (HandlerMethod m : listener.getHandlers()) {
            handlers.get(m.getEventClass()).remove(m);
        }
    }
    public boolean containsListener(FlightListener listener) {
        return listeners.contains(listener);
    }
    public List<FlightListener> getListeners() {
        return new ArrayList<>(listeners);
    }

    public void callEvent(FlightEvent e) {
        try {
            callMethods(e);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void callMethods(FlightEvent e) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        // Prevent CME by cloning List
        List<HandlerMethod> handlersCopy = new ArrayList<>(handlers.get(e.getClass()));

        for (HandlerMethod m : handlersCopy) {
            if (m.getPlugin().isEnabled()) m.getMethod().invoke(m.getListener(), e);
            else if (containsListener(m.getListener())) removeListener(m.getListener());
        }
    }

    public void registerEvent(FlightEvent flightEvent) {
        if (!handlers.containsKey(flightEvent.getClass())) {
            handlers.put(flightEvent.getClass(), new ArrayList<>());
        }
    }
    public void unregisterEvent(FlightEvent flightEvent) {
        handlers.remove(flightEvent.getClass());
    }
    public boolean containsEvent(FlightEvent flightEvent) {
        return handlers.containsKey(flightEvent.getClass());
    }
    public Set<Class> getEvents() {
        return handlers.keySet();
    }

    // Insert method in List at the correct priority
    private void sortInsert(HandlerMethod insert) {
        List<HandlerMethod> methods = handlers.get(insert.getEventClass());

        int i = 0;

        for (HandlerMethod m : methods) {
            if (insert.compareTo(m) < 1) break;
            i++;
        }

        if (i != methods.size()) methods.add(i, insert);
        else methods.add(insert);

//        StringBuilder sb = new StringBuilder();
//        for (HandlerMethod m : methods) {
//            sb.append(m.getPriority() + "(" + m.getMethod().getName() + "), ");
//        }
//        Bukkit.getLogger().severe(sb.toString());
    }

    public static APIManager getInstance() {
        if (instance == null) instance = new APIManager();
        return instance;
    }
}