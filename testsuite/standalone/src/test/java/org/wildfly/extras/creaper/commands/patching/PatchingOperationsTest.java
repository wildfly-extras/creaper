package org.wildfly.extras.creaper.commands.patching;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(Arquillian.class)
public class PatchingOperationsTest {
    private OnlineManagementClient client;
    private PatchingOperations patchingOps;

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
        patchingOps = new PatchingOperations(client);
    }

    @After
    public void close() throws IOException {
        client.close();
    }

    @Test
    public void getPatchHistory_noPatches() throws IOException {
        List<PatchingOperations.PatchHistoryEntry> history = patchingOps.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    public void getPatchHistoryEntry_nonExistingPatch() throws IOException {
        assertNull(patchingOps.getHistoryEntry("non-existing-patch-id"));
    }

    @Test
    public void isAnyPatchInstalled() throws IOException {
        List<PatchingOperations.PatchHistoryEntry> history = patchingOps.getHistory();
        assertEquals(history.isEmpty(), !patchingOps.isAnyPatchInstalled());
    }

    @Test
    public void isPatchInstalled_nonExistingPatch() throws IOException {
        assertFalse(patchingOps.isPatchInstalled("non-existing-patch-id"));
    }
}
