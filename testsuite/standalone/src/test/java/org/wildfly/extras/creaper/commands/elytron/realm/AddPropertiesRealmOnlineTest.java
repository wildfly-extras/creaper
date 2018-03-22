package org.wildfly.extras.creaper.commands.elytron.realm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddPropertiesRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_PROPERTIES_REALM_NAME = "CreaperTestPropertiesRealm";
    private static final Address TEST_PROPERTIES_REALM_ADDRESS = SUBSYSTEM_ADDRESS
            .and("properties-realm", TEST_PROPERTIES_REALM_NAME);
    private static final String TEST_PROPERTIES_REALM_NAME2 = "CreaperTestPropertiesRealm2";
    private static final Address TEST_PROPERTIES_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("properties-realm", TEST_PROPERTIES_REALM_NAME2);

    @ClassRule
    public static TemporaryFolder tmp = new TemporaryFolder();

    private static File usersProperties;

    @BeforeClass
    public static void createUsersProperties() throws Exception {
        usersProperties = tmp.newFile();
        FileWriter fw = null;
        try {
            fw = new FileWriter(usersProperties);
            fw.write("#$REALM_NAME=" + TEST_PROPERTIES_REALM_NAME + "$");
        } finally {
            if (fw != null) {
                fw.close();
            }
        }
    }

    @AfterClass
    public static void removeUsersProperties() throws Exception {
        if (usersProperties != null) {
            usersProperties.delete();
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_PROPERTIES_REALM_ADDRESS);
        ops.removeIfExists(TEST_PROPERTIES_REALM_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimplePropertiesRealm() throws Exception {
        AddPropertiesRealm addPropertiesRealm = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();

        client.apply(addPropertiesRealm);

        assertTrue("Properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS));

    }

    @Test
    public void addTwoSimplePropertiesRealms() throws Exception {
        AddPropertiesRealm addPropertiesRealm = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();

        AddPropertiesRealm addPropertiesRealm2 = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME2)
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();

        client.apply(addPropertiesRealm);
        client.apply(addPropertiesRealm2);

        assertTrue("Properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS));
        assertTrue("Second properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS2));
    }

    @Test
    public void addFullPropertiesRealm() throws Exception {
        AddPropertiesRealm addPropertiesRealm = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath("mgmt-users.properties")
                .userPropertiesRelativeTo("jboss.server.config.dir")
                .groupsProperiesPath("mgmt-groups.properties")
                .groupsPropertiesRelativeTo("jboss.server.config.dir")
                .plainText(true)
                .digestRealmName("someDigestRealmName")
                .groupsAttribute("myGroup")
                .build();

        client.apply(addPropertiesRealm);

        assertTrue("Properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS));

        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "users-properties.path", "mgmt-users.properties");
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "users-properties.relative-to", "jboss.server.config.dir");
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "users-properties.plain-text", "true");
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "users-properties.digest-realm-name", "someDigestRealmName");
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "groups-properties.path", "mgmt-groups.properties");
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "groups-properties.relative-to", "jboss.server.config.dir");
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "groups-attribute", "myGroup");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistPropertiesRealmNotAllowed() throws Exception {
        AddPropertiesRealm addPropertiesRealm = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();

        AddPropertiesRealm addPropertiesRealm2 = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath("mgmt-users.properties")
                .userPropertiesRelativeTo("jboss.server.config.dir")
                .build();

        client.apply(addPropertiesRealm);
        assertTrue("Properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS));
        client.apply(addPropertiesRealm2);
        fail("Properties realm CreaperTestPropertiesRealm already exists in configuration, exception should be thrown");

    }

    @Test
    public void addExistPropertiesRealmAllowed() throws Exception {
        AddPropertiesRealm addPropertiesRealm = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();

        AddPropertiesRealm addPropertiesRealm2 = new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath("mgmt-users.properties")
                .userPropertiesRelativeTo("jboss.server.config.dir")
                .replaceExisting()
                .build();

        client.apply(addPropertiesRealm);
        assertTrue("Properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS));
        client.apply(addPropertiesRealm2);
        assertTrue("Properties realm should be created", ops.exists(TEST_PROPERTIES_REALM_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_PROPERTIES_REALM_ADDRESS, "users-properties.path", "mgmt-users.properties");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertiesRealm_nullName() throws Exception {
        new AddPropertiesRealm.Builder(null)
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertiesRealm_emptyName() throws Exception {
        new AddPropertiesRealm.Builder("")
                .userProperiesPath(usersProperties.getAbsolutePath())
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertiesRealm_nullUserProperiesPath() throws Exception {
        new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath(null)
                .build();
        fail("Creating command with null user properties path name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertiesRealm_emptyUserProperiesPath() throws Exception {
        new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .userProperiesPath("")
                .build();
        fail("Creating command with empty user properties path name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPropertiesRealm_addGroupsPropertiesRelativeToWithoutGroupsProperiesPath() throws Exception {
        new AddPropertiesRealm.Builder(TEST_PROPERTIES_REALM_NAME)
                .groupsPropertiesRelativeTo("jboss.server.config.dir")
                .build();
        fail("Creating command with defined groups properties relative-to and without groups properties path should throw exception");
    }

}
