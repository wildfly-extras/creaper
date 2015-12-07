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
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.test.ManualTests;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test <b>needs</b> a manually-controlled Arquillian container and <b>can't</b> use
 * {@code Administration.restart}, because it's not possible to restart the application server via management
 * interface ({@code :shutdown(restart=true)}) if it was started by Arquillian. This is because restarting the entire
 * JVM process relies on the start script ({@code standalone.sh}), which Arquillian doesn't use.
 */
@Category(ManualTests.class)
@RunWith(Arquillian.class)
public class ReloadWhenRestartRequiredTest {
    private OnlineManagementClient client = ManagementClient.onlineLazy(
            OnlineOptions.standalone().localDefault().build());
    private Operations ops = new Operations(client);
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
    public void bringTheServerToRestartRequiredAndThenReload() throws Exception {
        ops.writeAttribute(Address.subsystem("transactions"), "jts", true);
        assertTrue(admin.isRestartRequired());
        assertFalse(admin.isReloadRequired());

        ops.writeAttribute(Address.subsystem("transactions"), "jts", false);
        assertTrue(admin.isRestartRequired());
        assertFalse(admin.isReloadRequired());

        admin.reload();

        assertTrue(admin.isRestartRequired());
        assertFalse(admin.isReloadRequired());

    }

    @Test
    @InSequence(3)
    public void restartServer() throws TimeoutException, InterruptedException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
        client.reconnect(10);
    }

    @Test
    @InSequence(4)
    public void assertRestartIsNotRequired() throws IOException {
        assertFalse(admin.isRestartRequired());
        assertFalse(admin.isReloadRequired());
    }

    @Test
    @InSequence(5)
    public void stopServer() {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
    }

    @After
    public void tearDown() throws IOException {
        client.close();
    }
}
