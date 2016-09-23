package org.wildfly.extras.creaper.core.online.operations.admin;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.test.ManualTests;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(ManualTests.class)
@RunWith(Arquillian.class)
public class ShutdownTest {
    private OnlineManagementClient client = ManagementClient.onlineLazy(
            OnlineOptions.standalone().localDefault().build());
    private Administration admin = new Administration(client);

    @ArquillianResource
    private ContainerController controller;

    @Test
    @InSequence(1)
    public void startServer() {
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
    }

    @Test
    @InSequence(2)
    public void shutdown() throws Exception {
        assertTrue(controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER));

        admin.shutdown();
        serverShutdown();

        assertServerNotRunning();
    }

    @Test
    @InSequence(3)
    public void startServerAgain() throws Exception {
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
        client.reconnect(10);
    }

    @Test
    @InSequence(4)
    public void shutdownGracefully() throws Exception {
        if (client.version().lessThan(ServerVersion.VERSION_3_0_0)) {
            // graceful shutdown not supported
            return;
        }

        assertTrue(controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER));

        admin.shutdownGracefully(5);
        serverShutdown();

        assertServerNotRunning();
    }

    @Test
    @InSequence(5)
    public void startServerYetAgain() throws Exception {
        startServerAgain();
    }

    @Test
    @InSequence(6)
    public void shutdownGracefullyWithZeroTimeout() throws Exception {
        if (client.version().lessThan(ServerVersion.VERSION_3_0_0)) {
            // graceful shutdown not supported
            return;
        }

        assertTrue(controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER));

        admin.shutdownGracefully(0);
        serverShutdown();

        assertServerNotRunning();
    }

    @Test
    @InSequence(7)
    public void stopServer() throws Exception {
        if (controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER)) {
            controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        }
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    private void serverShutdown() throws Exception {
        // server shutdown takes a couple of millis, we have to wait for it
        // TODO how could Creaper wait for the server to shut down? polling the mgmt port?
        Thread.sleep(1000);

        // this only lets Arquillian know that the server is gone (see FakeServerKillProcessor)
        controller.kill(ManualTests.ARQUILLIAN_CONTAINER);
    }

    private void assertServerNotRunning() throws Exception {
        assertFalse(controller.isStarted(ManualTests.ARQUILLIAN_CONTAINER));

        OnlineManagementClient client = null;
        try {
            client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
            fail("server is still running");
        } catch (IOException ignored) {
            // expected
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }
}
