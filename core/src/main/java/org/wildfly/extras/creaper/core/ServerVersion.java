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
 *
 * <p>Note that:</p>
 * <ul>
 * <li>AS 7.0.x didn't have {@code management-*-version} attributes in the management model at all</li>
 * <li>AS 7.1.x only had {@code management-{major,minor}-version} attributes in the management model</li>
 * <li>AS 7.2.x and later have {@code management-{major,minor,micro}-version} attributes in the management model</li>
 * </ul>
 *
 * <p>For converting version numbers obtained from the management model to the triad format, missing
 * {@code management-*-version} attributes default to 0.</p>
 *
 * <p>Also note that:</p>
 * <ul>
 * <li>AS 7.0.x used a schema version number of {@code 1.0} in XML configuration files</li>
 * <li>AS 7.1.0 used a schema version number of {@code 1.1} in XML configuration files</li>
 * <li>AS 7.1.1 used a schema version number of {@code 1.2} in XML configuration files</li>
 * <li>EAP 6.0.x used a schema version number of {@code 1.3} in XML configuration files</li>
 * <li>EAP 6.1.x and above have a schema version number in XML configuration files consistent
 *     with the {@code management-*-version} attributes of the management model</li>
 * </ul>
 *
 * <p>Using the rules above, the schema version numbers from the XML configuration files are normalized to match
 * management version numbers as defined in this class.</p>
 */
public final class ServerVersion {
    /** AS 7.0.x.Final */
    public static final ServerVersion VERSION_0_0_0 = new ServerVersion(0, 0, 0);
    /** AS 7.1.0.Final */
    public static final ServerVersion VERSION_1_0_0 = new ServerVersion(1, 0, 0);
    /** AS 7.1.1.Final */
    public static final ServerVersion VERSION_1_1_0 = new ServerVersion(1, 1, 0);
    /** AS 7.1.2.Final / EAP 6.0.0 */
    public static final ServerVersion VERSION_1_2_0 = new ServerVersion(1, 2, 0);
    /** AS 7.1.3.Final / EAP 6.0.1 */
    public static final ServerVersion VERSION_1_3_0 = new ServerVersion(1, 3, 0);
    /** AS 7.2.x.Final / EAP 6.1.x */
    public static final ServerVersion VERSION_1_4_0 = new ServerVersion(1, 4, 0);
    /** EAP 6.2.x */
    public static final ServerVersion VERSION_1_5_0 = new ServerVersion(1, 5, 0);
    /** EAP 6.3.x */
    public static final ServerVersion VERSION_1_6_0 = new ServerVersion(1, 6, 0);
    /** EAP 6.4.0 &ndash; EAP 6.4.6 */
    public static final ServerVersion VERSION_1_7_0 = new ServerVersion(1, 7, 0);
    /** EAP 6.4.7 and above */
    public static final ServerVersion VERSION_1_8_0 = new ServerVersion(1, 8, 0);
    /** WF 8.0.0.Final */
    public static final ServerVersion VERSION_2_0_0 = new ServerVersion(2, 0, 0);
    /** WF 8.1.0.Final */
    public static final ServerVersion VERSION_2_1_0 = new ServerVersion(2, 1, 0);
    /** WF 8.2.x.Final */
    public static final ServerVersion VERSION_2_2_0 = new ServerVersion(2, 2, 0);
    /** WF 9.0.x.Final */
    public static final ServerVersion VERSION_3_0_0 = new ServerVersion(3, 0, 0);
    /** WF 10.0.0.Final */
    public static final ServerVersion VERSION_4_0_0 = new ServerVersion(4, 0, 0);
    /** EAP 7.0.x */
    public static final ServerVersion VERSION_4_1_0 = new ServerVersion(4, 1, 0);
    /** WF 10.1.0.Final */
    public static final ServerVersion VERSION_4_2_0 = new ServerVersion(4, 2, 0);
    /** WF 11.0.0.Final */
    public static final ServerVersion VERSION_5_0_0 = new ServerVersion(5, 0, 0);
    /** WF 12.0.0.Final */
    public static final ServerVersion VERSION_6_0_0 = new ServerVersion(6, 0, 0);
    /** WF 13.0.0.Final */
    public static final ServerVersion VERSION_7_0_0 = new ServerVersion(7, 0, 0);
    /** WF 14.0.x.Final */
    public static final ServerVersion VERSION_8_0_0 = new ServerVersion(8, 0, 0);
    /** WF 15.0.x.Final */
    public static final ServerVersion VERSION_9_0_0 = new ServerVersion(9, 0, 0);
    /** WF 16.0.x.Final, 17.0.x.Final and 18.0.x.Final */
    public static final ServerVersion VERSION_10_0_0 = new ServerVersion(10, 0, 0);
    /** WF 19.0.x.Final */
    public static final ServerVersion VERSION_12_0_0 = new ServerVersion(12, 0, 0);
    /** WF 20.0.x.Final */
    public static final ServerVersion VERSION_13_0_0 = new ServerVersion(13, 0, 0);
    /** WF 21.0.x.Final */
    public static final ServerVersion VERSION_14_0_0 = new ServerVersion(14, 0, 0);
    /** WF 22.0.x.Final */
    public static final ServerVersion VERSION_15_0_0 = new ServerVersion(15, 0, 0);
    /** WF 23.0.x.Final */
    public static final ServerVersion VERSION_16_0_0 = new ServerVersion(16, 0, 0);
    /** WF 24.0.x.Final */
    public static final ServerVersion VERSION_17_0_0 = new ServerVersion(17, 0, 0);
    /** WF 25.0.x.Final */
    public static final ServerVersion VERSION_18_0_0 = new ServerVersion(18, 0, 0);
    /** WF 26.0.x.Final */
    public static final ServerVersion VERSION_19_0_0 = new ServerVersion(19, 0, 0);
    /** WF 27.0.x.Final */
    public static final ServerVersion VERSION_20_0_0 = new ServerVersion(20, 0, 0);
    /** WF 28.0.x.Final */
    public static final ServerVersion VERSION_21_0_0 = new ServerVersion(21, 0, 0);

    private static final ServerVersion[] KNOWN_VERSIONS = {
            VERSION_0_0_0,
            VERSION_1_0_0,
            VERSION_1_1_0,
            VERSION_1_2_0,
            VERSION_1_3_0,
            VERSION_1_4_0,
            VERSION_1_5_0,
            VERSION_1_6_0,
            VERSION_1_7_0,
            VERSION_1_8_0,
            VERSION_2_0_0,
            VERSION_2_1_0,
            VERSION_2_2_0,
            VERSION_3_0_0,
            VERSION_4_0_0,
            VERSION_4_1_0,
            VERSION_4_2_0,
            VERSION_5_0_0,
            VERSION_6_0_0,
            VERSION_7_0_0,
            VERSION_8_0_0,
            VERSION_9_0_0,
            VERSION_10_0_0,
            VERSION_12_0_0,
            VERSION_13_0_0,
            VERSION_14_0_0,
            VERSION_15_0_0,
            VERSION_16_0_0,
            VERSION_17_0_0,
            VERSION_18_0_0,
            VERSION_19_0_0,
            VERSION_20_0_0,
            VERSION_21_0_0
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
