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
 *
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
    /** EAP 6.4.x */
    public static final ServerVersion VERSION_1_7_0 = new ServerVersion(1, 7, 0);
    /** EAP 6.4.??? (currently, there's no released version that contains this) */
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
    /** WF 10.??? (currently, there's no released version that contains this) */
    public static final ServerVersion VERSION_4_1_0 = new ServerVersion(4, 1, 0);

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

    @Deprecated // only for ManagementVersion.from(ServerVersion)
    int major() {
        return major;
    }

    @Deprecated // only for ManagementVersion.from(ServerVersion)
    int minor() {
        return minor;
    }

    @Deprecated // only for ManagementVersion.from(ServerVersion)
    int micro() {
        return micro;
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

    /** Both {@code min} and {@code max} are <i>inclusive</i>. */
    public boolean inRange(ServerVersion min, ServerVersion max) {
        return this.greaterThanOrEqualTo(min) && this.lessThanOrEqualTo(max);
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
