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
public class AddAddPrefixRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_PREFIX_ROLE_MAPPER_NAME = "CreaperTestAddPrefixRoleMapper";
    private static final Address TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("add-prefix-role-mapper", TEST_ADD_PREFIX_ROLE_MAPPER_NAME);
    private static final String TEST_ADD_PREFIX_ROLE_MAPPER_NAME2 = "CreaperTestAddPrefixRoleMapper2";
    private static final Address TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("add-prefix-role-mapper", TEST_ADD_PREFIX_ROLE_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addAddPrefixRoleMapper() throws Exception {
        AddAddPrefixRoleMapper addAddPrefixRoleMapper =
            new AddAddPrefixRoleMapper.Builder(TEST_ADD_PREFIX_ROLE_MAPPER_NAME)
            .prefix("somePrefix").build();

        client.apply(addAddPrefixRoleMapper);

        assertTrue("Add prefix role mapper should be created", ops.exists(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS));
    }

    @Test
    public void addAddPrefixRoleMappers() throws Exception {
        AddAddPrefixRoleMapper addAddPrefixRoleMapper =
            new AddAddPrefixRoleMapper.Builder(TEST_ADD_PREFIX_ROLE_MAPPER_NAME)
            .prefix("somePrefix1").build();

        AddAddPrefixRoleMapper addAddPrefixRoleMapper2 =
            new AddAddPrefixRoleMapper.Builder(TEST_ADD_PREFIX_ROLE_MAPPER_NAME2)
            .prefix("somePrefix2").build();

        client.apply(addAddPrefixRoleMapper);
        client.apply(addAddPrefixRoleMapper2);

        assertTrue("Add prefix role mapper should be created", ops.exists(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS));
        assertTrue("Second add prefix role mapper should be created", ops.exists(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS2));

        checkAttribute(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS, "prefix", "somePrefix1");
        checkAttribute(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS2, "prefix", "somePrefix2");

        administration.reload();

        checkAttribute(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS, "prefix", "somePrefix1");
        checkAttribute(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS2, "prefix", "somePrefix2");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateAddPrefixRoleMapperNotAllowed() throws Exception {
        AddAddPrefixRoleMapper addAddPrefixRoleMapper =
            new AddAddPrefixRoleMapper.Builder(TEST_ADD_PREFIX_ROLE_MAPPER_NAME)
            .prefix("somePrefix").build();

        client.apply(addAddPrefixRoleMapper);
        assertTrue("Add prefix role mapper should be created", ops.exists(TEST_ADD_PREFIX_ROLE_MAPPER_ADDRESS));
        client.apply(addAddPrefixRoleMapper);
        fail("Add prefix role mapper " + TEST_ADD_PREFIX_ROLE_MAPPER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddPrefixRoleMapper_nullName() throws Exception {
        new AddAddPrefixRoleMapper.Builder(null);
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddPrefixRoleMapper_emptyName() throws Exception {
        new AddAddPrefixRoleMapper.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddPrefixRoleMapper_noPrefix() throws Exception {
        new AddAddPrefixRoleMapper.Builder(TEST_ADD_PREFIX_ROLE_MAPPER_NAME).build();
        fail("Creating command with no prefix should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddPrefixRoleMapper_emptyPrefix() throws Exception {
        new AddAddPrefixRoleMapper.Builder(TEST_ADD_PREFIX_ROLE_MAPPER_NAME).prefix("").build();
        fail("Creating command with empty prefix should throw exception");
    }
}
