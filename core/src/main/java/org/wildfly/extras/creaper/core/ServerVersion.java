package org.wildfly.extras.creaper.core;

/**
 * <p>A version of the application server management interface as the {@code {major, minor, micro}} triad.</p>
 *
 * <p>The {@code static} constants represent all known versions of the application server management interface.
 * See comments to the individual values for corresponding versions of the application server.</p>
 *
 * <p>The known versions are guaranteed to be canonical (i.e., they are singletons, unless reflection hackery is used).
 * It is possible to compare them using {@code ==} and {@code !=}. For as-yet-unknown versions, no such guarantees
 * are made.</p>
 */
public final class ServerVersion {
    /** WF 27.0.x.Final */
    public static final ServerVersion VERSION_20_0_0 = new ServerVersion(20, 0, 0);
    /** WF 28.0.x.Final */
    public static final ServerVersion VERSION_21_0_0 = new ServerVersion(21, 0, 0);
    /** WF 29.0.x.Final */
    public static final ServerVersion VERSION_22_0_0 = new ServerVersion(22, 0, 0);
    /** WF 30.0.x.Final */
    public static final ServerVersion VERSION_23_0_0 = new ServerVersion(23, 0, 0);
    /** WF 31.0.x.Final */
    public static final ServerVersion VERSION_24_0_0 = new ServerVersion(24, 0, 0);
    /** WF 32.0.x.Final */
    public static final ServerVersion VERSION_25_0_0 = new ServerVersion(25, 0, 0);
    /** WF 33.0.x.Final */
    public static final ServerVersion VERSION_26_0_0 = new ServerVersion(26, 0, 0);
    /** WF 34.0.x.Final */
    public static final ServerVersion VERSION_27_0_0 = new ServerVersion(27, 0, 0);
    /** WF 35.0.x.Final */
    public static final ServerVersion VERSION_28_0_0 = new ServerVersion(28, 0, 0);
    /** WF 36.0.x.Final */
    public static final ServerVersion VERSION_29_0_0 = new ServerVersion(29, 0, 0);
    /** WF 37.0.x.Final */
    public static final ServerVersion VERSION_30_0_0 = new ServerVersion(30, 0, 0);
    /** WF 38.0.x.Final */
    public static final ServerVersion VERSION_31_0_0 = new ServerVersion(31, 0, 0);
    /** WF 39.0.x.Final */
    public static final ServerVersion VERSION_32_0_0 = new ServerVersion(32, 0, 0);
    /** WF 40.0.x.Final */
    public static final ServerVersion VERSION_33_0_0 = new ServerVersion(33, 0, 0);

    private static final ServerVersion[] KNOWN_VERSIONS = {
            VERSION_20_0_0,
            VERSION_21_0_0,
            VERSION_22_0_0,
            VERSION_23_0_0,
            VERSION_24_0_0,
            VERSION_25_0_0,
            VERSION_26_0_0,
            VERSION_27_0_0,
            VERSION_28_0_0,
            VERSION_29_0_0,
            VERSION_30_0_0,
            VERSION_31_0_0,
            VERSION_32_0_0,
            VERSION_33_0_0
    };

    /**
     * Guarantees that the known versions are canonical (i.e., they are singletons, unless reflection hackery is used).
     * It is possible to compare them using {@code ==} and {@code !=}. For as-yet-unknown versions, no such guarantees
     * are made.
     */
    public static ServerVersion from(int major, int minor, int micro) {
        for (ServerVersion knownVersion : KNOWN_VERSIONS) {
            if (knownVersion.major == major && knownVersion.minor == minor && knownVersion.micro == micro) {
                return knownVersion;
            }
        }

        return new ServerVersion(major, minor, micro);
    }

    private final int major;
    private final int minor;
    private final int micro;

    private ServerVersion(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + micro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerVersion that = (ServerVersion) o;

        return major == that.major && minor == that.minor && micro == that.micro;
    }

    @Override
    public int hashCode() {
        int result = major;
        result = 31 * result + minor;
        result = 31 * result + micro;
        return result;
    }

    // ---

    /**
     * Returns a range of server versions, from {@code this} up to {@code other}. The lower bound
     * is <i>inclusive</i>, the upper bound is <i>exclusive</i>. It must be that {@code this <= other}.
     * If {@code this == other}, it <i>is</i> included in the range.
     */
    public ServerVersionRange upTo(ServerVersion other) {
        if (this.greaterThan(other)) {
            throw new IllegalArgumentException(this + " must be less than or equal to " + other);
        }

        return new ServerVersionRange(this, true, other, false);
    }

    /**
     * Returns a range of server versions, from {@code this} up to and including {@code other}. Both the lower bound
     * and upper bound are <i>inclusive</i>. It must be that {@code this <= other}.
     */
    public ServerVersionRange upToAndIncluding(ServerVersion other) {
        if (this.greaterThan(other)) {
            throw new IllegalArgumentException(this + " must be less than or equal to " + other);
        }

        return new ServerVersionRange(this, true, other, true);
    }

    // ---

    public boolean equalTo(ServerVersion that) {
        return this.major == that.major && this.minor == that.minor && this.micro == that.micro;
    }

    public boolean lessThan(ServerVersion that) {
        return (this.major < that.major)
                || (this.major == that.major && this.minor < that.minor)
                || (this.major == that.major && this.minor == that.minor && this.micro < that.micro);
    }

    public boolean lessThanOrEqualTo(ServerVersion that) {
        return lessThan(that) || equalTo(that);
    }

    public boolean greaterThan(ServerVersion that) {
        return !lessThanOrEqualTo(that);
    }

    public boolean greaterThanOrEqualTo(ServerVersion that) {
        return !lessThan(that);
    }

    /**
     * Both {@code min} and {@code max} are <i>inclusive</i>.
     *
     * @deprecated use {@link #inRange(ServerVersionRange)}
     */
    @Deprecated
    public boolean inRange(ServerVersion min, ServerVersion max) {
        return this.greaterThanOrEqualTo(min) && this.lessThanOrEqualTo(max);
    }

    public boolean inRange(ServerVersionRange range) {
        return range.contains(this);
    }

    public void assertAtLeast(ServerVersion minimum) {
        assertAtLeast(minimum, null);
    }

    public void assertAtLeast(ServerVersion minimum, String message) {
        String defaultMessage = "Expected management version to be at least " + minimum + ", but is " + this;
        if (message == null || message.isEmpty()) {
            message = defaultMessage;
        } else {
            message = message + "; " + defaultMessage;
        }

        if (this.lessThan(minimum)) {
            throw new AssertionError(message);
        }
    }
}
