package org.wildfly.extras.creaper.commands.security.realms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.dmr.ModelNode;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.online.CliException;
import org.wildfly.extras.creaper.core.online.ModelNodeResult;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.OnlineOptions;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.OperationException;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;
import org.wildfly.extras.creaper.core.ServerVersion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddKerberosAuthenticationOnlineTest {

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String TEST_SECURITY_REALM_NAME = "creaperSecRealm";
    private static final String JBOSSORG_PRINCIPAL = "HTTP/localhost@JBOSS.ORG";
    private static final String JBOSSCOM_PRINCIPAL = "HTTP/localhost@JBOSS.COM";

    private static final Address TEST_SECURITY_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_SECURITY_REALM_NAME);
    private static final Address KRB_SERVER_IDENTITY_ADDRESS = TEST_SECURITY_REALM_ADDRESS
            .and("server-identity", "kerberos");
    private static final Address SECURITY_REALM_KERBEROS_AUTHN_ADDRESS = TEST_SECURITY_REALM_ADDRESS
            .and("authentication", "kerberos");
    private static final Address JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS = KRB_SERVER_IDENTITY_ADDRESS
            .and("keytab", JBOSSORG_PRINCIPAL);
    private static final Address JBOSSCOM_PRINCIPAL_KEYTAB_ADDRESS = KRB_SERVER_IDENTITY_ADDRESS
            .and("keytab", JBOSSCOM_PRINCIPAL);

    @BeforeClass
    public static void checkServerVersionIsSupported() throws IOException {
        ServerVersion serverVersion
                = ManagementClient.online(OnlineOptions.standalone().localDefault().build()).version();
        Assume.assumeTrue("Kerberos authentication in security realm is available since WildFly 9 or in EAP 6.4.x.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_1_7_0)
                && !serverVersion.inRange(ServerVersion.VERSION_2_0_0, ServerVersion.VERSION_2_2_0));
        Assume.assumeFalse("Legacy security was removed in WildFly 25.",
                serverVersion.greaterThanOrEqualTo(ServerVersion.VERSION_18_0_0));
    }

    @Before
    public void connect() throws Exception {
        client = ManagementClient.online(OnlineOptions.standalone().localDefault().build());
        ops = new Operations(client);
        administration = new Administration(client);

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_SECURITY_REALM_NAME).build();
        client.apply(addSecurityRealm);
        assertTrue("The security realm should be created", ops.exists(TEST_SECURITY_REALM_ADDRESS));
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_SECURITY_REALM_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }

    @Test
    public void addSimple() throws Exception {
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .build())
                .build();

        assertFalse("The kerberos server identity in security realm should not exist",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertFalse("The kerberos authentication in security realm should not exist",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));
        client.apply(addSecurityRealmKerberosAuthentication);
        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos authentication in security realm should be created",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));
    }

    @Test(expected = CommandFailedException.class)
    public void existing() throws Exception {
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .path("a.keytab")
                        .build())
                .removeRealm(true)
                .build();
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication2
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .path("b.keytab")
                        .build())
                .removeRealm(false)
                .build();

        client.apply(addSecurityRealmKerberosAuthentication);
        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos authentication in security realm should be created",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));


        client.apply(addSecurityRealmKerberosAuthentication2);
        fail("Kerberos authentication is already configured in security realm, exception should be thrown");
    }

    @Test
    public void overrideExisting() throws Exception {
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .path("a.keytab")
                        .build())
                .removeRealm(true)
                .build();
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication2
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .path("b.keytab")
                        .build())
                .removeRealm(false)
                .replaceExisting()
                .build();

        client.apply(addSecurityRealmKerberosAuthentication);
        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos authentication in security realm should be created",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "path", "a.keytab");
        checkAttribute(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS, "remove-realm", "true");


        client.apply(addSecurityRealmKerberosAuthentication2);
        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos authentication in security realm should be created",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "path", "b.keytab");
        checkAttribute(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS, "remove-realm", "false");
    }

    @Test
    public void addFull_oneKeytab() throws Exception {
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .path("a.keytab")
                        .relativeTo("jboss.server.config.dir")
                        .validForHosts("localhost", "127.0.0.1")
                        .debug(true)
                        .build())
                .removeRealm(true)
                .build();

        client.apply(addSecurityRealmKerberosAuthentication);

        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos authentication in security realm should be created",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "path", "a.keytab");
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "relative-to", "jboss.server.config.dir");
        checkForHostAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "localhost", "127.0.0.1");
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "debug", "true");
        checkAttribute(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS, "remove-realm", "true");
    }

    @Test
    public void addFull_twoKeytabs() throws Exception {
        AddKerberosAuthentication addSecurityRealmKerberosAuthentication
                = new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .path("a.keytab")
                        .relativeTo("jboss.server.config.dir")
                        .validForHosts("localhost")
                        .debug(true)
                        .build())
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSCOM_PRINCIPAL)
                        .path("b.keytab")
                        .relativeTo("jboss.server.log.dir")
                        .validForAllHosts()
                        .debug(false)
                        .build())
                .removeRealm(false)
                .build();

        client.apply(addSecurityRealmKerberosAuthentication);

        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos server identity in security realm should be created",
                ops.exists(JBOSSCOM_PRINCIPAL_KEYTAB_ADDRESS));
        assertTrue("The kerberos authentication in security realm should be created",
                ops.exists(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS));
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "path", "a.keytab");
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "relative-to", "jboss.server.config.dir");
        checkForHostAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "localhost");
        checkAttribute(JBOSSORG_PRINCIPAL_KEYTAB_ADDRESS, "debug", "true");
        checkAttribute(JBOSSCOM_PRINCIPAL_KEYTAB_ADDRESS, "path", "b.keytab");
        checkAttribute(JBOSSCOM_PRINCIPAL_KEYTAB_ADDRESS, "relative-to", "jboss.server.log.dir");
        checkForHostAttribute(JBOSSCOM_PRINCIPAL_KEYTAB_ADDRESS, "*");
        checkAttribute(JBOSSCOM_PRINCIPAL_KEYTAB_ADDRESS, "debug", "false");
        checkAttribute(SECURITY_REALM_KERBEROS_AUTHN_ADDRESS, "remove-realm", "false");

    }

    @Test(expected = IllegalArgumentException.class)
    public void add_withoutKeytab() throws Exception {
        new AddKerberosAuthentication.Builder(TEST_SECURITY_REALM_NAME).build();

        fail("Creating command without keytab should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_nullSecurityRealm() throws Exception {
        new AddKerberosAuthentication.Builder(null)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .build())
                .build();

        fail("Creating command with null security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptySecurityRealm() throws Exception {
        new AddKerberosAuthentication.Builder("")
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(JBOSSORG_PRINCIPAL)
                        .build())
                .build();
        fail("Creating command with empty security realm should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_nullKeytabPrincipal() throws Exception {
        new AddKerberosAuthentication.Builder(null)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal(null)
                        .build())
                .build();

        fail("Creating command with null keytab principal should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void add_emptyKeytabPrincipal() throws Exception {
        new AddKerberosAuthentication.Builder(null)
                .addKeytab(new KerberosKeytab.Builder()
                        .principal("")
                        .build())
                .build();

        fail("Creating command with empty keytab principal should throw exception");
    }

    private void checkAttribute(Address address, String attribute, String expectedValue) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, attribute);
        readAttribute.assertSuccess("Read operation for " + attribute + " failed");
        assertEquals("Read operation for " + attribute + " return wrong value", expectedValue,
                readAttribute.stringValue());
    }

    private void checkForHostAttribute(Address address, String... expectedValues) throws IOException {
        ModelNodeResult readAttribute = ops.readAttribute(address, "for-hosts");
        readAttribute.assertSuccess("Read operation for for-hosts failed");
        ModelNode get = readAttribute.get("result");
        if (get == null) {
            fail("Read operation for for-hosts has not defined result.");
        }
        List<ModelNode> attributes = get.asList();
        List<String> toCheck = new ArrayList<String>();
        for (ModelNode node : attributes) {
            toCheck.add(node.asString());
        }
        for (String expectedValue : expectedValues) {
            assertTrue("Expected value " + expectedValue + " was not added to for-hosts attribute",
                    toCheck.contains(expectedValue));

        }
    }

}
