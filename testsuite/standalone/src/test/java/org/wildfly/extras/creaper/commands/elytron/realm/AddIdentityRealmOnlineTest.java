package org.wildfly.extras.creaper.commands.elytron.realm;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddIdentityRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_IDENTITY_REALM_NAME = "CreaperTestIdentityRealm";
    private static final Address TEST_IDENTITY_REALM_ADDRESS = SUBSYSTEM_ADDRESS
            .and("identity-realm", TEST_IDENTITY_REALM_NAME);
    private static final String TEST_IDENTITY_REALM_NAME2 = "CreaperTestIdentityRealm2";
    private static final Address TEST_IDENTITY_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("identity-realm", TEST_IDENTITY_REALM_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_IDENTITY_REALM_ADDRESS);
        ops.removeIfExists(TEST_IDENTITY_REALM_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleIdentityRealm() throws Exception {
        AddIdentityRealm addIdentityRealm = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someIdentity")
                .build();

        client.apply(addIdentityRealm);

        assertTrue("Identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS));
    }

    @Test
    public void addTwoSimpleIdentityRealms() throws Exception {
        AddIdentityRealm addIdentityRealm = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someIdentity")
                .build();

        AddIdentityRealm addIdentityRealm2 = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME2)
                .identity("someOtherIdentity")
                .build();

        client.apply(addIdentityRealm);
        client.apply(addIdentityRealm2);

        assertTrue("Identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS));
        assertTrue("Second identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS2));
    }

    @Test
    public void addFullIdentityRealm() throws Exception {
        AddIdentityRealm addIdentityRealm = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someIdentity")
                .attributeName("someAttribute")
                .attributeValues("someValue", "someOtherValue")
                .build();

        client.apply(addIdentityRealm);

        assertTrue("Identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS));

        checkAttribute(TEST_IDENTITY_REALM_ADDRESS, "identity", "someIdentity");
        checkAttribute(TEST_IDENTITY_REALM_ADDRESS, "attribute-name", "someAttribute");
        checkAttribute(TEST_IDENTITY_REALM_ADDRESS, "attribute-values[0]", "someValue");
        checkAttribute(TEST_IDENTITY_REALM_ADDRESS, "attribute-values[1]", "someOtherValue");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistIdentityRealmNotAllowed() throws Exception {
        AddIdentityRealm addIdentityRealm = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someIdentity")
                .build();

        AddIdentityRealm addIdentityRealm2 = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someOtherIdentity")
                .build();

        client.apply(addIdentityRealm);
        assertTrue("Identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS));
        client.apply(addIdentityRealm2);
        fail("Identity realm CreaperTestIdentityRealm already exists in configuration, exception should be thrown");

    }

    @Test
    public void addExistIdentityRealmAllowed() throws Exception {
        AddIdentityRealm addIdentityRealm = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someIdentity")
                .build();

        AddIdentityRealm addIdentityRealm2 = new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("someOtherIdentity")
                .replaceExisting()
                .build();

        client.apply(addIdentityRealm);
        assertTrue("Identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS));
        client.apply(addIdentityRealm2);
        assertTrue("Identity realm should be created", ops.exists(TEST_IDENTITY_REALM_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_IDENTITY_REALM_ADDRESS, "identity", "someOtherIdentity");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIdentityRealm_nullName() throws Exception {
        new AddIdentityRealm.Builder(null)
                .identity("someIdentity")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIdentityRealm_emptyName() throws Exception {
        new AddIdentityRealm.Builder("")
                .identity("someIdentity")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIdentityRealm_nullIdentity() throws Exception {
        new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity(null)
                .build();
        fail("Creating command with null identity should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addIdentityRealm_emptyIdentity() throws Exception {
        new AddIdentityRealm.Builder(TEST_IDENTITY_REALM_NAME)
                .identity("")
                .build();
        fail("Creating command with empty identity should throw exception");
    }
}
