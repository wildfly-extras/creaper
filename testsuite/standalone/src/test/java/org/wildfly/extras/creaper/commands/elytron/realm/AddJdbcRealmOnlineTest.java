package org.wildfly.extras.creaper.commands.elytron.realm;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.AttributeMappingBuilder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.BcryptMapperBuilder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.ClearPasswordMapperBuilder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.PrincipalQueryBuilder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.SaltedSimpleDigestMapperBuilder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.ScramMapperBuilder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddJdbcRealm.SimpleDigestMapperBuilder;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddJdbcRealmOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SALT_DIGEST_MD5 = "password-salt-digest-md5";
    private static final String TEST_SCRAM_ALGORITHM = "scram-sha-256";
    private static final String TEST_DIGEST_ALGORITHM = "simple-digest-md5";
    private static final Integer TEST_ITERATION_COUNT_INDEX = 3;
    private static final Integer TEST_SALT_INDEX = 2;
    private static final Integer TEST_PASSWORD_INDEX = 1;
    private static final String TEST_JDBC_REALM_NAME = "CreaperTestJdbcRealm";
    private static final Address TEST_JDBC_REALM_ADDRESS = SUBSYSTEM_ADDRESS.and("jdbc-realm", TEST_JDBC_REALM_NAME);
    private static final String TEST_JDBC_REALM_NAME2 = "CreaperTestJdbcRealm2";
    private static final Address TEST_JDBC_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS.and("jdbc-realm", TEST_JDBC_REALM_NAME2);
    private static final String TEST_SQL = "select * from users";
    private static final String TEST_SQL2 = "select * from users2";
    private static final String TEST_DS = "ExampleDS";

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_JDBC_REALM_ADDRESS);
        ops.removeIfExists(TEST_JDBC_REALM_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleJdbcRealm() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .build())
                .build();

        client.apply(addJdbcRealm);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));

    }

    @Test
    public void addTwoSimpleJdbcRealm() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .build())
                .build();

        AddJdbcRealm addJdbcRealm2 = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME2)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .build())
                .build();

        client.apply(addJdbcRealm);
        client.apply(addJdbcRealm2);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));
        assertTrue("Second jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS2));
    }

    @Test
    public void addFullJdbcRealmClearPasswordMapper() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .clearPasswordMapper(new ClearPasswordMapperBuilder()
                        .passwordIndex(TEST_PASSWORD_INDEX)
                        .build())
                    .attributeMapping(new AttributeMappingBuilder()
                        .index(1)
                        .to("groups")
                        .build(),
                        new AttributeMappingBuilder()
                        .index(2)
                        .to("roles")
                        .build())
                    .build())
                .build();

        client.apply(addJdbcRealm);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));

        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].clear-password-mapper.password-index",
                TEST_PASSWORD_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].attribute-mapping[0].index", "1");
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].attribute-mapping[0].to", "groups");
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].attribute-mapping[1].index", "2");
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].attribute-mapping[1].to", "roles");
    }

    @Test
    public void addBcryptMapper() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .bcryptMapper(new BcryptMapperBuilder()
                        .passwordIndex(TEST_PASSWORD_INDEX)
                        .saltIndex(TEST_SALT_INDEX)
                        .iterationCountIndex(TEST_ITERATION_COUNT_INDEX)
                        .build())
                    .build())
                .build();

        client.apply(addJdbcRealm);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));

        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].bcrypt-mapper.password-index",
                TEST_PASSWORD_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].bcrypt-mapper.salt-index",
                TEST_SALT_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].bcrypt-mapper.iteration-count-index",
                TEST_ITERATION_COUNT_INDEX.toString());
    }

    @Test
    public void addSimpleDigestMapper() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .simpleDigestMapper(new SimpleDigestMapperBuilder()
                        .passwordIndex(TEST_PASSWORD_INDEX)
                        .algorithm(TEST_DIGEST_ALGORITHM)
                        .build())
                    .build())
                .build();

        client.apply(addJdbcRealm);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));

        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].simple-digest-mapper.password-index",
                TEST_PASSWORD_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].simple-digest-mapper.algorithm",
                TEST_DIGEST_ALGORITHM);
    }

    @Test
    public void addSaltedSimpleDigestMapper() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .saltedSimpleDigestMapper(new SaltedSimpleDigestMapperBuilder()
                        .passwordIndex(TEST_PASSWORD_INDEX)
                        .saltIndex(TEST_SALT_INDEX)
                        .algorithm(TEST_SALT_DIGEST_MD5)
                        .build())
                    .build())
                .build();

        client.apply(addJdbcRealm);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));

        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].salted-simple-digest-mapper.password-index",
                TEST_PASSWORD_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].salted-simple-digest-mapper.salt-index",
                TEST_SALT_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].salted-simple-digest-mapper.algorithm",
                TEST_SALT_DIGEST_MD5);
    }

    @Test
    public void addFullJdbcRealmScramMapper() throws Exception {
        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .scramMapper(new ScramMapperBuilder()
                        .passwordIndex(TEST_PASSWORD_INDEX)
                        .saltIndex(TEST_SALT_INDEX)
                        .iterationCountIndex(TEST_ITERATION_COUNT_INDEX)
                        .algorithm(TEST_SCRAM_ALGORITHM)
                        .build())
                    .build())
                .build();

        client.apply(addJdbcRealm);

        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));

        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].scram-mapper.password-index",
                TEST_PASSWORD_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].scram-mapper.salt-index",
                TEST_SALT_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].scram-mapper.iteration-count-index",
                TEST_ITERATION_COUNT_INDEX.toString());
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].scram-mapper.algorithm",
                TEST_SCRAM_ALGORITHM);
    }

    @Test(expected = CommandFailedException.class)
    public void addJdbcRealmNotAllowed() throws Exception {

        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .build())
                .build();

        AddJdbcRealm addJdbcRealm2 = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .build())
                .build();

        client.apply(addJdbcRealm);
        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));
        client.apply(addJdbcRealm2);
        fail("Jdbc realm" + TEST_JDBC_REALM_NAME + " already exists in configuration, exception should be thrown");
    }

    @Test()
    public void addJdbcRealmAllowed() throws Exception {

        AddJdbcRealm addJdbcRealm = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL)
                    .dataSource(TEST_DS)
                    .build())
                .build();

        AddJdbcRealm addJdbcRealm2 = new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
                .principalQueries(new PrincipalQueryBuilder()
                    .sql(TEST_SQL2)
                    .dataSource(TEST_DS)
                    .build())
                .replaceExisting()
                .build();

        client.apply(addJdbcRealm);
        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));
        client.apply(addJdbcRealm2);
        assertTrue("Jdbc realm should be created", ops.exists(TEST_JDBC_REALM_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_JDBC_REALM_ADDRESS, "principal-query[0].sql", TEST_SQL2);

    }

    @Test(expected = IllegalArgumentException.class)
    public void addJdbcRealm_nullName() throws Exception {
        new AddJdbcRealm.Builder(null)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .build())
            .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJdbcRealm_emptyName() throws Exception {
        new AddJdbcRealm.Builder("")
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .build())
            .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJdbcRealm_nullDataSource() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(null)
                .build())
            .build();
        fail("Creating command with null data source should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addJdbcRealm_emptyDataSource() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource("")
                .build())
            .build();
        fail("Creating command with empty data source should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAttributeMapping_nullIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .attributeMapping(new AttributeMappingBuilder()
                    .index(null)
                    .to("groups")
                    .build())
                .build())
            .build();
        fail("Creating command with null index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAttributeMapping_nullTo() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .attributeMapping(new AttributeMappingBuilder()
                    .index(0)
                    .to(null)
                    .build())
                .build())
            .build();
        fail("Creating command with null \"to\" should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addClearPasswordMapper_nullPasswordIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .clearPasswordMapper(new ClearPasswordMapperBuilder()
                    .passwordIndex(null)
                    .build())
                .build())
            .build();
        fail("Creating command with null password-index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBcryptMapper_nullPasswordIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .bcryptMapper(new BcryptMapperBuilder()
                    .passwordIndex(null)
                    .saltIndex(TEST_SALT_INDEX)
                    .iterationCountIndex(TEST_ITERATION_COUNT_INDEX)
                    .build())
                .build())
            .build();
        fail("Creating command with null password-index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBcryptMapper_nullSaltIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .bcryptMapper(new BcryptMapperBuilder()
                    .passwordIndex(TEST_PASSWORD_INDEX)
                    .saltIndex(null)
                    .iterationCountIndex(TEST_ITERATION_COUNT_INDEX)
                    .build())
                .build())
            .build();
        fail("Creating command with null salt index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addBcryptMapper_nullIterationCountIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .bcryptMapper(new BcryptMapperBuilder()
                    .passwordIndex(TEST_PASSWORD_INDEX)
                    .saltIndex(TEST_SALT_INDEX)
                    .iterationCountIndex(null)
                    .build())
                .build())
            .build();
        fail("Creating command with null iteration count  index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSimpleDigestMapper_nullPasswordIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .simpleDigestMapper(new SimpleDigestMapperBuilder()
                    .passwordIndex(null)
                    .build())
                .build())
            .build();
        fail("Creating command with null password index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSaltedSimpleDigestMapper_nullPasswordIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .saltedSimpleDigestMapper(new SaltedSimpleDigestMapperBuilder()
                    .passwordIndex(null)
                    .saltIndex(TEST_SALT_INDEX)
                    .build())
                .build())
            .build();
        fail("Creating command with null password index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSaltedSimpleDigestMapper_nullSaltIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .saltedSimpleDigestMapper(new SaltedSimpleDigestMapperBuilder()
                    .passwordIndex(TEST_PASSWORD_INDEX)
                    .saltIndex(null)
                    .build())
                .build())
            .build();
        fail("Creating command with null salt index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addScramMapper_nullPasswordIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .scramMapper(new ScramMapperBuilder()
                    .passwordIndex(null)
                    .saltIndex(TEST_SALT_INDEX)
                    .iterationCountIndex(TEST_ITERATION_COUNT_INDEX)
                    .build())
                .build())
            .build();
        fail("Creating command with null password index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addScramMapper_nullSaltIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .scramMapper(new ScramMapperBuilder()
                    .passwordIndex(TEST_PASSWORD_INDEX)
                    .saltIndex(null)
                    .iterationCountIndex(TEST_ITERATION_COUNT_INDEX)
                    .build())
                .build())
            .build();
        fail("Creating command with null salt index should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addScramMapper_nullIterationCountIndex() throws Exception {
        new AddJdbcRealm.Builder(TEST_JDBC_REALM_NAME)
            .principalQueries(new PrincipalQueryBuilder()
                .sql(TEST_SQL)
                .dataSource(TEST_DS)
                .scramMapper(new ScramMapperBuilder()
                    .passwordIndex(TEST_PASSWORD_INDEX)
                    .saltIndex(TEST_SALT_INDEX)
                    .iterationCountIndex(null)
                    .build())
                .build())
            .build();
        fail("Creating command with null iteration count index should throw exception");
    }
}
