package org.wildfly.extras.creaper.core;

/** @deprecated use {@link ServerVersion} instead, this will be removed before 1.0 */
@Deprecated
public enum ManagementVersion {
    /** AS 7.0.x.Final */
    VERSION_0_0_0(0, 0, 0),
    /** AS 7.1.0.Final */
    VERSION_1_0_0(1, 0, 0),
    /** AS 7.1.1.Final */
    VERSION_1_1_0(1, 1, 0),
    /** AS 7.1.2.Final / EAP 6.0.0 */
    VERSION_1_2_0(1, 2, 0),
    /** AS 7.1.3.Final / EAP 6.0.1 */
    VERSION_1_3_0(1, 3, 0),
    /** AS 7.2.x.Final / EAP 6.1.x */
    VERSION_1_4_0(1, 4, 0),
    /** EAP 6.2.x */
    VERSION_1_5_0(1, 5, 0),
    /** EAP 6.3.x */
    VERSION_1_6_0(1, 6, 0),
    /** EAP 6.4.x */
    VERSION_1_7_0(1, 7, 0),
    /** WF 8.0.0.Final */
    VERSION_2_0_0(2, 0, 0),
    /** WF 8.1.0.Final */
    VERSION_2_1_0(2, 1, 0),
    /** WF 8.2.x.Final */
    VERSION_2_2_0(2, 2, 0),
    /** WF 9.0.x.Final */
    VERSION_3_0_0(3, 0, 0),
    /** WF 10.0.0.Final */
    VERSION_4_0_0(4, 0, 0),
    /** WF 10.??? (currently, there's no released version that contains this) */
    VERSION_4_1_0(4, 1, 0),
    ;

    private final int major;
    private final int minor;
    private final int micro;

    ManagementVersion(int major, int minor, int micro) {
        this.major = major;
        this.minor = minor;
        this.micro = micro;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + micro;
    }

    // ---

    public static ManagementVersion from(int major, int minor, int micro) {
        for (ManagementVersion version : ManagementVersion.values()) {
            if (major == version.major && minor == version.minor && micro == version.micro) {
                return version;
            }
        }

        throw new IllegalArgumentException("Unknown management version " + major + "." + minor + "." + micro);
    }

    public static ManagementVersion from(ServerVersion serverVersion) {
        for (ManagementVersion version : ManagementVersion.values()) {
            if (serverVersion.major() == version.major
                    && serverVersion.minor() == version.minor
                    && serverVersion.micro() == version.micro) {
                return version;
            }
        }

        throw new IllegalArgumentException("Unknown management version " + serverVersion);
    }

    // ---

    public boolean equalTo(ManagementVersion that) {
        return this.major == that.major && this.minor == that.minor && this.micro == that.micro;
    }

    public boolean lessThan(ManagementVersion that) {
        return (this.major < that.major)
                || (this.major == that.major && this.minor < that.minor)
                || (this.major == that.major && this.minor == that.minor && this.micro < that.micro);
    }

    public boolean lessThanOrEqualTo(ManagementVersion that) {
        return lessThan(that) || equalTo(that);
    }

    public boolean greaterThan(ManagementVersion that) {
        return !lessThanOrEqualTo(that);
    }

    public boolean greaterThanOrEqualTo(ManagementVersion that) {
        return !lessThan(that);
    }

    public boolean inRange(ManagementVersion min, ManagementVersion max) {
        return this.greaterThanOrEqualTo(min) && this.lessThanOrEqualTo(max);
    }

    public void assertAtLeast(ManagementVersion minimum) {
        assertAtLeast(minimum, null);
    }

    public void assertAtLeast(ManagementVersion minimum, String message) {
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
