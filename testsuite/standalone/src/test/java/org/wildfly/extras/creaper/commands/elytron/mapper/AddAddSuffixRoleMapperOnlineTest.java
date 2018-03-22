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
public class AddAddSuffixRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_ADD_SUFFIX_ROLE_MAPPER_NAME = "CreaperTestAddSuffixRoleMapper";
    private static final Address TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("add-suffix-role-mapper", TEST_ADD_SUFFIX_ROLE_MAPPER_NAME);
    private static final String TEST_ADD_SUFFIX_ROLE_MAPPER_NAME2 = "CreaperTestAddSuffixRoleMapper2";
    private static final Address TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("add-suffix-role-mapper", TEST_ADD_SUFFIX_ROLE_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addAddSuffixRoleMapper() throws Exception {
        AddAddSuffixRoleMapper addAddSuffixRoleMapper =
            new AddAddSuffixRoleMapper.Builder(TEST_ADD_SUFFIX_ROLE_MAPPER_NAME)
            .suffix("someSuffix").build();

        client.apply(addAddSuffixRoleMapper);

        assertTrue("Add suffix role mapper should be created", ops.exists(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS));
    }

    @Test
    public void addAddSuffixRoleMappers() throws Exception {
        AddAddSuffixRoleMapper addAddSuffixRoleMapper =
            new AddAddSuffixRoleMapper.Builder(TEST_ADD_SUFFIX_ROLE_MAPPER_NAME)
            .suffix("someSuffix1").build();

        AddAddSuffixRoleMapper addAddSuffixRoleMapper2 =
            new AddAddSuffixRoleMapper.Builder(TEST_ADD_SUFFIX_ROLE_MAPPER_NAME2)
            .suffix("someSuffix2").build();

        client.apply(addAddSuffixRoleMapper);
        client.apply(addAddSuffixRoleMapper2);

        assertTrue("Add suffix role mapper should be created", ops.exists(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS));
        assertTrue("Second add suffix role mapper should be created", ops.exists(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS2));

        checkAttribute(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS, "suffix", "someSuffix1");
        checkAttribute(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS2, "suffix", "someSuffix2");

        administration.reload();

        checkAttribute(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS, "suffix", "someSuffix1");
        checkAttribute(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS2, "suffix", "someSuffix2");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateAddSuffixRoleMapperNotAllowed() throws Exception {
        AddAddSuffixRoleMapper addAddSuffixRoleMapper =
            new AddAddSuffixRoleMapper.Builder(TEST_ADD_SUFFIX_ROLE_MAPPER_NAME)
            .suffix("someSuffix").build();

        client.apply(addAddSuffixRoleMapper);
        assertTrue("Add suffix role mapper should be created", ops.exists(TEST_ADD_SUFFIX_ROLE_MAPPER_ADDRESS));
        client.apply(addAddSuffixRoleMapper);
        fail("Add suffix role mapper " + TEST_ADD_SUFFIX_ROLE_MAPPER_NAME
            + " already exists in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddSuffixRoleMapper_nullName() throws Exception {
        new AddAddSuffixRoleMapper.Builder(null);
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddSuffixRoleMapper_emptyName() throws Exception {
        new AddAddSuffixRoleMapper.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddSuffixRoleMapper_noSuffix() throws Exception {
        new AddAddSuffixRoleMapper.Builder(TEST_ADD_SUFFIX_ROLE_MAPPER_NAME).build();
        fail("Creating command with no suffix should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAddSuffixRoleMapper_emptySuffix() throws Exception {
        new AddAddSuffixRoleMapper.Builder(TEST_ADD_SUFFIX_ROLE_MAPPER_NAME).suffix("").build();
        fail("Creating command with empty suffix should throw exception");
    }
}
