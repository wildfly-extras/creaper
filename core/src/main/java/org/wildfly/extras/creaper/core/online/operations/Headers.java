package org.wildfly.extras.creaper.core.online.operations;

import org.wildfly.extras.creaper.core.online.Constants;

/**
 * Utility methods to create {@link Values} for management operation headers.
 */
public final class Headers {
    private Headers() {} // avoid instantiation

    public static Values allowResourceServiceRestart() {
        return Values.of(Constants.ALLOW_RESOURCE_SERVICE_RESTART, true);
    }
}
