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
public class AddMappedRegexRealmMapperOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_MAPPED_REGEX_REALM_MAPPER_NAME = "CreaperTestMappedRegexRealmMapper";
    private static final Address TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("mapped-regex-realm-mapper", TEST_MAPPED_REGEX_REALM_MAPPER_NAME);
    private static final String TEST_MAPPED_REGEX_REALM_MAPPER_NAME2 = "CreaperTestMappedRegexRealmMapper2";
    private static final Address TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("mapped-regex-realm-mapper", TEST_MAPPED_REGEX_REALM_MAPPER_NAME2);

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleMappedRegexRealmMapper() throws Exception {
        AddMappedRegexRealmMapper addMappedRegexRealmMapper
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();

        client.apply(addMappedRegexRealmMapper);

        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS));
    }

    @Test
    public void addTwoMappedRegexRealmMappers() throws Exception {
        AddMappedRegexRealmMapper addMappedRegexRealmMapper
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();
        AddMappedRegexRealmMapper addMappedRegexRealmMapper2
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME2)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();

        client.apply(addMappedRegexRealmMapper);
        client.apply(addMappedRegexRealmMapper2);

        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS));
        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS2));
    }

    @Test
    public void addFullMappedRegexRealmMapper() throws Exception {
        AddMappedRegexRealmMapper addMappedRegexRealmMapper2
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME2)
                .pattern("(somePattern2)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();
        client.apply(addMappedRegexRealmMapper2);

        AddMappedRegexRealmMapper addMappedRegexRealmMapper
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .delegateRealmMapper(TEST_MAPPED_REGEX_REALM_MAPPER_NAME2)
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom1", "someTo1"),
                        new AddMappedRegexRealmMapper.RealmMapping("someFrom2", "someTo2"))
                .build();

        client.apply(addMappedRegexRealmMapper);

        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS));

        checkAttribute(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS, "pattern", "(somePattern)");
        checkAttribute(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS, "delegate-realm-mapper",
                TEST_MAPPED_REGEX_REALM_MAPPER_NAME2);
        checkAttribute(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS, "realm-map.someFrom1", "someTo1");
        checkAttribute(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS, "realm-map.someFrom2", "someTo2");
    }

    @Test(expected = CommandFailedException.class)
    public void addExistMappedRegexRealmMapperNotAllowed() throws Exception {
        AddMappedRegexRealmMapper addMappedRegexRealmMapper
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();

        AddMappedRegexRealmMapper addMappedRegexRealmMapper2
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern2)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();

        client.apply(addMappedRegexRealmMapper);
        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS));
        client.apply(addMappedRegexRealmMapper2);
        fail("Mapped regex realm mapper CreaperTestMappedRegexRealmMapper already exists in configuration, exception should be thrown");
    }

    public void addExistMappedRegexRealmMapperAllowed() throws Exception {
        AddMappedRegexRealmMapper addMappedRegexRealmMapper
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();

        AddMappedRegexRealmMapper addMappedRegexRealmMapper2
                = new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern2)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .replaceExisting()
                .build();

        client.apply(addMappedRegexRealmMapper);
        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS));
        client.apply(addMappedRegexRealmMapper2);
        assertTrue("Mapped regex realm mapper should be created", ops.exists(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_MAPPED_REGEX_REALM_MAPPER_ADDRESS, "pattern", "(somePattern2)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_nullName() throws Exception {
        new AddMappedRegexRealmMapper.Builder(null)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_emptyName() throws Exception {
        new AddMappedRegexRealmMapper.Builder("")
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_nullPattern() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern(null)
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();
        fail("Creating command with null pattern should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_emptyPattern() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", "someTo"))
                .build();
        fail("Creating command with empty pattern should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_nullRealmMapping() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(null)
                .build();
        fail("Creating command with null realm mapping should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_noRealmMapping() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .build();
        fail("Creating command with no realm mapping should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_emptyRealmMapping() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings()
                .build();
        fail("Creating command with empty realm mapping should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_nullFrom() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping(null, "someTo"))
                .build();
        fail("Creating command with null realm mapping 'from' should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappedRegexRealmMapper_nullTo() throws Exception {
        new AddMappedRegexRealmMapper.Builder(TEST_MAPPED_REGEX_REALM_MAPPER_NAME)
                .pattern("(somePattern)")
                .addRealmMappings(new AddMappedRegexRealmMapper.RealmMapping("someFrom", null))
                .build();
        fail("Creating command with null realm mapping 'to' should throw exception");
    }

}
