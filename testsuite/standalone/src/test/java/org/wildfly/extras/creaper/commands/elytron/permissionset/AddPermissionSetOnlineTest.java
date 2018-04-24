package org.wildfly.extras.creaper.commands.elytron.permissionset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddPermissionSetOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_PERMISSION_SET_NAME = "CreaperTestPermissionSet";
    private static final Address TEST_PERMISSION_SET_ADDRESS = SUBSYSTEM_ADDRESS
            .and("permission-set", TEST_PERMISSION_SET_NAME);
    private static final String TEST_PERMISSION_SET_NAME2 = "CreaperTestPermissionSet2";
    private static final Address TEST_PERMISSION_SET_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("permission-set", TEST_PERMISSION_SET_NAME2);

    private static final String ELYTRON_MODULE = "org.wildfly.security.elytron";

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeTrue("Permission set is available since WildFly 13.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_7_0_0));
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_PERMISSION_SET_ADDRESS);
        ops.removeIfExists(TEST_PERMISSION_SET_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addPermissionSet() throws Exception {
        AddPermissionSet addPermissionSet
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .build();

        client.apply(addPermissionSet);

        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));
    }

    @Test
    public void addTwoPermissionSets() throws Exception {
        AddPermissionSet addPermissionSet
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .build();

        AddPermissionSet addPermissionSet2
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME2)
                .build();

        client.apply(addPermissionSet);
        client.apply(addPermissionSet2);

        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));
        assertTrue("Second Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS2));
    }

    @Test
    public void addFullPermissionSet() throws Exception {
        AddPermissionSet addPermissionSet
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .action("login")
                        .targetName("loginPermissionName")
                        .build(),
                        new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.RunAsPrincipalPermission")
                        .action("read")
                        .targetName("runAsPrincipalPermissionName")
                        .build())
                .build();

        client.apply(addPermissionSet);

        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));

        checkPermissionSetAttribute("permissions[0].class-name",
                "org.wildfly.security.auth.permission.LoginPermission");
        checkPermissionSetAttribute("permissions[0].action", "login");
        checkPermissionSetAttribute("permissions[0].target-name", "loginPermissionName");

        checkPermissionSetAttribute("permissions[1].class-name",
                "org.wildfly.security.auth.permission.RunAsPrincipalPermission");
        checkPermissionSetAttribute("permissions[1].action", "read");
        checkPermissionSetAttribute("permissions[1].target-name", "runAsPrincipalPermissionName");
    }

    @Test
    public void addFullPermissionSet_samePermission() throws Exception {
        AddPermissionSet addPermissionSet
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .action("login")
                        .targetName("loginPermissionName")
                        .module(ELYTRON_MODULE)
                        .build(),
                        new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .action("login")
                        .targetName("loginPermissionName")
                        .module(ELYTRON_MODULE)
                        .build())
                .build();

        client.apply(addPermissionSet);

        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));

        checkPermissionSetAttribute("permissions[0].class-name",
                "org.wildfly.security.auth.permission.LoginPermission");
        checkPermissionSetAttribute("permissions[0].action", "login");
        checkPermissionSetAttribute("permissions[0].target-name", "loginPermissionName");
        checkPermissionSetAttribute("permissions[0].module", ELYTRON_MODULE);

        checkPermissionSetAttribute("permissions[1].class-name",
                "org.wildfly.security.auth.permission.LoginPermission");
        checkPermissionSetAttribute("permissions[1].action", "login");
        checkPermissionSetAttribute("permissions[1].target-name", "loginPermissionName");
        checkPermissionSetAttribute("permissions[1].module", ELYTRON_MODULE);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistPermissionSetNotAllowed() throws Exception {
        AddPermissionSet addPermissionSet
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();
        AddPermissionSet addPermissionSet2
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                        .targetName("someName")
                        .build())
                .build();

        client.apply(addPermissionSet);
        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));
        client.apply(addPermissionSet2);
        fail("Permission set CreaperTestPermissionSet already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistPermissionSetAllowed() throws Exception {
        AddPermissionSet addPermissionSet
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();
        AddPermissionSet addPermissionSet2
                = new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                        .targetName("someName")
                        .build())
                .replaceExisting()
                .build();

        client.apply(addPermissionSet);
        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));
        client.apply(addPermissionSet2);
        assertTrue("Permission set should be created", ops.exists(TEST_PERMISSION_SET_ADDRESS));
        // check whether it was really rewritten
        checkPermissionSetAttribute("permissions[0].class-name",
                "org.wildfly.security.auth.permission.ChangeRoleMapperPermission");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPermissionSet_nullName() throws Exception {
        new AddPermissionSet.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPermissionSet_emptyName() throws Exception {
        new AddPermissionSet.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPermissionSet_nullClassName() throws Exception {
        new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className(null)
                        .build())
                .build();
        fail("Creating command with null class-name for permission should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPermissionSet_emptyClassName() throws Exception {
        new AddPermissionSet.Builder(TEST_PERMISSION_SET_NAME)
                .addPermissions(new AddPermissionSet.PermissionBuilder()
                        .className("")
                        .build())
                .build();
        fail("Creating command with empty class-name for permission should throw exception");
    }

    private void checkPermissionSetAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_PERMISSION_SET_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());

    }
}
