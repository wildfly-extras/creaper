package org.wildfly.extras.creaper.commands.patching;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

@RunWith(Arquillian.class)
public class ApplyRollbackNonexistingPatchTest {
    private OnlineManagementClient client;

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        // https://issues.redhat.com/browse/WFCORE-6206
        Assume.assumeFalse("Patching subsystem has been removed in WildFly 29.",
                serverVersion.greaterThan(ServerVersion.VERSION_21_0_0));
    }

    @Before
    public void connect() throws IOException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
    }

    @After
    public void close() throws IOException {
        client.close();
    }

    @Test(expected = CommandFailedException.class)
    public void applyPatch_nonexistingPatch() throws CommandFailedException {
        client.apply(new ApplyPatch.Builder("invalid/path/to/patch.zip").build());
    }

    @Test(expected = CommandFailedException.class)
    public void rollbackPatch_nonexistingPatch() throws CommandFailedException {
        client.apply(new RollbackPatch.Builder("non-existing-patch-id").resetConfiguration(false).build());
    }
}
