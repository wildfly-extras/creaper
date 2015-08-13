package org.wildfly.extras.creaper.commands.undertow;

/**
 * Enum representing SSL client authentication modes for SSL channels which are allowed in Undertow.
 */
public enum SslVerifyClient {
    NOT_REQUESTED,
    REQUESTED,
    REQUIRED
}
