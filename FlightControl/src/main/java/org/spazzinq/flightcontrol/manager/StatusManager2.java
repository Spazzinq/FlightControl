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

package org.spazzinq.flightcontrol.manager;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.CheckSet;
import org.spazzinq.flightcontrol.util.CheckUtil;

public class StatusManager2 {
    FlightControl pl;

    public StatusManager2(FlightControl pl) {
        this.pl = pl;
    }

    // TODO Add debug stuff
    boolean checkEnable(Player p) {
        Category c = pl.getCategoryManager().getCategory(p);

        CheckUtil.checkAll(c.getChecks(), p);

        // eval always CheckSet
        // eval category CheckSet
    }

    boolean checkDisable(Player p) {
        ategory c = pl.getCategoryManager().getCategory(p);

        CheckSet d = new CheckSet();

        d.checkAll(p);

        // eval always CheckSet
        // eval category CheckSet
    }
}
