package org.wildfly.extras.creaper.commands.elytron.providerloader;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddAggregateProvidersOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AGGREGATE_PROVIDERS_NAME = "CreaperTestAggregateProviders";
    private static final Address TEST_AGGREGATE_PROVIDERS_ADDRESS = SUBSYSTEM_ADDRESS
            .and("aggregate-providers", TEST_AGGREGATE_PROVIDERS_NAME);
    private static final String TEST_AGGREGATE_PROVIDERS_NAME2 = "CreaperTestAggregateProviders2";
    private static final Address TEST_AGGREGATE_PROVIDERS_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("aggregate-providers", TEST_AGGREGATE_PROVIDERS_NAME2);

    protected static final String TEST_PROVIDER_LOADER_NAME = "CreaperTestProviderLoader";
    protected static final Address TEST_PROVIDER_LOADER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-loader", TEST_PROVIDER_LOADER_NAME);
    protected static final String TEST_PROVIDER_LOADER_NAME2 = "CreaperTestProviderLoader2";
    protected static final Address TEST_PROVIDER_LOADER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("provider-loader", TEST_PROVIDER_LOADER_NAME2);

    @Before
    public void createProviderLoaders() throws Exception {
        AddProviderLoader addProviderLoader = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME)
                .build();
        AddProviderLoader addProviderLoader2 = new AddProviderLoader.Builder(TEST_PROVIDER_LOADER_NAME2)
                .build();
        client.apply(addProviderLoader);
        client.apply(addProviderLoader2);
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AGGREGATE_PROVIDERS_ADDRESS);
        ops.removeIfExists(TEST_AGGREGATE_PROVIDERS_ADDRESS2);
        ops.removeIfExists(TEST_PROVIDER_LOADER_ADDRESS);
        ops.removeIfExists(TEST_PROVIDER_LOADER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addAggregateProviders() throws Exception {
        AddAggregateProviders addAggregateProviders
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME, TEST_PROVIDER_LOADER_NAME2)
                .build();

        client.apply(addAggregateProviders);

        assertTrue("Aggregate-providers should be created", ops.exists(TEST_AGGREGATE_PROVIDERS_ADDRESS));
        checkAttribute(TEST_AGGREGATE_PROVIDERS_ADDRESS, "providers[0]", TEST_PROVIDER_LOADER_NAME);
        checkAttribute(TEST_AGGREGATE_PROVIDERS_ADDRESS, "providers[1]", TEST_PROVIDER_LOADER_NAME2);
    }

    @Test
    public void addTwoAggregateProviders() throws Exception {
        AddAggregateProviders addAggregateProviders
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME, TEST_PROVIDER_LOADER_NAME2)
                .build();

        AddAggregateProviders addAggregateProviders2
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME2)
                .providers(TEST_PROVIDER_LOADER_NAME2, TEST_PROVIDER_LOADER_NAME2)
                .build();

        client.apply(addAggregateProviders);
        client.apply(addAggregateProviders2);

        assertTrue("Aggregate-providers should be created", ops.exists(TEST_AGGREGATE_PROVIDERS_ADDRESS));
        assertTrue("Second aggregate-providers should be created", ops.exists(TEST_AGGREGATE_PROVIDERS_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAggregateProvidersNotAllowed() throws Exception {
        AddAggregateProviders addAggregateProviders
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME, TEST_PROVIDER_LOADER_NAME2)
                .build();

        AddAggregateProviders addAggregateProviders2
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME2, TEST_PROVIDER_LOADER_NAME2)
                .build();

        client.apply(addAggregateProviders);
        assertTrue("Aggregate-providers should be created", ops.exists(TEST_AGGREGATE_PROVIDERS_ADDRESS));

        client.apply(addAggregateProviders2);
        fail("Aggregate-providers CreaperTestAggregateProviders already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistAggregateProvidersAllowed() throws Exception {
        AddAggregateProviders addAggregateProviders
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME, TEST_PROVIDER_LOADER_NAME2)
                .build();

        AddAggregateProviders addAggregateProviders2
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME2, TEST_PROVIDER_LOADER_NAME2)
                .replaceExisting()
                .build();

        client.apply(addAggregateProviders);
        assertTrue("Aggregate-providers should be created", ops.exists(TEST_AGGREGATE_PROVIDERS_ADDRESS));

        client.apply(addAggregateProviders2);
        assertTrue("Aggregate-providers should be created", ops.exists(TEST_AGGREGATE_PROVIDERS_ADDRESS));

        // check whether it was really rewritten
        checkAttribute(TEST_AGGREGATE_PROVIDERS_ADDRESS, "providers[1]", TEST_PROVIDER_LOADER_NAME2);
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregateProvidersWithoutConfiguredProviderLoader() throws Exception {
        AddAggregateProviders addAggregateProviders
                = new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME, "NotConfiguredProviderLoader")
                .build();

        client.apply(addAggregateProviders);
        fail("Aggregate-providers shouldn't be added when using unconfigured provider-loader");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateProviders_nullName() throws Exception {
        new AddAggregateProviders.Builder(null)
                .providers(TEST_PROVIDER_LOADER_NAME, TEST_PROVIDER_LOADER_NAME2)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateProviders_emptyName() throws Exception {
        new AddAggregateProviders.Builder("")
                .providers(TEST_PROVIDER_LOADER_NAME, TEST_PROVIDER_LOADER_NAME2)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateProviders_nullProviders() throws Exception {
        new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(null)
                .build();
        fail("Creating command with null providers should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateProviders_emptyProviders() throws Exception {
        new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers("")
                .build();
        fail("Creating command with empty providers should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateProviders_oneProvider() throws Exception {
        new AddAggregateProviders.Builder(TEST_AGGREGATE_PROVIDERS_NAME)
                .providers(TEST_PROVIDER_LOADER_NAME)
                .build();
        fail("Creating command with only one provider should throw exception");
    }

}
