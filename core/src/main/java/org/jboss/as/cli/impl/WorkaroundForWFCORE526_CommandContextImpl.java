package org.jboss.as.cli.impl;

import org.jboss.as.cli.CliInitializationException;

/**
 * <p>This is a workaround for <a href="https://issues.jboss.org/browse/WFCORE-526">WFCORE-526</a> (affects AS 7 and
 * WildFly 8, fixed in WildFly 9). This class has a stupidly long name to signify that it's <b>not</b> meant to be used
 * publicly, even if it is {@code public}.</p>
 *
 * <p>All methods are inherited unmodified, except of {@code getControllerHost} and {@code getControllerPort}
 * that return correct values. It needs to be in the {@code org.jboss.as.cli.impl} package so that it can inherit
 * from a package-private class.</p>
 */
public final class WorkaroundForWFCORE526_CommandContextImpl extends CommandContextImpl {
    private final String host;
    private final int port;

    public WorkaroundForWFCORE526_CommandContextImpl(String host, int port) throws CliInitializationException {
        super();
        this.host = host;
        this.port = port;
    }

    @Override
    public String getControllerHost() {
        return getModelControllerClient() != null ? host : null;
    }

    @Override
    public int getControllerPort() {
        return getModelControllerClient() != null ? port : -1;
    }
}
