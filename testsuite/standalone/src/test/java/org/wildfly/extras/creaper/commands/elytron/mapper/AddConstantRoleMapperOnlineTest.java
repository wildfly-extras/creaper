package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddConstantRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CONSTANT_ROLE_MAPPER_NAME = "CreaperTestConstantRoleMapper";
    private static final Address TEST_CONSTANT_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME);
    private static final String TEST_CONSTANT_ROLE_MAPPER_NAME2 = "CreaperTestConstantRoleMapper2";
    private static final Address TEST_CONSTANT_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleConstantRoleMapper() throws Exception {
        AddConstantRoleMapper addConstantRoleMapper = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME)
                .addRoles("AnyRole")
                .build();

        client.apply(addConstantRoleMapper);

        assertTrue("Constant role mapper should be created", ops.exists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS));
    }

    @Test
    public void addTwoConstantRoleMappers() throws Exception {
        AddConstantRoleMapper addConstantRoleMapper = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME)
                .addRoles("AnyRole")
                .build();

        AddConstantRoleMapper addConstantRoleMapper2
                = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME2)
                .addRoles("OtherRole", "Role#3")
                .build();

        client.apply(addConstantRoleMapper);
        client.apply(addConstantRoleMapper2);

        assertTrue("Constant role mapper should be created", ops.exists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS));
        assertTrue("Second constant role mapper should be created", ops.exists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS2));

        assertRoles(TEST_CONSTANT_ROLE_MAPPER_ADDRESS, "AnyRole");
        assertRoles(TEST_CONSTANT_ROLE_MAPPER_ADDRESS2, "OtherRole", "Role#3");

        administration.reload();

        assertRoles(TEST_CONSTANT_ROLE_MAPPER_ADDRESS, "AnyRole");
        assertRoles(TEST_CONSTANT_ROLE_MAPPER_ADDRESS2, "OtherRole", "Role#3");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConstantRoleMapperNotAllowed() throws Exception {
        AddConstantRoleMapper addConstantRoleMapper = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME)
                .addRoles("AnyRole")
                .build();

        client.apply(addConstantRoleMapper);
        assertTrue("Constant role mapper should be created", ops.exists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS));
        client.apply(addConstantRoleMapper);
        fail("Constant role mapper " + TEST_CONSTANT_ROLE_MAPPER_NAME
                + " already exists in configuration, exception should be thrown");
    }

    public void addExistConstantRoleMapperAllowed() throws Exception {
        AddConstantRoleMapper addConstantRoleMapper = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME)
                .addRoles("AnyRole")
                .build();

        AddConstantRoleMapper addConstantRoleMapper2 = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME)
                .addRoles("AnyRole2")
                .replaceExisting()
                .build();

        client.apply(addConstantRoleMapper);
        assertTrue("Constant role mapper should be created", ops.exists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS));
        client.apply(addConstantRoleMapper2);
        assertTrue("Constant role mapper should be created", ops.exists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS));
        // check whether it was really rewritten
        assertRoles(TEST_CONSTANT_ROLE_MAPPER_ADDRESS, "AnyRole2");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantRoleMapper_nullName() throws Exception {
        new AddConstantRoleMapper.Builder(null);
        fail("Creating command with null name should throw exception");
    }


    @Test(expected = IllegalArgumentException.class)
    public void addConstantRoleMapper_emptyName() throws Exception {
        new AddConstantRoleMapper.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantRoleMapper_noRole() throws Exception {
        new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME).build();
        fail("Creating command with no role should throw exception");
    }

    private void assertRoles(Address address, String... expectedRoles) throws Exception {
        List<String> actualRoles = ops.readAttribute(address, "roles").stringListValue();
        assertEquals("Unexpected roles attribute value in " + address, Arrays.asList(expectedRoles), actualRoles);
    }

}
