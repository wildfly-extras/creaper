package org.wildfly.extras.creaper.commands.elytron.http;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.Mechanism;
import org.wildfly.extras.creaper.commands.elytron.credfactory.AddKerberosSecurityFactory;
import org.wildfly.extras.creaper.commands.elytron.domain.AddSecurityDomain;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddConstantPrincipalTransformer;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddSimpleRegexRealmMapper;
import org.wildfly.extras.creaper.commands.elytron.realm.AddFilesystemRealm;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddHttpAuthenticationFactoryOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_AUTHENTICATION_FACTORY_NAME
            = "CreaperTestHttpAuthenticationFactory";
    private static final Address TEST_AUTHENTICATION_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("http-authentication-factory", TEST_AUTHENTICATION_FACTORY_NAME);
    private static final String TEST_AUTHENTICATION_FACTORY_NAME2
            = "CreaperTestHttpAuthenticationFactory2";
    private static final Address TEST_AUTHENTICATION_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("http-authentication-factory", TEST_AUTHENTICATION_FACTORY_NAME2);

    private static final String TEST_FILESYSTEM_REALM_NAME = "CreaperTestFilesystemRealm";
    private static final Address TEST_FILESYSTEM_REALM_ADDRESS = SUBSYSTEM_ADDRESS
            .and("filesystem-realm", TEST_FILESYSTEM_REALM_NAME);
    private final AddFilesystemRealm addFilesystemRealm = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME)
            .path("/path/to/filesystem")
            .build();

    private static final String TEST_FILESYSTEM_REALM_NAME2 = "CreaperTestFilesystemRealm2";
    private static final Address TEST_FILESYSTEM_REALM_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("filesystem-realm", TEST_FILESYSTEM_REALM_NAME2);
    private final AddFilesystemRealm addFilesystemRealm2 = new AddFilesystemRealm.Builder(TEST_FILESYSTEM_REALM_NAME2)
            .path("/path/to/filesystem")
            .build();

    private static final String TEST_SECURITY_DOMAIN_NAME = "CreaperTestSecurityDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS = SUBSYSTEM_ADDRESS
            .and("security-domain", TEST_SECURITY_DOMAIN_NAME);
    private final AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
            .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
            .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                    .build())
            .build();

    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME
            = "CreaperTestProviderHttpServerMechanismFactory";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME);
    private final AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory
            = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME)
            .build();

    private static final String TEST_SERVER_MECHANISM_FACTORY_NAME2
            = "CreaperTestProviderHttpServerMechanismFactory2";
    private static final Address TEST_SERVER_MECHANISM_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("provider-http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME2);
    private final AddProviderHttpServerMechanismFactory addProviderHttpServerMechanismFactory2
            = new AddProviderHttpServerMechanismFactory.Builder(TEST_SERVER_MECHANISM_FACTORY_NAME2)
            .build();

    private static final String TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME = "CreaperTestConstantPrincipalTransformer";
    private static final Address TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
    private final AddConstantPrincipalTransformer addConstantPrincipalTransformer
            = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
            .constant("name1")
            .build();

    private static final String TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2 = "CreaperTestConstantPrincipalTransformer2";
    private static final Address TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
    private final AddConstantPrincipalTransformer addConstantPrincipalTransformer2
            = new AddConstantPrincipalTransformer.Builder(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
            .constant("name2")
            .build();

    private static final String TEST_SIMPLE_REGEX_REALM_MAPPER_NAME = "CreaperTestSimpleRegexRealmMapper";
    private static final Address TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-regex-realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME);
    private final AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
            = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
            .pattern("(somePattern)")
            .build();

    private static final String TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2 = "CreaperTestSimpleRegexRealmMapper2";
    private static final Address TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("simple-regex-realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2);
    private final AddSimpleRegexRealmMapper addSimpleRegexRealmMapper2
            = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
            .pattern("(somePattern2)")
            .build();

    private static final String TEST_KERBEROS_SECURITY_FACTORY_NAME = "CreaperTestKerberosSecurityFactory";
    private static final Address TEST_KERBEROS_SECURITY_FACTORY_ADDRESS = SUBSYSTEM_ADDRESS
            .and("kerberos-security-factory", TEST_KERBEROS_SECURITY_FACTORY_NAME);
    private final AddKerberosSecurityFactory addKerberosSecurityFactory
            = new AddKerberosSecurityFactory.Builder(TEST_KERBEROS_SECURITY_FACTORY_NAME)
            .principal("principal1")
            .path("/path/to/keytab")
            .mechanismOIDs("1.2.840.113554.1.2.2")
            .build();

    private static final String TEST_KERBEROS_SECURITY_FACTORY_NAME2 = "CreaperTestKerberosSecurityFactory2";
    private static final Address TEST_KERBEROS_SECURITY_FACTORY_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("kerberos-security-factory", TEST_KERBEROS_SECURITY_FACTORY_NAME2);
    private final AddKerberosSecurityFactory addKerberosSecurityFactory2
            = new AddKerberosSecurityFactory.Builder(TEST_KERBEROS_SECURITY_FACTORY_NAME2)
            .principal("principal2")
            .path("/path/to/keytab")
            .mechanismOIDs("1.2.840.113554.1.2.2")
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_AUTHENTICATION_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_AUTHENTICATION_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS);
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS);
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS2);
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_SERVER_MECHANISM_FACTORY_ADDRESS2);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2);
        ops.removeIfExists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS2);
        ops.removeIfExists(TEST_KERBEROS_SECURITY_FACTORY_ADDRESS);
        ops.removeIfExists(TEST_KERBEROS_SECURITY_FACTORY_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleHttpAuthenticationFactory() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addSecurityDomain);
        client.apply(addProviderHttpServerMechanismFactory);

        AddHttpAuthenticationFactory addHttpAuthenticationFactory
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        client.apply(addHttpAuthenticationFactory);

        assertTrue("Http authentication factory should be created", ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS));
    }

    @Test
    public void addTwoHttpAuthenticationFactories() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addSecurityDomain);
        client.apply(addProviderHttpServerMechanismFactory);

        AddHttpAuthenticationFactory addHttpAuthenticationFactory
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        AddHttpAuthenticationFactory addHttpAuthenticationFactory2
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME2)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        client.apply(addHttpAuthenticationFactory);
        client.apply(addHttpAuthenticationFactory2);

        assertTrue("Http authentication factory should be created", ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS));
        assertTrue("Second http authentication factory should be created",
                ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS2));
    }

    @Test
    public void addFullHttpAuthenticationFactory() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addFilesystemRealm2);
        client.apply(addSecurityDomain);
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addConstantPrincipalTransformer);
        client.apply(addConstantPrincipalTransformer2);
        client.apply(addSimpleRegexRealmMapper);
        client.apply(addSimpleRegexRealmMapper2);
        client.apply(addKerberosSecurityFactory);
        client.apply(addKerberosSecurityFactory2);

        AddHttpAuthenticationFactory addHttpAuthenticationFactory
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addMechanismConfigurations(new Mechanism.Builder()
                        .credentialSecurityFactory(TEST_KERBEROS_SECURITY_FACTORY_NAME)
                        .mechanismName("someMechanismName")
                        .hostName("someHostName")
                        .protocol("someProtocol")
                        .preRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                        .postRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                        .finalPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                        .realmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                        .addMechanismRealmConfigurations(new Mechanism.MechanismRealmBuilder()
                                .realmName(TEST_FILESYSTEM_REALM_NAME)
                                .preRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                                .postRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                                .finalPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                                .realmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                                .build(),
                                new Mechanism.MechanismRealmBuilder()
                                .realmName(TEST_FILESYSTEM_REALM_NAME2)
                                .preRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                                .postRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                                .finalPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                                .realmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
                                .build())
                        .build(),
                        new Mechanism.Builder()
                        .credentialSecurityFactory(TEST_KERBEROS_SECURITY_FACTORY_NAME2)
                        .mechanismName("someMechanismName2")
                        .hostName("someHostName2")
                        .protocol("someProtocol2")
                        .preRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                        .postRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                        .finalPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                        .realmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
                        .addMechanismRealmConfigurations(new Mechanism.MechanismRealmBuilder()
                                .realmName(TEST_FILESYSTEM_REALM_NAME2)
                                .preRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                                .postRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                                .finalPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                                .realmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2)
                                .build())
                        .build())
                .build();

        client.apply(addHttpAuthenticationFactory);

        assertTrue("Http authentication factory should be created", ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS));

        checkAttribute("security-domain", TEST_SECURITY_DOMAIN_NAME);
        checkAttribute("http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME);

        checkAttribute("mechanism-configurations[0].mechanism-name", "someMechanismName");
        checkAttribute("mechanism-configurations[0].host-name", "someHostName");
        checkAttribute("mechanism-configurations[0].protocol", "someProtocol");
        checkAttribute("mechanism-configurations[0].pre-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute("mechanism-configurations[0].post-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[0].final-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute("mechanism-configurations[0].realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME);
        checkAttribute("mechanism-configurations[0].credential-security-factory", TEST_KERBEROS_SECURITY_FACTORY_NAME);

        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[0].realm-name",
                TEST_FILESYSTEM_REALM_NAME);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[0].pre-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[0].post-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[0].final-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[0].realm-mapper",
                TEST_SIMPLE_REGEX_REALM_MAPPER_NAME);

        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[1].realm-name",
                TEST_FILESYSTEM_REALM_NAME2);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[1].pre-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[1].post-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[1].final-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[0].mechanism-realm-configurations[1].realm-mapper",
                TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2);

        checkAttribute("mechanism-configurations[1].mechanism-name", "someMechanismName2");
        checkAttribute("mechanism-configurations[1].host-name", "someHostName2");
        checkAttribute("mechanism-configurations[1].protocol", "someProtocol2");
        checkAttribute("mechanism-configurations[1].pre-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[1].post-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute("mechanism-configurations[1].final-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[1].realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2);
        checkAttribute("mechanism-configurations[1].credential-security-factory",
                TEST_KERBEROS_SECURITY_FACTORY_NAME2);

        checkAttribute("mechanism-configurations[1].mechanism-realm-configurations[0].realm-name",
                TEST_FILESYSTEM_REALM_NAME2);
        checkAttribute("mechanism-configurations[1].mechanism-realm-configurations[0].pre-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[1].mechanism-realm-configurations[0].post-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[1].mechanism-realm-configurations[0].final-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute("mechanism-configurations[1].mechanism-realm-configurations[0].realm-mapper",
                TEST_SIMPLE_REGEX_REALM_MAPPER_NAME2);

    }

    @Test(expected = CommandFailedException.class)
    public void addExistHttpAuthenticationFactoryNotAllowed() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addSecurityDomain);
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);

        AddHttpAuthenticationFactory addHttpAuthenticationFactory
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        AddHttpAuthenticationFactory addHttpAuthenticationFactory2
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME2)
                .build();

        client.apply(addHttpAuthenticationFactory);
        assertTrue("Http authentication factory should be created", ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS));
        client.apply(addHttpAuthenticationFactory2);
        fail("Http authentication factory CreaperTestHttpAuthenticationFactory already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistHttpAuthenticationFactoryAllowed() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addSecurityDomain);
        client.apply(addProviderHttpServerMechanismFactory);
        client.apply(addProviderHttpServerMechanismFactory2);

        AddHttpAuthenticationFactory addHttpAuthenticationFactory
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();

        AddHttpAuthenticationFactory addHttpAuthenticationFactory2
                = new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME2)
                .replaceExisting()
                .build();

        client.apply(addHttpAuthenticationFactory);
        assertTrue("Http authentication factory should be created", ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS));
        client.apply(addHttpAuthenticationFactory2);
        assertTrue("Http authentication factory should be created", ops.exists(TEST_AUTHENTICATION_FACTORY_ADDRESS));
        // check whether it was really rewritten
        checkAttribute("http-server-mechanism-factory", TEST_SERVER_MECHANISM_FACTORY_NAME2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_nullName() throws Exception {
        new AddHttpAuthenticationFactory.Builder(null)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_emptyName() throws Exception {
        new AddHttpAuthenticationFactory.Builder("")
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_nullSecurityDomain() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(null)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with null security domain should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_emptySecurityDomain() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain("")
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .build();
        fail("Creating command with empty security domain should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_nullHttpServerMechanismFactory() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(null)
                .build();
        fail("Creating command with null http server mechanism factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_emptyHttpServerMechanismFactory() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory("")
                .build();
        fail("Creating command with empty http server mechanism factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_nullMechanismConfigurations() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addMechanismConfigurations(null)
                .build();
        fail("Creating command with null mechanism configuration should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_nullMechanismRealmConfigurations() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addMechanismConfigurations(new Mechanism.Builder()
                        .addMechanismRealmConfigurations(null)
                        .build())
                .build();
        fail("Creating command with null mechanism realm configuration should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_nullRealmName_mechanismRealm() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addMechanismConfigurations(new Mechanism.Builder()
                        .addMechanismRealmConfigurations(new Mechanism.MechanismRealmBuilder()
                                .realmName(null)
                                .build())
                        .build())
                .build();
        fail("Creating command with null realm name in mechanism realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addHttpAuthenticationFactory_emptyRealmName_mechanismRealm() throws Exception {
        new AddHttpAuthenticationFactory.Builder(TEST_AUTHENTICATION_FACTORY_NAME)
                .securityDomain(TEST_SECURITY_DOMAIN_NAME)
                .httpServerMechanismFactory(TEST_SERVER_MECHANISM_FACTORY_NAME)
                .addMechanismConfigurations(new Mechanism.Builder()
                        .addMechanismRealmConfigurations(new Mechanism.MechanismRealmBuilder()
                                .realmName("")
                                .build())
                        .build())
                .build();
        fail("Creating command with empty realm name in mechanism realm should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(TEST_AUTHENTICATION_FACTORY_ADDRESS, attribute, expectedValue);
    }
}
