package org.wildfly.extras.creaper.commands.elytron.credentialstore;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.CredentialRef;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class RemoveCredentialStoreOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CREDENTIAL_STORE_NAME = "CreaperTestCredentialStore";
    private static final Address TEST_CREDENTIAL_STORE_ADDRESS = SUBSYSTEM_ADDRESS
            .and("credential-store", TEST_CREDENTIAL_STORE_NAME);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CREDENTIAL_STORE_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void removeCredentialStore() throws Exception {
        AddCredentialStore addCredentialStore = new AddCredentialStore.Builder(TEST_CREDENTIAL_STORE_NAME)
                .create(true)
                .credentialReference(new CredentialRef.CredentialRefBuilder()
                        .clearText("somePassword")
                        .build())
                .build();

        client.apply(addCredentialStore);
        assertTrue("Credential store should be created", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));

        RemoveCredentialStore removeCredentialStore = new RemoveCredentialStore(TEST_CREDENTIAL_STORE_NAME);

        client.apply(removeCredentialStore);
        assertFalse("Credential store should be removed", ops.exists(TEST_CREDENTIAL_STORE_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void removeNonExistingCredentialStore() throws Exception {
        RemoveCredentialStore removeCredentialStore = new RemoveCredentialStore(TEST_CREDENTIAL_STORE_NAME);
        client.apply(removeCredentialStore);

        fail("Specified credential store does not exist in configuration, an exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeCredentialStore_nullName() throws Exception {
        new RemoveCredentialStore(null);
        fail("Creating command with null credential store name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeCredentialStore_emptyName() throws Exception {
        new RemoveCredentialStore("");
        fail("Creating command with empty credential store name should throw exception");
    }
}
