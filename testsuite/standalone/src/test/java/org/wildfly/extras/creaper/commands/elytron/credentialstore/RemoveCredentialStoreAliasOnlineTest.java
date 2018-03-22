package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineCommand;
import org.wildfly.extras.creaper.core.online.OnlineCommandContext;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.Values;

@RunWith(Arquillian.class)
public class RemoveCredentialStoreAliasOnlineTest extends AbstractCredentialStoreOnlineTest {

    private static final String TEST_CREDENTIAL_STORE_NAME = "CreaperTestCredentialStore";
    private static final Address TEST_CREDENTIAL_STORE_ADDRESS = SUBSYSTEM_ADDRESS.and("credential-store",
        TEST_CREDENTIAL_STORE_NAME);
    private static final String TEST_CREDENTIAL_STORE_ALIAS_NAME = "creapertestcredentialstorealias";

    private static final String PATH = "path";
    private static final String TMP = "tmp";
    private static final Address TEST_PATH_TMP_ADDRESS = Address.root()
            .and(PATH, TMP);

    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();

    @BeforeClass
    public static void createTmpPath() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            AddTmpDirectoryToPath addTargetToPath = new AddTmpDirectoryToPath();
            client.apply(addTargetToPath);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Before
    public void createCredentialStore() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .relativeTo("tmp")
                .build();

        client.apply(addCredentialStore);
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CREDENTIAL_STORE_ADDRESS);
        administration.reloadIfRequired();
    }

    @AfterClass
    public static void removeTmpPath() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations operations = new Operations(client);
            operations.removeIfExists(TEST_PATH_TMP_ADDRESS);
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @Test
    public void removeCredentialStore() throws Exception {
        AddCredentialStoreAlias addCredentialStoreAlias
                = new AddCredentialStoreAlias.Builder(TEST_CREDENTIAL_STORE_ALIAS_NAME)
                .credentialStore(TEST_CREDENTIAL_STORE_NAME)
                .secretValue("someSecretValue")
                .build();
        client.apply(addCredentialStoreAlias);
        assertTrue("Credential store alias should be created",
            aliasExists(TEST_CREDENTIAL_STORE_ADDRESS, TEST_CREDENTIAL_STORE_ALIAS_NAME));

        RemoveCredentialStoreAlias removeCredentialStoreAlias
                = new RemoveCredentialStoreAlias(TEST_CREDENTIAL_STORE_NAME, TEST_CREDENTIAL_STORE_ALIAS_NAME);

        client.apply(removeCredentialStoreAlias);
        assertFalse("Credential store alias should be removed",
            aliasExists(TEST_CREDENTIAL_STORE_ADDRESS, TEST_CREDENTIAL_STORE_ALIAS_NAME));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingCredentialStore() throws Exception {
        RemoveCredentialStoreAlias removeCredentialStoreAlias
                = new RemoveCredentialStoreAlias(TEST_CREDENTIAL_STORE_NAME, TEST_CREDENTIAL_STORE_ALIAS_NAME);
        client.apply(removeCredentialStoreAlias);

        fail("Specified credential store alias does not exist in configuration, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeCredentialStore_nullCredentialStore() throws Exception {
        new RemoveCredentialStoreAlias(null, TEST_CREDENTIAL_STORE_ALIAS_NAME);
        fail("Creating command with null credential store name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeCredentialStore_emptyCredentialStore() throws Exception {
        new RemoveCredentialStoreAlias("", TEST_CREDENTIAL_STORE_ALIAS_NAME);
        fail("Creating command with empty credential store name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeCredentialStore_nullCredentialStoreAlias() throws Exception {
        new RemoveCredentialStoreAlias(TEST_CREDENTIAL_STORE_NAME, null);
        fail("Creating command with null credential store alias name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeCredentialStore_emptyCredentialStoreAlias() throws Exception {
        new RemoveCredentialStoreAlias(TEST_CREDENTIAL_STORE_NAME, "");
        fail("Creating command with empty credential store alias name should throw exception");
    }

    private static final class AddTmpDirectoryToPath implements OnlineCommand {

        @Override
        public void apply(OnlineCommandContext ctx) throws Exception {
            Operations ops = new Operations(ctx.client);
            Address pathAddress = Address.root()
                    .and(PATH, TMP);

            ops.add(pathAddress, Values.empty()
                    .and(PATH, tmpFolder.getRoot().getAbsolutePath()));
        }
    }
}
