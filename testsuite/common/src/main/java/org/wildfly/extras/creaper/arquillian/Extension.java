package org.wildfly.extras.creaper.arquillian;

import org.jboss.arquillian.container.spi.ServerKillProcessor;
import org.jboss.arquillian.core.spi.LoadableExtension;

public final class Extension implements LoadableExtension {
    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(ServerKillProcessor.class, FakeServerKillProcessor.class);
    }
}
