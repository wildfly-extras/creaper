package org.wildfly.extras.creaper.core.offline;

import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

final class OfflineManagementClientImpl implements OfflineManagementClient {
    private static final Logger log = Logger.getLogger(OfflineManagementClient.class);

    private final OfflineOptions options;
    private final ManagementVersion managementVersion;

    OfflineManagementClientImpl(OfflineOptions options) throws IOException {
        File configurationFile = options.configurationFile;
        if (!configurationFile.exists()) {
            throw new IOException("Configuration file doesn't exist: " + configurationFile);
        }

        this.options = options;
        this.managementVersion = null; // TODO discover and expose as OfflineManagementClient.serverVersion()
    }

    @Override
    public OfflineOptions options() {
        return options;
    }

    @Override
    public void apply(OfflineCommand... commands) throws CommandFailedException {
        apply(Arrays.asList(commands));
    }

    @Override
    public void apply(Iterable<OfflineCommand> commands) throws CommandFailedException {
        try {
            OfflineCommandContext ctx = new OfflineCommandContext(this, managementVersion);
            for (OfflineCommand command : commands) {
                log.infof("Applying command %s", command);
                command.apply(ctx);
            }
        } catch (Exception e) {
            if (e instanceof CommandFailedException) {
                throw (CommandFailedException) e;
            }
            throw new CommandFailedException(e);
        }
    }
}
