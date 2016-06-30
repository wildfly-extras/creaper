package org.wildfly.extras.creaper.core.online;

import org.wildfly.extras.creaper.core.Command;

/**
 * <p>A management command suitable for executing against a management interface of a <i>running</i> application server.
 * A command is typically comprised of multiple management operations and is <b>not</b> transactional. It is expected
 * that there will be <b>no</b> other management clients connected to the same application server.</p>
 *
 * <p>Error handling for commands is provided automatically by the {@code ManagementClient}. Most importantly, all
 * management operation results are inspected and if the operation failed, an exception is thrown for you. This should
 * be the common case; if you expect operation failures in a certain block of code, delimit that block of code using
 * {@link OnlineManagementClient#allowFailures()}. When you are implementing the {@code apply} method, you
 * <i>should</i> let all the exceptions bubble up. Only catch exceptions if you can recover from them. This is signified
 * by the {@code throws} clause of the {@code apply} method anyway.</p>
 */
public interface OnlineCommand extends Command {
    void apply(OnlineCommandContext ctx) throws Exception;

    /** Commands should provide a short description that is useful for logging purposes. */
    String toString();

    OnlineCommand NOOP = new OnlineCommand() {
        @Override
        public void apply(OnlineCommandContext ctx) throws Exception {
            // noop
        }

        @Override
        public String toString() {
            return "noop";
        }
    };
}
