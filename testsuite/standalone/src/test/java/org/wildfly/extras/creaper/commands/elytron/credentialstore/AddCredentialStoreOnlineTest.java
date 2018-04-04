package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
public class AddCredentialStoreOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CREDENTIAL_STORE_NAME = "CreaperTestCredentialStore";
    private static final Address TEST_CREDENTIAL_STORE_ADDRESS = SUBSYSTEM_ADDRESS
            .and("credential-store", TEST_CREDENTIAL_STORE_NAME);
    private static final String TEST_CREDENTIAL_STORE_NAME2 = "CreaperTestCredentialStore2";
    private static final Address TEST_CREDENTIAL_STORE_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("credential-store", TEST_CREDENTIAL_STORE_NAME2);
    private static final String PATH_ADDRESS_NAME = "tmp.path";
    private static final Address TMP_PATH_ADDRESS = Address.of("path", PATH_ADDRESS_NAME);

    private static final String TEST_PASSWORD = "somePassword";

    private static String tmpFilePath;

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder();

    @BeforeClass
    public static void createTmpFilePath() throws IOException {
        tmpFilePath = tmp.getRoot().getAbsolutePath();
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CREDENTIAL_STORE_ADDRESS2);
        ops.removeIfExists(TEST_CREDENTIAL_STORE_ADDRESS);
        ops.removeIfExists(TMP_PATH_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void removeCreatedCredentialStoreFiles() throws IOException {
        deleteIfExists(TEST_CREDENTIAL_STORE_NAME);
        deleteIfExists(TEST_CREDENTIAL_STORE_NAME2);
    }

    @Test
    public void addSimpleCredentialStore() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile1")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);

        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
    }

    @Test
    public void addFullCredentialStoreClearText() throws Exception {
        new Operations(client)
                .add(TMP_PATH_ADDRESS, Values.empty()
                        .and("path", tmpFilePath));

        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .providers("elytron")
                .providerName("WildFlyElytron")
                .otherProviders("elytron")
                .relativeTo(PATH_ADDRESS_NAME)
                .type("KeyStoreCredentialStore")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .addImplementationProperties("cryptoAlg", "MD5")
                .location("someLocationFile2")
                .modifiable(true)
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "relative-to", PATH_ADDRESS_NAME);
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "type", "KeyStoreCredentialStore");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "credential-reference.clear-text", TEST_PASSWORD);
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "providers", "elytron");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "provider-name", "WildFlyElytron");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "other-providers", "elytron");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "implementation-properties.cryptoAlg", "MD5");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "location", "someLocationFile2");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "modifiable", "true");
    }

    @Test
    public void addFullCredentialStoreAliasStore() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile3")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStoreAlias addCredentialStoreAlias = new AddCredentialStoreAlias.Builder("alias")
                .credentialStore(TEST_CREDENTIAL_STORE_NAME)
                .secretValue("secret")
                .build();
        client.apply(addCredentialStore, addCredentialStoreAlias);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME2)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile4")
                .type("KeyStoreCredentialStore")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .alias("alias")
                        .store(TEST_CREDENTIAL_STORE_NAME)
                        .build())
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS2));

        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "type", "KeyStoreCredentialStore");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "location", tmpFilePath + File.pathSeparator + "someLocationFile4");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "credential-reference.alias", "alias");
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS2, "credential-reference.store", TEST_CREDENTIAL_STORE_NAME);
    }

    @Test
    public void addTwoCredentialStores() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile5")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStore addCredentialStore2 = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME2)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile6")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);
        client.apply(addCredentialStore2);

        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
        assertTrue("Second credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistCredentialStoreNotAllowed() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile7")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        AddCredentialStore addCredentialStore2 = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile8")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
        client.apply(addCredentialStore2);
        fail("Credential store CreaperTestCredentialStore already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistCredentialStoreAllowed() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile9")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .modifiable(false)
                .build();

        AddCredentialStore addCredentialStore2 = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .location(tmpFilePath + File.pathSeparator + "someLocationFile10")
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .modifiable(true)
                .replaceExisting()
                .build();

        client.apply(addCredentialStore);
        assertTrue("Constant permission mapper should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
        client.apply(addCredentialStore2);
        assertTrue("Constant permission mapper should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        // check whether it was really rewritten
        checkAttribute(TEST_CREDENTIAL_STORE_ADDRESS, "modifiable", "true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_nullName() throws Exception {
        new AddCredentialStore.Builder(null)
                .create(true)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_emptyName() throws Exception {
        new AddCredentialStore.Builder("")
                .create(true)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText(TEST_PASSWORD)
                        .build())
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_nullCredentialReference() throws Exception {
        new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .credentialReference(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCredentialStore_insufficientCredentialReference() throws Exception {
        new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .alias("someAlias")
                        .build())
                .build();
    }

    private static void deleteIfExists(String filename) {
        File file = new File(filename);
        if (file.exists()) {
            file.delete();
        }
    }
}
