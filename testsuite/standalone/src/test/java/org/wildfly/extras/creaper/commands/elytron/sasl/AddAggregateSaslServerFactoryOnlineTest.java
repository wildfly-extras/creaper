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
public class AddAggregateSaslServerFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_FACTORY_NAME = "CreaperTestAggregateSaslServerFactory";
    private static final Address TEST_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("aggregate-sasl-server-factory", TEST_SERVER_FACTORY_NAME);
    private static final String TEST_SERVER_FACTORY_NAME2 = "CreaperTestAggregateSaslServerFactory2";
    private static final Address TEST_SERVER_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("aggregate-sasl-server-factory", TEST_SERVER_FACTORY_NAME2);

    private static final String TEST_PROVIDER_SERVER_FACTORY_NAME = "CreaperTestProviderSaslServerFactory";
    private static final Address TEST_PROVIDER_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_PROVIDER_SERVER_FACTORY_NAME);
    private final AddProviderSaslServerFactory addProviderSaslServerFactory
            = new AddProviderSaslServerFactory.Builder(TEST_PROVIDER_SERVER_FACTORY_NAME)
            .build();
    private static final String TEST_PROVIDER_SERVER_FACTORY_NAME2 = "CreaperTestProviderSaslServerFactory2";
    private static final Address TEST_PROVIDER_SERVER_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_PROVIDER_SERVER_FACTORY_NAME2);
    private final AddProviderSaslServerFactory addProviderSaslServerFactory2
            = new AddProviderSaslServerFactory.Builder(TEST_PROVIDER_SERVER_FACTORY_NAME2)
            .build();
    private static final String TEST_PROVIDER_SERVER_FACTORY_NAME3 = "CreaperTestProviderSaslServerFactory3";
    private static final Address TEST_PROVIDER_SERVER_FACTORY_ADDRESS3 = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_PROVIDER_SERVER_FACTORY_NAME3);
    private final AddProviderSaslServerFactory addProviderSaslServerFactory3
            = new AddProviderSaslServerFactory.Builder(TEST_PROVIDER_SERVER_FACTORY_NAME3)
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_SERVER_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_PROVIDER_SERVER_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_SERVER_FACTORY_ADDRESS3);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleAggregateSaslServerFactory() throws Exception {
        client.apply(addProviderSaslServerFactory);
        client.apply(addProviderSaslServerFactory2);

        AddAggregateSaslServerFactory addAggregateSaslServerFactory
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();

        client.apply(addAggregateSaslServerFactory);

        assertTrue("Aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoAggregateSaslServerFactories() throws Exception {
        client.apply(addProviderSaslServerFactory);
        client.apply(addProviderSaslServerFactory2);

        AddAggregateSaslServerFactory addAggregateSaslServerFactory
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();

        AddAggregateSaslServerFactory addAggregateSaslServerFactory2
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME2)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();
        client.apply(addAggregateSaslServerFactory);
        client.apply(addAggregateSaslServerFactory2);

        assertTrue("Aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        assertTrue("Second aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullAggregateSaslServerFactory() throws Exception {
        client.apply(addProviderSaslServerFactory);
        client.apply(addProviderSaslServerFactory2);
        client.apply(addProviderSaslServerFactory3);

        AddAggregateSaslServerFactory addAggregateSaslServerFactory
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2,
                        TEST_PROVIDER_SERVER_FACTORY_NAME3)
                .build();

        client.apply(addAggregateSaslServerFactory);

        assertTrue("Aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "sasl-server-factories[0]", TEST_PROVIDER_SERVER_FACTORY_NAME);
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "sasl-server-factories[1]", TEST_PROVIDER_SERVER_FACTORY_NAME2);
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "sasl-server-factories[2]", TEST_PROVIDER_SERVER_FACTORY_NAME3);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregateSaslServerFactoryNotAllowed() throws Exception {
        client.apply(addProviderSaslServerFactory);
        client.apply(addProviderSaslServerFactory2);

        AddAggregateSaslServerFactory addAggregateSaslServerFactory
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();

        AddAggregateSaslServerFactory addAggregateSaslServerFactory2
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME3)
                .build();

        client.apply(addAggregateSaslServerFactory);
        assertTrue("Aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addAggregateSaslServerFactory2);
        fail("Aggregate sasl server factory CreaperTestAggregateSaslServerFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistAggregateSaslServerFactoryAllowed() throws Exception {
        client.apply(addProviderSaslServerFactory);
        client.apply(addProviderSaslServerFactory2);
        client.apply(addProviderSaslServerFactory3);

        AddAggregateSaslServerFactory addAggregateSaslServerFactory
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();

        AddAggregateSaslServerFactory addAggregateSaslServerFactory2
                = new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME3)
                .replaceExisting()
                .build();

        client.apply(addAggregateSaslServerFactory);
        assertTrue("Aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addAggregateSaslServerFactory2);
        assertTrue("Aggregate sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "sasl-server-factories[1]",
                TEST_PROVIDER_SERVER_FACTORY_NAME3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSaslServerFactory_nullName() throws Exception {
        new AddAggregateSaslServerFactory.Builder(null)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSaslServerFactory_emptyName() throws Exception {
        new AddAggregateSaslServerFactory.Builder("")
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_FACTORY_NAME2)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSaslServerFactory_nullSaslServerFactories() throws Exception {
        new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(null)
                .build();
        fail("Creating command with null sasl-server-factories should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSaslServerFactory_emptySaslServerFactories() throws Exception {
        new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories("")
                .build();
        fail("Creating command with empty sasl-server-factories should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateSaslServerFactory_oneSaslServerFactories() throws Exception {
        new AddAggregateSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .addSaslServerFactories(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .build();
        fail("Creating command with only one sasl-server-factories should throw exception");
    }

}
