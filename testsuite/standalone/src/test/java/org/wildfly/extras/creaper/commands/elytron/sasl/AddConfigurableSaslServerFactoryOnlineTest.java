package org.wildfly.extras.creaper.commands.elytron.sasl;

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
public class AddConfigurableSaslServerFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SERVER_FACTORY_NAME = "CreaperTestConfigurableSaslServerFactory";
    private static final Address TEST_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("configurable-sasl-server-factory", TEST_SERVER_FACTORY_NAME);
    private static final String TEST_SERVER_FACTORY_NAME2 = "CreaperTestConfigurableSaslServerFactory2";
    private static final Address TEST_SERVER_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("configurable-sasl-server-factory", TEST_SERVER_FACTORY_NAME2);

    private static final String TEST_PROVIDER_SERVER_FACTORY_NAME = "CreaperTestProviderSaslServerFactory";
    private static final Address TEST_PROVIDER_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_PROVIDER_SERVER_FACTORY_NAME);
    private final AddProviderSaslServerFactory addProviderSaslServerFactory
            = new AddProviderSaslServerFactory.Builder(TEST_PROVIDER_SERVER_FACTORY_NAME)
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_SERVER_FACTORY_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleConfigurableSaslServerFactory() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .build();

        client.apply(addConfigurableSaslServerFactory);

        assertTrue("Configurable sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoConfigurableSaslServerFactories() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .build();

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory2
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME2)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .build();

        client.apply(addConfigurableSaslServerFactory);
        client.apply(addConfigurableSaslServerFactory2);

        assertTrue("Configurable sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        assertTrue("Second configurable sasl server factory should be created",
                ops.exists(TEST_SERVER_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullConfigurableSaslServerFactory() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .protocol("someProtocol")
                .serverName("someServerName")
                .addFilters(new AddConfigurableSaslServerFactory.FilterBuilder()
                        .predefinedFilter("BINDING")
                        .enabling(false)
                        .build(),
                        new AddConfigurableSaslServerFactory.FilterBuilder()
                        .patternFilter("somePattern")
                        .build())
                .addProperties(new Property("a", "b"),
                        new Property("c", "d"))
                .build();

        client.apply(addConfigurableSaslServerFactory);

        assertTrue("Configurable sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));

        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "sasl-server-factory", TEST_PROVIDER_SERVER_FACTORY_NAME);
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "protocol", "someProtocol");
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "server-name", "someServerName");
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "filters[0].predefined-filter", "BINDING");
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "filters[0].enabling", "false");
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "filters[1].pattern-filter", "somePattern");
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "properties.a", "b");
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "properties.c", "d");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConfigurableSaslServerFactoryNotAllowed() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addProperties(new Property("a", "b"))
                .build();

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory2
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addProperties(new Property("c", "d"))
                .build();

        client.apply(addConfigurableSaslServerFactory);
        assertTrue("Configurable sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addConfigurableSaslServerFactory2);
        fail("Configurable sasl server factory CreaperTestConfigurableSaslServerFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConfigurableSaslServerFactoryAllowed() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addProperties(new Property("a", "b"))
                .build();

        AddConfigurableSaslServerFactory addConfigurableSaslServerFactory2
                = new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addProperties(new Property("c", "d"))
                .replaceExisting()
                .build();

        client.apply(addConfigurableSaslServerFactory);
        assertTrue("Configurable sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        client.apply(addConfigurableSaslServerFactory2);
        assertTrue("Configurable sasl server factory should be created", ops.exists(TEST_SERVER_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SERVER_FACTORY_ADDRESS, "properties.c", "d");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_nullName() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(null)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_emptyName() throws Exception {
        new AddConfigurableSaslServerFactory.Builder("")
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_nullSaslServerFactory() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(null)
                .build();
        fail("Creating command with null sasl-server-factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_emptySaslServerFactory() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory("")
                .build();
        fail("Creating command with empty sasl-server-factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_nullPatternFilter() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addFilters(new AddConfigurableSaslServerFactory.FilterBuilder().patternFilter(null).build())
                .build();
        fail("Creating command with null pattern-filter and without any predefined-filter in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_emptyPatternFilter() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addFilters(new AddConfigurableSaslServerFactory.FilterBuilder().patternFilter("").build())
                .build();
        fail("Creating command with empty pattern-filter and without any predefined-filter in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_nullPredefinedFilter() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addFilters(new AddConfigurableSaslServerFactory.FilterBuilder().predefinedFilter(null).build())
                .build();
        fail("Creating command with null predefined-filter and without any pattern-filter in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_emptyPredefinedFilter() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addFilters(new AddConfigurableSaslServerFactory.FilterBuilder().predefinedFilter("").build())
                .build();
        fail("Creating command with empty predefined-filter and without any pattern-filter in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_nullFilters() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addFilters(null)
                .build();
        fail("Creating command with null filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_nullProperties() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addProperties(null)
                .build();
        fail("Creating command with null properties should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConfigurableSaslServerFactory_bothPredefinedFilterAndPatternFilter() throws Exception {
        new AddConfigurableSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
                .saslServerFactory(TEST_PROVIDER_SERVER_FACTORY_NAME)
                .addFilters(new AddConfigurableSaslServerFactory.FilterBuilder()
                        .patternFilter("somePattern")
                        .predefinedFilter("BINDING")
                        .build())
                .build();
        fail("Creating command both predefined-filter a pattern-filter should throw exception");
    }

}
