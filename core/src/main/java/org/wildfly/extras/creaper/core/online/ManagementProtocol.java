package org.wildfly.extras.creaper.core.online;

public enum ManagementProtocol {
    /** Used in JBoss AS 7. Default port 9999. */
    REMOTE("remote"),
    /** Used in WildFly. Default port 9990. */
    HTTP_REMOTING("http-remoting"),

    /** @deprecated use {@link #REMOTE} instead, this will be removed before 1.0 */
    @Deprecated
    REMOTING("remote"),
    /** @deprecated use {@link #HTTP_REMOTING} instead, this will be removed before 1.0 */
    @Deprecated
    HTTP_REMOTE("http-remoting"),
    ;

    private final String protocolName;

    ManagementProtocol(String protocolName) {
        this.protocolName = protocolName;
    }

    /** The name of the protocol as expected by the client libraries. */
    public String protocolName() {
        return protocolName;
    }
}
