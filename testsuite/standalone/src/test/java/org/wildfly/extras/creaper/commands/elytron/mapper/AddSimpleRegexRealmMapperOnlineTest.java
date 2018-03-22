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
public class AddSimpleRegexRealmMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SIMPLE_REGEX_REALM_MAPPER_NAME = "CreaperTestSimpleRegexRealmMapper";
    private static final Address TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-regex-realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME);
    private static final String TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2 = "CreaperTestSimpleRegexRealmMapper2";
    private static final Address TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("simple-regex-realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleRegexRealmMapper() throws Exception {
        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .build();

        client.apply(addSimpleRegexRealmMapper);

        assertTrue("Simple regex realm mapper should be created", ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS));
    }

    @Test
    public void addTwoSimpleRegexRealmMappers() throws Exception {
        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .build();

        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper2
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
                .pattern("(somePattern)")
                .build();

        client.apply(addSimpleRegexRealmMapper);
        client.apply(addSimpleRegexRealmMapper2);

        assertTrue("Simple regex realm mapper should be created", ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS));
        assertTrue("Second simple regex realm mapper should be created",
                ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS2));
    }

    @Test
    public void addFullSimpleRegexRealmMapper() throws Exception {
        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper2
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
                .pattern("(somePattern2)")
                .build();
        client.apply(addSimpleRegexRealmMapper2);

        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .delegateRealmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
                .build();

        client.apply(addSimpleRegexRealmMapper);

        assertTrue("Simple regex realm mapper should be created", ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS));

        checkAttribute(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS, "pattern", "(somePattern)");
        checkAttribute(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS, "delegate-realm-mapper",
                TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSimpleRegexRealmMapperNotAllowed() throws Exception {
        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .build();

        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper2
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern2)")
                .build();

        client.apply(addSimpleRegexRealmMapper);
        assertTrue("Simple regex realm mapper should be created", ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS));
        client.apply(addSimpleRegexRealmMapper2);
        fail("Simple regex realm mapper CreaperTestSimpleRegexRealmMapper already exists in configuration, exception should be thrown");
    }

    public void addExistSimpleRegexRealmMapperAllowed() throws Exception {
        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .build();

        AddSimpleRegexRealmMapper addSimpleRegexRealmMapper2
                = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern2)")
                .replaceExisting()
                .build();

        client.apply(addSimpleRegexRealmMapper);
        assertTrue("Simple regex realm mapper should be created", ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS));
        client.apply(addSimpleRegexRealmMapper2);
        assertTrue("Simple regex realm mapper should be created", ops.exists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS, "pattern", "(somePattern2)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRegexRealmMapper_nullName() throws Exception {
        new AddSimpleRegexRealmMapper.Builder(null)
                .pattern("(somePattern)")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRegexRealmMapper_emptyName() throws Exception {
        new AddSimpleRegexRealmMapper.Builder("")
                .pattern("(somePattern)")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRegexRealmMapper_nullPattern() throws Exception {
        new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern(null)
                .build();
        fail("Creating command with null pattern should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleRegexRealmMapper_emptyPattern() throws Exception {
        new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .pattern("")
                .build();
        fail("Creating command with empty pattern should throw exception");
    }

}
