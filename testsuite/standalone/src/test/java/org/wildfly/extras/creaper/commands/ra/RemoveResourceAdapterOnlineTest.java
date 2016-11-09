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
public class RemoveResourceAdapterOnlineTest {
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
    public void removeRaTestCase() throws Exception {
        client.apply(new AddResourceAdapter.Builder(RA_ID, "org.jboss.genericjms", TransactionType.XA).build());
        admin.reloadIfRequired();
        ModelNodeResult resSuc = ops.readResource(Address.subsystem("resource-adapters")
                .and("resource-adapter", RA_ID));
        resSuc.assertSuccess();
        client.apply(new RemoveResourceAdapter.Builder(RA_ID).build());
        ModelNodeResult resFail = ops.readResource(Address.subsystem("resource-adapters")
                .and("resource-adapter", RA_ID));
        resFail.assertFailed();
    }
}
