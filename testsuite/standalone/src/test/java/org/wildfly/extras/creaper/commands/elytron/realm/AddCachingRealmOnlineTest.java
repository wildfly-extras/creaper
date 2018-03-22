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
public class AddCachingRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CACHING_REALM_NAME = "CreaperTestCachingRealm";
    private static final Address TEST_CACHING_REALM_ADDRESS = SUBSYSTEM_ADDRESS.and("caching-realm",
        TEST_CACHING_REALM_NAME);
    private static final String TEST_CACHING_REALM_NAME2 = "CreaperTestCachingRealm2";
    private static final Address TEST_CACHING_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS.and("caching-realm",
        TEST_CACHING_REALM_NAME2);

    private static final String FILESYSTEM_REALM_TYPE = "filesystem-realm";
    private static final String TEST_DEFAULT_FILESYSTEM_REALM_NAME = "CreaperTestFilesystemRealm";

    public AddCachingRealmOnlineTest() {
        super();
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CACHING_REALM_ADDRESS);
        ops.removeIfExists(TEST_CACHING_REALM_ADDRESS2);

        removeAllFilesystemRealms();

        administration.reloadIfRequired();
    }

    @Test
    public void addCachingRealm() throws Exception {
        addDefaultFilesystemRealm();

        AddCachingRealm addCachingRealm = new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME)
            .realm(TEST_DEFAULT_FILESYSTEM_REALM_NAME)
            .build();

        client.apply(addCachingRealm);

        assertTrue("Caching realm should be created", ops.exists(TEST_CACHING_REALM_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void addCachingRealm_nonAuthenticationRealm() throws Exception {
        addDefaultFilesystemRealm();

        AddCachingRealm addCachingRealm = new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME)
            .realm("nonExistRealm").build();

        client.apply(addCachingRealm);

        fail("Creating command with non existing realms should throw exception");
    }

    @Test
    public void addCachingRealms() throws Exception {
        addFilesystemRealm("filesystemRealm1");
        addFilesystemRealm("filesystemRealm2");

        AddCachingRealm addCachingRealm = new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME)
            .realm("filesystemRealm1")
            .build();

        AddCachingRealm addCachingRealm2 = new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME2)
            .realm("filesystemRealm1")
            .build();

        client.apply(addCachingRealm);
        client.apply(addCachingRealm2);

        assertTrue("Caching realm should be created", ops.exists(TEST_CACHING_REALM_ADDRESS));
        assertTrue("Second caching realm should be created", ops.exists(TEST_CACHING_REALM_ADDRESS2));

        checkAttribute(TEST_CACHING_REALM_ADDRESS, "realm", "filesystemRealm1");
        checkAttribute(TEST_CACHING_REALM_ADDRESS2, "realm", "filesystemRealm1");

        administration.reload();

        checkAttribute(TEST_CACHING_REALM_ADDRESS, "realm", "filesystemRealm1");
        checkAttribute(TEST_CACHING_REALM_ADDRESS2, "realm", "filesystemRealm1");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateCachingRealmNotAllowed() throws Exception {
        addFilesystemRealm("filesystemRealm1");

        AddCachingRealm addCachingRealm = new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME)
            .realm("filesystemRealm1").build();

        client.apply(addCachingRealm);
        assertTrue("Caching realm should be created", ops.exists(TEST_CACHING_REALM_ADDRESS));
        client.apply(addCachingRealm);
        fail("Caching realm " + TEST_CACHING_REALM_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCachingRealm_nullName() throws Exception {
        new AddCachingRealm.Builder(null);
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCachingRealm_emptyName() throws Exception {
        new AddCachingRealm.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCachingRealm_noRealms() throws Exception {
        new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME).build();
        fail("Creating command with no realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCachingRealm_noAuthenticationRealm() throws Exception {
        addFilesystemRealm("filesystemRealm1");

        new AddCachingRealm.Builder(TEST_CACHING_REALM_NAME)
            .realm(null)
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
