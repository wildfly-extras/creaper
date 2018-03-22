package org.wildfly.extras.creaper.commands.elytron.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;

@RunWith(Arquillian.class)
public class AddAggregateRoleMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AGGREGATE_ROLE_MAPPER_NAME = "CreaperTestAggregateRoleMapper";
    private static final Address TEST_AGGREGATE_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS.and("aggregate-role-mapper",
        TEST_AGGREGATE_ROLE_MAPPER_NAME);
    private static final String TEST_AGGREGATE_ROLE_MAPPER_NAME2 = "CreaperTestAggregateRoleMapper2";
    private static final Address TEST_AGGREGATE_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS.and("aggregate-role-mapper",
        TEST_AGGREGATE_ROLE_MAPPER_NAME2);

    private static final String ADD_PREFIX_ROLE_MAPPER_TYPE = "add-prefix-role-mapper";
    private static final String ADD_SUFFIX_ROLE_MAPPER_TYPE = "add-suffix-role-mapper";
    private static final String TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME = "CreaperTestAddPrefixRoleMapper";
    private static final String TEST_DEFAULT_ADD_SUFFIX_ROLE_MAPPER_NAME = "CreaperTestAddSuffixRoleMapper";

    public AddAggregateRoleMapperOnlineTest() {
        super();
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS2);

        removeAllAddPrefixRoleMappers();
        removeAllAddSuffixRoleMappers();

        administration.reloadIfRequired();
    }

    @Test
    public void addAggregateRoleMapper() throws Exception {
        addDefaultPrefixMapper();
        addDefaultSuffixMapper();

        AddAggregateRoleMapper addAggregateRoleMapper =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME)
            .addRoleMappers(TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME, TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME)
                .build();

        client.apply(addAggregateRoleMapper);

        assertTrue("Aggregate role mapper should be created", ops.exists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS));
    }

    @Test
    public void addAggregateRoleMapper_duplicateRoleMapper() throws Exception {
        addDefaultPrefixMapper();

        AddAggregateRoleMapper addAggregateRoleMapper =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME)
            .addRoleMappers(TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME, TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME)
                .build();

        client.apply(addAggregateRoleMapper);

        assertTrue("Aggregate role mapper should be created", ops.exists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregateRoleMapper_oneRoleMapper() throws Exception {
        addDefaultPrefixMapper();

        AddAggregateRoleMapper addAggregateRoleMapper =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME)
            .addRoleMappers(TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME).build();

        client.apply(addAggregateRoleMapper);

        fail("Creating command with only one role mapper should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void addAggregateRoleMapper_nonExistingRoleMappers() throws Exception {
        AddAggregateRoleMapper addAggregateRoleMapper =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME)
            .addRoleMappers("nonExistsRoleMapper1", "nonExistsRoleMapper2").build();

        client.apply(addAggregateRoleMapper);

        fail("Creating command with non existing role mappers should throw exception");
    }

    @Test
    public void addAggregateRoleMappers() throws Exception {
        addPrefixMapper("prefixMapper1", "prefix1");
        addPrefixMapper("prefixMapper2", "prefix2");
        addSuffixMapper("suffixMapper1", "suffix1");
        addSuffixMapper("suffixMapper2", "suffix2");

        AddAggregateRoleMapper addAggregateRoleMapper =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME)
            .addRoleMappers("prefixMapper1", "prefixMapper2")
                .build();

        AddAggregateRoleMapper addAggregateRoleMapper2 =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME2)
            .addRoleMappers("suffixMapper1", "suffixMapper2")
                .build();

        client.apply(addAggregateRoleMapper);
        client.apply(addAggregateRoleMapper2);

        assertTrue("Aggregate role mapper should be created", ops.exists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS));
        assertTrue("Second aggregate role mapper should be created", ops.exists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS2));

        assertRolesMappers(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS, "prefixMapper1", "prefixMapper2");
        assertRolesMappers(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS2, "suffixMapper1", "suffixMapper2");

        administration.reload();

        assertRolesMappers(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS, "prefixMapper1", "prefixMapper2");
        assertRolesMappers(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS2, "suffixMapper1", "suffixMapper2");
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateAggregateRoleMapperNotAllowed() throws Exception {
        addPrefixMapper("prefixMapper1", "prefix1");

        AddAggregateRoleMapper addAggregateRoleMapper =
            new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME)
            .addRoleMappers("prefixMapper1")
                .build();

        client.apply(addAggregateRoleMapper);
        assertTrue("Aggregate role mapper should be created", ops.exists(TEST_AGGREGATE_ROLE_MAPPER_ADDRESS));
        client.apply(addAggregateRoleMapper);
        fail("Aggregate role mapper " + TEST_AGGREGATE_ROLE_MAPPER_NAME
                + " already exists in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRoleMapper_nullName() throws Exception {
        new AddAggregateRoleMapper.Builder(null);
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRoleMapper_emptyName() throws Exception {
        new AddAggregateRoleMapper.Builder("");
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAggregateRoleMapper_noRoleMapper() throws Exception {
        new AddAggregateRoleMapper.Builder(TEST_AGGREGATE_ROLE_MAPPER_NAME).build();
        fail("Creating command with no role mapper should throw exception");
    }

    private void assertRolesMappers(Address address, String... expectedRoleMappers) throws Exception {
        List<String> actualRolesMappers = ops.readAttribute(address, "role-mappers").stringListValue();
        assertEquals("Unexpected role mappers attribute value in "
                + address, Arrays.asList(expectedRoleMappers), actualRolesMappers);
    }

    private void addDefaultPrefixMapper() throws CommandFailedException {
        addPrefixMapper(TEST_DEFAULT_ADD_PREFIX_ROLE_MAPPER_NAME, "somePrefix");
    }

    private void addPrefixMapper(String roleMapperName, String prefix) throws CommandFailedException {
        AddAddPrefixRoleMapper addAddPrefixRoleMapper = new AddAddPrefixRoleMapper.Builder(roleMapperName)
            .prefix(prefix).build();
        client.apply(addAddPrefixRoleMapper);
    }

    private void addDefaultSuffixMapper() throws CommandFailedException {
        addSuffixMapper(TEST_DEFAULT_ADD_SUFFIX_ROLE_MAPPER_NAME, "someSuffix");
    }

    private void addSuffixMapper(String roleMapperName, String suffix) throws CommandFailedException {
        AddAddSuffixRoleMapper addAddSuffixRoleMapper = new AddAddSuffixRoleMapper.Builder(roleMapperName)
            .suffix(suffix).build();
        client.apply(addAddSuffixRoleMapper);
    }

    private void removeAllAddPrefixRoleMappers() throws IOException, OperationException {
        removeAllElytronChildrenType(ADD_PREFIX_ROLE_MAPPER_TYPE);
    }

    private void removeAllAddSuffixRoleMappers() throws IOException, OperationException {
        removeAllElytronChildrenType(ADD_SUFFIX_ROLE_MAPPER_TYPE);
    }
}
