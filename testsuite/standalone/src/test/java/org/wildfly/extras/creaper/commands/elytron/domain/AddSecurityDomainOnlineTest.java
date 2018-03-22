package org.wildfly.extras.creaper.commands.elytron.domain;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.audit.AddFileAuditLog;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddConstantPrincipalTransformer;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddConstantPrincipalDecoder;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddConstantRoleMapper;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddSimplePermissionMapper;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddSimpleRegexRealmMapper;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddSimpleRoleDecoder;
import org.wildfly.extras.creaper.commands.elytron.realm.AddFilesystemRealm;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.operations.Address;

@RunWith(Arquillian.class)
public class AddSecurityDomainOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_SECURITY_DOMAIN_NAME = "CreaperTestSecurityDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS = SUBSYSTEM_ADDRESS
            .and("security-domain", TEST_SECURITY_DOMAIN_NAME);
    private static final String TEST_SECURITY_DOMAIN_NAME2 = "CreaperTestSecurityDomain2";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("security-domain", TEST_SECURITY_DOMAIN_NAME2);

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
            .path("/path/to/filesystem2")
            .build();

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

    private static final String TEST_CONSTANT_PRINCIPAL_DECODER_NAME = "CreaperTestConstantPrincipalDecoder";
    private static final Address TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-decoder", TEST_CONSTANT_PRINCIPAL_DECODER_NAME);
    private final AddConstantPrincipalDecoder addConstantPrincipalDecoder
            = new AddConstantPrincipalDecoder.Builder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
            .constant("role1")
            .build();

    private static final String TEST_SIMPLE_REGEX_REALM_MAPPER_NAME = "CreaperTestSimpleRegexRealmMapper";
    private static final Address TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-regex-realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME);
    private final AddSimpleRegexRealmMapper addSimpleRegexRealmMapper
            = new AddSimpleRegexRealmMapper.Builder(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
            .pattern("(somePattern)")
            .build();

    private static final String TEST_CONSTANT_ROLE_MAPPER_NAME = "CreaperTestConstantRoleMapper";
    private static final Address TEST_CONSTANT_ROLE_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME);
    private final AddConstantRoleMapper addConstantRoleMapper
            = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME)
            .addRoles("AnyRole")
            .build();

    private static final String TEST_CONSTANT_ROLE_MAPPER_NAME2 = "CreaperTestConstantRoleMapper2";
    private static final Address TEST_CONSTANT_ROLE_MAPPER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("constant-role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME2);
    private final AddConstantRoleMapper addConstantRoleMapper2
            = new AddConstantRoleMapper.Builder(TEST_CONSTANT_ROLE_MAPPER_NAME2)
            .addRoles("AnyRole2")
            .build();

    private static final String TEST_SIMPLE_ROLE_DECODER_NAME = "CreaperTestSimpleRoleDecoder";
    private static final Address TEST_SIMPLE_ROLE_DECODER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("simple-role-decoder", TEST_SIMPLE_ROLE_DECODER_NAME);
    private final AddSimpleRoleDecoder addSimpleRoleDecoder
            = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME)
            .attribute("groups")
            .build();

    private static final String TEST_SIMPLE_ROLE_DECODER_NAME2 = "CreaperTestSimpleRoleDecoder2";
    private static final Address TEST_SIMPLE_ROLE_DECODER_ADDRESS2 = SUBSYSTEM_ADDRESS
            .and("simple-role-decoder", TEST_SIMPLE_ROLE_DECODER_NAME2);
    private final AddSimpleRoleDecoder addSimpleRoleDecoder2
            = new AddSimpleRoleDecoder.Builder(TEST_SIMPLE_ROLE_DECODER_NAME2)
            .attribute("groups2")
            .build();

    private static final String TEST_SECURITY_DOMAIN_NAME3 = "CreaperTestSecurityDomain3";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS3 = SUBSYSTEM_ADDRESS
            .and("security-domain", TEST_SECURITY_DOMAIN_NAME3);
    private final AddSecurityDomain addSecurityDomain3 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME3)
            .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
            .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                    .build())
            .build();

    private static final String TEST_SECURITY_DOMAIN_NAME4 = "CreaperTestSecurityDomain4";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS4 = SUBSYSTEM_ADDRESS
            .and("security-domain", TEST_SECURITY_DOMAIN_NAME4);
    private final AddSecurityDomain addSecurityDomain4 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME4)
            .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
            .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                    .build())
            .build();

    private static final String TEST_FILE_AUDIT_LOG_NAME = "CreaperTestFileAuditLog";
    private static final Address TEST_FILE_AUDIT_LOG_ADDRESS = SUBSYSTEM_ADDRESS
            .and("file-audit-log", TEST_FILE_AUDIT_LOG_NAME);
    AddFileAuditLog addFileAuditLog = new AddFileAuditLog.Builder(TEST_FILE_AUDIT_LOG_NAME)
            .path("audit.log")
            .build();

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS);
        ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS2);
        ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS3);
        ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS4);
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS);
        ops.removeIfExists(TEST_FILESYSTEM_REALM_ADDRESS2);
        ops.removeIfExists(TEST_SIMPLE_PERMISSION_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_ADDRESS2);
        ops.removeIfExists(TEST_CONSTANT_PRINCIPAL_DECODER_ADDRESS);
        ops.removeIfExists(TEST_SIMPLE_REGEX_REALM_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS);
        ops.removeIfExists(TEST_CONSTANT_ROLE_MAPPER_ADDRESS2);
        ops.removeIfExists(TEST_SIMPLE_ROLE_DECODER_ADDRESS);
        ops.removeIfExists(TEST_SIMPLE_ROLE_DECODER_ADDRESS2);
        ops.removeIfExists(TEST_FILE_AUDIT_LOG_ADDRESS);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleSecurityDomain() throws Exception {
        client.apply(addFilesystemRealm);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();

        client.apply(addSecurityDomain);

        assertTrue("Security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
    }

    @Test
    public void addTwoSecurityDomains() throws Exception {
        client.apply(addFilesystemRealm);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();
        AddSecurityDomain addSecurityDomain2 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME2)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();

        client.apply(addSecurityDomain);
        client.apply(addSecurityDomain2);

        assertTrue("Security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
        assertTrue("Second security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS2));
    }

    @Test
    public void addFullSecurityDomain() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addFilesystemRealm2);
        client.apply(addSimplePermissionMapper);
        client.apply(addConstantPrincipalTransformer);
        client.apply(addConstantPrincipalTransformer2);
        client.apply(addConstantPrincipalDecoder);
        client.apply(addSimpleRegexRealmMapper);
        client.apply(addConstantRoleMapper);
        client.apply(addConstantRoleMapper2);
        client.apply(addSimpleRoleDecoder);
        client.apply(addSimpleRoleDecoder2);
        client.apply(addFileAuditLog);
        client.apply(addSecurityDomain3);
        client.apply(addSecurityDomain4);
        AddSecurityDomain addSecurityDomain2 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME2)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME2)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME2)
                        .build())
                .build();
        client.apply(addSecurityDomain2);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .permissionMapper(TEST_SIMPLE_PERMISSION_MAPPER_NAME)
                .postRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                .preRealmPrincipalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                .principalDecoder(TEST_CONSTANT_PRINCIPAL_DECODER_NAME)
                .realmMapper(TEST_SIMPLE_REGEX_REALM_MAPPER_NAME)
                .roleMapper(TEST_CONSTANT_ROLE_MAPPER_NAME)
                .trustedSecurityDomains(TEST_SECURITY_DOMAIN_NAME2, TEST_SECURITY_DOMAIN_NAME3)
                .outflowAnonymous(true)
                .outflowSecurityDomains(TEST_SECURITY_DOMAIN_NAME3, TEST_SECURITY_DOMAIN_NAME4)
                .securityEventListener(TEST_FILE_AUDIT_LOG_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .principalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME)
                        .roleDecoder(TEST_SIMPLE_ROLE_DECODER_NAME)
                        .roleMapper(TEST_CONSTANT_ROLE_MAPPER_NAME)
                        .build(),
                        new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME2)
                        .principalTransformer(TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2)
                        .roleDecoder(TEST_SIMPLE_ROLE_DECODER_NAME2)
                        .roleMapper(TEST_CONSTANT_ROLE_MAPPER_NAME2)
                        .build())
                .build();

        client.apply(addSecurityDomain);
        assertTrue("Security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));

        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "default-realm", TEST_FILESYSTEM_REALM_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "permission-mapper", TEST_SIMPLE_PERMISSION_MAPPER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "post-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "pre-realm-principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "principal-decoder", TEST_CONSTANT_PRINCIPAL_DECODER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realm-mapper", TEST_SIMPLE_REGEX_REALM_MAPPER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "trusted-security-domains[0]", TEST_SECURITY_DOMAIN_NAME2);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "trusted-security-domains[1]", TEST_SECURITY_DOMAIN_NAME3);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "outflow-anonymous", "true");
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "outflow-security-domains[0]", TEST_SECURITY_DOMAIN_NAME3);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "outflow-security-domains[1]", TEST_SECURITY_DOMAIN_NAME4);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "security-event-listener", TEST_FILE_AUDIT_LOG_NAME);

        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[0].realm", TEST_FILESYSTEM_REALM_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[0].principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[0].role-decoder", TEST_SIMPLE_ROLE_DECODER_NAME);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[0].role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME);

        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[1].realm", TEST_FILESYSTEM_REALM_NAME2);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[1].principal-transformer",
                TEST_CONSTANT_PRINCIPAL_TRANSFORMER_NAME2);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[1].role-decoder", TEST_SIMPLE_ROLE_DECODER_NAME2);
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "realms[1].role-mapper", TEST_CONSTANT_ROLE_MAPPER_NAME2);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistSecurityDomainNotAllowed() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addFilesystemRealm2);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();

        AddSecurityDomain addSecurityDomain2 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME2)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME2)
                        .build())
                .build();

        client.apply(addSecurityDomain);
        assertTrue("Security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
        client.apply(addSecurityDomain2);
        fail("Security domain CreaperTestSecurityDomain already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistSecurityDomainAllowed() throws Exception {
        client.apply(addFilesystemRealm);
        client.apply(addFilesystemRealm2);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();

        AddSecurityDomain addSecurityDomain2 = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME2)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME2)
                        .build())
                .replaceExisting()
                .build();

        client.apply(addSecurityDomain);
        assertTrue("Security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
        client.apply(addSecurityDomain2);
        assertTrue("Security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(TEST_SECURITY_DOMAIN_ADDRESS, "default-realm", TEST_FILESYSTEM_REALM_NAME2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_nullName() throws Exception {
        new AddSecurityDomain.Builder(null)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_emptyName() throws Exception {
        new AddSecurityDomain.Builder("")
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(TEST_FILESYSTEM_REALM_NAME)
                        .build())
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_nullRealms() throws Exception {
        new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(null)
                .build();
        fail("Creating command with null realms should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_noRealms() throws Exception {
        new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .build();
        fail("Creating command with no realms should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_emptyRealms() throws Exception {
        new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms()
                .build();
        fail("Creating command with empty realms should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_nullRealmsRealmName() throws Exception {
        new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder(null)
                        .build())
                .build();
        fail("Creating command with null name of realm in realms should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSecurityDomain_emptyRealmsRealmName() throws Exception {
        new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME)
                .defaultRealm(TEST_FILESYSTEM_REALM_NAME)
                .realms(new AddSecurityDomain.RealmBuilder("")
                        .build())
                .build();
        fail("Creating command with empty name of realm in realms should throw exception");
    }
}
