package org.wildfly.extras.creaper.commands.elytron.providerloader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.modules.AddModule;
import org.wildfly.extras.creaper.commands.modules.RemoveModule;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddProviderLoaderOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_PROVIDER_LOADER_NAME = "CreaperTestProviderLoader";
    private static final Address TEST_PROVIDER_LOADER_ADDRESS = SUBSYSTEM_ADDRESS.and("provider-loader",
            TEST_PROVIDER_LOADER_NAME);
    private static final String TEST_PROVIDER_LOADER_NAME2 = "CreaperTestProviderLoader2";
    private static final Address TEST_PROVIDER_LOADER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("provider-loader",
            TEST_PROVIDER_LOADER_NAME2);

    private static final String TEST_MODULE_NAME = "com.example.creaper-test-module";

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_PROVIDER_LOADER_ADDRESS);
        ops.removeIfExists(TEST_PROVIDER_LOADER_ADDRESS2);
        removeModuleQuietly();

        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleProviderLoader() throws Exception {
        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .build();

        client.apply(addProviderLoader);

        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
    }

    @Test
    public void addTwoProviderLoaders() throws Exception {
        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .build();
        AddProviderLoader addProviderLoader2 = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME2)
                .build();

        client.apply(addProviderLoader);
        client.apply(addProviderLoader2);

        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        assertTrue("Second provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS2));
    }

    @Test
    public void addFullProviderLoaderConfiguration() throws Exception {
        File testJar1 = createJar("testJar", AddProviderLoaderImpl.class);
        AddModule addModule = new AddModule.Builder(TEST_MODULE_NAME)
                .resource(testJar1)
                .build();
        client.apply(addModule);

        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .module(TEST_MODULE_NAME)
                .classNames(AddProviderLoaderImpl.class.getCanonicalName())
                // https://issues.redhat.com/browse/WFCORE-5952
                // java.security.Provider changed after JDK8
                // can be enabled again when we test only on JDK 11+ and update AddProviderLoaderImpl
                // .addConfiguration("configurationName", "configurationValue")
                .build();

        client.apply(addProviderLoader);

        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "module", TEST_MODULE_NAME);
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "class-names[0]", AddProviderLoaderImpl.class.getCanonicalName());
        // as above
        // checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "configuration.configurationName", "configurationValue");
    }

    @Test
    public void addFullProviderLoaderPath() throws Exception {
        File testJar1 = createJar("testJar", AddProviderLoaderImpl.class);

        AddModule addModule = new AddModule.Builder(TEST_MODULE_NAME)
                .resource(testJar1)
                .build();
        client.apply(addModule);

        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .module(TEST_MODULE_NAME)
                .classNames(AddProviderLoaderImpl.class.getCanonicalName())
                // https://issues.redhat.com/browse/WFCORE-5952
                // java.security.Provider changed after JDK8
                // can be enabled again when we test only on JDK 11+ and update AddProviderLoaderImpl
                // .path("application-users.properties")
                // .relativeTo("jboss.server.config.dir")
                .build();

        client.apply(addProviderLoader);

        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "module", TEST_MODULE_NAME);
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "class-names[0]", AddProviderLoaderImpl.class.getCanonicalName());
        // as above
        // checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "path", "application-users.properties");
        // checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "relative-to", "jboss.server.config.dir");
    }

    @Test
    public void addProviderLoaderArgument() throws Exception {
        File testJar1 = createJar("testJar", AddProviderLoaderImpl.class);

        AddModule addModule = new AddModule.Builder(TEST_MODULE_NAME)
                .resource(testJar1)
                .build();
        client.apply(addModule);

        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .module(TEST_MODULE_NAME)
                .classNames(AddProviderLoaderImpl.class.getCanonicalName())
                .argument("constructor_argument")
                .build();

        client.apply(addProviderLoader);

        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "module", TEST_MODULE_NAME);
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "class-names[0]", AddProviderLoaderImpl.class.getCanonicalName());
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "argument", "constructor_argument");
    }

    @Test
    public void addProviderLoaderConfiguration() throws Exception {
        File testJar1 = createJar("testJar", AddProviderLoaderImpl.class);

        AddModule addModule = new AddModule.Builder(TEST_MODULE_NAME)
                .resource(testJar1)
                .build();
        client.apply(addModule);

        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .module(TEST_MODULE_NAME)
                .classNames(AddProviderLoaderImpl.class.getCanonicalName())
                // https://issues.redhat.com/browse/WFCORE-5952
                // java.security.Provider changed after JDK8
                // can be enabled again when we test only on JDK 11+ and update AddProviderLoaderImpl
                // .addConfiguration("key", "value")
                .build();

        client.apply(addProviderLoader);

        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "module", TEST_MODULE_NAME);
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "class-names[0]", AddProviderLoaderImpl.class.getCanonicalName());
        // as above
        // checkAttributeObject(TEST_PROVIDER_LOADER_ADDRESS, "configuration", "key", "value");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistProviderLoaderNotAllowed() throws Exception {
        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .build();
        AddProviderLoader addProviderLoader2 = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .build();

        client.apply(addProviderLoader);
        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        client.apply(addProviderLoader2);
        fail("Provider loader CreaperTestProviderLoader already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistProviderLoaderAllowed() throws Exception {
        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .build();
        AddProviderLoader addProviderLoader2 = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .addConfiguration("configurationName", "configurationValue")
                .replaceExisting()
                .build();

        client.apply(addProviderLoader);
        assertTrue("Provider loader should be created", ops.exists(TEST_PROVIDER_LOADER_ADDRESS));
        client.apply(addProviderLoader2);

        // check whether it was really rewritten
        checkAttribute(TEST_PROVIDER_LOADER_ADDRESS, "configuration.configurationName", "configurationValue");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_configurationAndPath() throws Exception {
        new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .addConfiguration("configurationName", "configurationValue")
                .path("application-users.properties")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_configurationAndArgument() throws Exception {
        new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .addConfiguration("configurationName", "configurationValue")
                .argument("argument")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_pathAndArgument() throws Exception {
        new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .path("path")
                .argument("argument")
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_nullName() throws Exception {
        new AddProviderLoader.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_emptyName() throws Exception {
        new AddProviderLoader.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_nullClassNames() throws Exception {
        new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .classNames(null)
                .build();
        fail("Creating command with null class-names should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderLoader_nullConfiguration() throws Exception {
        new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .addConfiguration(null, null)
                .build();
        fail("Creating command with null configuration should throw exception");
    }

    private void removeModuleQuietly() throws IOException {
        try {
            RemoveModule removeModule = new RemoveModule(TEST_MODULE_NAME);
            client.apply(removeModule);
        } catch (CommandFailedException ignore) {
            // ignored
        }
    }
}
