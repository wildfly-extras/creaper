package org.wildfly.extras.creaper.commands.elytron.providerloader;

import java.security.Provider;

public class AddProviderLoaderImpl extends Provider {

    private static final long serialVersionUID = 6200844516312931250L;

    public AddProviderLoaderImpl() {
        super("name", 1.0, "some info");
    }

    public AddProviderLoaderImpl(String argument) {
        this();
    }

}
