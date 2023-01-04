package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class RemoveLoginModuleOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String LOGIN_MODULE1_CODE = "UsersRoles";
    private static final String LOGIN_MODULE2_CODE = "RealmDirect";
    private static final String LOGIN_MODULE2_NAME = "NamedRealmDirect";

    private static final String TEST_SECURITY_DOMAIN_NAME = "creaperSecDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS
            = Address.subsystem("security").and("security-domain", TEST_SECURITY_DOMAIN_NAME);
    private static final Address TEST_AUTHENTICATION_ADDRESS
            = TEST_SECURITY_DOMAIN_ADDRESS.and("authentication", "classic");
    private static final Address TEST_LOGIN_MODULE1_ADDRESS
            = TEST_AUTHENTICATION_ADDRESS.and("login-module", LOGIN_MODULE1_CODE);
    private static final Address TEST_LOGIN_MODULE2_ADDRESS
            = TEST_AUTHENTICATION_ADDRESS.and("login-module", LOGIN_MODULE2_NAME);

    private static final String TEST_NON_EXIST_SECURITY_DOMAIN = "nonExistSecurityDomain";

    private static final AddLoginModule ADD_LOGIN_MODULE_1 = new AddLoginModule.Builder(LOGIN_MODULE1_CODE)
            .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
            .flag("required")
            .addModuleOption("usersProperties", "users.properties")
            .addModuleOption("rolesProperties", "roles.properties")
            .build();

    private static final AddLoginModule ADD_LOGIN_MODULE_2 = new AddLoginModule.Builder(LOGIN_MODULE2_CODE,
            LOGIN_MODULE2_NAME)
            .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
            .flag("required")
            .addModuleOption("realm", "ApplicationRealm")
            .build();

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
    }

    @Before
    public void connect() throws IOException, CommandFailedException, OperationException {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);

        AddSecurityDomain addSecurityDomain = new AddSecurityDomain.Builder(TEST_SECURITY_DOMAIN_NAME).build();
        client.apply(addSecurityDomain);
        assertTrue("The security domain should be created", ops.exists(TEST_SECURITY_DOMAIN_ADDRESS));
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_SECURITY_DOMAIN_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void removeOneOfMoreLoginModules() throws Exception {
        client.apply(ADD_LOGIN_MODULE_1);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE1_ADDRESS));
        client.apply(ADD_LOGIN_MODULE_2);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE2_ADDRESS));

        client.apply(new RemoveLoginModule(TEST_SECURITY_DOMAIN_NAME, LOGIN_MODULE1_CODE));
        assertFalse("The login module should be removed", ops.exists(TEST_LOGIN_MODULE1_ADDRESS));
    }

    @Test
    public void removeLastLoginModule() throws Exception {
        client.apply(ADD_LOGIN_MODULE_1);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE1_ADDRESS));

        client.apply(new RemoveLoginModule(TEST_SECURITY_DOMAIN_NAME, LOGIN_MODULE1_CODE));
        assertFalse("The login module should be removed", ops.exists(TEST_LOGIN_MODULE1_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    @Ignore("https://issues.jboss.org/browse/JBEAP-3082")
    public void removeNonExistingLoginModule() throws Exception {
        client.apply(ADD_LOGIN_MODULE_2);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE2_ADDRESS));

        client.apply(new RemoveLoginModule(TEST_SECURITY_DOMAIN_NAME, LOGIN_MODULE1_CODE));
        fail("Login module UsersRoles does not exist in configuration, exception should be thrown");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNullNameLoginModule() throws Exception {
        client.apply(new RemoveLoginModule(TEST_SECURITY_DOMAIN_NAME, null));
        fail("Creating command with null login module name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeEmptyNameLoginModule() throws Exception {
        client.apply(new RemoveLoginModule(TEST_SECURITY_DOMAIN_NAME, ""));
        fail("Creating command with empty login module name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    public void removeOnNonExistingSecurityDomain() throws Exception {
        client.apply(ADD_LOGIN_MODULE_1);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE1_ADDRESS));

        client.apply(new RemoveLoginModule(TEST_NON_EXIST_SECURITY_DOMAIN, LOGIN_MODULE1_CODE));
        fail("Using non-existing security domain should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeOnNullNameSecurityDomain() throws Exception {
        client.apply(new RemoveLoginModule(null, LOGIN_MODULE1_CODE));
        fail("Creating command with null security domain name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeOnEmptyNameSecurityDomain() throws Exception {
        client.apply(new RemoveLoginModule("", LOGIN_MODULE1_CODE));
        fail("Creating command with empty security domain name should throw exception");
    }

    @Test(expected = CommandFailedException.class)
    @Ignore("https://issues.jboss.org/browse/JBEAP-3082")
    public void doNotRemoveNamedLoginModuleByCodeReference() throws Exception {
        client.apply(ADD_LOGIN_MODULE_2);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE2_ADDRESS));

        client.apply(new RemoveLoginModule(TEST_SECURITY_DOMAIN_NAME, LOGIN_MODULE2_CODE));
        fail("Removing named login module based on code should be unable, exception should be thrown");
    }

}
