package org.wildfly.extras.creaper.commands.security;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddLoginModuleOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_DOMAIN_NAME = "creaperSecDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS
            = Address.subsystem("security").and("security-domain", TEST_SECURITY_DOMAIN_NAME);
    private static final Address TEST_AUTHN_CLASSIC_ADDRESS = TEST_SECURITY_DOMAIN_ADDRESS
            .and("authentication", "classic");
    private static final String TEST_LOGIN_MODULE_NAME = "UsersRoles";
    private static final Address TEST_LOGIN_MODULE_ADDRESS = TEST_AUTHN_CLASSIC_ADDRESS
            .and("login-module", TEST_LOGIN_MODULE_NAME);
    private static final String TEST_LOGIN_MODULE_NAME_2 = "RealmDirect";
    private static final Address TEST_LOGIN_MODULE_ADDRESS_2 = TEST_AUTHN_CLASSIC_ADDRESS
            .and("login-module", TEST_LOGIN_MODULE_NAME_2);

    @BeforeClass
    public static void checkServerVersionIsSupported() throws Exception {
        // check version is supported
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
    }

    @Before
    public void connect() throws Exception {
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
    public void addSimpleLoginModule() throws Exception {
        AddLoginModule addLoginModule = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();
        client.apply(addLoginModule);

        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE_ADDRESS));
    }

    @Test
    public void addTwoSimpleLoginModules() throws Exception {
        AddLoginModule addLoginModule = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        AddLoginModule addLoginModule2 = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME_2)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        client.apply(addLoginModule);
        client.apply(addLoginModule2);

        assertTrue("The first login module should be created", ops.exists(TEST_LOGIN_MODULE_ADDRESS));
        assertTrue("The second login module should be created", ops.exists(TEST_LOGIN_MODULE_ADDRESS_2));
    }

    @Test
    public void addFullLoginModule() throws Exception {
        AddLoginModule addLoginModule = new AddLoginModule.Builder("org.jboss.security.auth.spi.UsersRolesLoginModule",
                TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .module("org.picketbox")
                .addModuleOption("usersProperties", "creaperUsers.properties")
                .addModuleOption("rolesProperties", "creaperRoles.properties")
                .build();

        client.apply(addLoginModule);

        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE_ADDRESS));

        checkLoginModuleAttribute("code", "org.jboss.security.auth.spi.UsersRolesLoginModule");
        checkLoginModuleAttribute("flag", "required");
        checkLoginModuleAttribute("module", "org.picketbox");

        ModelNodeResult readAttribute = ops.readAttribute(TEST_LOGIN_MODULE_ADDRESS, "module-options");
        readAttribute.assertSuccess("Read operation for module-options failed");
        List<ModelNode> moduleOptions = readAttribute.listValue();
        if (moduleOptions.size() != 2) {
            fail("Login module should contain 2 module options instead of " + moduleOptions.size());
        }
        boolean moduleOption1used = false;
        boolean moduleOption2used = false;
        for (ModelNode moduleOption : moduleOptions) {
            Property moduleOptionProperty = moduleOption.asProperty();
            String name = moduleOptionProperty.getName();
            if (name.equals("usersProperties")) {
                assertEquals("creaperUsers.properties", moduleOptionProperty.getValue().asString());
                moduleOption1used = true;
            }
            if (name.equals("rolesProperties")) {
                assertEquals("creaperRoles.properties", moduleOptionProperty.getValue().asString());
                moduleOption2used = true;
            }
        }
        assertTrue("Wrong options were added as module-options.", moduleOption1used && moduleOption2used);
    }

    @Test(expected = CommandFailedException.class)
    public void addExistLoginModuleNotAllowed() throws Exception {
        AddLoginModule addLoginModule = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();

        AddLoginModule addLoginModule2 = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .build();

        client.apply(addLoginModule);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE_ADDRESS));
        client.apply(addLoginModule2);
        fail("Login Module UsersRoles already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistLoginModuleAllowed() throws Exception {
        AddLoginModule addLoginModule = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .replaceExisting()
                .build();

        AddLoginModule addLoginModule2 = new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("sufficient")
                .replaceExisting()
                .build();

        client.apply(addLoginModule);
        assertTrue("The first login module should be created even if it is creating with \"replaceExisting\" and the "
                + "same login module does not exist", ops.exists(TEST_LOGIN_MODULE_ADDRESS));
        client.apply(addLoginModule2);
        assertTrue("The login module should be created", ops.exists(TEST_LOGIN_MODULE_ADDRESS));
        checkLoginModuleAttribute("flag", "sufficient");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_nullSecurityDomain() throws Exception {
        new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .flag("required")
                .build();
        fail("Creating command with null security domain name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_emptySecurityDomain() throws Exception {
        new AddLoginModule.Builder(TEST_LOGIN_MODULE_NAME)
                .securityDomainName("")
                .flag("required")
                .build();
        fail("Creating command with empty security domain name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_nullName() throws Exception {
        new AddLoginModule.Builder("org.jboss.security.auth.spi.UsersRolesLoginModule", null)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_emptyName() throws Exception {
        new AddLoginModule.Builder("org.jboss.security.auth.spi.UsersRolesLoginModule", "")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_nullCode() throws Exception {
        new AddLoginModule.Builder(null, "UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();
        fail("Creating command with null code should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_emptyCode() throws Exception {
        new AddLoginModule.Builder("", "UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .flag("required")
                .build();
        fail("Creating command with empty code should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addLoginModule_nullFlag() throws Exception {
        new AddLoginModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .build();
        fail("Creating command with null flag should throw exception");
    }

    private void checkLoginModuleAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_LOGIN_MODULE_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

}
