package org.wildfly.extras.creaper.arquillian;

import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ServerKillProcessor;

final class FakeServerKillProcessor implements ServerKillProcessor {
    @Override
    public void kill(Container container) throws Exception {
        // does nothing, this is only to let Arquillian know that the server is gone
    }
}
