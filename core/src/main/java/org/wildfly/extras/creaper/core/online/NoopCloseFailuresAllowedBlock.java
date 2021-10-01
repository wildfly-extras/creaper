package org.wildfly.extras.creaper.core.online;

import java.io.IOException;

/** An implementation of {@link FailuresAllowedBlock} whose {@code close} method is a no-op. */
final class NoopCloseFailuresAllowedBlock implements FailuresAllowedBlock {
    static final NoopCloseFailuresAllowedBlock INSTANCE = new NoopCloseFailuresAllowedBlock();

    private NoopCloseFailuresAllowedBlock() {
    }

    @Override
    public void close() throws IOException {
    }
}
