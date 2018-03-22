package org.wildfly.extras.creaper.commands.elytron.sasl;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddMechanismProviderFilteringSaslServerFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_FACTORY_NAME = "CreaperTestFactory";
    private static final Address TEST_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("mechanism-provider-filtering-sasl-server-factory", TEST_FACTORY_NAME);
    private static final String TEST_FACTORY_NAME2 = "CreaperTestFactory2";
    private static final Address TEST_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("mechanism-provider-filtering-sasl-server-factory", TEST_FACTORY_NAME2);

    private static final String TEST_SERVER_FACTORY_NAME
            = "CreaperTestProviderSaslServerFactory";
    private static final Address TEST_SERVER_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-sasl-server-factory", TEST_SERVER_FACTORY_NAME);
    private final AddProviderSaslServerFactory addProviderSaslServerFactory
            = new AddProviderSaslServerFactory.Builder(TEST_SERVER_FACTORY_NAME)
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_SERVER_FACTORY_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleMechanismProviderFilteringSaslServerFactory() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .build();

        client.apply(addMechanismProviderFilteringSaslServerFactory);

        assertTrue("Mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoMechanismProviderFilteringSaslServerFactories() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .build();
        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory2
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME2)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .build();

        client.apply(addMechanismProviderFilteringSaslServerFactory);
        client.apply(addMechanismProviderFilteringSaslServerFactory2);

        assertTrue("Mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS));
        assertTrue("Second mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullMechanismProviderFilteringSaslServerFactory() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .enabling(false)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .mechanismName("someMechanismName")
                        .providerVersion(2.0)
                        .versionComparison(
                                AddMechanismProviderFilteringSaslServerFactory.VersionComparison.GREATER_THAN)
                        .build(),
                        new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName2")
                        .mechanismName("someMechanismName2")
                        .providerVersion(1.5)
                        .versionComparison(AddMechanismProviderFilteringSaslServerFactory.VersionComparison.LESS_THAN)
                        .build())
                .build();

        client.apply(addMechanismProviderFilteringSaslServerFactory);

        assertTrue("Mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS));

        checkAttribute("sasl-server-factory", TEST_SERVER_FACTORY_NAME);
        checkAttribute("enabling", "false");

        checkAttribute("filters[0].provider-name", "someProviderName");
        checkAttribute("filters[0].mechanism-name", "someMechanismName");
        checkAttribute("filters[0].provider-version", "2.0");
        checkAttribute("filters[0].version-comparison", "greater-than");

        checkAttribute("filters[1].provider-name", "someProviderName2");
        checkAttribute("filters[1].mechanism-name", "someMechanismName2");
        checkAttribute("filters[1].provider-version", "1.5");
        checkAttribute("filters[1].version-comparison", "less-than");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistMechanismProviderFilteringSaslServerFactoryNotAllowed() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .build())
                .build();
        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory2
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName2")
                        .build())
                .build();

        client.apply(addMechanismProviderFilteringSaslServerFactory);
        assertTrue("Mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS));
        client.apply(addMechanismProviderFilteringSaslServerFactory2);
        fail("Mechanism provider filtering sasl server factory CreaperTestFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistMechanismProviderFilteringSaslServerFactoryAllowed() throws Exception {
        client.apply(addProviderSaslServerFactory);

        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .build())
                .build();
        AddMechanismProviderFilteringSaslServerFactory addMechanismProviderFilteringSaslServerFactory2
                = new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName2")
                        .build())
                .replaceExisting()
                .build();

        client.apply(addMechanismProviderFilteringSaslServerFactory);
        assertTrue("Mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS));
        client.apply(addMechanismProviderFilteringSaslServerFactory2);
        assertTrue("Mechanism provider filtering sasl server factory should be created",
                ops.exists(TEST_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute("filters[0].provider-name", "someProviderName2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMechanismProviderFilteringSaslServerFactory_nullName() throws Exception {
        new AddMechanismProviderFilteringSaslServerFactory.Builder(null)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .build())
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMechanismProviderFilteringSaslServerFactory_emptyName() throws Exception {
        new AddMechanismProviderFilteringSaslServerFactory.Builder("")
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .build())
                .build();
        fail("Creating command with empty name should throw exception");

    }

    @Test(expected = IllegalArgumentException.class)
    public void addMechanismProviderFilteringSaslServerFactory_nullSaslServerFactory() throws Exception {
        new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(null)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .build())
                .build();
        fail("Creating command with null sasl-server-factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMechanismProviderFilteringSaslServerFactory_emptySaslServerFactory() throws Exception {
        new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory("")
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("someProviderName")
                        .build())
                .build();
        fail("Creating command with empty sasl-server-factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMechanismProviderFilteringSaslServerFactory_nullProviderName_filters() throws Exception {
        new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName(null)
                        .build())
                .build();
        fail("Creating command with null provider name in filters should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMechanismProviderFilteringSaslServerFactory_emptyProviderName_filters() throws Exception {
        new AddMechanismProviderFilteringSaslServerFactory.Builder(TEST_FACTORY_NAME)
                .saslServerFactory(TEST_SERVER_FACTORY_NAME)
                .addFilters(new AddMechanismProviderFilteringSaslServerFactory.FilterBuilder()
                        .providerName("")
                        .build())
                .build();
        fail("Creating command with empty provider name in filters should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_FACTORY_ADDRESS, attribute, expectedValue);
    }
}
