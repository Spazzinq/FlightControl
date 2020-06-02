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

<<<<<<< HEAD:FlightControl/src/main/java/org/spazzinq/flightcontrol/check/towny/TownyHookBase.java
package org.spazzinq.flightcontrol.hook.towny;
=======
package org.spazzinq.flightcontrol.check;
>>>>>>> breaking:FlightControl/src/main/java/org/spazzinq/flightcontrol/check/Check.java

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.object.Cause;

<<<<<<< HEAD:FlightControl/src/main/java/org/spazzinq/flightcontrol/check/towny/TownyHookBase.java
public class TownyHookBase extends Hook {
    public boolean townyOwn(Player p) {
        return false;
    }

    public boolean wartime() {
        return false;
=======
public abstract class Check {
    public abstract boolean check(Player p);

    public abstract Cause getCause();

    @Override public String toString() {
        return getClass().getSimpleName();
>>>>>>> breaking:FlightControl/src/main/java/org/spazzinq/flightcontrol/check/Check.java
    }
}
