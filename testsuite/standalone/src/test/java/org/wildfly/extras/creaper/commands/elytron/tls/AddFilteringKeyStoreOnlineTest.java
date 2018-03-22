package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
public class AddFilteringKeyStoreOnlineTest extends AbstractElytronOnlineTest {

    private static final String KEY_STORE_NAME = "CreaperTestKeyStore";
    private static final Address KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store", KEY_STORE_NAME);
    private static final String KEY_STORE_TYPE = "JKS";
    private static final String TEST_KEY_STORE_PASSWORD = "password";

    private static final String FILTERING_KEY_STORE_NAME = "CreaperTestFilteringKeyStore";
    private static final String FILTERING_KEY_STORE_NAME2 = "CreaperTestFilteringKeyStore2";
    private static final Address FILTERING_KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("filtering-key-store",
            FILTERING_KEY_STORE_NAME);
    private static final Address FILTERING_KEY_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS.and("filtering-key-store",
            FILTERING_KEY_STORE_NAME2);
    private static final String ALIAS_FILTER = "alias";
    private static final String ALIAS_FILTER2 = "alias2";

    @BeforeClass
    public static void addKeyStores() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddKeyStore addKeyStore = new AddKeyStore.Builder(KEY_STORE_NAME)
                    .type(KEY_STORE_TYPE)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_STORE_PASSWORD)
                            .build())
                    .build();
            client.apply(addKeyStore);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removeKeyStores() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            Administration administration = new Administration(client);
            ops.removeIfExists(KEY_STORE_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(FILTERING_KEY_STORE_ADDRESS);
        ops.removeIfExists(FILTERING_KEY_STORE_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleFilteringKeyStore() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();
        assertFalse("The filtering key store should not exist", ops.exists(FILTERING_KEY_STORE_ADDRESS));
        client.apply(addFilteringKeyStore);
        assertTrue("Filtering key store should be created", ops.exists(FILTERING_KEY_STORE_ADDRESS));
    }

    @Test
    public void addTwoSimpleFilteringKeyStores() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();
        AddFilteringKeyStore addFilteringKeyStore2 = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME2)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();

        assertFalse("The filtering key store should not exist", ops.exists(FILTERING_KEY_STORE_ADDRESS));
        assertFalse("The filtering key store should not exist", ops.exists(FILTERING_KEY_STORE_ADDRESS2));

        client.apply(addFilteringKeyStore);
        client.apply(addFilteringKeyStore2);

        assertTrue("Filtering key store should be created", ops.exists(FILTERING_KEY_STORE_ADDRESS));
        assertTrue("Filtering key store should be created", ops.exists(FILTERING_KEY_STORE_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicatefilteringKeyStoreNotAllowed() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();
        AddFilteringKeyStore addFilteringKeyStore2 = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();

        client.apply(addFilteringKeyStore);
        assertTrue("The filtering key store should be created", ops.exists(FILTERING_KEY_STORE_ADDRESS));

        client.apply(addFilteringKeyStore2);
        fail("Filtering key store is already configured, exception should be thrown");
    }

    @Test
    public void addDuplicateFilteringKeyStoreAllowed() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();
        AddFilteringKeyStore addFilteringKeyStore2 = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER2)
                .replaceExisting()
                .build();
        client.apply(addFilteringKeyStore);
        assertTrue("The filtering key store should be created", ops.exists(FILTERING_KEY_STORE_ADDRESS));

        client.apply(addFilteringKeyStore2);
        assertTrue("The filtering key store should be created", ops.exists(FILTERING_KEY_STORE_ADDRESS));
        checkAttribute(FILTERING_KEY_STORE_ADDRESS, "alias-filter", ALIAS_FILTER2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilteringKeyStore_nullName() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(null)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();
        fail("Creating command with null filtering keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_emptyName() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder("")
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(ALIAS_FILTER)
                .build();
        fail("Creating command with empty filtering keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilteringKeyStore_nullKeyStore() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(null)
                .aliasFilter(ALIAS_FILTER)
                .build();
        fail("Creating command with null filtering keystore keystore should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_emptyKeyStore() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore("")
                .aliasFilter(ALIAS_FILTER)
                .build();
        fail("Creating command with empty filtering keystore keystore should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilteringKeyStore_nullFilterAlias() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter(null)
                .build();
        fail("Creating command with null filtering keystore filter alias should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_emptyFilterAlias() throws Exception {
        AddFilteringKeyStore addFilteringKeyStore = new AddFilteringKeyStore.Builder(FILTERING_KEY_STORE_NAME)
                .keyStore(KEY_STORE_NAME)
                .aliasFilter("")
                .build();
        fail("Creating command with empty filtering keystore filter alias should throw exception");
    }

}
