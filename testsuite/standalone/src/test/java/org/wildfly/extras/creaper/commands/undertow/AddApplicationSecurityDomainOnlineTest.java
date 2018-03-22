package org.wildfly.extras.creaper.commands.undertow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.AbstractElytronOnlineTest;
import org.wildfly.extras.creaper.commands.elytron.domain.AddSecurityDomain;
import org.wildfly.extras.creaper.commands.elytron.http.AddHttpAuthenticationFactory;
import org.wildfly.extras.creaper.commands.elytron.realm.AddIdentityRealm;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
public class AddApplicationSecurityDomainOnlineTest extends AbstractElytronOnlineTest {

    private static final String TEST_APP_SEC_DOMAIN_NAME = "testAppSecDomain";
    private static final String TEST_APP_SEC_DOMAIN_NAME2 = "testAppSecDomain2";
    private static final String TEST_HTTP_AUTH_FACTORY = "httpAuthFactory";
    private static final String IDENTITY_REALM_NAME = "identityRealm";
    private static final String SECURITY_DOMAIN_NAME = "securityDomain";
    private static final String PROVIDER_HTTP_SERVER_MECH_FACTORY = "global";
    private static final Address TEST_APP_SEC_DOMAIN_ADDRESS = Address.subsystem("undertow")
            .and("application-security-domain", TEST_APP_SEC_DOMAIN_NAME);
    private static final Address TEST_APP_SEC_DOMAIN_ADDRESS2 = Address.subsystem("undertow")
            .and("application-security-domain", TEST_APP_SEC_DOMAIN_NAME2);

    @BeforeClass
    public static void addRequiredCapabilities() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            client.apply(new AddIdentityRealm.Builder(IDENTITY_REALM_NAME)
                    .identity("identity")
                    .build());

            client.apply(new AddSecurityDomain.Builder(SECURITY_DOMAIN_NAME)
                    .defaultRealm(IDENTITY_REALM_NAME)
                    .realms(new AddSecurityDomain.RealmBuilder(IDENTITY_REALM_NAME).build())
                    .build());

            client.apply(new AddHttpAuthenticationFactory.Builder(TEST_HTTP_AUTH_FACTORY)
                    .securityDomain(SECURITY_DOMAIN_NAME)
                    .httpServerMechanismFactory(PROVIDER_HTTP_SERVER_MECH_FACTORY)
                    .build());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removeAddedCapabilities() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            Administration administration = new Administration(client);
            ops.removeIfExists(SUBSYSTEM_ADDRESS.and("http-authentication-factory", TEST_HTTP_AUTH_FACTORY));
            ops.removeIfExists(SUBSYSTEM_ADDRESS.and("security-domain", SECURITY_DOMAIN_NAME));
            ops.removeIfExists(SUBSYSTEM_ADDRESS.and("identity-realm", IDENTITY_REALM_NAME));
            administration.reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(TEST_APP_SEC_DOMAIN_ADDRESS);
        ops.removeIfExists(TEST_APP_SEC_DOMAIN_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addAppSecDomain() throws Exception {
        AddApplicationSecurityDomain addAppSecDomain = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();

        assertFalse("The application security domain should not exist", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));
        client.apply(addAppSecDomain);
        assertTrue("Application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));
    }

    @Test
    public void addTwoSimpleAppSecDomains() throws Exception {
        AddApplicationSecurityDomain addAppSecDomain = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();
        AddApplicationSecurityDomain addAppSecDomain2 = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME2)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();

        assertFalse("The application security domain should not exist", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));
        assertFalse("The application security domain should not exist", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS2));

        client.apply(addAppSecDomain);
        client.apply(addAppSecDomain2);

        assertTrue("Application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));
        assertTrue("Application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addExistAppSecDomainNotAllowed() throws Exception {
        AddApplicationSecurityDomain addAppSecDomain = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();
        AddApplicationSecurityDomain addAppSecDomain2 = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();

        client.apply(addAppSecDomain);
        assertTrue("The application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));

        client.apply(addAppSecDomain2);
        fail("Application security domain is already configured, exception should be thrown");
    }

    @Test
    public void addExistAppSecDomainAllowed() throws Exception {
        AddApplicationSecurityDomain addAppSecDomain = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();
        AddApplicationSecurityDomain addAppSecDomain2 = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .overrideDeploymentConfig(true)
                .replaceExisting()
                .build();

        client.apply(addAppSecDomain);
        assertTrue("The application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));

        client.apply(addAppSecDomain2);
        assertTrue("The application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));
        // check whether it was really rewritten
        checkAttribute("override-deployment-config", "true");
    }

    @Test
    public void addFullAppSecDomain() throws Exception {
        AddApplicationSecurityDomain addAppSecDomain = new AddApplicationSecurityDomain
                .Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .overrideDeploymentConfig(true)
                .build();
        client.apply(addAppSecDomain);
        assertTrue("Application security domain should be created", ops.exists(TEST_APP_SEC_DOMAIN_ADDRESS));

        checkAttribute("http-authentication-factory", TEST_HTTP_AUTH_FACTORY);
        checkAttribute("override-deployment-config", "true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAppSecDomain_nullName() throws Exception {
        new AddApplicationSecurityDomain.Builder(null)
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();
        fail("Creating command with null name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAppSecDomain_emptyName() throws Exception {
        new AddApplicationSecurityDomain.Builder("")
                .httpAuthenticationFactory(TEST_HTTP_AUTH_FACTORY)
                .build();
        fail("Creating command with empty keystore name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAppSecDomain_nullHttpAuthFactory() throws Exception {
        new AddApplicationSecurityDomain.Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory(null)
                .build();
        fail("Creating command with null http authentication factory should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addAppSecDomain_emptyHttpAuthFactory() throws Exception {
        new AddApplicationSecurityDomain.Builder(TEST_APP_SEC_DOMAIN_NAME)
                .httpAuthenticationFactory("")
                .build();
        fail("Creating command with empty http authentication factory should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(TEST_APP_SEC_DOMAIN_ADDRESS, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }
}
