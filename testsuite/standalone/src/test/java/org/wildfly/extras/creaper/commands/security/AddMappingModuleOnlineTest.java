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
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.ServerVersion;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddMappingModuleOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_DOMAIN_NAME = "creaperSecDomain";
    private static final Address TEST_SECURITY_DOMAIN_ADDRESS
            = Address.subsystem("security").and("security-domain", TEST_SECURITY_DOMAIN_NAME);
    private static final Address TEST_MAPPING_CLASSIC_ADDRESS = TEST_SECURITY_DOMAIN_ADDRESS
            .and("mapping", "classic");
    private static final String TEST_MAPPING_MODULE_NAME = "PropertiesRoles";
    private static final Address TEST_MAPPING_MODULE_ADDRESS = TEST_MAPPING_CLASSIC_ADDRESS
            .and("mapping-module", TEST_MAPPING_MODULE_NAME);
    private static final String TEST_MAPPING_MODULE_NAME_2 = "SimpleRoles";
    private static final Address TEST_MAPPING_MODULE_ADDRESS_2 = TEST_MAPPING_CLASSIC_ADDRESS
            .and("mapping-module", TEST_MAPPING_MODULE_NAME_2);

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
    public void addSimpleMappingModule() throws Exception {
        AddMappingModule addMappingModule = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();
        client.apply(addMappingModule);

        assertTrue("The mapping module should be created", ops.exists(TEST_MAPPING_MODULE_ADDRESS));
    }

    @Test
    public void addTwoSimpleMappingModules() throws Exception {
        AddMappingModule addMappingModule = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();

        AddMappingModule addMappingModule2 = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME_2)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();

        client.apply(addMappingModule);
        client.apply(addMappingModule2);

        assertTrue("The first mapping module should be created", ops.exists(TEST_MAPPING_MODULE_ADDRESS));
        assertTrue("The second mapping module should be created", ops.exists(TEST_MAPPING_MODULE_ADDRESS_2));
    }

    @Test
    public void addFullMappingModule() throws Exception {
        AddMappingModule addMappingModule = new AddMappingModule.Builder(
                "org.jboss.security.mapping.providers.role.PropertiesRolesMappingProvider",
                TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .module("org.picketbox")
                .addModuleOption("rolesProperties", "creaperRoles.properties")
                .build();

        client.apply(addMappingModule);

        assertTrue("The mapping module should be created", ops.exists(TEST_MAPPING_MODULE_ADDRESS));

        checkMappingModuleAttribute("code", "org.jboss.security.mapping.providers.role.PropertiesRolesMappingProvider");
        checkMappingModuleAttribute("type", "role");
        checkMappingModuleAttribute("module", "org.picketbox");

        ModelNodeResult readAttribute = ops.readAttribute(TEST_MAPPING_MODULE_ADDRESS, "module-options");
        readAttribute.assertSuccess("Read operation for module-options failed");
        List<ModelNode> moduleOptions = readAttribute.listValue();
        if (moduleOptions.size() != 1) {
            fail("Mapping module should contain 1 module options instead of " + moduleOptions.size());
        }

        ModelNode moduleOption = moduleOptions.get(0);
        Property moduleOptionProperty = moduleOption.asProperty();
        assertEquals("rolesProperties", moduleOptionProperty.getName());
        assertEquals("creaperRoles.properties", moduleOptionProperty.getValue().asString());
    }

    @Test(expected = CommandFailedException.class)
    public void addExistMappingModuleNotAllowed() throws Exception {
        AddMappingModule addMappingModule = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();

        AddMappingModule addMappingModule2 = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();

        client.apply(addMappingModule);
        assertTrue("The mapping module should be created", ops.exists(TEST_MAPPING_MODULE_ADDRESS));
        client.apply(addMappingModule2);
        fail("Mapping Module UsersRoles already exists in configuration, exception should be thrown");
    }

    @Test
    public void addExistMappingModuleAllowed() throws Exception {
        AddMappingModule addMappingModule = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .replaceExisting()
                .build();

        AddMappingModule addMappingModule2 = new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("attribute")
                .replaceExisting()
                .build();

        client.apply(addMappingModule);
        assertTrue("The first mapping module should be created even if it is creating with \"replaceExisting\" and the "
                + "same mapping module does not exist", ops.exists(TEST_MAPPING_MODULE_ADDRESS));
        client.apply(addMappingModule2);
        assertTrue("The mapping module should be created", ops.exists(TEST_MAPPING_MODULE_ADDRESS));
        checkMappingModuleAttribute("type", "attribute");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_nullSecurityDomain() throws Exception {
        new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .type("role")
                .build();
        fail("Creating command with null security domain name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_emptySecurityDomain() throws Exception {
        new AddMappingModule.Builder(TEST_MAPPING_MODULE_NAME)
                .securityDomainName("")
                .type("role")
                .build();
        fail("Creating command with empty security domain name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_nullName() throws Exception {
        new AddMappingModule.Builder("org.jboss.security.mapping.providers.role.PropertiesRolesMappingProvider", null)
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_emptyName() throws Exception {
        new AddMappingModule.Builder("org.jboss.security.mapping.providers.role.PropertiesRolesMappingProvider", "")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();
        fail("Creating command with empty name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_nullCode() throws Exception {
        new AddMappingModule.Builder(null, "UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();
        fail("Creating command with null code should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_emptyCode() throws Exception {
        new AddMappingModule.Builder("", "UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .type("role")
                .build();
        fail("Creating command with empty code should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addMappingModule_nullType() throws Exception {
        new AddMappingModule.Builder("UsersRoles")
                .securityDomainName(TEST_SECURITY_DOMAIN_NAME)
                .build();
        fail("Creating command with null type should throw exception");
    }

    private void checkMappingModuleAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_MAPPING_MODULE_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
