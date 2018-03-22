package org.wildfly.extras.creaper.commands.elytron.http;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddProviderHttpServerMechanismFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestProviderHttpServerMechanismFactory";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME);
    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME2
            = "CreaperTestProviderHttpServerMechanismFactory2";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME2);

    private static final String PROVIDER_LOADER_NAME = "elytron";
    private static final String PROVIDER_LOADER_NAME2 = "combined-providers";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleProviderHttpServerMechanismFactory() throws Exception {
        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        client.apply(addProviderHttpServerMechanismFactory);

        assertTrue("Provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoProviderHttpServerMechanismFactories() throws Exception {
        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory2
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);

        assertTrue("Provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        assertTrue("Second provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullProviderHttpServerMechanismFactory() throws Exception {
        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME)
                .build();

        client.apply(addProviderHttpServerMechanismFactory);

        assertTrue("Provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "providers", PROVIDER_LOADER_NAME);

    }

    @Test(expected = CommandFailedException.class)
    public void addExistProviderHttpServerMechanismFactoryNotAllowed() throws Exception {
        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME)
                .build();

        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory2
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME2)
                .build();

        client.apply(addProviderHttpServerMechanismFactory);
        assertTrue("Provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addProviderHttpServerMechanismFactory2);
        fail("Provider http server mechanism factory CreaperTestProviderHttpServerMechanismFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistProviderHttpServerMechanismFactoryAllowed() throws Exception {
        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME)
                .build();

        AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory2
                = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME2)
                .replaceExisting()
                .build();

        client.apply(addProviderHttpServerMechanismFactory);
        assertTrue("Provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addProviderHttpServerMechanismFactory2);
        assertTrue("Provider http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "providers", PROVIDER_LOADER_NAME2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderHttpServerMechanismFactory_nullName() throws Exception {
        new AddProviderHttpServerMechanismFactory.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderHttpServerMechanismFactory_emptyName() throws Exception {
        new AddProviderHttpServerMechanismFactory.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }
}
