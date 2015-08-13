package org.wildfly.extras.creaper.core.online.operations.admin;

import org.jboss.arquillian.junit.Arquillian;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ManagementVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.ReadAttributeOption;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class AdminTest {
    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        admin = new Administration(client);
    }

    @After
    public void close() throws IOException {
        client.close();
    }

    @Test
    public void reload() throws IOException, InterruptedException, TimeoutException {
        admin.reload();

        assertFalse(admin.isReloadRequired());
        assertFalse(admin.reloadIfRequired());

        Address jspConfigurationAddress;
        if (client.serverVersion().lessThan(ManagementVersion.VERSION_2_0_0)) { // AS7, JBoss Web
            jspConfigurationAddress = Address.subsystem("web").and("configuration", "jsp-configuration");
        } else { // WildFly, Undertow
            jspConfigurationAddress = Address.subsystem("undertow").and("servlet-container", "default").and("setting", "jsp");
        }

        ModelNodeResult originalValueIgnoreDefaults = null;
        try {
            originalValueIgnoreDefaults = ops.readAttribute(jspConfigurationAddress, "development",
                    ReadAttributeOption.NOT_INCLUDE_DEFAULTS);
            ModelNodeResult originalValue = ops.readAttribute(jspConfigurationAddress, "development",
                    ReadAttributeOption.INCLUDE_DEFAULTS);
            boolean development = originalValue.booleanValue();
            ops.writeAttribute(jspConfigurationAddress, "development", !development);

            assertTrue(admin.isReloadRequired());
            assertTrue(admin.reloadIfRequired());
        } finally {
            if (originalValueIgnoreDefaults != null) {
                ops.writeAttribute(jspConfigurationAddress, "development", originalValueIgnoreDefaults.value());
                admin.reloadIfRequired();
            }
        }
    }
}
