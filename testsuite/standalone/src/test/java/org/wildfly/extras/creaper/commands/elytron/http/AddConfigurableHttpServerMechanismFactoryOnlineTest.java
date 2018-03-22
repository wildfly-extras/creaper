package org.wildfly.extras.creaper.commands.elytron.http;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.Property;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddConfigurableHttpServerMechanismFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestConfigurableHttpServerMechanismFactory";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("configurable-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME);
    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME2
            = "CreaperTestConfigurableHttpServerMechanismFactory2";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("configurable-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME2);

    private static final String TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestProviderHttpServerMechanismFactory";
    private static final Address TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME);
    private final AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
            = new AddProviderHttpServerMechanismFactory.Builder(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleConfigurableHttpServerMechanismFactory() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        client.apply(addConfigurableHttpServerMechanismFactory);

        assertTrue("Configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoConfigurableHttpServerMechanismFactories() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory2
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME2)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        client.apply(addConfigurableHttpServerMechanismFactory);
        client.apply(addConfigurableHttpServerMechanismFactory2);

        assertTrue("Configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        assertTrue("Second configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullConfigurableHttpServerMechanismFactory() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addFilters(new AddConfigurableHttpServerMechanismFactory.FilterBuilder()
                        .patternFilter("somePattern")
                        .enabling(false)
                        .build(),
                        new AddConfigurableHttpServerMechanismFactory.FilterBuilder()
                        .patternFilter("somePattern2")
                        .build())
                .addProperties(new Property("a", "b"),
                        new Property("c", "d"))
                .build();

        client.apply(addConfigurableHttpServerMechanismFactory);

        assertTrue("Configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "http-server-mechanism-factory",
                TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME);
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "filters[0].pattern-filter", "somePattern");
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "filters[0].enabling", "false");
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "filters[1].pattern-filter", "somePattern2");
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "properties.a", "b");
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "properties.c", "d");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConfigurableHttpServerMechanismFactoryNotAllowed() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addProperties(new Property("a", "b"))
                .build();

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory2
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addProperties(new Property("c", "d"))
                .build();

        client.apply(addConfigurableHttpServerMechanismFactory);
        assertTrue("Configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addConfigurableHttpServerMechanismFactory2);
        fail("Configurable http server mechanism factory CreaperTestConfigurableHttpServerMechanismFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConfigurableHttpServerMechanismFactoryAllowed() throws Exception {
        client.apply(addProviderHttpServerMechanismFactory);

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addProperties(new Property("a", "b"))
                .build();

        AddConfigurableHttpServerMechanismFactory addConfigurableHttpServerMechanismFactory2
                = new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addProperties(new Property("c", "d"))
                .replaceExisting()
                .build();

        client.apply(addConfigurableHttpServerMechanismFactory);
        assertTrue("Configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        client.apply(addConfigurableHttpServerMechanismFactory2);
        assertTrue("Configurable http server mechanism factory should be created",
                ops.exists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_MECHANISM_FACTORY_ADDRESS, "properties.c", "d");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_nullName() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(null)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_emptyName() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder("")
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_nullHttpServerMechanismFactory() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(null)
                .build();
        fail("Creating command with null http-server-mechanism-factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_emptyHttpServerMechanismFactory() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory("")
                .build();
        fail("Creating command with empty http-server-mechanism-factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_nullPatternFilter() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addFilters(new AddConfigurableHttpServerMechanismFactory.FilterBuilder().patternFilter(null).build())
                .build();
        fail("Creating command with null pattern-filter in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_emptyPatternFilter() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addFilters(new AddConfigurableHttpServerMechanismFactory.FilterBuilder().patternFilter("").build())
                .build();
        fail("Creating command with empty pattern-filter in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_nullFilters() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addFilters(null)
                .build();
        fail("Creating command with null filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableHttpServerMechanismFactory_nullProperties() throws Exception {
        new AddConfigurableHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .httpServerMechanismFactory(TEST_PROVIDER_SERVER_MECHANISM_FACTORY_NAME)
                .addProperties(null)
                .build();
        fail("Creating command with null properties should throw exception");
    }
}
