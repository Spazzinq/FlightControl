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

package org.spazzinq.flightcontrol.test;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.spazzinq.flightcontrol.FlightControl;
import org.spazzinq.flightcontrol.manager.UpdateManager;
import org.spazzinq.flightcontrol.object.Version;
import org.spazzinq.flightcontrol.object.VersionType;

public class UpdateTest {
    @Test public void testVersionComparison() {
        Version oldVersion = new Version("3.0.0");
        Version newVersion = new Version("4.0.0");
        assert newVersion.isNewer(oldVersion);

        oldVersion = new Version("4.1.0");
        newVersion = new Version("4.2.0");
        assert newVersion.isNewer(oldVersion);

        oldVersion = new Version("4.0.1");
        newVersion = new Version("4.0.2");
        assert newVersion.isNewer(oldVersion);

        oldVersion = new Version("4.0.0");
        newVersion = new Version("4.0.0");
        assert !newVersion.isNewer(oldVersion);
        assert newVersion.equals(oldVersion);
    }

    @Test public void testVersionTagging() {
        Version version = new Version("4.0.0");
        assert version.getVersionType() == VersionType.RELEASE;

        Version beta = new Version("4.0.0-BETA");
        assert beta.getVersionType() == VersionType.BETA;
    }

    @Test public void testUpdate() {
        UpdateManager updateManager = FlightControl.getInstance().getUpdateManager();

        updateManager.setVersion(new Version("4.0.0"));
        assert updateManager.updateExists(true);
    }
}
