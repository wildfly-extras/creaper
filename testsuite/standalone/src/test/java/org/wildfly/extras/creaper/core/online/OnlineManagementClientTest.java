package org.wildfly.extras.creaper.core.online;

import org.jboss.arquillian.junit.Arquillian;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersionPart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class OnlineManagementClientTest {
    protected OnlineManagementClient client;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
    }

    @After
    public void close() throws IOException {
        if (client != null) {
            client.close();
        }
    }

    private void assertStillValid() throws IOException {
        executeFromModelNode_operationSucceeds();
    }

    @Test(expected = IllegalStateException.class)
    public void clientConfiguredForDomain() throws IOException {
        ManagementClient.online(OnlineOptions.domain().build().localDefault().build());
    }

    @Test
    public void executeFromModelNode_operationSucceeds() throws IOException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WHOAMI);
        op.get(Constants.OP_ADDR).setEmptyList();
        client.execute(op).assertSuccess();
    }

    @Test
    public void executeFromModelNode_operationFails() throws IOException, CliException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WRITE_ATTRIBUTE_OPERATION);
        op.get(Constants.OP_ADDR).setEmptyList();
        op.get(Constants.NAME).set(ManagementVersionPart.MAJOR.attributeName());
        op.get(Constants.VALUE).set(42);
        client.execute(op).assertFailed();

        assertStillValid();
    }

    @Test
    public void executeFromOperation_operationSucceeds() throws IOException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WHOAMI);
        op.get(Constants.OP_ADDR).setEmptyList();
        OperationBuilder operationBuilder = new OperationBuilder(op);
        client.execute(operationBuilder.build()).assertSuccess();
    }

    @Test
    public void executeFromOperation_operationFails() throws IOException, CliException {
        ModelNode op = new ModelNode();
        op.get(Constants.OP).set(Constants.WRITE_ATTRIBUTE_OPERATION);
        op.get(Constants.OP_ADDR).setEmptyList();
        op.get(Constants.NAME).set(ManagementVersionPart.MAJOR.attributeName());
        op.get(Constants.VALUE).set(42);
        OperationBuilder operationBuilder = new OperationBuilder(op);
        client.execute(operationBuilder.build()).assertFailed();

        assertStillValid();
    }

    @Test
    public void executeFromString_operationSucceeds() throws IOException, CliException {
        client.execute(":whoami").assertSuccess();
        client.execute("/:whoami").assertSuccess();
    }

    @Test
    public void executeFromString_operationFails() throws IOException, CliException {
        client.execute("/:write-attribute(name=management-major-version, value=42)").assertFailed();

        assertStillValid();
    }

    @Test
    public void executeFromString_syntaxError() throws IOException, CliException {
        try {
            client.execute("123");
        } catch (CliException e) {
            // expected
        } catch (IOException e) {
            fail();
        }

        assertStillValid();
    }

    @Test
    public void executeThroughCli_operationSucceeds() {
        try {
            client.executeCli(":whoami");
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    public void executeThroughCli_operationFails() throws IOException {
        try {
            client.executeCli(":write-attribute(name=management-major-version, value=42)");
            fail();
        } catch (CliException e) {
            // expected
        } catch (IOException e) {
            fail();
        }

        assertStillValid();
    }

    @Test
    public void executeThroughCli_syntaxError() throws IOException {
        try {
            client.executeCli("123");
            fail();
        } catch (CliException e) {
            // expected
        } catch (IOException e) {
            fail();
        }

        assertStillValid();
    }

    @Test
    public void applyCommand_operationSucceeds() throws IOException {
        try {
            client.apply(new OnlineCommand() {
                @Override
                public void apply(OnlineCommandContext ctx) throws IOException, CliException {
                    ctx.client.execute(":whoami");
                }
            });
        } catch (CommandFailedException e) {
            fail();
        }
    }

    @Test
    public void applyCommand_operationFails() throws IOException {
        final String operation = ":write-attribute(name=management-major-version, value=42)";

        try {
            client.apply(new OnlineCommand() {
                @Override
                public void apply(OnlineCommandContext ctx) throws IOException, CliException {
                    ctx.client.execute(operation);
                }
            });
            fail();
        } catch (CommandFailedException e) {
            // expected
            assertTrue(e.getMessage().contains("Operation " + operation + " failed"));
        }

        assertStillValid();
    }

    @Test
    public void applyCommand_syntaxError() throws IOException {
        try {
            client.apply(new OnlineCommand() {
                @Override
                public void apply(OnlineCommandContext ctx) throws IOException, CliException {
                    ctx.client.execute("123");
                }
            });
            fail();
        } catch (CommandFailedException e) {
            // expected
            assertTrue(e.getMessage().contains("OperationFormatException"));
        }

        assertStillValid();
    }
}
