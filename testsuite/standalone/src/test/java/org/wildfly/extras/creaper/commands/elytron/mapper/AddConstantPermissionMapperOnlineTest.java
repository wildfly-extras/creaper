package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddConstantPermissionMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CONSTANT_PERMISSION_MAPPER_NAME = "CreaperTestConstantPermissionMapper";
    private static final Address TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-permission-mapper", TEST_CONSTANT_PERMISSION_MAPPER_NAME);
    private static final String TEST_CONSTANT_PERMISSION_MAPPER_NAME2 = "CreaperTestConstantPermissionMapper2";
    private static final Address TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-permission-mapper", TEST_CONSTANT_PERMISSION_MAPPER_NAME2);

    private static final String PREDEFINED_PERMISSION_SET1 = "default-permissions";
    private static final String PREDEFINED_PERMISSION_SET2 = "login-permission";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleConstantPermissionMapper() throws Exception {
        AddConstantPermissionMapper addConstantPermissionMapper
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();

        client.apply(addConstantPermissionMapper);

        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));
    }

    @Test
    public void addTwoConstantPermissionMappers() throws Exception {
        AddConstantPermissionMapper addConstantPermissionMapper
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();

        AddConstantPermissionMapper addConstantPermissionMapper2
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME2)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();

        client.apply(addConstantPermissionMapper);
        client.apply(addConstantPermissionMapper2);

        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));
        assertTrue("Second constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS2));
    }

    @Test
    public void addFullConstantPermissionMapper() throws Exception {
        AddConstantPermissionMapper addConstantPermissionMapper
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .action("login")
                        .targetName("loginPermissionName")
                        .build(),
                        new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.RunAsPrincipalPermission")
                        .action("read")
                        .targetName("runAsPrincipalPermissionName")
                        .build())
                .build();

        client.apply(addConstantPermissionMapper);
        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));

        checkConstantPermissionMapperAttribute("permissions[0].class-name",
                "org.wildfly.security.auth.permission.LoginPermission");
        checkConstantPermissionMapperAttribute("permissions[0].action", "login");
        checkConstantPermissionMapperAttribute("permissions[0].target-name", "loginPermissionName");

        checkConstantPermissionMapperAttribute("permissions[1].class-name",
                "org.wildfly.security.auth.permission.RunAsPrincipalPermission");
        checkConstantPermissionMapperAttribute("permissions[1].action", "read");
        checkConstantPermissionMapperAttribute("permissions[1].target-name", "runAsPrincipalPermissionName");
    }

    @Test
    public void addFullConstantPermissionMapper_permissionSets() throws Exception {
        Assume.assumeTrue("permission-set is available since WildFly 13.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_7_0_0));

        AddConstantPermissionMapper addConstantPermissionMapper
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissionSets(PREDEFINED_PERMISSION_SET1, PREDEFINED_PERMISSION_SET2)
                .build();

        client.apply(addConstantPermissionMapper);
        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));

        checkConstantPermissionMapperAttribute("permission-sets[0].permission-set", PREDEFINED_PERMISSION_SET1);
        checkConstantPermissionMapperAttribute("permission-sets[1].permission-set", PREDEFINED_PERMISSION_SET2);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConstantPermissionMapperNotAllowed() throws Exception {
        AddConstantPermissionMapper addConstantPermissionMapper
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();

        AddConstantPermissionMapper addConstantPermissionMapper2
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                        .targetName("someName")
                        .build())
                .build();

        client.apply(addConstantPermissionMapper);
        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));
        client.apply(addConstantPermissionMapper2);
        fail("Constant permission mapper CreaperTestConstantPermissionMapper already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConstantPermissionMapperAllowed() throws Exception {
        AddConstantPermissionMapper addConstantPermissionMapper
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();

        AddConstantPermissionMapper addConstantPermissionMapper2
                = new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                        .targetName("someName")
                        .build())
                .replaceExisting()
                .build();

        client.apply(addConstantPermissionMapper);
        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));
        client.apply(addConstantPermissionMapper2);
        assertTrue("Constant permission mapper should be created",
                ops.exists(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS));
        // check whether it was really rewritten
        checkConstantPermissionMapperAttribute("permissions[0].class-name",
                "org.wildfly.security.auth.permission.ChangeRoleMapperPermission");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPermissionMapper_nullName() throws Exception {
        new AddConstantPermissionMapper.Builder(null)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPermissionMapper_emptyName() throws Exception {
        new AddConstantPermissionMapper.Builder("")
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPermissionMapper_nullClassName() throws Exception {
        new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className(null)
                        .build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPermissionMapper_emptyClassName() throws Exception {
        new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("")
                        .build())
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantPermissionMapper_permissionsPermissionSet() throws Exception {
        new AddConstantPermissionMapper.Builder(TEST_CONSTANT_PERMISSION_MAPPER_NAME)
                .addPermissions(new AddConstantPermissionMapper.PermissionBuilder()
                        .className("org.wildfly.security.auth.permission.LoginPermission")
                        .build())
                .addPermissionSets(PREDEFINED_PERMISSION_SET1)
                .build();
        fail("Creating command with both permissions and permission-sets should throw exception");
    }

    private void checkConstantPermissionMapperAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_CONSTANT_PERMISSION_MAPPER_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
