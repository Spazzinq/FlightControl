/*
 * This file is part of FlightControl, which is licensed under the MIT License
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

package org.spazzinq.flightcontrol.api.objects;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.spazzinq.flightcontrol.api.APIManager;

import java.lang.reflect.Method;

public class HandlerMethod implements Comparable<HandlerMethod> {
    @Getter private Class<?> eventClass;
    @Getter private final FlightListener listener;
    @Getter private final Method method;
    @Getter private FlightEventHandler.Priority priority;

    HandlerMethod(FlightListener listener, Method method) {
        method.setAccessible(true);
        this.listener = listener;
        this.method = method;

        if (method.isAnnotationPresent(FlightEventHandler.class)) {
            if (method.getParameterCount() == 1) {
                Class<?> firstParameter = method.getParameterTypes()[0];
                if (APIManager.getInstance().getEvents().contains(firstParameter)) {
                    eventClass = firstParameter;
                } else throw new IllegalArgumentException("Method does not contain a specific Event parameter");
            } else throw new IllegalArgumentException("Method does not have only one parameter");

            priority = method.getAnnotation(FlightEventHandler.class).priority();
        } else throw new IllegalArgumentException("EventHandler annotation is not present");
    }

    /**
     * Returns the method's plugin owner.
     * @return the method's plugin owner
     */
    public Plugin getPlugin() { return getListener().getPlugin(); }

    @Override public int compareTo(HandlerMethod o) {
        return priority.compareTo(o.priority);
    }
}
