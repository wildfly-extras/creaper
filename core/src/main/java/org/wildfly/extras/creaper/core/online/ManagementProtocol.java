package org.wildfly.extras.creaper.core.online;

public enum ManagementProtocol {
    /** Used in JBoss AS 7. Default port 9999. */
    REMOTING("remote"),
    /** Used in WildFly. Default port 9990. */
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
