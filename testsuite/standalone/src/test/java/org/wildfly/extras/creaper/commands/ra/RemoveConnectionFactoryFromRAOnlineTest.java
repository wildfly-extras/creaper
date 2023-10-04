package org.wildfly.extras.creaper.commands.ra;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.IOException;

// doesn't work on AS7/EAP6 because org.jboss.genericjms module.xml requires external jars
@RunWith(Arquillian.class)
public class RemoveConnectionFactoryFromRAOnlineTest {
    private static final String RA_ID = "genericjmsRA";
    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        Assume.assumeTrue(client.version().greaterThanOrEqualTo(ServerVersion.VERSION_3_0_0));
        ops = new Operations(client);
        admin = new Administration(client);
    }

    @After
    public void close() throws Exception {
        ops.removeIfExists(Address.subsystem("resource-adapters").and("resource-adapter", RA_ID));
        admin.reloadIfRequired();
        client.close();
    }

    @Test
    public void removeConnectionFactoryTestcase() throws Exception {
        // uses legacy security which no longer works in WildFly 29+
        // (likely it was already no-op in few previous WF releases)
        Assume.assumeTrue(client.version().lessThan(ServerVersion.VERSION_22_0_0));
        client.apply(new AddResourceAdapter.Builder(RA_ID, "org.jboss.genericjms", TransactionType.NONE).build());
        admin.reloadIfRequired();
        client.apply(new AddConnectionFactoryToRA.Builder("cf1", "java:/jms/myRAConnectionFactory", RA_ID)
                .addProperty("SessionDefaultType", "javax.jms.Queue")
                .addXARecovery("name", "pw")
                .setTracking(true)
                .setApplicationSecurity(true).build());
        admin.reloadIfRequired();
        ModelNodeResult res = ops.readResource(Address.subsystem("resource-adapters")
                .and("resource-adapter", RA_ID)
                .and("connection-definitions", "cf1"));
        res.assertSuccess();
        client.apply(new RemoveConnectionFactoryFromRA.Builder("cf1", RA_ID).build());
        admin.reloadIfRequired();
        res = ops.readResource(Address.subsystem("resource-adapters")
                .and("resource-adapter", RA_ID)
                .and("connection-definitions", "cf1"));
        res.assertFailed();
    }
}
