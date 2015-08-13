package org.wildfly.extras.creaper.core.offline;

import org.wildfly.extras.creaper.core.Command;

/**
 * <p>A management command suitable for executing against a configuration file of a <i>stopped</i> application server.
 * A command is typically comprised of multiple management operations and is <b>not</b> transactional. It is expected
 * that there will be <b>no</b> other process manipulating the same configuration file.</p>
 *
 * <p>Error handling for commands is provided automatically by the {@code ManagementClient}. That is, if you are
 * implementing the {@code apply} method, you <i>should</i> let all the exceptions bubble up. Only catch exceptions
 * if you can recover from them. This is signified by the {@code throws} clause of the {@code apply} method anyway.</p>
 */
public interface OfflineCommand extends Command {
    void apply(OfflineCommandContext ctx) throws Exception;

    /** Commands should provide a short description that is useful for logging purposes. */
    String toString();
}
