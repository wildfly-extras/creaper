package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * A management client that works against a management interface of a <i>running</i> application server.
 */
public interface OnlineManagementClient extends Closeable {
    /** The {@link OnlineOptions} this client was created with. */
    OnlineOptions options();

    /**
     * The current version ({@link ManagementVersion}) of the server this client is connected to.
     * @throws IOException if an I/O error occurs during the management operation (this can only happen
     * with {@link LazyOnlineManagementClient}; the eager client discovers the current version during initialization)
     */
    ManagementVersion serverVersion() throws IOException;

    /**
     * Applies a set of {@code commands} sequentially. If any one of the commands fails, the rest of the commands is
     * skipped and the exception is rethrown.
     * @throws CommandFailedException if any of the commands fails; see documentation of the exception for more
     * details about error handling
     */
    void apply(OnlineCommand... commands) throws CommandFailedException;

    /**
     * Applies a set of {@code commands} sequentially. If any one of the commands fails, the rest of the commands
     * is skipped and the exception is rethrown.
     * @throws CommandFailedException if any of the commands failed; see documentation of the exception for more
     * details about error handling
     */
    void apply(Iterable<OnlineCommand> commands) throws CommandFailedException;

    /**
     * Performs the management {@code operation} synchronously and returns its result as a {@link ModelNodeResult}.
     * @throws IOException if an I/O error occurs during the management operation
     */
    ModelNodeResult execute(ModelNode operation) throws IOException;

    /**
     * Performs the management {@code operation} synchronously and returns its result as a {@link ModelNodeResult}.
     * @throws IOException if an I/O error occurs during the management operation
     */
    ModelNodeResult execute(Operation operation) throws IOException;

    /**
     * Performs the management {@code operation} (given in the CLI syntax) synchronously and returns its result as
     * a {@link ModelNodeResult}. The operation must be a server-side management operation; it must not be a local
     * CLI operation such as {@code cd} or {@code ls}.
     * @throws CliException if the {@code operation} doesn't correspond to a server-side management operation
     * or if there is a syntax error
     * @throws IOException if an I/O error occurs during the management operation
     */
    ModelNodeResult execute(String operation) throws CliException, IOException;

    /**
     * Performs the management operation (given in the CLI syntax) synchronously. The {@code cliOperation} can be
     * either a server-side management operation or a local CLI operation such as {@code cd} or {@code ls}. All local
     * CLI operations are supported <b>except of</b> {@code connect}, {@code reload} and {@code shutdown}. The
     * {@code connect} operation can't be supported because it changes the underlying essential state.
     * The {@code reload} operation is <b>only</b> supported when no {@code --xxx} options are passed (i.e.,
     * the {@code cliOperation} string is equal to {@code "reload"}). The {@code shutdown} operation is not supported
     * at all. The restrictions on {@code reload} and {@code shutdown} could be lifted in the future.
     * This method returns normally if the operation was performed successfully; if there was any kind of error,
     * it always throws an exception.
     * @throws CliException if there is a syntax error, the command failed or something else is horribly broken
     * @throws IOException if an I/O error occurs during the management operation
     */
    void executeCli(String cliOperation) throws CliException, IOException;

    /**
     * Closes the current connection to the server and opens a new one. Note that this is <b>not possible</b>
     * when the client was created using {@code OnlineOptions.wrap}.
     * @throws TimeoutException if a reconnect is not successful in given {@code timeoutInSeconds}
     * @throws InterruptedException if the thread is interrupted during a pause before a reconnect attempt
     * @throws UnsupportedOperationException when the client was created using {@code OnlineOptions.wrap}
     */
    void reconnect(int timeoutInSeconds) throws TimeoutException, InterruptedException;

    /**
     * <p><b>This method can typically be ignored</b>; it's only important when management operations on this client
     * can be performed from inside an {@link OnlineCommand} and failures of these operations are expected.
     * Specifically, inside a command, operation failures are automatically converted to exceptions (see
     * {@link OnlineCommand}). However, in some situations, this is wrong, because you may expect the failure
     * and act on it. In such situations, you want to use this method.</p>
     *
     * <p>This method starts a block of code in which management operations performed by this client are allowed
     * to fail. Inside that block, operation failures are <i>not</i> converted to exceptions.
     * The {@link FailuresAllowedBlock} must be {@code close}d to end this behavior. Nesting is supported
     * (i.e., you can call {@code allowFailures} inside an failures-allowed block, but you must close
     * the inner block before closing the outer block).</p>
     */
    FailuresAllowedBlock allowFailures() throws IOException;

    /**
     * Closes the client and releases all the resources held. In contrast to the offline management client, which
     * doesn't manage any resources, it is <b>needed</b> here.
     * @throws IOException if an I/O error occurs
     */
    void close() throws IOException;
}
