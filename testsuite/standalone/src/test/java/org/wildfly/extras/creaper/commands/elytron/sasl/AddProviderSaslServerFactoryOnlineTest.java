package org.wildfly.extras.creaper.commands.elytron.sasl;

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
public class AddProviderSaslServerFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_FACTORY_NAME = "CreaperTestProviderSaslServerFactory";
    private static final Address TEST_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_SERVER_FACTORY_NAME);
    private static final String TEST_SERVER_FACTORY_NAME2 = "CreaperTestProviderSaslServerFactory2";
    private static final Address TEST_SERVER_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_SERVER_FACTORY_NAME2);

    private static final String PROVIDER_LOADER_NAME = "elytron";
    private static final String PROVIDER_LOADER_NAME2 = "combined-providers";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleProviderSaslServerFactory() throws Exception {
        AddProviderSaslServerFactory addProviderSaslServerFactory
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .build();

        client.apply(addProviderSaslServerFactory);

        assertTrue("Provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoProviderSaslServerFactories() throws Exception {
        AddProviderSaslServerFactory addProviderSaslServerFactory
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .build();

        AddProviderSaslServerFactory addProviderSaslServerFactory2
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME2)
                .build();

        client.apply(addProviderSaslServerFactory);
        client.apply(addProviderSaslServerFactory2);

        assertTrue("Provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        assertTrue("Second provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullProviderSaslServerFactory() throws Exception {
        AddProviderSaslServerFactory addProviderSaslServerFactory
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME)
                .build();

        client.apply(addProviderSaslServerFactory);

        assertTrue("Provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "providers", PROVIDER_LOADER_NAME);

    }

    @Test(expected = CommandFailedException.class)
    public void addExistProviderSaslServerFactoryNotAllowed() throws Exception {
        AddProviderSaslServerFactory addProviderSaslServerFactory
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME)
                .build();

        AddProviderSaslServerFactory addProviderSaslServerFactory2
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME2)
                .build();

        client.apply(addProviderSaslServerFactory);
        assertTrue("Provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addProviderSaslServerFactory2);
        fail("Provider sasl server factory CreaperTestProviderSaslServerFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistProviderSaslServerFactoryAllowed() throws Exception {
        AddProviderSaslServerFactory addProviderSaslServerFactory
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME)
                .build();

        AddProviderSaslServerFactory addProviderSaslServerFactory2
                = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .providers(PROVIDER_LOADER_NAME2)
                .replaceExisting()
                .build();

        client.apply(addProviderSaslServerFactory);
        assertTrue("Provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addProviderSaslServerFactory2);
        assertTrue("Provider sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "providers", PROVIDER_LOADER_NAME2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderSaslServerFactory_nullName() throws Exception {
        new AddProviderSaslServerFactory.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addProviderSaslServerFactory_emptyName() throws Exception {
        new AddProviderSaslServerFactory.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

}
