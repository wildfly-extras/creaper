package org.wildfly.extras.creaper.core.online;

public enum ManagementProtocol {

    /** Default port 9999. */
    REMOTE("remote"),

    /**
     * Used in WildFly. Default port 9990.
     * @deprecated use @{code REMOTE_HTTP} for WildFly 11 and above
     */
    @Deprecated
    HTTP_REMOTING("http-remoting"),

    /**
     * Used in WildFly, supported since WildFly 11. Default port 9990.
     */
    REMOTE_HTTP("remote+http"),

    /**
     * Used in WildFly. Default port 9993.
     * @deprecated use @{code REMOTE_HTTPS} for WildFly 11 and above
     */
    @Deprecated
    HTTPS_REMOTING("https-remoting"),

    /**
     * Used in WildFly, supported since WildFly 11. Default port 9993.
     */
    REMOTE_HTTPS("remote+https"),
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
