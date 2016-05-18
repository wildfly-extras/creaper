package org.wildfly.extras.creaper.core.online;

/**
 * See https://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#SSLContext
 */
public enum SslProtocol {
    /** Supports some version of SSL. */
    SSL("SSL"),
    /** Supports SSL version 2 or later; may support other versions. */
    SSL_V2("SSLv2"),
    /** Supports SSL version 3; may support other versions. */
    SSL_V3("SSLv3"),
    /** Supports some version of TLS. */
    TLS("TLS"),
    /** Supports RFC 2246: TLS version 1.0; may support other versions. */
    TLS_V1("TLSv1"),
    /** Supports RFC 4346: TLS version 1.1; may support other versions. */
    TLS_V11("TLSv1.1"),
    /** Supports RFC 5246: TLS version 1.2; may support other versions. */
    TLS_V12("TLSv1.2"),
    ;

    private final String protocolName;

    SslProtocol(String protocolName) {
        this.protocolName = protocolName;
    }

    /** The name of the protocol as expected by the client libraries. */
    public String protocolName() {
        return this.protocolName;
    }
}
