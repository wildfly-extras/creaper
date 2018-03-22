package org.wildfly.extras.creaper.commands.elytron.realm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.commands.elytron.tls.AddKeyStore;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;


@RunWith(Arquillian.class)
public class AddKeyStoreRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_KEY_STORE_REALM_NAME = "CreaperTestAddKeyStoreRealm";
    private static final Address TEST_ADD_KEY_STORE_REALM_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store-realm",
        TEST_ADD_KEY_STORE_REALM_NAME);
    private static final String TEST_ADD_KEY_STORE_REALM_NAME2 = "CreaperTestAddKeyStoreRealm2";
    private static final Address TEST_ADD_KEY_STORE_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-store-realm",
        TEST_ADD_KEY_STORE_REALM_NAME2);
    private static final String TEST_DEFAULT_KEYSTORE_NAME = "CreaperTestKeyStore";
    private static final String TEST_KEY_STORE_TYPE = "JKS";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_KEY_STORE_REALM_ADDRESS);
        ops.removeIfExists(TEST_ADD_KEY_STORE_REALM_ADDRESS2);
        removeAllElytronChildrenType("key-store");

        administration.reloadIfRequired();
    }

    @Test
    public void addKeyStoreRealm() throws Exception {
        addDefaultKeyStore();

        AddKeyStoreRealm addAddPrefixRoleMapper = new AddKeyStoreRealm.Builder(TEST_ADD_KEY_STORE_REALM_NAME)
            .keyStore(TEST_DEFAULT_KEYSTORE_NAME)
            .build();

        client.apply(addAddPrefixRoleMapper);

        assertTrue("Add key-store-realm should be created", ops.exists(TEST_ADD_KEY_STORE_REALM_ADDRESS));
    }

    @Test
    public void addKeyStoreRealms() throws Exception {
        addKeyStore("keyStore1");
        addKeyStore("keyStore2");

        AddKeyStoreRealm addKeyStoreRealm1 = new AddKeyStoreRealm.Builder(TEST_ADD_KEY_STORE_REALM_NAME)
            .keyStore("keyStore1")
            .build();

        AddKeyStoreRealm addKeyStoreRealm2 = new AddKeyStoreRealm.Builder(TEST_ADD_KEY_STORE_REALM_NAME2)
            .keyStore("keyStore2")
            .build();

        client.apply(addKeyStoreRealm1);
        client.apply(addKeyStoreRealm2);

        assertTrue("Add key-store-realm should be created", ops.exists(TEST_ADD_KEY_STORE_REALM_ADDRESS));
        assertTrue("Second key-store-realm should be created", ops.exists(TEST_ADD_KEY_STORE_REALM_ADDRESS2));

        checkAttribute(TEST_ADD_KEY_STORE_REALM_ADDRESS, "key-store", "keyStore1");
        checkAttribute(TEST_ADD_KEY_STORE_REALM_ADDRESS2, "key-store", "keyStore2");

        administration.reload();

        checkAttribute(TEST_ADD_KEY_STORE_REALM_ADDRESS, "key-store", "keyStore1");
        checkAttribute(TEST_ADD_KEY_STORE_REALM_ADDRESS2, "key-store", "keyStore2");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateKeyStoreRealmNotAllowed() throws Exception {
        addDefaultKeyStore();

        AddKeyStoreRealm addKeyStoreRealm = new AddKeyStoreRealm.Builder(TEST_ADD_KEY_STORE_REALM_NAME)
            .keyStore(TEST_DEFAULT_KEYSTORE_NAME)
            .build();

        client.apply(addKeyStoreRealm);
        assertTrue("Add key-store-realm should be created", ops.exists(TEST_ADD_KEY_STORE_REALM_ADDRESS));
        client.apply(addKeyStoreRealm);
        fail("Add key-store-realm " + TEST_ADD_KEY_STORE_REALM_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStoreReal_nullName() throws Exception {
        new AddKeyStoreRealm.Builder(null);
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStoreReal_emptyName() throws Exception {
        new AddKeyStoreRealm.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStoreReal_noKeyStore() throws Exception {
        new AddKeyStoreRealm.Builder(TEST_ADD_KEY_STORE_REALM_NAME).build();
        fail("Creating command with no key-store should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStoreReal_emptykeyStore() throws Exception {
        new AddKeyStoreRealm.Builder(TEST_ADD_KEY_STORE_REALM_NAME).keyStore("").build();
        fail("Creating command with empty key-store should throw exception");
    }

    private void addDefaultKeyStore() throws CommandFailedException {
        addKeyStore(TEST_DEFAULT_KEYSTORE_NAME);
    }

    private void addKeyStore(String keyStoreName) throws CommandFailedException {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(keyStoreName)
                .type(TEST_KEY_STORE_TYPE)
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText("test-Password")
                    .build())
                .build();
        client.apply(addKeyStore);
    }
}
