package org.spazzinq.flightcontrol.object;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

public class Version implements Comparable<Version> {
    int[] versionData = new int[3];
    @Getter private VersionType versionType;

    public Version(String versionStr) {
        String[] versionTypeSplit = versionStr.split("-");
        versionType = versionTypeSplit.length == 1 ? VersionType.RELEASE : VersionType.BETA;

        versionStr = versionTypeSplit[0];
        String[] versionDataSplit = versionStr.split("\\.");

        for (int i = 0; i < versionDataSplit.length; i++) {
            versionData[i] = Integer.parseInt(versionDataSplit[i]);
        }
    }

    public boolean isNewer(Version o) {
        return compareTo(o) > 0;
    }

    @Override
    public int compareTo(Version o) {
        // Cycles through the MAJOR, MINOR, and PATCH version
        for (int i = 0; i < versionData.length; i++) {
            int typeDiff = versionData[i] - o.versionData[i];

            if (typeDiff !=  0) {
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
        if (this == o) return true;
        if (!(o instanceof Version)) return false;
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
