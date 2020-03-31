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

package org.spazzinq.flightcontrol.object;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class Version implements Comparable<Version> {
    final int[] versionData = new int[3];
    @Getter private final VersionType versionType;

    public Version(String entireVersionStr) {
        String[] versionTypeSplit = entireVersionStr.split("-");
        versionType = versionTypeSplit.length == 1 ? VersionType.RELEASE : VersionType.BETA;

        String versionStr = versionTypeSplit[0];
        String[] versionDataSplit = versionStr.split("\\.");

        for (int i = 0; i < versionDataSplit.length; i++) {
            versionData[i] = Integer.parseInt(versionDataSplit[i]);
        }
    }

    public boolean isNewer(Version o) {
        return compareTo(o) > 0;
    }

    @Override
    public int compareTo(@NotNull Version o) {
        // Cycles through the MAJOR, MINOR, and PATCH version
        for (int i = 0; i < versionData.length; i++) {
            int typeDiff = versionData[i] - o.versionData[i];

            if (typeDiff != 0) {
                return typeDiff;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return versionData[0] + "." + versionData[1] + "." + versionData[2];
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Version)) {
            return false;
        }
        Version version = (Version) o;
        return Arrays.equals(versionData, version.versionData) &&
                versionType == version.versionType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(versionType);
        result = 31 * result + Arrays.hashCode(versionData);
        return result;
    }
}
