package org.wildfly.extras.creaper.core.online;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.impl.WorkaroundForWFCORE526_CommandContextImpl;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.Operation;
import org.jboss.dmr.ModelNode;
import org.jboss.logging.Logger;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class OnlineManagementClientImpl implements OnlineManagementClient {
    private static final Logger log = Logger.getLogger(OnlineManagementClient.class);

    private static final String JBOSS_CLI_CONFIG = "jboss.cli.config";

    private final OnlineOptions options;
    private final AdjustOperationForDomain adjustOperationForDomain;

    private ModelControllerClient client;
    private CommandContext cliContext;
    private ManagementVersion managementVersion;

    private ThisIsWhereTheClientWasClosed closedAt; // != null <=> already closed

    OnlineManagementClientImpl(OnlineOptions options) throws IOException {
        this.options = options;
        this.adjustOperationForDomain = new AdjustOperationForDomain(options);
        connect();
    }

    private void connect() throws IOException {
        this.client = options.createModelControllerClient();

        try {
            fakeJbossCliConfigToAvoidWarning();

            this.cliContext = new WorkaroundForWFCORE526_CommandContextImpl(options.host, options.port);
            cliContext.setSilent(true); // CLI shouldn't print messages to the console directly; proper logging is fine
            cliContext.bindClient(client);
        } catch (CliInitializationException e) {
            try {
                client.close();
            } catch (IOException ignored) {
                // so that it doesn't suppress the original exception
            }
            throw new IOException(e);
        }

        try {
            this.managementVersion = OnlineManagementVersion.discover(client);
            checkStandaloneVsDomain();
        } catch (IOException e) {
            try {
                client.close();
                cliContext.disconnectController();
            } catch (IOException ignored) {
                // so that it doesn't suppress the original exception
            }
            throw e;
        }
    }

    private static void fakeJbossCliConfigToAvoidWarning() {
        // this works since AS 7.2.0, that is EAP 6.1.0 and above
        // jboss-as-cli 7.0.x and 7.1.x don't know this system property
        if (System.getProperty(JBOSS_CLI_CONFIG) == null) {
            // setting the property to a path of a non-existing file is actually fine -- if the file doesn't exist,
            // a default set of configuration values will be used (just like when no path to jboss-cli.xml is set)
            // and there will be NO warnings
            //
            // this of course relies on an implementation detail and on such file in fact not existing,
            // but it's the best we can do here without actually creating a full jboss-cli.xml file
            System.setProperty(JBOSS_CLI_CONFIG, "creaper.doesnt.exist." + System.currentTimeMillis());
        }
    }

    private void checkStandaloneVsDomain() throws IOException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.READ_CHILDREN_TYPES);
        op.get(Constants.OP_ADDR).setEmptyList();
        ModelNodeResult result = this.execute(op);
        List<String> rootChildrenTypes = result.stringListValue();

        if (options.isStandalone && !rootChildrenTypes.contains(Constants.SUBSYSTEM)) {
            throw new IllegalStateException("According to the options, this client should be connected"
                    + " to a standalone server, but the server appears to be something else");
        } else if (options.isDomain && !rootChildrenTypes.contains(Constants.PROFILE)) {
            throw new IllegalStateException("According to the options, this client should be connected"
                    + " to a domain controller, but the server appears to be something else");
        }
    }

    private void checkClosed() {
        if (closedAt != null) {
            throw new ClientAlreadyClosedException(closedAt);
        }
    }

    @Override
    public OnlineOptions options() {
        return options;
    }

    @Override
    public ManagementVersion serverVersion() {
        return managementVersion;
    }

    @Override
    public void apply(OnlineCommand... commands) throws CommandFailedException {
        apply(Arrays.asList(commands));
    }

    @Override
    public void apply(Iterable<OnlineCommand> commands) throws CommandFailedException {
        checkClosed();
        try {
            OnlineManagementClient client = AutomaticErrorHandlingForCommands.wrap(this);
            OnlineCommandContext ctx = new OnlineCommandContext(client, managementVersion);
            for (OnlineCommand command : commands) {
                log.infof("Applying command %s", command);
                command.apply(ctx);
            }
        } catch (RuntimeCommandFailedException e) {
            throw e.original;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandFailedException(e);
        } catch (Exception e) {
            throw new CommandFailedException(e);
        }
    }

    @Override
    public ModelNodeResult execute(ModelNode operation) throws IOException {
        checkClosed();
        operation = adjustOperationForDomain.adjust(operation);
        log.debugf("Executing operation %s", ModelNodeOperationToCliString.convert(operation));
        ModelNode result = client.execute(operation);
        return new ModelNodeResult(result);
    }

    @Override
    public ModelNodeResult execute(Operation operation) throws IOException {
        checkClosed();
        operation = adjustOperationForDomain.adjust(operation);
        log.debugf("Executing operation %s", ModelNodeOperationToCliString.convert(operation.getOperation()));
        ModelNode result = client.execute(operation);
        return new ModelNodeResult(result);
    }

    @Override
    public ModelNodeResult execute(String operation) throws CliException, IOException {
        checkClosed();
        operation = adjustOperationForDomain.adjust(operation);
        log.debugf("Executing operation %s", operation);
        ModelNode parsedOperation;
        try {
            parsedOperation = cliContext.buildRequest(operation);
        } catch (CommandFormatException e) {
            throw new CliException(e);
        }

        ModelNode result = client.execute(parsedOperation);
        return new ModelNodeResult(result);
    }

    @Override
    public void executeCli(String cliOperation) throws CliException, IOException {
        if (cliOperation.trim().startsWith("connect")) {
            throw new CliException("The 'connect' operation is not supported");
        }

        checkClosed();
        cliOperation = adjustOperationForDomain.adjust(cliOperation);
        log.debugf("Executing CLI operation %s", cliOperation);

        try {
            cliContext.handle(cliOperation);
        } catch (Exception e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }

            throw new CliException(e);
        }

        if (cliContext.getExitCode() != 0 || cliContext.isTerminated()) {
            throw new CliException("CLI operation failed: " + cliOperation);
        }
    }

    @Override
    public void reconnect(int timeoutInSeconds) throws TimeoutException, InterruptedException {
        if (options.isWrappedClient) {
            throw new UnsupportedOperationException("Can't reconnect a wrapped client");
        }

        log.info("Reconnecting the client");

        try {
            client.close();
            cliContext.disconnectController();
        } catch (Throwable ignored) {
        }

        long endTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(timeoutInSeconds);
        while (System.currentTimeMillis() < endTime) {
            try {
                connect();
                return;
            } catch (Throwable e) {
                log.info(e.getMessage());
                // server is probably down, will retry
            }

            Thread.sleep(500);
        }

        throw new TimeoutException("Timeout reconnecting to server");
    }

    @Override
    public void close() throws IOException {
        client.close();
        cliContext.disconnectController();

        if (closedAt == null) {
            closedAt = new ThisIsWhereTheClientWasClosed();
        }
    }
}
