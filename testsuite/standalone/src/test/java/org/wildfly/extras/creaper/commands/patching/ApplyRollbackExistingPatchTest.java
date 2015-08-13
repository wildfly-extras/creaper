package org.wildfly.extras.creaper.commands.patching;

import com.google.common.base.Charsets;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.shrinkwrap.AssetByteSource;
import org.wildfly.extras.creaper.test.ManualTests;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This test <b>needs</b> a manually-controlled Arquillian container and <b>can't</b> use
 * {@code Administration.restartIfRequired}, because it's not possible to restart the application server via management
 * interface ({@code :shutdown(restart=true)}) if it was started by Arquillian. This is because restarting the entire
 * JVM process relies on the start script ({@code standalone.sh}), which Arquillian doesn't use.
 */
@Category(ManualTests.class)
@RunWith(Arquillian.class)
public class ApplyRollbackExistingPatchTest {
    private OnlineManagementClient client = ManagementClient.onlineLazy(
            OnlineOptions.standalone().localDefault().build());
    private Operations ops = new Operations(client);
    private PatchingOperations patching = new PatchingOperations(client);

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @ArquillianResource
    private ContainerController controller;

    @Test
    @InSequence(1)
    public void startServer() {
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
    }

    @Test
    @InSequence(2)
    public void applyPatch() throws Exception {
        File patchZip = tmp.newFile("test-patch.zip");

        ModelNodeResult serverVersionResult = ops.readAttribute(Address.root(), "product-version");
        if (!serverVersionResult.hasDefinedValue()) { // happens on WildFly 8
            serverVersionResult = ops.readAttribute(Address.root(), "release-version");
        }
        serverVersionResult.assertDefinedValue("Server version required for generating a test patch");
        String serverVersion = serverVersionResult.stringValue();

        GenericArchive zip = ShrinkWrap.create(ZipImporter.class)
                .importFrom(ApplyRollbackExistingPatchTest.class.getResourceAsStream("test-patch.zip.template"))
                .as(GenericArchive.class);
        String patchXml = new AssetByteSource(zip.delete("patch.xml").getAsset()).asCharSource(Charsets.UTF_8).read();
        String newPatchXml = patchXml
                .replace("%EAP_VERSION_ORIG%", serverVersion)
                .replace("%EAP_VERSION_NEW%", serverVersion + "_PATCHED");
        zip.add(new StringAsset(newPatchXml), "patch.xml");
        zip.as(ZipExporter.class).exportTo(patchZip, true);

        client.apply(new ApplyPatch.Builder(patchZip).build());
    }

    @Test
    @InSequence(3)
    public void restartServerAfterPatchApply() throws TimeoutException, InterruptedException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
        client.reconnect(10);
    }

    @Test
    @InSequence(4)
    public void assertPatchInstalled() throws IOException {
        assertTrue(patching.isPatchInstalled("test-patch"));
    }

    @Test
    @InSequence(5)
    public void rollbackPatch() throws CommandFailedException {
        client.apply(new RollbackPatch.Builder("test-patch").resetConfiguration(false).build());
    }

    @Test
    @InSequence(6)
    public void restartServerAfterPatchRollback() throws TimeoutException, InterruptedException {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
        controller.start(ManualTests.ARQUILLIAN_CONTAINER);
        client.reconnect(10);
    }

    @Test
    @InSequence(7)
    public void assertPatchNotInstalled() throws IOException {
        assertFalse(patching.isAnyPatchInstalled());
    }

    @Test
    @InSequence(8)
    public void stopServer() {
        controller.stop(ManualTests.ARQUILLIAN_CONTAINER);
    }

    @After
    public void tearDown() throws IOException {
        client.close();
    }
}
