package org.wildfly.extras.creaper.commands.deployments;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeoutException;

/**
 * This class tests deploy and undeploy commands in standalone mode.
 */
@RunWith(Arquillian.class)
@RunAsClient
public class DeployUndeployCmdTest {

    private static final String DEPLOYMENT_NAME = "test-deployment.war";
    private static final Address TEST_DEPLOYMENT_ADDRESS = Address.deployment(DEPLOYMENT_NAME);
    private static final WebArchive TEST_DEPLOYMENT = ShrinkWrap.create(WebArchive.class, DEPLOYMENT_NAME)
            .add(new StringAsset("Hello Creaper"), "index.html");

    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private OnlineManagementClient client;
    private Operations ops;
    private Administration admin;

    @Before
    public void prepare() throws IOException, OperationException, TimeoutException, InterruptedException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        admin = new Administration(client);
        ops.removeIfExists(TEST_DEPLOYMENT_ADDRESS);
        admin.reloadIfRequired();
    }

    @After
    public void cleanAndClose() throws Exception {
        try {
            ops.removeIfExists(TEST_DEPLOYMENT_ADDRESS);
            admin.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void deployAsInputStream_commandSucceeds() throws Exception {
        InputStream inputStream = TEST_DEPLOYMENT.as(ZipExporter.class).exportAsInputStream();
        client.apply(new Deploy.Builder(inputStream, DEPLOYMENT_NAME, true).build());
        Assert.assertTrue(ops.exists(TEST_DEPLOYMENT_ADDRESS));
        undeploy(DEPLOYMENT_NAME);
    }

    @Test
    public void deployAsFile_commandSucceeds() throws Exception {
        deployAsFile(TEST_DEPLOYMENT);
        undeploy(TEST_DEPLOYMENT.getName());
    }

    @Test
    public void deployAfterUndeployTest() throws Exception {
        deployAsFile(TEST_DEPLOYMENT);
        undeploy(TEST_DEPLOYMENT.getName());
        deployAsFile(TEST_DEPLOYMENT);
        undeploy(TEST_DEPLOYMENT.getName());
    }

    @Test
    public void undeployKeepContentTest() throws OperationException, IOException, CommandFailedException {
        deployAsFile(TEST_DEPLOYMENT);
        client.apply(new Undeploy.Builder(TEST_DEPLOYMENT.getName()).keepContent().build());
        assertDeploymentExists(TEST_DEPLOYMENT.getName(), true);
        ops.removeIfExists(TEST_DEPLOYMENT_ADDRESS);
    }

    private void assertDeploymentExists(String deploymentName, boolean shouldExist)
            throws IOException, OperationException {
        boolean exists = ops.exists(Address.deployment(deploymentName));
        if (shouldExist) {
            Assert.assertTrue("Deployment should exist!", exists);
        } else {
            Assert.assertFalse("Deployment shouldn't exist!", exists);
        }
    }

    private void deployAsFile(Archive archive) throws CommandFailedException, IOException, OperationException {
        File testDeploymentFile = new File(tmp.getRoot(), archive.getName());
        TEST_DEPLOYMENT.as(ZipExporter.class).exportTo(testDeploymentFile, true);
        client.apply(new Deploy.Builder(testDeploymentFile).build());
        assertDeploymentExists(archive.getName(), true);
    }

    private void undeploy(String deploymentName) throws CommandFailedException, IOException, OperationException {
        client.apply(new Undeploy.Builder(deploymentName).build());
        assertDeploymentExists(deploymentName, false);
    }
}
