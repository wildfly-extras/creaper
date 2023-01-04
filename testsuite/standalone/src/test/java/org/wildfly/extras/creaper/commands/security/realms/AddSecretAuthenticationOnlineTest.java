package org.wildfly.extras.creaper.commands.security.realms;

import com.google.common.io.BaseEncoding;
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
public class AddSecretAuthenticationOnlineTest {

    private static final String TEST_REALM_NAME = "creaperSecretRealm";
    private static final Address TEST_REALM_ADDRESS
            = Address.coreService("management").and("security-realm", TEST_REALM_NAME);
    private static final Address TEST_SECRET_IDENTITY_ADDRESS
            = TEST_REALM_ADDRESS.and("server-identity", "secret");
    private static final String PASSWORD = "password1";

    private OnlineManagementClient client;
    private Operations ops;
    private Administration administration;

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
        client.apply(new AddSecretAuthentication.Builder(TEST_REALM_NAME)
                .password(PASSWORD)
                .build());
        assertTrue("Server identity should be created", ops.exists(TEST_SECRET_IDENTITY_ADDRESS));
        ModelNodeResult result = ops.readAttribute(TEST_SECRET_IDENTITY_ADDRESS, "value");
        result.assertSuccess();
        assertEquals("Password should be encoded properly.",
                PASSWORD, new String(BaseEncoding.base64().decode(result.stringValue()), "UTF-8"));
    }

    @Test(expected = CommandFailedException.class)
    public void addExisting() throws Exception {
        client.apply(new AddSecretAuthentication.Builder(TEST_REALM_NAME)
                .password(PASSWORD)
                .build());
        assertTrue("Server identyty should be created", ops.exists(TEST_SECRET_IDENTITY_ADDRESS));
        client.apply(new AddSecretAuthentication.Builder(TEST_REALM_NAME)
                .password(PASSWORD)
                .build());
        fail("Secret authentication method has been already configured. Should have failed.");
    }

    @Test
    public void replaceExisting() throws Exception {
        client.apply(new AddSecretAuthentication.Builder(TEST_REALM_NAME)
                .password("secret")
                .build());
        assertTrue("Server identyty should be created", ops.exists(TEST_SECRET_IDENTITY_ADDRESS));

        client.apply(new AddSecretAuthentication.Builder(TEST_REALM_NAME)
                .password(PASSWORD)
                .replaceExisting()
                .build());
        assertTrue("Server identity should be created", ops.exists(TEST_SECRET_IDENTITY_ADDRESS));
        ModelNodeResult result = ops.readAttribute(TEST_SECRET_IDENTITY_ADDRESS, "value");
        result.assertSuccess();
        assertEquals("Password should be encoded properly.",
                PASSWORD, new String(BaseEncoding.base64().decode(result.stringValue()), "UTF-8"));
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
