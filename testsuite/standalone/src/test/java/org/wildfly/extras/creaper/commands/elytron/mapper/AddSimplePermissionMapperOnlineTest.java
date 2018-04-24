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
public class AddSimplePermissionMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SIMPLE_PERMISSION_MAPPER_NAME = "CreaperTestSimplePermissionMapper";
    private static final Address TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-permission-mapper", TEST_SIMPLE_PERMISSION_MAPPER_NAME);
    private static final String TEST_SIMPLE_PERMISSION_MAPPER_NAME2 = "CreaperTestSimplePermissionMapper2";
    private static final Address TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("simple-permission-mapper", TEST_SIMPLE_PERMISSION_MAPPER_NAME2);

    private static final String PREDEFINED_PERMISSION_SET1 = "default-permissions";
    private static final String PREDEFINED_PERMISSION_SET2 = "login-permission";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimplePermissionMapper() throws Exception {
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        client.apply(addSimplePermissionMapper);

        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));
    }

    @Test
    public void addTwoSimplePermissionMappers() throws Exception {
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        AddSimplePermissionMapper addSimplePermissionMapper2
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME2)
                .build();

        client.apply(addSimplePermissionMapper);
        client.apply(addSimplePermissionMapper2);

        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));
        assertTrue("Second Simple permission mapper should be created",
                ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS2));
    }

    @Test
    public void addFullSimplePermissionMapper() throws Exception {
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .mappingMode(AddSimplePermissionMapper.MappingMode.XOR)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .action("login")
                                .targetName("loginPermissionName")
                                .build(),
                                new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.RunAsPrincipalPermission")
                                .action("read")
                                .targetName("runAsPrincipalPermissionName")
                                .build())
                        .addRoles("SomeRoles1")
                        .addPrincipals("SomePrincipal1")
                        .build(),
                        new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                                .action("write")
                                .targetName("changeRoleMapperPermissionName")
                                .build())
                        .addRoles("SomeRoles2", "SomeRoles3")
                        .addPrincipals("SomePrincipal2", "SomePrincipal3")
                        .build())
                .build();

        client.apply(addSimplePermissionMapper);

        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));

        checkSimplePermissionMapperAttribute("mapping-mode", "xor");

        checkSimplePermissionMapperAttribute("permission-mappings[0].roles[0]", "SomeRoles1");
        checkSimplePermissionMapperAttribute("permission-mappings[0].principals[0]", "SomePrincipal1");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[0].class-name",
                "org.wildfly.security.auth.permission.LoginPermission");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[0].action", "login");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[0].target-name", "loginPermissionName");

        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[1].class-name",
                "org.wildfly.security.auth.permission.RunAsPrincipalPermission");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[1].action", "read");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[1].target-name",
                "runAsPrincipalPermissionName");

        checkSimplePermissionMapperAttribute("permission-mappings[1].roles[0]", "SomeRoles2");
        checkSimplePermissionMapperAttribute("permission-mappings[1].roles[1]", "SomeRoles3");
        checkSimplePermissionMapperAttribute("permission-mappings[1].principals[0]", "SomePrincipal2");
        checkSimplePermissionMapperAttribute("permission-mappings[1].principals[1]", "SomePrincipal3");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permissions[0].class-name",
                "org.wildfly.security.auth.permission.ChangeRoleMapperPermission");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permissions[0].action", "write");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permissions[0].target-name",
                "changeRoleMapperPermissionName");
    }

    @Test
    public void addFullSimplePermissionMapper_matchAll() throws Exception {
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .mappingMode(AddSimplePermissionMapper.MappingMode.OR)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .build())
                        .matchAll(true)
                        .build(),
                        new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                                .action("write")
                                .targetName("changeRoleMapperPermissionName")
                                .build())
                        .matchAll(false)
                        .build())
                .build();

        client.apply(addSimplePermissionMapper);

        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));

        checkSimplePermissionMapperAttribute("mapping-mode", "or");

        checkSimplePermissionMapperAttribute("permission-mappings[0].match-all", "true");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[0].class-name",
                "org.wildfly.security.auth.permission.LoginPermission");

        checkSimplePermissionMapperAttributeIsUndefined("permission-mappings[1].match-all");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permissions[0].class-name",
                "org.wildfly.security.auth.permission.ChangeRoleMapperPermission");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permissions[0].action", "write");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permissions[0].target-name",
                "changeRoleMapperPermissionName");
    }

    @Test
    public void addFullSimplePermissionMapper_permissionSets() throws Exception {
        Assume.assumeTrue("permission-set is available since WildFly 13.",
                client.version().greaterThanOrEqualTo(ServerVersion.VERSION_7_0_0));
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .mappingMode(AddSimplePermissionMapper.MappingMode.OR)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissionSets(PREDEFINED_PERMISSION_SET1, PREDEFINED_PERMISSION_SET2)
                        .matchAll(true)
                        .build(),
                        new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissionSets(PREDEFINED_PERMISSION_SET1)
                        .matchAll(false)
                        .build())
                .build();

        client.apply(addSimplePermissionMapper);

        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));

        checkSimplePermissionMapperAttribute("mapping-mode", "or");

        checkSimplePermissionMapperAttribute("permission-mappings[0].match-all", "true");
        checkSimplePermissionMapperAttribute("permission-mappings[0].permission-sets[0].permission-set",
                PREDEFINED_PERMISSION_SET1);
        checkSimplePermissionMapperAttribute("permission-mappings[0].permission-sets[1].permission-set",
                PREDEFINED_PERMISSION_SET2);

        checkSimplePermissionMapperAttributeIsUndefined("permission-mappings[1].match-all");
        checkSimplePermissionMapperAttribute("permission-mappings[1].permission-sets[0].permission-set",
                PREDEFINED_PERMISSION_SET1);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSimplePermissionMapperNotAllowed() throws Exception {
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .build())
                        .build())
                .build();

        AddSimplePermissionMapper addSimplePermissionMapper2
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                                .targetName("someName")
                                .build())
                        .build())
                .build();

        client.apply(addSimplePermissionMapper);
        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));
        client.apply(addSimplePermissionMapper2);
        fail("Simple permission mapper CreaperTestSimplePermissionMapper already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistSimplePermissionMapperAllowed() throws Exception {
        AddSimplePermissionMapper addSimplePermissionMapper
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .build())
                        .build())
                .build();

        AddSimplePermissionMapper addSimplePermissionMapper2
                = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.ChangeRoleMapperPermission")
                                .targetName("someName")
                                .build())
                        .build())
                .replaceExisting()
                .build();

        client.apply(addSimplePermissionMapper);
        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));
        client.apply(addSimplePermissionMapper2);
        assertTrue("Simple permission mapper should be created", ops.exists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS));
        // check whether it was really rewritten
        checkSimplePermissionMapperAttribute("permission-mappings[0].permissions[0].class-name",
                "org.wildfly.security.auth.permission.ChangeRoleMapperPermission");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_nullName() throws Exception {
        new AddSimplePermissionMapper.Builder(null)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_emptyName() throws Exception {
        new AddSimplePermissionMapper.Builder("")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_nullClassName() throws Exception {
        new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className(null)
                                .build())
                        .build())
                .build();
        fail("Creating command with null class-name for permission should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_emptyClassName() throws Exception {
        new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("")
                                .build())
                        .build())
                .build();
        fail("Creating command with empty class-name for permission should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_principalsMatchAll() throws Exception {
        new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .build())
                        .addPrincipals("SomePrincipal1")
                        .matchAll(true)
                        .build())
                .build();
        fail("Creating command with both principals and match-all should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_rolesMatchAll() throws Exception {
        new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .build())
                        .addRoles("SomeRole1")
                        .matchAll(true)
                        .build())
                .build();
        fail("Creating command with both roles and match-all should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimplePermissionMapper_permissionsPermissionSet() throws Exception {
        new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                        .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                                .className("org.wildfly.security.auth.permission.LoginPermission")
                                .build())
                        .addPermissionSets(PREDEFINED_PERMISSION_SET1)
                        .build())
                .build();
        fail("Creating command with both permissions and permission-sets should throw exception");
    }

    private void checkSimplePermissionMapperAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

    private void checkSimplePermissionMapperAttributeIsUndefined(String attribute) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        readAttribute.assertNotDefinedValue("match-all should not have defined value");
    }

}
