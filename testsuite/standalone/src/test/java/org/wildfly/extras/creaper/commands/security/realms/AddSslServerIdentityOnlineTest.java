package org.wildfly.extras.creaper.commands.security.realms;

import org.jboss.arquillian.junit.Arquillian;
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

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(Arquillian.class)
public class AddSslServerIdentityOnlineTest {

    private static final String TEST_REALM_NAME = "creaperSsslRealm";
    private static final Address TEST_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_REALM_NAME);
    private static final Address TEST_SSL_IDENTITY_ADDRESS
            = TEST_REALM_ADDRESS.and("server-identity", "ssl");

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

    private static final String KEYSTORE_AND_KEY_PASSWORD = "password";

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

        AddSecurityRealm addSecurityRealm = new AddSecurityRealm.Builder(TEST_REALM_NAME).build();
        client.apply(addSecurityRealm);
        assertTrue("The security realm should be created", ops.exists(TEST_REALM_ADDRESS));
    }

    @Test
    public void add() throws Exception {
        client.apply(new AddSslServerIdentity.Builder(TEST_REALM_NAME)
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword(KEYSTORE_AND_KEY_PASSWORD)
                .keyPassword(KEYSTORE_AND_KEY_PASSWORD)
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .generateSelfSignedCertHost("localhost") // should be ignored for older versions than WildFly 10.1
                .build());
        assertTrue("Server identity should be created", ops.exists(TEST_SSL_IDENTITY_ADDRESS));
        ModelNodeResult result = ops.readAttribute(TEST_SSL_IDENTITY_ADDRESS, "keystore-password");
        result.assertSuccess();
        assertEquals("Password should be set properly.",
                KEYSTORE_AND_KEY_PASSWORD, result.stringValue());
        // check that generate-self-signed-certificate-host is defined properly, as this attribute is available
        // only for WildFly 10.1 and newer, do the check only for valid servers.
        if (client.version().greaterThanOrEqualTo(ServerVersion.VERSION_4_2_0)) {
            ModelNodeResult generateSelfSignedCertHost = ops.readAttribute(TEST_SSL_IDENTITY_ADDRESS,
                    "generate-self-signed-certificate-host");
            generateSelfSignedCertHost.assertDefinedValue();
            assertEquals("localhost", generateSelfSignedCertHost.stringValue());
        }
    }

    @Test(expected = CommandFailedException.class)
    public void addExisting() throws Exception {
        client.apply(new AddSslServerIdentity.Builder(TEST_REALM_NAME)
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword(KEYSTORE_AND_KEY_PASSWORD)
                .keyPassword(KEYSTORE_AND_KEY_PASSWORD)
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .build());
        assertTrue("Server identity should be created", ops.exists(TEST_SSL_IDENTITY_ADDRESS));
        client.apply(new AddSslServerIdentity.Builder(TEST_REALM_NAME)
                .alias("alias")
                .keystorePath("server2.keystore")
                .keystorePassword("password2").build());
        fail("Secret authentication method has been already configured. Should have failed.");
    }

    @Test
    public void replaceExisting() throws Exception {
        client.apply(new AddSslServerIdentity.Builder(TEST_REALM_NAME)
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword("xxx")
                .keyPassword("xxx")
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .build());
        assertTrue("Server identity should be created", ops.exists(TEST_SSL_IDENTITY_ADDRESS));

        client.apply(new AddSslServerIdentity.Builder(TEST_REALM_NAME)
                .alias("alias")
                .keystorePath("server.keystore")
                .keystorePassword(KEYSTORE_AND_KEY_PASSWORD)
                .keyPassword(KEYSTORE_AND_KEY_PASSWORD)
                .keystoreProvider("JKS")
                .protocol("TLS")
                .keystoreRelativeTo("jboss.server.config.dir")
                .cipherSuitesToEnable("DEFAULT", "ALL")
                .protocolsToEnable("SSL", "TLS")
                .replaceExisting()
                .build());
        assertTrue("Server identity should be created", ops.exists(TEST_SSL_IDENTITY_ADDRESS));
        ModelNodeResult result = ops.readAttribute(TEST_SSL_IDENTITY_ADDRESS, "keystore-password");
        result.assertSuccess();
        assertEquals("Password should be set properly.",
                KEYSTORE_AND_KEY_PASSWORD, result.stringValue());
    }

    @After
    public void cleanup() throws IOException, CliException, OperationException, TimeoutException, InterruptedException {
        try {
            ops.removeIfExists(TEST_REALM_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            client.close();
        }
    }
}
