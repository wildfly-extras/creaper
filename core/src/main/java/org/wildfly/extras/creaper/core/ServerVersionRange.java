package org.wildfly.extras.creaper.core;

/**
 * Not useful on its own, just a representation of a range of {@link ServerVersion}s.
 */
public final class ServerVersionRange {
    private final ServerVersion low;
    private final boolean lowIncluded;
    private final ServerVersion high;
    private final boolean highIncluded;

    ServerVersionRange(ServerVersion low, boolean lowIncluded, ServerVersion high, boolean highIncluded) {
        this.low = low;
        this.lowIncluded = lowIncluded;
        this.high = high;
        this.highIncluded = highIncluded;
    }

    boolean contains(ServerVersion version) {
        if (version.equals(low)) {
            return lowIncluded;
        } else if (version.equals(high)) {
            return highIncluded;
        } else {
            return low.lessThan(version) && high.greaterThan(version);
        }
    }
}
