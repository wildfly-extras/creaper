package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.net.ssl.KeyManagerFactory;

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
public class AddKeyManagerOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_KEY_STORE_NAME = "CreaperTestKeyStore";
    private static final String TEST_KEY_STORE_NAME2 = "CreaperTestKeyStore2";
    private static final Address TEST_KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store", TEST_KEY_STORE_NAME);
    private static final Address TEST_KEY_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-store", TEST_KEY_STORE_NAME2);
    private static final String TEST_KEY_STORE_TYPE = "JKS";
    private static final String TEST_KEY_STORE_PASSWORD = "password";
    private static final String TEST_KEY_PASSWORD = "password";

    private static final String TEST_KEY_MNGR_NAME = "CreaperTestKeyManager";
    private static final String TEST_KEY_MNGR_NAME2 = "CreaperTestKeyManager2";
    private static final Address TEST_KEY_MNGR_ADDRESS = SUBSYSTEM_ADDRESS.and("key-manager", TEST_KEY_MNGR_NAME);
    private static final Address TEST_KEY_MNGR_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-manager", TEST_KEY_MNGR_NAME2);
    private static final String TEST_KEY_MANAGER_ALGORITHM = KeyManagerFactory.getDefaultAlgorithm();

    @BeforeClass
    public static void addKeyStores() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                    .type(TEST_KEY_STORE_TYPE)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_STORE_PASSWORD)
                            .build())
                    .build();
            AddKeyStore addKeyStore2 = new AddKeyStore.Builder(TEST_KEY_STORE_NAME2)
                    .type(TEST_KEY_STORE_TYPE)
                    .credentialReference(new CredentialRef.CredentialRefBuilder()
                            .clearText(TEST_KEY_STORE_PASSWORD)
                            .build())
                    .build();

            client.apply(addKeyStore);
            client.apply(addKeyStore2);
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
            ops.removeIfExists(TEST_KEY_STORE_ADDRESS);
            ops.removeIfExists(TEST_KEY_STORE_ADDRESS2);
            administration.reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_KEY_MNGR_ADDRESS);
        ops.removeIfExists(TEST_KEY_MNGR_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleKeyManager() throws Exception {
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        assertFalse("The key manager should not exist", ops.exists(TEST_KEY_MNGR_ADDRESS));
        client.apply(addKeyManager);
        assertTrue("Key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS));
    }

    @Test
    public void addTwoSimpleKeyManagers() throws Exception {
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        AddKeyManager addKeyManager2 = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME2)
                .keyStore(TEST_KEY_STORE_NAME2)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();

        assertFalse("The key manager should not exist", ops.exists(TEST_KEY_MNGR_ADDRESS));
        assertFalse("The key manager should not exist", ops.exists(TEST_KEY_MNGR_ADDRESS2));

        client.apply(addKeyManager);
        client.apply(addKeyManager2);

        assertTrue("Key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS));
        assertTrue("Key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateKeyManagersNotAllowed() throws Exception {
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        AddKeyManager addKeyManager2 = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();

        client.apply(addKeyManager);
        assertTrue("The key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS));

        client.apply(addKeyManager2);
        fail("Key manager is already configured, exception should be thrown");
    }

    @Test
    public void addDuplicateKeyManagerAllowed() throws Exception {
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        AddKeyManager addKeyManager2 = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("test-Password")
                        .build())
                .replaceExisting()
                .build();

        client.apply(addKeyManager);
        assertTrue("The key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS));

        client.apply(addKeyManager2);
        assertTrue("The key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS));
        // check whether it was really rewritten
        checkAttributeObject(TEST_KEY_MNGR_ADDRESS, "credential-reference", "clear-text", "test-Password");

    }

    @Test
    public void addFullKeyManager() throws Exception {
        AddKeyManager addKeyManager = new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .algorithm(TEST_KEY_MANAGER_ALGORITHM)
                .aliasFilter("server-alias")
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        client.apply(addKeyManager);
        assertTrue("Key manager should be created", ops.exists(TEST_KEY_MNGR_ADDRESS));

        checkAttribute("algorithm", TEST_KEY_MANAGER_ALGORITHM);
        checkAttribute("alias-filter", "server-alias");
        checkAttribute("key-store", TEST_KEY_STORE_NAME);
        checkAttributeObject(TEST_KEY_MNGR_ADDRESS, "credential-reference", "clear-text", TEST_KEY_PASSWORD);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_nullName() throws Exception {
        new AddKeyManager.Builder(null)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        fail("Creating command with null key manager name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_emptyName() throws Exception {
        new AddKeyManager.Builder("")
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        fail("Creating command with empty key manager name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_nullKeyPassword() throws Exception {
        new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(TEST_KEY_STORE_NAME)
                .credentialReference(null)
                .build();
        fail("Creating command with null key password name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_nullKeyStore() throws Exception {
        new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore(null)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        fail("Creating command with null key password name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_emptyKeyStore() throws Exception {
        new AddKeyManager.Builder(TEST_KEY_MNGR_NAME)
                .keyStore("")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_PASSWORD)
                        .build())
                .build();
        fail("Creating command with null key password name should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_KEY_MNGR_ADDRESS, attribute, expectedValue);
    }

}
