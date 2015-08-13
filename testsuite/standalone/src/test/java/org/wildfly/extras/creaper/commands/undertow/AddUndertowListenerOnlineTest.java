package org.wildfly.extras.creaper.commands.undertow;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.test.WildFlyTests;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * This test uses the {@code ajp} socket binding for all listener types, because there's no listener configured
 * by default that uses it (unlike {@code http}).
 */
@Category(WildFlyTests.class)
@RunWith(Arquillian.class)
public class AddUndertowListenerOnlineTest {
    private static final String TEST_LISTENER_NAME = "test-listener";

    private static final Address DEFAULT_SERVER_ADDRESS = Address.subsystem("undertow")
            .and("server", UndertowConstants.DEFAULT_SERVER_NAME);

    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        assumeTrue("The test requires Undertow that supports HTTP/2 options on listener, which is available since WildFly 9",
                client.serverVersion().greaterThanOrEqualTo(ManagementVersion.VERSION_3_0_0));
        ops = new Operations(client);
        admin = new Administration(client);
    }

    @After
    public void close() throws IOException, CliException, OperationException {
        client.close();
    }

    @Test
    public void addHttpConnector_commandSucceeds() throws Exception {
        client.apply(new AddUndertowListener.HttpBuilder(TEST_LISTENER_NAME, "ajp").build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("http-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("http-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.HTTP_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("http-listener", TEST_LISTENER_NAME)));
    }

    @Test
    public void addHttpsConnector_commandSucceeds() throws Exception {
        client.apply(new AddUndertowListener.HttpsBuilder(TEST_LISTENER_NAME, "ajp")
                .securityRealm("ApplicationRealm")
                .build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.HTTPS_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("https-listener", TEST_LISTENER_NAME)));
    }

    @Test
    public void addAjpConnector_commandSucceeds() throws Exception {
        client.apply(new AddUndertowListener.AjpBuilder(TEST_LISTENER_NAME, "ajp").build());

        assertTrue(ops.exists(DEFAULT_SERVER_ADDRESS.and("ajp-listener", TEST_LISTENER_NAME)));
        ops.readAttribute(DEFAULT_SERVER_ADDRESS.and("ajp-listener", TEST_LISTENER_NAME), "socket-binding")
                .assertSuccess();

        client.apply(new RemoveUndertowListener.Builder(UndertowListenerType.AJP_LISTENER, TEST_LISTENER_NAME)
                .forDefaultServer());
        admin.reloadIfRequired();
        assertFalse(ops.exists(DEFAULT_SERVER_ADDRESS.and("ajp-listener", TEST_LISTENER_NAME)));
    }
}
