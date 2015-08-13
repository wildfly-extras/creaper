package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A variant of {@link OnlineManagementClient} that is initialized lazily. In fact, it's a wrapper that delegates
 * to an eager implementation.
 */
final class LazyOnlineManagementClient implements OnlineManagementClient {
    private final OnlineOptions options;

    private OnlineManagementClient delegate;

    LazyOnlineManagementClient(OnlineOptions options) {
        this.options = options;
    }

    private void ensureInitialized() throws IOException {
        if (delegate == null) {
            delegate = ManagementClient.online(options);
        }
    }

    @Override
    public OnlineOptions options() {
        return options;
    }

    @Override
    public ManagementVersion serverVersion() throws IOException {
        ensureInitialized();
        return delegate.serverVersion();
    }

    @Override
    public void apply(OnlineCommand... commands) throws CommandFailedException {
        try {
            ensureInitialized();
        } catch (IOException e) {
            throw new CommandFailedException("Failed to create underlying OnlineManagementClient", e);
        }

        delegate.apply(commands);
    }

    @Override
    public void apply(Iterable<OnlineCommand> commands) throws CommandFailedException {
        try {
            ensureInitialized();
        } catch (IOException e) {
            throw new CommandFailedException("Failed to create underlying OnlineManagementClient", e);
        }

        delegate.apply(commands);
    }

    @Override
    public ModelNodeResult execute(ModelNode operation) throws IOException {
        ensureInitialized();
        return delegate.execute(operation);
    }

    @Override
    public ModelNodeResult execute(Operation operation) throws IOException {
        ensureInitialized();
        return delegate.execute(operation);
    }

    @Override
    public ModelNodeResult execute(String operation) throws CliException, IOException {
        ensureInitialized();
        return delegate.execute(operation);
    }

    @Override
    public void executeCli(String cliOperation) throws CliException, IOException {
        ensureInitialized();
        delegate.executeCli(cliOperation);
    }

    @Override
    public void reconnect(int timeoutInSeconds) throws TimeoutException, InterruptedException {
        if (delegate != null) {
            delegate.reconnect(timeoutInSeconds);
        }
    }

    @Override
    public void close() throws IOException {
        if (delegate != null) {
            delegate.close();
            delegate = null;
        }
    }
}
