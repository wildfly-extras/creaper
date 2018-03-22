package org.wildfly.extras.creaper.commands.elytron.mapper;

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
public class AddLogicalRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_LOGICAL_ROLE_MAPPER_NAME = "CreaperTestLogicalRoleMapper";
    private static final Address TEST_LOGICAL_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("logical-role-mapper", TEST_LOGICAL_ROLE_MAPPER_NAME);
    private static final String TEST_LOGICAL_ROLE_MAPPER_NAME2 = "CreaperTestLogicalRoleMapper2";
    private static final Address TEST_LOGICAL_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("logical-role-mapper", TEST_LOGICAL_ROLE_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addLogicalRoleMapper() throws Exception {
        AddLogicalRoleMapper addLogicalRoleMapper = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();

        client.apply(addLogicalRoleMapper);

        assertTrue("Logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS));
    }

    @Test
    public void addLogicalRoleMappers() throws Exception {
        AddLogicalRoleMapper addLogicalRoleMapper = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();

        AddLogicalRoleMapper addLogicalRoleMapper2 = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME2)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();

        client.apply(addLogicalRoleMapper);
        client.apply(addLogicalRoleMapper2);

        assertTrue("Logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS));
        assertTrue("Second logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS2));
    }

    @Test
    public void addFullLogicalRoleMapper() throws Exception {
        AddConstantRoleMapper addConstantRoleMapper = new AddConstantRoleMapper.Builder("creaper-contant-role-mapper-1")
                .addRoles("AnyRole1")
                .build();
        AddConstantRoleMapper addConstantRoleMapper2 = new AddConstantRoleMapper.Builder("creaper-contant-role-mapper-2")
                .addRoles("AnyRole2")
                .build();
        client.apply(addConstantRoleMapper);
        client.apply(addConstantRoleMapper2);

        AddLogicalRoleMapper addLogicalRoleMapper = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .left("creaper-contant-role-mapper-1")
                .right("creaper-contant-role-mapper-2")
                .build();

        client.apply(addLogicalRoleMapper);

        assertTrue("Logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS));

        checkAttribute(TEST_LOGICAL_ROLE_MAPPER_ADDRESS, "logical-operation", "or");
        checkAttribute(TEST_LOGICAL_ROLE_MAPPER_ADDRESS, "left", "creaper-contant-role-mapper-1");
        checkAttribute(TEST_LOGICAL_ROLE_MAPPER_ADDRESS, "right", "creaper-contant-role-mapper-2");
    }

    @Test(expected = CommandFailedException.class)
    public void addLogicalRoleMapperNotAllowed() throws Exception {
        AddLogicalRoleMapper addLogicalRoleMapper = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();

        AddLogicalRoleMapper addLogicalRoleMapper2 = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.AND)
                .build();

        client.apply(addLogicalRoleMapper);
        assertTrue("Logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS));
        client.apply(addLogicalRoleMapper2);
        fail("Logical role mapper CreaperTestLogicalRoleMapper already exists in configuration, exception should be thrown");
    }

    @Test
    public void addLogicalRoleMapperAllowed() throws Exception {
        AddLogicalRoleMapper addLogicalRoleMapper = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();

        AddLogicalRoleMapper addLogicalRoleMapper2 = new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.AND)
                .replaceExisting()
                .build();

        client.apply(addLogicalRoleMapper);
        assertTrue("Logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS));
        client.apply(addLogicalRoleMapper2);
        assertTrue("Logical role mapper should be created", ops.exists(TEST_LOGICAL_ROLE_MAPPER_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_LOGICAL_ROLE_MAPPER_ADDRESS, "logical-operation", "and");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalRoleMapper_nullName() throws Exception {
        new AddLogicalRoleMapper.Builder(null)
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalRoleMapper_emptyName() throws Exception {
        new AddLogicalRoleMapper.Builder("")
                .logicalOperation(AddLogicalRoleMapper.LogicalOperation.OR)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalRoleMapper_nullLogicalOperation() throws Exception {
        new AddLogicalRoleMapper.Builder(TEST_LOGICAL_ROLE_MAPPER_NAME)
                .logicalOperation(null)
                .build();
        fail("Creating command with null logical operation should throw exception");
    }

}
