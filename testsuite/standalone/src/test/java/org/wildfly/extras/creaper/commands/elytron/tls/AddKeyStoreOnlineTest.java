package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddKeyStoreOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_KEY_STORE_NAME = "CreaperTestKeyStore";
    private static final String TEST_KEY_STORE_NAME2 = "CreaperTestKeyStore2";
    private static final String TEST_KEY_STORE_TYPE = "JKS";
    private static final String TEST_KEY_STORE_PASSWORD = "password";
    private static final Address TEST_KEY_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("key-store", TEST_KEY_STORE_NAME);
    private static final Address TEST_KEY_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS.and("key-store", TEST_KEY_STORE_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_KEY_STORE_ADDRESS);
        ops.removeIfExists(TEST_KEY_STORE_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleKeyStore() throws Exception {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                .type(TEST_KEY_STORE_TYPE)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_STORE_PASSWORD)
                        .build())
                .build();
        assertFalse("The key store should not exist", ops.exists(TEST_KEY_STORE_ADDRESS));
        client.apply(addKeyStore);
        assertTrue("Key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS));
    }

    @Test
    public void addTwoSimpleKeyStores() throws Exception {
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

        assertFalse("The key store should not exist", ops.exists(TEST_KEY_STORE_ADDRESS));
        assertFalse("The key store should not exist", ops.exists(TEST_KEY_STORE_ADDRESS2));

        client.apply(addKeyStore);
        client.apply(addKeyStore2);

        assertTrue("Key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS));
        assertTrue("Key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistKeyStoreNotAllowed() throws Exception {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                .type(TEST_KEY_STORE_TYPE)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_STORE_PASSWORD)
                        .build())
                .build();
        AddKeyStore addKeyStore2 = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                .type(TEST_KEY_STORE_TYPE)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_STORE_PASSWORD)
                        .build())
                .build();

        client.apply(addKeyStore);
        assertTrue("The key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS));

        client.apply(addKeyStore2);
        fail("Key store is already configured, exception should be thrown");
    }

    @Test
    public void addExistKeyStoreAllowed() throws Exception {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
             .type(TEST_KEY_STORE_TYPE)
             .credentialReference(new CredentialRef.CredentialRefBuilder()
                     .clearText(TEST_KEY_STORE_PASSWORD)
                     .build())
            .build();
        AddKeyStore addKeyStore2 = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
            .type(TEST_KEY_STORE_TYPE)
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText(TEST_KEY_STORE_PASSWORD)
                    .build())
            .aliasFilter("alias")
            .replaceExisting()
            .build();

        client.apply(addKeyStore);
        assertTrue("The key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS));

        client.apply(addKeyStore2);
        assertTrue("The key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_KEY_STORE_ADDRESS, "alias-filter", "alias");
    }

    @Test
    public void addFullKeyStore() throws Exception {
        AddKeyStore addKeyStore = new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
                .type(TEST_KEY_STORE_TYPE)
                .aliasFilter("server-alias")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_KEY_STORE_PASSWORD)
                        .build())
                .build();
        client.apply(addKeyStore);
        assertTrue("Key store should be created", ops.exists(TEST_KEY_STORE_ADDRESS));

        checkAttribute("type", TEST_KEY_STORE_TYPE);
        checkAttributeObject(TEST_KEY_STORE_ADDRESS, "credential-reference", "clear-text", TEST_KEY_STORE_PASSWORD);
        checkAttribute("alias-filter", "server-alias");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_nullName() throws Exception {
        new AddKeyStore.Builder(null)
            .type(TEST_KEY_STORE_TYPE)
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText(TEST_KEY_STORE_PASSWORD)
                    .build())
            .build();
        fail("Creating command with null keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_emptyName() throws Exception {
        new AddKeyStore.Builder("")
            .type(TEST_KEY_STORE_TYPE)
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText(TEST_KEY_STORE_PASSWORD)
                    .build())
            .build();
        fail("Creating command with empty keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_nullType() throws Exception {
        new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
            .type(null)
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText(TEST_KEY_STORE_PASSWORD)
                    .build())
            .build();
        fail("Creating command with null keystore type should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_emptyType() throws Exception {
        new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
            .type("")
            .credentialReference(new CredentialRef.CredentialRefBuilder()
                    .clearText(TEST_KEY_STORE_PASSWORD)
                    .build())
            .build();
        fail("Creating command with empty keystore type should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyStore_nullCredentialRef() throws Exception {
        new AddKeyStore.Builder(TEST_KEY_STORE_NAME)
            .type(TEST_KEY_STORE_TYPE)
            .credentialReference(null)
            .build();
        fail("Creating command with null credential ref should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_KEY_STORE_ADDRESS, attribute, expectedValue);
    }

}
