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
public class AddAggregateHttpServerMechanismFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestAggregateHttpServerMechanismFactory";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("aggregate-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME);
    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME2
            = "CreaperTestAggregateHttpServerMechanismFactory2";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("aggregate-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME2);

    private static final String TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestProviderHttpServerMechanismFactory";
    private static final Address TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME);
    private final AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
            = new AddProviderHttpServerMechanismFactory.Builder(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
            .build();
    private static final String TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2
            = "CreaperTestProviderHttpServerMechanismFactory2";
    private static final Address TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2);
    private final AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory2
            = new AddProviderHttpServerMechanismFactory.Builder(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
            .build();
    private static final String TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3
            = "CreaperTestProviderHttpServerMechanismFactory3";
    private static final Address TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS3 = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3);
    private final AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory3
            = new AddProviderHttpServerMechanismFactory.Builder(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3)
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS3);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleAggregateHttpServerMechanismFactory() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        client.apply(addAggregateHttpServerMechanismFactory);

        assertTrue("Aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoAggregateHttpServerMechanismFactories() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory2
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME2)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();
        client.apply(addAggregateHttpServerMechanismFactory);
        client.apply(addAggregateHttpServerMechanismFactory2);

        assertTrue("Aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        assertTrue("Second aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullAggregateHttpServerMechanismFactory() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);
        client.apply(addProviderHttpServerMechanismFactory3);

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3)
                .build();

        client.apply(addAggregateHttpServerMechanismFactory);

        assertTrue("Aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "http-server-mechanism-factories[0]",
                TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME);
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "http-server-mechanism-factories[1]",
                TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2);
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "http-server-mechanism-factories[2]",
                TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregateHttpServerMechanismFactoryNotAllowed() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory2
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3)
                .build();

        client.apply(addAggregateHttpServerMechanismFactory);
        assertTrue("Aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addAggregateHttpServerMechanismFactory2);
        fail("Aggregate http server mechanism factory CreaperTestAggregateHttpServerMechanismFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistAggregateHttpServerMechanismFactoryAllowed() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);
        client.apply(addProviderHttpServerMechanismFactory3);

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        AddAggregateHttpServerMechanismFactory addAggregateHttpServerMechanismFactory2
                = new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3)
                .replaceExisting()
                .build();

        client.apply(addAggregateHttpServerMechanismFactory);
        assertTrue("Aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addAggregateHttpServerMechanismFactory2);
        assertTrue("Aggregate http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "http-server-mechanism-factories[1]",
                TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateHttpServerMechanismFactory_nullName() throws Exception {
        new AddAggregateHttpServerMechanismFactory.Builder(null)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateHttpServerMechanismFactory_emptyName() throws Exception {
        new AddAggregateHttpServerMechanismFactory.Builder("")
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME,
                        TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME2)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateHttpServerMechanismFactory_nullHttpServerMechanismFactories() throws Exception {
        new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(null)
                .build();
        fail("Creating command with null http-server-mechanism-factories should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateHttpServerMechanismFactory_emptyHttpServerMechanismFactories() throws Exception {
        new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories("")
                .build();
        fail("Creating command with empty http-server-mechanism-factories should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateHttpServerMechanismFactory_oneHttpServerMechanismFactories() throws Exception {
        new AddAggregateHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addHttpServerMechanismFactories(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with only one http-server-mechanism-factories should throw exception");
    }
}
