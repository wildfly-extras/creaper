package org.wildfly.extras.creaper.core.offline;

import org.wildfly.extras.creaper.core.CommandFailedException;

/**
 * A management client that works against a configuration file of a <i>stopped</i> application server. If the server
 * is running, it will not pick up the configuration file changes and even overwrite them when it gets stopped.
 */
public interface OfflineManagementClient {
    /** The {@link OfflineOptions} this client was created with. */
    OfflineOptions options();

    /**
     * Applies a set of {@code commands} sequentially. If any one of the commands fails, the rest of the commands
     * is skipped and the exception is rethrown.
     * @throws CommandFailedException if any of the commands failed; see documentation of the exception for more
     * details about error handling
     */
    void apply(OfflineCommand... commands) throws CommandFailedException;

    /**
     * Applies a set of {@code commands} sequentially. If any one of the commands fails, the rest of the commands
     * is skipped and the exception is rethrown.
     * @throws CommandFailedException if any of the commands failed; see documentation of the exception for more
     * details about error handling
     */
    void apply(Iterable<OfflineCommand> commands) throws CommandFailedException;
}
