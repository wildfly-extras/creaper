package org.wildfly.extras.creaper.commands.elytron.mapper;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddConstantRealmMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_CONSTANT_REALM_MAPPER_NAME = "CreaperTestConstantRealmMapper";
    private static final Address TEST_CONSTANT_REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-realm-mapper", TEST_CONSTANT_REALM_MAPPER_NAME);
    private static final String TEST_CONSTANT_REALM_MAPPER_NAME2 = "CreaperTestConstantRealmMapper2";
    private static final Address TEST_CONSTANT_REALM_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-realm-mapper", TEST_CONSTANT_REALM_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_CONSTANT_REALM_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_REALM_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addConstantRealmMapper() throws Exception {
        AddConstantRealmMapper addConstantRealmMapper
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("someRealmName")
                .build();

        client.apply(addConstantRealmMapper);

        assertTrue("Constant realm mapper should be created", ops.exists(TEST_CONSTANT_REALM_MAPPER_ADDRESS));
        checkAttribute(TEST_CONSTANT_REALM_MAPPER_ADDRESS, "realm-name", "someRealmName");
    }

    @Test
    public void addTwoConstantRealmMappers() throws Exception {
        AddConstantRealmMapper addConstantRealmMapper
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("someRealmName")
                .build();
        AddConstantRealmMapper addConstantRealmMapper2
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME2)
                .realmName("someOtherRealmName")
                .build();

        client.apply(addConstantRealmMapper);
        client.apply(addConstantRealmMapper2);

        assertTrue("Constant realm mapper should be created", ops.exists(TEST_CONSTANT_REALM_MAPPER_ADDRESS));
        assertTrue("Constant realm mapper should be created", ops.exists(TEST_CONSTANT_REALM_MAPPER_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistConstantRealmMapperNotAllowed() throws Exception {
        AddConstantRealmMapper addConstantRealmMapper
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("someRealmName")
                .build();
        AddConstantRealmMapper addConstantRealmMapper2
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("someRealmName")
                .build();

        client.apply(addConstantRealmMapper);
        assertTrue("Constant realm mapper should be created", ops.exists(TEST_CONSTANT_REALM_MAPPER_ADDRESS));

        client.apply(addConstantRealmMapper2);
        fail("Constant realm mapper CreaperTestConstantRealmMapper already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistConstantRealmMapperAllowed() throws Exception {
        AddConstantRealmMapper addConstantRealmMapper
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("someRealmName")
                .build();
        AddConstantRealmMapper addConstantRealmMapper2
                = new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("someOtherRealmName")
                .replaceExisting()
                .build();

        client.apply(addConstantRealmMapper);
        assertTrue("Constant realm mapper should be created", ops.exists(TEST_CONSTANT_REALM_MAPPER_ADDRESS));
        checkAttribute(TEST_CONSTANT_REALM_MAPPER_ADDRESS, "realm-name", "someRealmName");

        client.apply(addConstantRealmMapper2);
        assertTrue("Constant realm mapper should be created", ops.exists(TEST_CONSTANT_REALM_MAPPER_ADDRESS));
        checkAttribute(TEST_CONSTANT_REALM_MAPPER_ADDRESS, "realm-name", "someOtherRealmName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantRealmMapper_nullName() throws Exception {
        new AddConstantRealmMapper.Builder(null)
                .realmName("someRealmName")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantRealmMapper_emptyName() throws Exception {
        new AddConstantRealmMapper.Builder("")
                .realmName("someRealmName")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantRealmMapper_nullRealmName() throws Exception {
        new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName(null)
                .build();
        fail("Creating command with null realm-name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addConstantRealmMapper_emptyrealmName() throws Exception {
        new AddConstantRealmMapper.Builder(TEST_CONSTANT_REALM_MAPPER_NAME)
                .realmName("")
                .build();
        fail("Creating command with empty realm-name should throw exception");
    }

}

