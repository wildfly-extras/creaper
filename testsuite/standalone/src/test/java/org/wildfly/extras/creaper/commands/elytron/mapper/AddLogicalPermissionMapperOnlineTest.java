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
public class AddLogicalPermissionMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_LOGICAL_PERMISSION_MAPPER_NAME = "CreaperTestLogicalPermissionMapper";
    private static final Address TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("logical-permission-mapper", TEST_LOGICAL_PERMISSION_MAPPER_NAME);
    private static final String TEST_LOGICAL_PERMISSION_MAPPER_NAME2 = "CreaperTestLogicalPermissionMapper2";
    private static final Address TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("logical-permission-mapper", TEST_LOGICAL_PERMISSION_MAPPER_NAME2);

    private static final String TEST_SIMPLE_PERMISSION_MAPPER_NAME = "CreaperTestSimplePermissionMapper";
    private static final Address TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-permission-mapper", TEST_SIMPLE_PERMISSION_MAPPER_NAME);
    private final AddSimplePermissionMapper addSimplePermissionMapper
            = new AddSimplePermissionMapper.Builder(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
            .addPermissionMappings(new AddSimplePermissionMapper.PermissionMappingBuilder()
                    .addPermissions(new AddSimplePermissionMapper.PermissionBuilder()
                            .className("org.wildfly.security.auth.permission.LoginPermission")
                            .build())
                    .build())
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS2);
        ops.removeIfExists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addFullLogicalPermissionMapper() throws Exception {
        client.apply(addSimplePermissionMapper);

        AddLogicalPermissionMapper addLogicalPermissionMapper
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.UNLESS)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        client.apply(addLogicalPermissionMapper);

        assertTrue("Logical permission mapper should be created", ops.exists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS));

        checkAttribute(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS, "logical-operation", "unless");
        checkAttribute(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS, "left", TEST_SIMPLE_PERMISSION_MAPPER_NAME);
        checkAttribute(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS, "right", TEST_SIMPLE_PERMISSION_MAPPER_NAME);
    }

    @Test
    public void addTwoLogicalPermissionMappers() throws Exception {
        client.apply(addSimplePermissionMapper);

        AddLogicalPermissionMapper addLogicalPermissionMapper
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        AddLogicalPermissionMapper addLogicalPermissionMapper2
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME2)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.AND)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        client.apply(addLogicalPermissionMapper);
        client.apply(addLogicalPermissionMapper2);

        assertTrue("Logical permission mapper should be created", ops.exists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS));
        assertTrue("Second logical permission mapper should be created",
                ops.exists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistLogicalPermissionMapperNotAllowed() throws Exception {
        client.apply(addSimplePermissionMapper);

        AddLogicalPermissionMapper addLogicalPermissionMapper
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        AddLogicalPermissionMapper addLogicalPermissionMapper2
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.AND)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        client.apply(addLogicalPermissionMapper);
        assertTrue("Logical permission mapper should be created", ops.exists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS));
        client.apply(addLogicalPermissionMapper2);
        fail("Logical permission mapper CreaperTestLogicalPermissionMapper already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistLogicalPermissionMapperAllowed() throws Exception {
        client.apply(addSimplePermissionMapper);

        AddLogicalPermissionMapper addLogicalPermissionMapper
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();

        AddLogicalPermissionMapper addLogicalPermissionMapper2
                = new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.AND)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .replaceExisting()
                .build();

        client.apply(addLogicalPermissionMapper);
        assertTrue("Logical permission mapper should be created", ops.exists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS));
        client.apply(addLogicalPermissionMapper2);
        assertTrue("Logical permission mapper should be created", ops.exists(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_LOGICAL_PERMISSION_MAPPER_ADDRESS, "logical-operation", "and");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalPermissionMapper_nullName() throws Exception {
        new AddLogicalPermissionMapper.Builder(null)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalPermissionMapper_emptyName() throws Exception {
        new AddLogicalPermissionMapper.Builder("")
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalPermissionMapper_nullLogicalOperation() throws Exception {
        new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(null)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();
        fail("Creating command with null logical operation should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalPermissionMapper_nullLeft() throws Exception {
        new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(null)
                .right(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .build();
        fail("Creating command with null left should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLogicalPermissionMapper_nullRight() throws Exception {
        new AddLogicalPermissionMapper.Builder(TEST_LOGICAL_PERMISSION_MAPPER_NAME)
                .logicalOperation(AddLogicalPermissionMapper.LogicalOperation.OR)
                .left(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .right(null)
                .build();
        fail("Creating command with null right should throw exception");
    }

}
