package org.wildfly.extras.creaper.commands.elytron.tls;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddConstantPrincipalTransformer;
import org.wildfly.extras.creaper.commands.elytron.mapper.AddConstantRealmMapper;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.online.OnlineManagementClient;
import org.wildfly.extras.creaper.core.online.operations.Address;
import org.wildfly.extras.creaper.core.online.operations.Operations;
import org.wildfly.extras.creaper.core.online.operations.admin.Administration;

@RunWith(Arquillian.class)
public class AddServerSSLContextOnlineTest extends AbstractAddSSLContextOnlineTest {

    private static final String SERVER_SSL_CONTEXT_PROTOCOL = "TLSv1.2";
    private static final String SERVER_SSL_CONTEXT_NAME = "CreaperTestServerSSLContext";
    private static final String SERVER_SSL_CONTEXT_NAME2 = "CreaperTestServerSSLContext2";
    private static final String PRE_REALM_PRINCIPAL_TRANSFORMER = "preRealmPrincipalTransformer";
    private static final String POST_REALM_PRINCIPAL_TRANSFORMER = "postRealmPrincipalTransformer";
    private static final String FINAL_PRINCIPAL_TRANSFORMER = "finalPrincipalTransformer";
    private static final String REALM_MAPPER = "realmMapper";
    private static final Address SERVER_SSL_CONTEXT_ADDRESS = SUBSYSTEM_ADDRESS.and("server-ssl-context",
            SERVER_SSL_CONTEXT_NAME);
    private static final Address SERVER_SSL_CONTEXT_ADDRESS2 = SUBSYSTEM_ADDRESS.and("server-ssl-context",
            SERVER_SSL_CONTEXT_NAME2);
    private static final Address REALM_MAPPER_ADDRESS = SUBSYSTEM_ADDRESS.and("constant-realm-mapper",
            REALM_MAPPER);
    private static final Address PRE_REALM_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("pre-realm-principal-transformer", PRE_REALM_PRINCIPAL_TRANSFORMER);
    private static final Address POST_REALM_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", POST_REALM_PRINCIPAL_TRANSFORMER);
    private static final Address FINAL_PRINCIPAL_TRANSFORMER_ADDRESS = SUBSYSTEM_ADDRESS
            .and("constant-principal-transformer", FINAL_PRINCIPAL_TRANSFORMER);

    @BeforeClass
    public static void addServerSslContextDependentResources() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            client.apply(new AddConstantPrincipalTransformer.Builder(PRE_REALM_PRINCIPAL_TRANSFORMER)
                .constant(PRE_REALM_PRINCIPAL_TRANSFORMER)
                .build());

            client.apply(new AddConstantPrincipalTransformer.Builder(POST_REALM_PRINCIPAL_TRANSFORMER)
                .constant(POST_REALM_PRINCIPAL_TRANSFORMER)
                .build());

            client.apply(new AddConstantPrincipalTransformer.Builder(FINAL_PRINCIPAL_TRANSFORMER)
                .constant(FINAL_PRINCIPAL_TRANSFORMER)
                .build());

            client.apply(new AddConstantRealmMapper.Builder(REALM_MAPPER)
                .realmName(REALM_MAPPER)
                .build());
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @AfterClass
    public static void removeServerSslDependentResources() throws Exception {
        OnlineManagementClient client = null;
        try {
            client = createManagementClient();
            Operations ops = new Operations(client);
            Administration administration = new Administration(client);
            ops.removeIfExists(REALM_MAPPER_ADDRESS);
            ops.removeIfExists(PRE_REALM_PRINCIPAL_TRANSFORMER_ADDRESS);
            ops.removeIfExists(POST_REALM_PRINCIPAL_TRANSFORMER_ADDRESS);
            ops.removeIfExists(FINAL_PRINCIPAL_TRANSFORMER_ADDRESS);
            administration.reloadIfRequired();
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    @After
    public void cleanup() throws Exception {
        ops.removeIfExists(SERVER_SSL_CONTEXT_ADDRESS);
        ops.removeIfExists(SERVER_SSL_CONTEXT_ADDRESS2);
        administration.reloadIfRequired();
    }

    @Test
    public void addSimpleServerSSLContext() throws Exception {
        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        assertFalse("The server ssl context should not exist", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        client.apply(addServerSSLContext);
        assertTrue("Server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
    }

    @Test
    public void addTwoSimpleServerSSLContexts() throws Exception {
        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        AddServerSSLContext addServerSSLContext2 = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME2)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();

        assertFalse("The server ssl context should not exist", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        assertFalse("The server ssl context should not exist", ops.exists(SERVER_SSL_CONTEXT_ADDRESS2));

        client.apply(addServerSSLContext);
        client.apply(addServerSSLContext2);

        assertTrue("Server SSL context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        assertTrue("Server SSL context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS2));
    }

    @Test(expected = CommandFailedException.class)
    public void addDuplicateServerSSLContextNotAllowed() throws Exception {
        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        AddServerSSLContext addServerSSLContext2 = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();

        client.apply(addServerSSLContext);
        assertTrue("The server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));

        client.apply(addServerSSLContext2);
        fail("Server ssl context is already configured, exception should be thrown");
    }

    @Test
    public void addDuplicateKeyManagerAllowed() throws Exception {
        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .build();
        AddServerSSLContext addServerSSLContext2 = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .keyManager(TEST_KEY_MNGR_NAME)
                .sessionTimeout(5)
                .replaceExisting()
                .build();

        client.apply(addServerSSLContext);
        assertTrue("The server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));

        client.apply(addServerSSLContext2);
        assertTrue("The server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));
        // check whether it was really rewritten
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, "session-timeout", "5");
    }

    @Test
    public void addFullServerSSLContext() throws Exception {
        AddServerSSLContext addServerSSLContext = new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
                .cipherSuiteFilter("ALL")
                .keyManager(TEST_KEY_MNGR_NAME)
                .trustManager(TRUST_MNGR_NAME)
                .maximumSessionCacheSize(0)
                .sessionTimeout(0)
                .protocols(SERVER_SSL_CONTEXT_PROTOCOL)
                .needClientAuth(true)
                .wantClientAuth(true)
                .authenticationOptional(true)
                .securityDomain("ApplicationDomain")
                .realmMapper(REALM_MAPPER)
                .preRealmPrincipalTransformer(PRE_REALM_PRINCIPAL_TRANSFORMER)
                .postRealmPrincipalTransformer(POST_REALM_PRINCIPAL_TRANSFORMER)
                .finalPrincipalTransformer(FINAL_PRINCIPAL_TRANSFORMER)
                .useCipherSuitesOrder(false)
                .wrap(true)
                .build();
        client.apply(addServerSSLContext);
        assertTrue("The server ssl context should be created", ops.exists(SERVER_SSL_CONTEXT_ADDRESS));

        checkAttribute("cipher-suite-filter", "ALL");
        checkAttribute("key-manager", TEST_KEY_MNGR_NAME);
        checkAttribute("trust-manager", TRUST_MNGR_NAME);
        checkAttribute("maximum-session-cache-size", "0");
        checkAttribute("session-timeout", "0");
        checkAttribute("protocols", Arrays.asList(SERVER_SSL_CONTEXT_PROTOCOL));
        checkAttribute("need-client-auth", "true");
        checkAttribute("want-client-auth", "true");
        checkAttribute("authentication-optional", "true");
        checkAttribute("security-domain", "ApplicationDomain");
        checkAttribute("realm-mapper", REALM_MAPPER);
        checkAttribute("pre-realm-principal-transformer", PRE_REALM_PRINCIPAL_TRANSFORMER);
        checkAttribute("post-realm-principal-transformer", POST_REALM_PRINCIPAL_TRANSFORMER);
        checkAttribute("final-principal-transformer", FINAL_PRINCIPAL_TRANSFORMER);
        checkAttribute("use-cipher-suites-order", "false");
        checkAttribute("wrap", "true");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_nullName() throws Exception {
        new AddServerSSLContext.Builder(null)
            .keyManager(TEST_KEY_MNGR_NAME)
            .build();
        fail("Creating command with null server SSL context name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addServerSSLContext_emptyName() throws Exception {
        new AddServerSSLContext.Builder("")
            .keyManager(TEST_KEY_MNGR_NAME)
            .build();
        fail("Creating command with empty server ssl context name should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_nullKeyManager() throws Exception {
        new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
            .keyManager(null)
            .build();
        fail("Creating command with null key manager should throw exception");
    }

    @Test(expected = IllegalArgumentException.class)
    public void addKeyManager_emptyAlgorithm() throws Exception {
        new AddServerSSLContext.Builder(SERVER_SSL_CONTEXT_NAME)
            .keyManager("")
            .build();
        fail("Creating command with empty key manager should throw exception");
    }

    private void checkAttribute(String attribute, String expectedValue) throws IOException {
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, attribute, expectedValue);
    }

    private void checkAttribute(String attribute, List<String> expected) throws IOException {
        checkAttribute(SERVER_SSL_CONTEXT_ADDRESS, attribute, expected);
    }

}
