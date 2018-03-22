package org.wildfly.extras.creaper.commands.elytron.realm;

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
import org.wildfly.extras.creaper.core.online.operations.OperationException;

@RunWith(Arquillian.class)
public class AddAggregateRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AGGREGATE_REALM_NAME = "CreaperTestAggregateRealm";
    private static final Address TEST_AGGREGATE_REALM_ADDRESS = SUBSYSTEM_ADDRESS.and("aggregate-realm",
        TEST_AGGREGATE_REALM_NAME);
    private static final String TEST_AGGREGATE_REALM_NAME2 = "CreaperTestAggregateRealm2";
    private static final Address TEST_AGGREGATE_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS.and("aggregate-realm",
        TEST_AGGREGATE_REALM_NAME2);

    private static final String FILESYSTEM_REALM_TYPE = "filesystem-realm";
    private static final String TEST_DEFAULT_FILESYSTEM_REALM_NAME = "CreaperTestFilesystemRealm";

    public AddAggregateRealmOnlineTest() {
        super();
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AGGREGATE_REALM_ADDRESS);
        ops.removeIfExists(TEST_AGGREGATE_REALM_ADDRESS2);

        removeAllFilesystemRealms();

        administration.reloadIfRequired();
    }

    @Test
    public void addAggregateRealm() throws Exception {
        addDefaultFilesystemRealm();

        AddAggregateRealm addAggregateRealm = new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME)
            .authorizationRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME)
            .build();

        client.apply(addAggregateRealm);

        assertTrue("Aggregate realm should be created", ops.exists(TEST_AGGREGATE_REALM_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregateRealm_nonAuthenticationRealm() throws Exception {
        addDefaultFilesystemRealm();

        AddAggregateRealm addAggregateRealm = new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm("nonExistRealm").authorizationRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME).build();

        client.apply(addAggregateRealm);

        fail("Creating command with non existing realms should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregateRealm_nonAuthorizationRealm() throws Exception {
        addDefaultFilesystemRealm();

        AddAggregateRealm addAggregateRealm = new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME)
            .authorizationRealm("nonExistRealm")
            .build();

        client.apply(addAggregateRealm);

        fail("Creating command with non existing realms should throw exception");
    }

    @Test
    public void addAggregateRealms() throws Exception {
        addFilesystemRealm("filesystemRealm1");
        addFilesystemRealm("filesystemRealm2");

        AddAggregateRealm addAggregateRealm = new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm("filesystemRealm1")
            .authorizationRealm("filesystemRealm2")
            .build();

        AddAggregateRealm addAggregateRealm2 = new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME2)
            .authenticationRealm("filesystemRealm1")
            .authorizationRealm("filesystemRealm2")
            .build();

        client.apply(addAggregateRealm);
        client.apply(addAggregateRealm2);

        assertTrue("Aggregate realm should be created", ops.exists(TEST_AGGREGATE_REALM_ADDRESS));
        assertTrue("Second aggregate realm should be created", ops.exists(TEST_AGGREGATE_REALM_ADDRESS2));

        checkAttribute(TEST_AGGREGATE_REALM_ADDRESS, "authentication-realm", "filesystemRealm1");
        checkAttribute(TEST_AGGREGATE_REALM_ADDRESS2, "authorization-realm", "filesystemRealm2");

        administration.reload();

        checkAttribute(TEST_AGGREGATE_REALM_ADDRESS, "authentication-realm", "filesystemRealm1");
        checkAttribute(TEST_AGGREGATE_REALM_ADDRESS2, "authorization-realm", "filesystemRealm2");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateAggregateRealmNotAllowed() throws Exception {
        addFilesystemRealm("filesystemRealm1");

        AddAggregateRealm addAggregateRealm = new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm("filesystemRealm1").authorizationRealm("filesystemRealm1").build();

        client.apply(addAggregateRealm);
        assertTrue("Aggregate realm should be created", ops.exists(TEST_AGGREGATE_REALM_ADDRESS));
        client.apply(addAggregateRealm);
        fail("Aggregate realm " + TEST_AGGREGATE_REALM_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRealm_nullName() throws Exception {
        new AddAggregateRealm.Builder(null);
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRealm_emptyName() throws Exception {
        new AddAggregateRealm.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRealm_noRealms() throws Exception {
        new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME).build();
        fail("Creating command with no realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRealm_noAuthenticationRealm() throws Exception {
        addFilesystemRealm("filesystemRealm1");

        new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm(null)
            .authorizationRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME)
            .build();
        fail("Creating command with no realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRealm_noAuthorizationRealm() throws Exception {
        addFilesystemRealm("filesystemRealm1");

        new AddAggregateRealm.Builder(TEST_AGGREGATE_REALM_NAME)
            .authenticationRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME)
            .authorizationRealm(null)
            .build();
        fail("Creating command with no realm should throw exception");
    }

    private void addDefaultFilesystemRealm() throws CommandFailedException {
        addFilesystemRealm(TEST_DEFAULT_FILESYSTEM_REALM_NAME);
    }

    private void addFilesystemRealm(String realmName) throws CommandFailedException {
        AddFilesystemRealm addAddPrefixRealm = new AddFilesystemRealm.Builder(realmName)
            .path("/path/to/filesystem").build();
        client.apply(addAddPrefixRealm);
    }

    private void removeAllFilesystemRealms() throws IOException, OperationException {
        removeAllElytronChildrenType(FILESYSTEM_REALM_TYPE);
    }
}
