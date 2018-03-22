package org.wildfly.extras.creaper.commands.elytron.realm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddFilesystemRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_FILESYSTEM_REALM_NAME = "CreaperTestFilesystemRealm";
    private static final Address TEST_FILESYSTEM_REALM_ADDRESS = SUBSYSTEM_ADDRESS
            .and("filesystem-realm", TEST_FILESYSTEM_REALM_NAME);
    private static final String TEST_FILESYSTEM_REALM_NAME2 = "CreaperTestFilesystemRealm2";
    private static final Address TEST_FILESYSTEM_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("filesystem-realm", TEST_FILESYSTEM_REALM_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS);
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleFilesystemRealm() throws Exception {
        AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("/path/to/filesystem")
                .build();

        client.apply(addFilesystemRealm);

        assertTrue("Filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS));
    }

    @Test
    public void addTwoSimpleFilesystemRealms() throws Exception {
        AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("/path/to/filesystem")
                .build();

        AddFilesystemRealm addFilesystemRealm2 = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME2)
                .path("/path/to/filesystem2")
                .build();

        client.apply(addFilesystemRealm);
        client.apply(addFilesystemRealm2);

        assertTrue("Filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS));
        assertTrue("Second filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS2));
    }

    @Test
    public void addFullFilesystemRealm() throws Exception {
        AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("filesystem")
                .relativeTo("jboss.server.config.dir")
                .levels(5)
                .encoded(false)
                .build();

        client.apply(addFilesystemRealm);

        assertTrue("Filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS));

        checkAttribute(TEST_FILESYSTEM_REALM_ADDRESS, "path", "filesystem");
        checkAttribute(TEST_FILESYSTEM_REALM_ADDRESS, "relative-to", "jboss.server.config.dir");
        checkAttribute(TEST_FILESYSTEM_REALM_ADDRESS, "levels", "5");
        checkAttribute(TEST_FILESYSTEM_REALM_ADDRESS, "encoded", "false");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistFilesystemRealmNotAllowed() throws Exception {
        AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("/path/to/filesystem")
                .build();

        AddFilesystemRealm addFilesystemRealm2 = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("/path/to/second/filesystem")
                .build();

        client.apply(addFilesystemRealm);
        assertTrue("Filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS));
        client.apply(addFilesystemRealm2);
        fail("Filesystem realm CreaperTestFilesystemRealm already exists in configuration, exception should be thrown");

    }

    @Test
    public void addExistFilesystemRealmAllowed() throws Exception {
        AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("/path/to/filesystem")
                .build();

        AddFilesystemRealm addFilesystemRealm2 = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("/path/to/second/filesystem")
                .replaceExisting()
                .build();

        client.apply(addFilesystemRealm);
        assertTrue("Filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS));
        client.apply(addFilesystemRealm2);
        assertTrue("Filesystem realm should be created", ops.exists(TEST_FILESYSTEM_REALM_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_FILESYSTEM_REALM_ADDRESS, "path", "/path/to/second/filesystem");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilesystemRealm_nullName() throws Exception {
        new AddFilesystemRealm.Builder(null)
                .path("/path/to/filesystem")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilesystemRealm_emptyName() throws Exception {
        new AddFilesystemRealm.Builder("")
                .path("/path/to/filesystem")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilesystemRealm_nullPath() throws Exception {
        new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addFilesystemRealm_emptyPath() throws Exception {
        new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
                .path("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

}
